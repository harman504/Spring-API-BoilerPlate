package com.gitub.harman54.quarantine.automations;

import java.nio.file.Path;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.symbolsolver.resolution.typeinference.MethodType;

public class DAOCodeGenerator extends CodeGenerator {

	private boolean isHibernateCrudMethodUsed;
	private boolean isMyBatisSessionUsed;
	public DAOCodeGenerator(Path filePath) {
		super(filePath);
	}

	@Override
	public void parseCode() 
    { 
        super.parseCode();
        NodeList<ImportDeclaration> imports =getCompilationUnit().getImports();
        isHibernateCrudMethodUsed = imports.stream().anyMatch(
        		importDeclacration->importDeclacration.getNameAsString().contains("hibernate"));
        isMyBatisSessionUsed=imports.stream().anyMatch(
        		importDeclacration->importDeclacration.getNameAsString().contains("mybatis"));
        
    } 
	public boolean isHibernateCrudMethodUsed() {
		return isHibernateCrudMethodUsed;
	}
	
	public void declareMethodAbstarct(String apiName, String returnType) {
		 getMainClass().addMethod(apiName,
				 com.github.javaparser.ast.Modifier.Keyword.PUBLIC).setType(new TypeParameter(returnType)).setBody(null);
	}

	public String createMybatisDAOMethod(String apiName, String returnType) {
		MethodDeclaration method = getMainClass().addMethod(apiName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
		
		MethodDeclaration existingMethod =this.getMainClass().getMethods().get(0);
		ReturnStmt callToMapper=existingMethod.getBody().get().findFirst(ReturnStmt.class).orElse(new ReturnStmt());
		VariableDeclarationExpr genericStatement=existingMethod.getBody().get().findFirst(VariableDeclarationExpr.class).orElse(new VariableDeclarationExpr());
		callToMapper.findFirst(MethodCallExpr.class).get().setName(apiName);
		
		BlockStmt blockStmt=new BlockStmt();
		blockStmt.addStatement(genericStatement);
		blockStmt.addStatement(callToMapper);
		method.setBody(blockStmt);
		method.setType(new TypeParameter(returnType));
		System.out.println(callToMapper.findFirst(MethodCallExpr.class).get().asMethodCallExpr().getNameAsString());
		
		return callToMapper.findFirst(MethodCallExpr.class).get().asMethodCallExpr().getNameAsString();
	}
	
	public void createHibernateDAOMethod(String apiName, String returnType) {
		MethodDeclaration method = getMainClass().addMethod(apiName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC);
		method.addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"), new StringLiteralExpr("unchecked") ));
		CastExpr calltoDAOMethod = new CastExpr(new TypeParameter(returnType),new NameExpr("sessionFactory.getCurrentSession()\r\n" + 
				"        		.createQuery().getResultList()"));
		
		method.setBody(new BlockStmt().addStatement(new ReturnStmt(calltoDAOMethod)));	
	}
	

	public boolean isMyBatisSessionUsed() {
		return isMyBatisSessionUsed;
	}

}
