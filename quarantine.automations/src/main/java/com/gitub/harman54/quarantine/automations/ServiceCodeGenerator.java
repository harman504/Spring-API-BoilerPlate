package com.gitub.harman54.quarantine.automations;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class ServiceCodeGenerator extends CodeGenerator {
	
	private Map<String,DAOCodeGenerator> autowiredDAOs;

	public ServiceCodeGenerator(Path filePath) {
		super(filePath);
	}

	public String addServiceMethodtoClass(String apiName, String returnType) {
		
		MethodDeclaration method = getMainClass().addMethod(apiName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
				.addAnnotation(new SingleMemberAnnotationExpr(new Name("Transactional"), new StringLiteralExpr("txManager") ));
		
		detectAllAutowiredDAOs();
		String chosenDAO =chooseDAO();
		MethodCallExpr calltoDAOMethod = new MethodCallExpr(new NameExpr(chosenDAO),apiName);
		method.setBody(new BlockStmt().addStatement(
						new ReturnStmt(
								calltoDAOMethod
						)
					));
		
		method.setType(new TypeParameter(returnType));
		
		return chosenDAO;
	}
	
	public void detectAllAutowiredDAOs() {
		MarkerAnnotationExpr autowired = new MarkerAnnotationExpr("Autowired");
		autowiredDAOs= new HashMap<>();
		
		for(FieldDeclaration field : getMainClass().getFields()) { 
			
			if(field.getAnnotations().contains(autowired)) {
				try {
					ResolvedType resolvedType=field.getVariable(0).getType().resolve();
					autowiredDAOs.put(field.getVariable(0).getNameAsString(), getDAOCodeGenerator(
							resolvedType.asReferenceType().getQualifiedName()
							));
				}
				catch(Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}
	
	private String chooseDAO() {
		AtomicInteger counter = new AtomicInteger(0);
		Map<Integer, String > dAOOptions=new HashMap<>();
		ScannerInput scanIn=ScannerInput.getScanner();
		
		System.out.println("Please choose one of the autowired DAO for the API logic:");
		//detect if autowired empty then create new
		autowiredDAOs.keySet().forEach(dAOType->{
			dAOOptions.put(counter.getAndIncrement(), dAOType);
		});
		System.out.println(dAOOptions.toString());
		
        int chosen = scanIn.in.nextInt();
		
        return dAOOptions.get(chosen);
	}

	private DAOCodeGenerator getDAOCodeGenerator(String qName) {
		DAOCodeGenerator dAOCodeGenerator =new DAOCodeGenerator(qualifiedNameToAbsolutePath(qName));
		return dAOCodeGenerator;
	}

	public Map<String,DAOCodeGenerator> getAutowiredDAOs() {
		return autowiredDAOs;
	}

	public void setAutowiredDAOs(Map<String,DAOCodeGenerator> autowiredDAOs) {
		this.autowiredDAOs = autowiredDAOs;
	}
	
	public void declareMethodAbstarct(String apiName, String returnType) {
		 getMainClass().addMethod(apiName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC).setType(new TypeParameter(returnType)).setBody(null);
	}
}
