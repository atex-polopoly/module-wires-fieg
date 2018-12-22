package com.atex.custom.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import com.atex.plugins.structured.text.StructuredText;
import org.jfree.util.Log;

import com.atex.onecms.app.dam.standard.aspects.DamWireArticleAspectBean;
import com.atex.onecms.app.dam.util.PrefixedProperty;

public class TextParserWcTagged extends BaseTextParser<OneArticleBean> {


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

        final OneArticleBean articleBean = new OneArticleBean();
        articleBean.setInputTemplate(OneArticleBean.INPUT_TEMPLATE_WIRE);

        BufferedReader input = null;

        final Map<String, String> wcFileValues;
        try {
            //use buffering
            //this implementation reads one line at a time
            input = new BufferedReader(reader);
            String line = null; //not declared within while loop


            wcFileValues = new HashMap<String, String>();
            /* reading TSRE wctagged format file... */

            String wcFieldName = "";
            StringBuffer wcFieldValue = new StringBuffer();

            while ((line = input.readLine()) != null) {
                if ((line.startsWith("@"))) {

                    if (!wcFieldName.equals("")) {
                        wcFileValues.put(wcFieldName, wcFieldValue.toString());
                    }

                    wcFieldName = line.substring(line.indexOf("@") + 1, line.indexOf(":"));

                    wcFieldValue = new StringBuffer();
                    wcFieldValue.append(line.substring(line.indexOf(":") + 1));
                } else {
                    wcFieldValue.append(System.getProperty("line.separator"));
                    wcFieldValue.append(line);
                }

            }
            if (!wcFieldName.equals("")) {
                wcFileValues.put(wcFieldName, wcFieldValue.toString());
            }
        } finally {
            if (input != null) {
                input.close(); // Important to avoid NFS errors
            }
        }

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
        Date creationDate = sdf.parse(tDate);

        final PrefixedProperty fieldValueMapping = getFieldValueMapping();
        if (fieldValueMapping != null) {

            if (fieldValueMapping.getProperty("headline", headline) != null) {
                headline = fieldValueMapping.getProperty("headline", headline);
            }

            if (fieldValueMapping.getProperty("source", source) != null) {
                source = fieldValueMapping.getProperty("source", source);
            }

            if (fieldValueMapping.getProperty("section", section) != null) {
                section = fieldValueMapping.getProperty("section", section);
            }

            if (fieldValueMapping.getProperty("priority", priority) != null) {
                priority = fieldValueMapping.getProperty("priority", priority);
            }


            if (fieldValueMapping.getProperty("text", text) != null) {
                text = fieldValueMapping.getProperty("text", text);
            }
        }

        articleBean.setHeadline(new StructuredText(headline));
        articleBean.setSource(source);
        articleBean.setSection(section);
        articleBean.setBody(new StructuredText(text));
        articleBean.setCreationdate(creationDate);

        return articleBean;
    }

}
