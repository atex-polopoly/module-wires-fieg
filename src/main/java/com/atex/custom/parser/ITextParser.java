package com.atex.custom.parser;

import java.io.File;

import com.atex.onecms.app.dam.util.PrefixedProperty;
import com.atex.onecms.app.dam.wire.DamWireArticleBean;

public interface ITextParser {


	public void setFieldValueMapping(PrefixedProperty fieldValueMapping) ;

	public DamWireArticleBean parseFile(File file) throws Exception;
}
