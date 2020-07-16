package com.gitub.harman54.quarantine.automations;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;

public class CodeGenerator {
	private Path filePath;
	private List<String> methodsToAdd;
	private CompilationUnit compilationUnit;
	private ClassOrInterfaceDeclaration mainClass;
	private static final Path ROOT_PATH=Paths.get("projectRoot");
	
	public CodeGenerator(Path filePath) {
		super();
		this.filePath = filePath;
	}

	public Path getFilePath() {
		return filePath;
	}

	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}


	
	public void parseCode() {
		CompilationUnit cu = null;
		
		TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(getRootPath().toFile()); 
		
		JavaSymbolSolver symbolSolver = new JavaSymbolSolver(javaParserTypeSolver); 
		
		try(InputStream in=new FileInputStream(this.filePath.toFile()))
		{
        	ParserConfiguration parserConfiguration = new ParserConfiguration(); 
        	parserConfiguration.setAttributeComments(false);
        	parserConfiguration.setSymbolResolver(symbolSolver);
        	StaticJavaParser.setConfiguration(parserConfiguration);
            cu = StaticJavaParser.parse(in);
        }
        catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if(cu!=null) {
        	setCompilationUnit(cu);
        	
        	ClassOrInterfaceDeclaration clas =getCompilationUnit().findFirst(ClassOrInterfaceDeclaration.class).orElseThrow(() -> new RuntimeException("User not found with userId "));
    		setMainClass(clas);
		}
	}
	
	public List<String> getMethodsToAdd() {
		return methodsToAdd;
	}

	public void setMethodsToAdd(List<String> methodsToAdd) {
		this.methodsToAdd = methodsToAdd;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public void setCompilationUnit(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public ClassOrInterfaceDeclaration getMainClass() {
		return mainClass;
	}

	public void setMainClass(ClassOrInterfaceDeclaration mainClass) {
		this.mainClass = mainClass;
	}
	
	public String toString () {
		return compilationUnit.toString();
	}

	public Path getRootPath() {
		return ROOT_PATH;
	}
	
	public boolean isInterface() {
		return this.getMainClass().isInterface();
	}
	
	public Path qualifiedNameToAbsolutePath(String qName) { 
		return getRootPath().resolve(qName.replace('.', '/')+".java"); 
	}
	 
}
