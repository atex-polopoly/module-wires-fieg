package com.atex.custom.camel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.atex.onecms.app.dam.DamContentBean;
import com.atex.onecms.app.dam.util.PrefixedProperty;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentWriteBuilder;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.aspects.annotations.AspectDefinition;
import com.polopoly.application.Application;
import com.polopoly.cm.client.CMServer;
import com.polopoly.cm.client.CmClientFacade;
import com.polopoly.integration.IntegrationServerApplication;

public abstract class BaseFeedProcessor implements Processor {

    private static CMServer cmServer;
    private static ContentManager contentManager;

    public PrefixedProperty fieldValueMapping = null;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /*
     * default value can be overwritten by configuration
     */
    private long sleep = 1000;
    private String parserClass;
    private String fieldValueProperties;
    private String securityParent = "dam.assets.common.d";

    ContentId securityParentContentId;

    public static CMServer getCmServer() {
        return cmServer;
    }

    public static ContentManager getContentManager() {
        return contentManager;
    }

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


            CmClientFacade cmclient = (CmClientFacade) application.getApplicationComponent("cm_client");

            cmServer = cmclient.getCMServer();
            contentManager = cmclient.getContentManager();

            if (securityParentContentId == null) {
                securityParentContentId = contentManager.resolve(securityParent, Subject.NOBODY_CALLER).getContentId();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void loadFieldValueMapping() {

        if (fieldValueMapping == null) {
            fieldValueMapping = new PrefixedProperty();
        }

        if (fieldValueProperties != null) {
            InputStream is = null;
            try {
                try {
                    is = new ClassPathResource(fieldValueProperties).getInputStream();
                    fieldValueMapping.load(is);
                } catch (IOException e) {
                    is = new FileSystemResource(fieldValueProperties).getInputStream();
                    fieldValueMapping.load(is);
                }
                /*
                if (fieldValueProperties.startsWith("/")) {
                    FileInputStream fileInput = new FileInputStream(new File(fieldValueProperties));
                    fieldValueMapping.load(fileInput);
                    fileInput.close();
                } else {
                    final InputStream is = this.getClass().getResourceAsStream(fieldValueProperties);
                    fieldValueMapping.load(is);
                    is.close();
                }
                */
            } catch (Exception e) {
                LOGGER.error("Error while reading configuration file:" + fieldValueProperties, e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

    public void process(final Exchange exchange) throws Exception {
        if (cmServer == null) {
            init();
        }

        if (cmServer == null) {
            throw new Exception("Unable to get an instance of CMServer");
        }

        if (fieldValueMapping == null) {
            loadFieldValueMapping();
        }

        Object o = exchange.getIn().getBody();
        if (o instanceof GenericFile) {
            GenericFile file = (GenericFile) o;
            try {
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

    protected ContentWriteBuilder<DamContentBean> createContentWriteBuilder(final DamContentBean aspect) {
        ContentWriteBuilder<DamContentBean> cwb = new ContentWriteBuilder<>();
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
}
