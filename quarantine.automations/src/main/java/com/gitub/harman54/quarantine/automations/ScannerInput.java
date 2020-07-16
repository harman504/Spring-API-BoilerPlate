package com.gitub.harman54.quarantine.automations;

import java.util.Scanner;

//Singleton class to be used for taking console inputs
public class ScannerInput {

	private static ScannerInput scannerInput=null;
	
	public Scanner in;
	private ScannerInput() {
		in = new Scanner(System.in);
	}
	
	public static ScannerInput getScanner() {
		if(scannerInput==null) {
			scannerInput=new ScannerInput();
		}
		
		return scannerInput;
	}
}
