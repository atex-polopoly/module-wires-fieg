package com.atex.custom.camel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atex.custom.parser.ITextParser;
import com.atex.onecms.app.dam.util.PrefixedProperty;
import com.atex.onecms.app.dam.wire.DamWireArticleBean;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.ContentWriteBuilder;
import com.atex.onecms.content.InsertionInfoAspectBean;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.metadata.MetadataInfo;
import com.polopoly.application.Application;
import com.polopoly.cm.client.CMServer;
import com.polopoly.cm.client.CmClientFacade;
import com.polopoly.integration.IntegrationServerApplication;
import com.polopoly.metadata.Metadata;

public class FiegFeedProcessor implements Processor {

	private static CMServer cmServer;
	private static ContentManager contentManager;


	public static final String SCHEME_TMP = "tmp";
	private static final Subject SYSTEM_SUBJECT = new Subject("98", null);

	public PrefixedProperty fieldValueMapping = null;

	protected final Logger log = LoggerFactory.getLogger(getClass());

	/*
	 * default value can be overwritten by configuration
	 */
	private long sleep = 1000;
	private String parserClass;
	private String fieldValueProperties;
	private String securityParent = "dam.assets.common.d";
	
	ContentId securityParentContentId;
	
	public String getSecurityParent() {
		return securityParent;
	}

	public void setSecurityParent(String securityParent) {
		this.securityParent = securityParent;
	}

	
	public long getSleep() {
		return sleep;
	}

	public void setSleep(long sleep) {
		this.sleep = sleep;
	}
	
	public String getParserClass() {
		return parserClass;
	}

	public void setParserClass(String parserClass) {
		this.parserClass = parserClass;
	}


	public String getFieldValueProperties() {
		return fieldValueProperties;
	}

	public void setFieldValueProperties(String fieldValueProperties) {
		this.fieldValueProperties = fieldValueProperties;
	}
	
	
	public void init() {
		
		
		Application application = IntegrationServerApplication.getPolopolyApplication();
		try {


			CmClientFacade cmclient = (CmClientFacade)application.getApplicationComponent("cm_client");

			cmServer = cmclient.getCMServer();
			contentManager = cmclient.getContentManager();

			if (securityParentContentId==null) {
				securityParentContentId = contentManager.resolve(securityParent, Subject.NOBODY_CALLER).getContentId();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public  void loadFieldValueMapping(){
	
		if (fieldValueMapping == null)
			fieldValueMapping = new PrefixedProperty();

		try {
			FileInputStream fileInput = new FileInputStream(new File(fieldValueProperties));  
			fieldValueMapping.load(fileInput);
			fileInput.close();

		} catch (Exception e) {
			log.error("Error while reading configuration file:"+fieldValueProperties);
		}    	
	}

	public void process(Exchange exchange) throws Exception {

		if(cmServer == null)
			init();

		if(cmServer == null)
			throw new Exception("Impossible to get an instance of FileService");

		if(fieldValueMapping == null)
			loadFieldValueMapping();

		
		Object o = exchange.getIn().getBody();
		if (o instanceof GenericFile){
			GenericFile file = (GenericFile)o;	
			/*
			 * check whether the file upload is completed
			 * 
			 */
			BufferedInputStream bis = null;
			try {
				String fileName = file.getFileName();
				File f = new File(file.getAbsoluteFilePath());

				long curr_size = -1;
				while (curr_size  < file.getFileLength() ){
					/*
					 * you need to reload the file to calculate the current size
					 */	
					curr_size = f.length();
					log.debug("Waiting for file "+fileName+" to be completed...");
					log.debug("Uploaded: "+curr_size+" of file: "+fileName);
					Thread.sleep(sleep);
					log.debug("New length: "+f.length()+ " of file: "+fileName);

				}

				String filePath = file.getAbsoluteFilePath();

				/*
				 * Upload file to File Storage Server
				 */
				bis = new BufferedInputStream(new FileInputStream(filePath));


				Class<?> c = Class.forName(parserClass);
				ITextParser parser = (ITextParser)c.newInstance();
				
				parser.setFieldValueMapping(fieldValueMapping);

				// contentData
				DamWireArticleBean damWireArticleBean = parser.parseFile(new File(filePath));

				// p.InsertionInfo
				InsertionInfoAspectBean insertionInfoAspectBean = new InsertionInfoAspectBean(securityParentContentId);


				/*
				 * add atex.metadata
				 */
				MetadataInfo metadataInfo = new MetadataInfo();
				Set<String> set = new HashSet<String>();
				set.add("p.StandardCategorization");
				metadataInfo.setTaxonomyIds(set);
				Metadata metadata = new Metadata();

				/*
				 * metadata empty ... to be defined if any availabel metadata for these feeds
				 */
				metadataInfo.setMetadata(metadata);



				ContentWriteBuilder<DamWireArticleBean> cwb = new ContentWriteBuilder<DamWireArticleBean>();
				cwb.mainAspectData(damWireArticleBean);

				cwb.aspect("p.InsertionInfo", insertionInfoAspectBean);
				cwb.aspect("atex.Metadata", metadataInfo);

				ContentWrite<DamWireArticleBean> content = cwb.buildCreate();
				contentManager.create(content, SYSTEM_SUBJECT);
			} catch (Exception e) {
				exchange.setException(e);
				throw e;
			} finally{
				bis.close();
			}


		}
	}




}
