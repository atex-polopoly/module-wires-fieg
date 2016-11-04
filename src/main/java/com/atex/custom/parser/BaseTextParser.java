package com.atex.custom.parser;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.jfree.util.Log;

import com.atex.onecms.app.dam.DamContentBean;
import com.atex.onecms.app.dam.util.PrefixedProperty;

/**
 * Base class for parsers.
 *
 * @author mnova
 */
public abstract class BaseTextParser<T extends DamContentBean> implements ITextParser {

	private String encoding = "UTF-8";
    private PrefixedProperty fieldValueMapping;

    public void setFieldValueMapping(final PrefixedProperty fieldValueMapping) {
        this.fieldValueMapping = fieldValueMapping;
    }

    public PrefixedProperty getFieldValueMapping() {
        return fieldValueMapping;
    }

    
    public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public T parseFile(final File inputFile) throws Exception {
        try {
            //FileReader always assumes default encoding is OK!
            return parseFile(new FileReader(inputFile));
        } catch (Exception e) {
            Log.error("Error in processing file: " + inputFile.getAbsolutePath() + " " + e.getMessage(), e);
            throw e;
        }
    }

    abstract T parseFile(final Reader reader) throws Exception;

}
