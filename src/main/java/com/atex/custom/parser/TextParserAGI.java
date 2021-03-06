package com.atex.custom.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import com.atex.plugins.structured.text.StructuredText;
import org.jfree.util.Log;

import com.atex.onecms.app.dam.util.PrefixedProperty;

public class TextParserAGI extends BaseTextParser<OneArticleBean> {


	private static PrefixedProperty fieldValueMapping;


	public void setFieldValueMapping(PrefixedProperty fieldValueMapping) {
		TextParserAGI.fieldValueMapping = fieldValueMapping;
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
			if (line.trim().equals("ZCZC")){
				// do nothing, just a marker
			}else if( l == 1){
				hdrline = line;
			}else if( l == 2 && line.trim().length() == 0){
				// do nothing, empty line
			}else if( l == 3 && line.endsWith("=")){
				headline = line;
			}else {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
			l++;
		}

		input.close(); // Important to avoid NFS errors

		String text = processText(contents.toString());
		Date creationDate = processDate(contents.toString());


		String source = hdrline.substring(0, 3);
		String seqnum = hdrline.substring(3, 8); 
		String priority = hdrline.substring(8, 9); 
		String section 	= hdrline.substring(10, 13); 



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

		int endIndex = content.lastIndexOf("(AGI)");
		content = content.substring(0, endIndex);

		return content;
	}

	private Date processDate(String content){

		Date d = new Date();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("ddhhmm MMM yy");

			String[] lines = content.split("\n");
			int dateLineIndex = lines.length - 3;
			for (int i = lines.length - 1; i > 0; i--) {
				String line = lines[i];
				if(line.trim().equals("NNNN")){
					dateLineIndex=i-2;
					break;
				}
			}
			String dateLine = lines[dateLineIndex];
			return sdf.parse(dateLine);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
}
