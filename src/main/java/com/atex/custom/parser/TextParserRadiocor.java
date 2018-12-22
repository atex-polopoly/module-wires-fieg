package com.atex.custom.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import com.atex.plugins.structured.text.StructuredText;
import org.jfree.util.Log;

import com.atex.onecms.app.dam.standard.aspects.DamWireArticleAspectBean;
import com.atex.onecms.app.dam.util.PrefixedProperty;

public class TextParserRadiocor extends BaseTextParser<OneArticleBean>  {


	private static PrefixedProperty fieldValueMapping;



	public void setFieldValueMapping(PrefixedProperty fieldValueMapping) {
		TextParserRadiocor.fieldValueMapping = fieldValueMapping;
	}


	@Override
	public OneArticleBean parseFile(final File inputFile) throws Exception {
		try {
			//FileReader always assumes default encoding is OK!
			return parseFile(new InputStreamReader(new FileInputStream(inputFile), getEncoding()));
		} catch (Exception e) {
			Log.error("Error in processing file: " + inputFile.getAbsolutePath() + " " + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	OneArticleBean parseFile(final Reader reader) throws Exception {

		OneArticleBean articleBean = new OneArticleBean();
		articleBean.setInputTemplate(OneArticleBean.INPUT_TEMPLATE_WIRE);

		StringBuffer contents = new StringBuffer();

		//declared here only to make visible to finally clause
		BufferedReader input = null;

		//use buffering
		//this implementation reads one line at a time
		//FileReader always assumes default encoding is OK!
		input = new BufferedReader(reader);
		String line = null; //not declared within while loop
		int l = 0;
		String hdrline = "";
		String headline = "";

		while (( line = input.readLine()) != null){
			if( l== 0 && line.trim().length() > 0 && line.getBytes()[0] == 1)
			{
				hdrline = line;
			}else if( l == 1 && line.trim().length() == 0){
				// do nothing, empty line
			}else if( l == 2 && line.trim().length() > 0){
				headline = line;
			}else if( l == 3 && line.trim().length() == 0){
				// do nothing, empty line
			}else {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
			l++;
		}

		input.close(); // Important to avoid NFS errors

		String text = processText(contents.toString());


		String lastLine = getLastLine(contents.toString()); 
		String[] infos = lastLine.split(" ");

		Date creationDate = processDate(infos[1]+" "+infos[2]);

		String source = infos[0].replaceFirst("\\(", "").replace(")", "");
		String seqnum = infos[3].replaceFirst("\\(", "").replace(")", ""); 
		String priority = infos[4];
		String section 	= hdrline.substring(hdrline.indexOf("(")+1, hdrline.indexOf(")")); 

		// compile the headline 
		headline = hdrline.substring(hdrline.indexOf("\t")+1) + "\n" + headline;

		if(fieldValueMapping.getProperty("headline", headline)!=null)
			headline = fieldValueMapping.getProperty("headline", headline);

		if(fieldValueMapping.getProperty("source", source)!=null)
			source = fieldValueMapping.getProperty("source", source);

		if(fieldValueMapping.getProperty("section", section)!=null)
			section = fieldValueMapping.getProperty("section", section);

		if(fieldValueMapping.getProperty("text", text)!=null)
			text = fieldValueMapping.getProperty("text", text);


		articleBean.setHeadline(new StructuredText(headline));
		articleBean.setSource(source);
		articleBean.setSection(section);
		articleBean.setBody(new StructuredText(text));
		articleBean.setCreationdate(creationDate);



		return articleBean;

	}

	private String processText(String content){
		String[] lines = content.split("\n");
		if (lines[0]!=null && lines[0].trim().length()==0)
			content = content.replaceFirst("\n", "").trim();

		int endIndex = content.lastIndexOf("NNNN");


		return content;
	}

	private String getLastLine(String content){

		String lastLineContent = "";
		try {

			String[] lines = content.split("\n");
			int lastLine = lines.length - 3;
			for (int i = lines.length-1; i > 0; i--) {
				String line = lines[i];
				if(line.indexOf("NNNN")>0){
					lastLine=i;
					break;
				}
			}
			lastLineContent = lines[lastLine];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lastLineContent;
	}

	private Date processDate(String dateTime){

		Date d = new Date();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy hh:mm", Locale.ITALY);

			return sdf.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
}
