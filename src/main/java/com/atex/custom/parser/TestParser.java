package com.atex.custom.parser;

import java.io.File;
import java.io.FileInputStream;

import com.atex.custom.util.PrefixedProperty;

public class TestParser {

	private static PrefixedProperty fieldValueMapping;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		loadFieldValueMapping();
		
		String hdrline = "AGI0733 3 ECO 0 R01 / ";
		String sender = hdrline.substring(0, 3);
		String seqnum = hdrline.substring(3, 8); 
		String priority = hdrline.substring(9, 10); 
		String section 	= hdrline.substring(10, 13); 
		
		TextParserADK parser = new TextParserADK();
		
		parser.setFieldValueMapping(fieldValueMapping);
		try {
			parser.parseFile(new File("/Users/rdemattei/Documents/Lavoro/ADAM/agenzie_ita/infogate_esempi/ADK-kronos/1342-  33- 507-050114.txt"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void loadFieldValueMapping(){
		
		if (fieldValueMapping == null)
			fieldValueMapping = new PrefixedProperty();

		try {
			FileInputStream fileInput = new FileInputStream(new File("/Users/rdemattei/Documents/Lavoro/Polopoly/gitrepo/gong/module-wires-fieg/src/main/resources/valueMapping.properties"));  
			fieldValueMapping.load(fileInput);
			fileInput.close();

		} catch (Exception e) {
			e.printStackTrace();
		}    	
	}
}
