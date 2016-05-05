package com.atex.custom.parser;

import java.io.File;

import com.atex.onecms.app.dam.DamContentBean;
import com.atex.onecms.app.dam.util.PrefixedProperty;

public interface ITextParser {

	void setFieldValueMapping(PrefixedProperty fieldValueMapping) ;

	DamContentBean parseFile(File file) throws Exception;
}
