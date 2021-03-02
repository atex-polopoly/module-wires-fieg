package com.atex.custom.camel;

import java.io.File;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.file.GenericFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.atex.onecms.app.dam.publish.DamPublisherProcessor;
import com.atex.onecms.app.dam.standard.aspects.OneContentBean;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentWriteBuilder;
import com.atex.onecms.content.aspects.annotations.AspectDefinition;
import com.atex.onecms.content.files.FileService;
import com.polopoly.application.Application;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cm.client.CMServer;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.cm.client.HttpFileServiceClient;
import com.polopoly.integration.IntegrationServerApplication;

public abstract class BaseProcessor extends DamPublisherProcessor  {

    public static CMServer cmServer;
    public static ContentManager contentManager;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private long sleep = 1000;
    private File filePath;
    FileService fileService = null;
    private String parserClass;
    private String securityParent = "";

    private CmClient cmclient;


	public void init() {
        Application application = IntegrationServerApplication.getPolopolyApplication();
        try {


        	cmclient = (CmClient)application.getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);
            cmServer = cmclient.getCMServer();
            contentManager = cmclient.getContentManager();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	public static CMServer getCmServer() {
		return cmServer;
	}

	public static ContentManager getContentManager() {
		return contentManager;
	}
	
    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }
    
    protected void checkFileStability(final GenericFile file) throws InterruptedException {
        String fileName = file.getFileName();
        File f = new File(file.getAbsoluteFilePath());

        long curr_size = -1;
        while (curr_size < file.getFileLength()) {
            /*
             * you need to reload the file to calculate the current size
             */
            curr_size = f.length();
            LOGGER.debug("Waiting for file " + fileName + " to be completed...");
            LOGGER.debug("Uploaded: " + curr_size + " of file: " + fileName);
            Thread.sleep(getSleep());
            LOGGER.debug("New length: " + f.length() + " of file: " + fileName);
        }
    }
    
    @Override
    public void process(final Exchange exchange) throws Exception {
    	
        final Message originalMessage = exchange.getIn();
        final Map<String, Object> headers = originalMessage.getHeaders();

        filePath = new File((String) headers.get("CamelFileAbsolutePath"));

        LOGGER.info("Parsing " + filePath.getAbsolutePath());
        if (cmServer == null) {
            init();
        }

        if (cmServer == null) {
            throw new Exception("Unable to get an instance of CMServer");
        }

        Object o = exchange.getIn().getBody();
        if (o instanceof GenericFile) {
            GenericFile file = (GenericFile) o;
            try {
                String filePath = file.getAbsoluteFilePath();
                LOGGER.info("Processing image file: "+filePath);
                processFile(file);
            } catch (Exception e) {
                LOGGER.error("Error during processing", e);
                exchange.setException(e);
                throw e;
            }
        } else {
            LOGGER.error("body is not a file: " + o);
        }
    }
    
    protected abstract void processFile(final GenericFile file) throws Exception;

    protected ContentWriteBuilder<OneContentBean> createContentWriteBuilder(final OneContentBean aspect) {
        ContentWriteBuilder<OneContentBean> cwb = new ContentWriteBuilder<>();
        final AspectDefinition aspectDefinition = AnnotationUtils.findAnnotation(aspect.getClass(), AspectDefinition.class);
        if (aspectDefinition != null) {
            final String[] types = aspectDefinition.value();
            if (types.length > 0) {
                cwb.type(types[0]);
            }
        }
        cwb.mainAspectData(aspect);
        return cwb;
    }
    
    public FileService getFileService() {
    	if (fileService == null) {
    		try {
    			Application application = IntegrationServerApplication.getPolopolyApplication();

    			HttpFileServiceClient httpFileServiceClient = application.getPreferredApplicationComponent(HttpFileServiceClient.class);
    			//return httpFileServiceClient.getFileService();
    			fileService = httpFileServiceClient.getFileService();
    		} catch (IllegalApplicationStateException e) {
    			LOGGER.error(e.getMessage(), e);
    			throw new RuntimeException(e);
    		}
    	}
    	return fileService;
    }
    
    public String getParserClass() {
        return parserClass;
    }

    public void setParserClass(String parserClass) {
        this.parserClass = parserClass;
    }

	public String getSecurityParent() {
		return securityParent;
	}

	public void setSecurityParent(String securityParent) {
		this.securityParent = securityParent;
	}   
}
