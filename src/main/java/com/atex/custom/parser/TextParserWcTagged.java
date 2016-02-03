package com.atex.custom.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.jfree.util.Log;

import com.atex.onecms.app.dam.util.PrefixedProperty;
import com.atex.onecms.app.dam.wire.DamWireArticleBean;

public class TextParserWcTagged implements ITextParser {


	private static PrefixedProperty fieldValueMapping;

	private static String TDATE_FIELD = "TDATE";
	
	private static String TYPE_FIELD = "TYPE";
	private static String TIME_FIELD = "TIME";
	private static String PRIORITY_FIELD = "PRIORITY";
	private static String SEQNUM_FIELD = "SEQNUM";
	private static String SECTION_FIELD = "SECTION";
	private static String SENDER_FIELD = "SENDER";
	private static String SIZE_FIELD = "SIZE";
	private static String TITLE_FIELD = "TITLE";
	private static String TEXT_FIELD = "TEXT";
	
	
	/*
	 * Sample input looks like:
	 * 
	 * 
	 * @TIMESTAMP:1448465238
	 * @TIMEKEY:A7BB3F00
	 * @TDATE:15:27-25/11/15
	 * @SIZE:01446
	 * @SENDER:RADCOR
	 * @SECTION:ECONOMIA
	 * @SEQNUM:0420
	 * @PRIORITY:6
	 * @TITLE:CONFCOMMERCIO: SANGALLI, L'ILLEGALITÀ SOTTRAE AL SETTORE 27 MILIARDI DI EUROE METTE A RISC
	 * @AGDAY:25
	 * @AGMON:11
	 * @TIME:1527
	 * @TYPE:W
	 * @TEXT: CONFCOMMERCIO....
	 */
	



	public void setFieldValueMapping(PrefixedProperty fieldValueMapping) {
		TextParserWcTagged.fieldValueMapping = fieldValueMapping;
	}


	public DamWireArticleBean parseFile(File inputFile) throws Exception{

		DamWireArticleBean articleBean = new DamWireArticleBean();	

		try{


			//declared here only to make visible to finally clause
			BufferedReader input = null;

			//use buffering
			//this implementation reads one line at a time
			//FileReader always assumes default encoding is OK!
			input = new BufferedReader( new InputStreamReader(new FileInputStream(inputFile), "Cp1252"));
			String line = null; //not declared within while loop
		
	
			HashMap<String, String> wcFileValues = new HashMap<String, String>();
			/* reading TSRE wctagged format file... */

			String wcFieldName = "";
			StringBuffer wcFieldValue = new StringBuffer();

			while (( line = input.readLine()) != null){
				if ((line.startsWith("@")) ){

					if (!wcFieldName.equals(""))
						wcFileValues.put(wcFieldName, wcFieldValue.toString());

					wcFieldName = line.substring(line.indexOf("@")+1, line.indexOf(":"));

					wcFieldValue = new StringBuffer();
					wcFieldValue.append(line.substring(line.indexOf(":")+1));
				}else{
					wcFieldValue.append(System.getProperty("line.separator")+line);
				}

			}
			if (!wcFieldName.equals(""))
				wcFileValues.put(wcFieldName, wcFieldValue.toString());


			input.close(); // Important to avoid NFS errors
			


			HashMap<String, String> values = new HashMap<String, String>();	

			String source = wcFileValues.get(SENDER_FIELD);
			String section = wcFileValues.get(SECTION_FIELD);
			String headline = wcFileValues.get(TITLE_FIELD);
			String priority = wcFileValues.get(PRIORITY_FIELD);
			String seqnum = wcFileValues.get(SEQNUM_FIELD);
			String size = wcFileValues.get(SIZE_FIELD);
			String text = wcFileValues.get(TEXT_FIELD);

			/* ------------
			 * Date fields
			 * ------------
			 */
			String tDate = wcFileValues.get(TDATE_FIELD);
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm-dd/MM/yy");		
			Date d = sdf.parse(tDate);

			Date creationDate = d;


			if(fieldValueMapping.getProperty("headline", headline)!=null)
				headline = fieldValueMapping.getProperty("headline", headline);

			if(fieldValueMapping.getProperty("source", source)!=null)
				source = fieldValueMapping.getProperty("source", source);

			if(fieldValueMapping.getProperty("section", section)!=null)
				section = fieldValueMapping.getProperty("section", section);
			
			if(fieldValueMapping.getProperty("priority", priority)!=null)
				priority = fieldValueMapping.getProperty("priority", priority);


			if(fieldValueMapping.getProperty("text", text)!=null)
				text = fieldValueMapping.getProperty("text", text);


			articleBean.setHeadline(headline);
			articleBean.setSource(source);
			articleBean.setSection(section);
			articleBean.setPriority(priority);
			articleBean.setSeqnum(seqnum);
			articleBean.setSize(size);
			articleBean.setBody(text);
			articleBean.setCreationdate(creationDate);

			wcFieldValue = null;
			wcFileValues = null;
			line = null;
			wcFieldName = null;
			
		}catch (Exception ex) {
			Log.error("Error in processing file: "+inputFile.getAbsolutePath() + " " +ex.getMessage());
			throw ex;

		}
		return articleBean;

	}


}
