package com.atex.custom.parser;

import java.io.File;


import com.atex.onecms.app.dam.standard.aspects.OneContentBean;
import com.atex.onecms.app.dam.util.PrefixedProperty;

public interface ITextParser {

	void setEncoding(String encoding);
	void setFieldValueMapping(PrefixedProperty fieldValueMapping) ;

	OneContentBean parseFile(File file) throws Exception;
}
