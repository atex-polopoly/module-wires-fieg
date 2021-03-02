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

import org.jfree.util.Log;

import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import com.atex.onecms.app.dam.util.PrefixedProperty;
import com.atex.plugins.structured.text.StructuredText;

public class TextParserItalpress extends BaseTextParser<OneArticleBean> {


	private static PrefixedProperty fieldValueMapping;


	public void setFieldValueMapping(PrefixedProperty fieldValueMapping) {
		TextParserItalpress.fieldValueMapping = fieldValueMapping;
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
		String hdrline1 = "";
		String hdrline2 = "";
		String headline = "";

		while (( line = input.readLine()) != null){
			if (l == 0 ){
				hdrline1 = line;
			}else if( l == 1){
				hdrline2 = line;
			}else if( l == 2 ){
			  headline = line;
			}else {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
			l++;
		}

		input.close(); // Important to avoid NFS errors

		String text = processText(contents.toString());
		String signature = processSignature(contents.toString());
		Date creationDate = processDate(contents.toString());

		String section = hdrline2.substring(0, 3);
		String source = getSource(hdrline1);

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
		if (signature.isEmpty()) 
		  articleBean.setBody(new StructuredText(text));
		else
		  articleBean.setBody(new StructuredText(String.format("%s<BR>%s", text, signature)));
		articleBean.setCreationdate(creationDate);

		return articleBean;

	}

  private String getSource(String line)
  {
    String[] headers = line.split(" ");
		String source = "";
		if (headers.length > 1) {
		  source = headers[1];
		}
    return source;
  }

	private String processText(String content){
		content=content.trim();
		int endIndex = content.lastIndexOf("(ITALPRESS)");
		if (endIndex<0){
			endIndex = content.length()-1;
			for (int i=0; i<3; i++){
				if (content.substring(0,endIndex).lastIndexOf("\n") > 0) {
					endIndex = content.substring(0, endIndex).lastIndexOf("\n");
				}
			}
		}
		if (endIndex>0){
			content = content.substring(0, endIndex);
		}

		return content;
	}

  private Date processDate(String content){

    Date d = new Date();
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy HH:mm", Locale.ITALY);

      String[] lines = content.split("\n");
      int dateLineIndex = lines.length - 3;
      for (int i = lines.length - 1; i > 0; i--) {
        String line = lines[i];
        if(line.trim().equals("NNNN")){
          dateLineIndex=i-1;
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
  
   private String processSignature(String content){

    String signature = null;
		try {

			String[] lines = content.split("\n");
			int signatureLineIndex = lines.length - 3;
			for (int i = lines.length - 1; i > 0; i--) {
				String line = lines[i];
				if(line.trim().equals("NNNN")) {
					signatureLineIndex=i-2;
					break;
				}
			}
			signature = lines[signatureLineIndex];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return signature;
	}
}
