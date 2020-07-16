package com.gitub.harman54.quarantine.automations;


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.TypeParameter;

public class ControllerCodeGenerator extends CodeGenerator {

	private Map<String,ServiceCodeGenerator> autowiredServices;
	
	public ControllerCodeGenerator(Path filePath) {
		super(filePath);
	}

	public String addControllerMethodtoClass(String apiName, String returnType) {
		NodeList<MemberValuePair> controllerAnnotations = new NodeList<>() ;
    	controllerAnnotations.add(new MemberValuePair("value",new StringLiteralExpr(apiName)));
    	controllerAnnotations.add(new MemberValuePair("method",new StringLiteralExpr("RequestMethod.GET")));
		
		MethodDeclaration method = getMainClass().addMethod(apiName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
				.addAnnotation(new NormalAnnotationExpr(new Name("RequestMapping"),controllerAnnotations));
		
		String chosenService =chooseService();
		MethodCallExpr calltoServiceMethod = new MethodCallExpr(new NameExpr(chosenService),apiName);
		
		method.setBody(
				new BlockStmt().addStatement(
						new ReturnStmt(
								calltoServiceMethod
						)
					));
		
		method.setType(new TypeParameter(returnType));
		
		return chosenService;
	}

	private String chooseService() {
		AtomicInteger counter = new AtomicInteger(0);
		Map<Integer, String > serviceOptions=new HashMap<Integer, String>();
		ScannerInput scanIn=ScannerInput.getScanner(); 
		
		System.out.println("Please choose one of the autowired service for the API logic:");
		//detect if autowired empty
		detectAllAutowiredServices();
		autowiredServices.keySet().forEach(serviceType->{
				serviceOptions.put(counter.getAndIncrement(), serviceType);
		});
		System.out.println(serviceOptions.toString());
		
        int chosen = scanIn.in.nextInt(); 
		
        return serviceOptions.get(chosen);
	}

	public Map<String, ServiceCodeGenerator> getAutowiredServices() {
		return autowiredServices;
	}

	public void setAutowiredServices(Map<String, ServiceCodeGenerator> autowiredServices) {
		this.autowiredServices = autowiredServices;
	}

	public void detectAllAutowiredServices() {
		MarkerAnnotationExpr autowired = new MarkerAnnotationExpr("Autowired");
		autowiredServices=getMainClass().getFields().stream()
				.filter(it->it.getAnnotations().contains(autowired))
				.collect(
						Collectors.toConcurrentMap(
								key->key.getVariable(0).getNameAsString(), 
								value->getServiceCodeGenerator(
										value.getVariable(0).getType().resolve().asReferenceType().getQualifiedName()
										)
								)
						);
		
	}

	private ServiceCodeGenerator getServiceCodeGenerator(String qName) {
		return new ServiceCodeGenerator(qualifiedNameToAbsolutePath(qName));
	}
	
	
}
