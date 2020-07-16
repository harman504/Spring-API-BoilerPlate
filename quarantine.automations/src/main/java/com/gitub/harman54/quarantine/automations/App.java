package com.gitub.harman54.quarantine.automations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.gitub.harman54.quarantine.automations.ControllerCodeGenerator;


public class App 
{
	private final String controllerLocation;
	private final String apiName;
	private final String returnType;
	public List<Path> projectFiles =new LinkedList<>();
	private ControllerCodeGenerator controller;
	private ServiceCodeGenerator service;
	private DAOCodeGenerator dAO;
	private ServiceCodeGenerator serviceImpl;
	private DAOCodeGenerator dAoImpl;
	
	public App(String controllerLocation,String apiName, String returnType) {
		this.controllerLocation=controllerLocation;
		this.apiName=apiName;
		this.returnType= returnType;
	}
	
	private List<Path> getAllProjectFiles() {
		if(projectFiles.isEmpty()) {
			try (Stream<Path> paths = Files.walk(controller.getRootPath())) {
				projectFiles=paths.filter(Files::isRegularFile).collect(Collectors.toList());
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} 
		}
		return projectFiles;
	}
	
	public String controller() {
		controller = new ControllerCodeGenerator(Paths.get(controllerLocation));
		controller.parseCode();
		return controller.addControllerMethodtoClass(apiName, returnType);
	}
	
	public String service(String seviceVariable) {
		String dAOVariable=null;
		service =controller.getAutowiredServices().get(seviceVariable);
		service.parseCode();
		if(service.isInterface()) {
			service.declareMethodAbstarct(apiName, returnType);
			
			//System.out.println("-------------------------------Service------------------------------");
			//System.out.println(service.toString());
			
			for(Path javaFile:getAllProjectFiles()) {
				if( javaFile.compareTo(service.getFilePath()) !=0
				        && fileNameWithoutExtensions(javaFile).contains(fileNameWithoutExtensions(service.getFilePath()))) {
					serviceImpl =new ServiceCodeGenerator(javaFile);
					serviceImpl.parseCode();
					dAOVariable=serviceImpl.addServiceMethodtoClass(apiName, returnType);
					//move this to DAO
					dAO= serviceImpl.getAutowiredDAOs().get(dAOVariable);
				}
			}
		}
		else {
			dAOVariable=service.addServiceMethodtoClass(apiName, returnType);
			//System.out.println("-------------------------------Service------------------------------");
			//System.out.println(service.toString());
			//move this to DAO
			dAO= service.getAutowiredDAOs().get(dAOVariable);			
		}
		return dAOVariable;
	}
	
	public void dAO() {
		dAO.parseCode();
		String mapper;
		if(dAO.isInterface()) {
			dAO.declareMethodAbstarct(apiName, "Harman");
			
			for(Path javaFile:getAllProjectFiles()) {
				if( javaFile.compareTo(service.getFilePath()) !=0
				        && fileNameWithoutExtensions(javaFile).contains(fileNameWithoutExtensions(service.getFilePath()))) {
					dAoImpl =new DAOCodeGenerator(javaFile);
					dAoImpl.parseCode();
					if(dAoImpl.isHibernateCrudMethodUsed() && dAoImpl.isMyBatisSessionUsed()) {
						System.out.println("1. Mybatis");
						System.out.println("2. Hibernate");
						Scanner in = new Scanner(System.in);
						 int chosen = in.nextInt(); 
						 in.close();
						 if(chosen==1) {
							 dAoImpl.createMybatisDAOMethod(apiName, returnType);
						 }
						 else {
							 dAoImpl.createHibernateDAOMethod(apiName, returnType);
						 }
					}
					else if(dAoImpl.isHibernateCrudMethodUsed()) {
						dAoImpl.createHibernateDAOMethod(apiName, returnType);
					}
					else if(dAoImpl.isMyBatisSessionUsed()){
						dAoImpl.createMybatisDAOMethod(apiName, returnType);
					}
					
				}
			}
		}
		else {
			if(dAO.isHibernateCrudMethodUsed() && dAO.isMyBatisSessionUsed()) {
				ScannerInput scanIn=ScannerInput.getScanner();
				System.out.println("1. Mybatis");
				System.out.println("2. Hibernate");
				 int chosen = scanIn.in.nextInt(); 
				 if(chosen==1) {
					 mapper=dAO.createMybatisDAOMethod(apiName, returnType);
				 }
				 else {
					 dAO.createHibernateDAOMethod(apiName, returnType);
				 }
			}
			else if(dAO.isHibernateCrudMethodUsed()) {
				dAO.createHibernateDAOMethod(apiName, returnType);
			}
			else if(dAO.isMyBatisSessionUsed()){
				mapper=dAO.createMybatisDAOMethod(apiName, returnType);
			}
		}
	}
	
	public void save() throws IOException {
		saveCodeGenerator(controller);
		if(serviceImpl!=null) {
			saveCodeGenerator(serviceImpl);
		}
		
		saveCodeGenerator(service);
		saveCodeGenerator(dAO);
		if(dAoImpl!=null) {
			saveCodeGenerator(dAoImpl);
		}
	}
	
	private static String fileNameWithoutExtensions(Path path) {
		return path.getName(path.getNameCount()-1).toString().replaceFirst("[.][^.]+$", "");
	}

	public static void saveCodeGenerator(CodeGenerator codeGenerator) throws IOException {
		try(FileWriter fr = new FileWriter(codeGenerator.getFilePath().toFile())){
			fr.write(codeGenerator.toString());
		}
	}
	
	public static void main( String[] args )  {
		App app = new App("controllerLocation",
				"getHarman",
				"Harman");
		String seviceVariable=app.controller();
		
		String daoVariable = app.service(seviceVariable);
		app.dAO();
		try {
			app.save();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ScannerInput scanIn=ScannerInput.getScanner();
		scanIn.in.close();
	}

}
