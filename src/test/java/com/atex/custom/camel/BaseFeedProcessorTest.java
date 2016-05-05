package com.atex.custom.camel;

import org.apache.camel.component.file.GenericFile;
import org.junit.Assert;
import org.junit.Test;

import com.atex.onecms.app.dam.DamContentBean;
import com.atex.onecms.app.dam.standard.aspects.DamWireArticleAspectBean;
import com.atex.onecms.app.dam.standard.aspects.DamWireImageAspectBean;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.ContentWriteBuilder;

/**
 * Unit test for {@link BaseFeedProcessor}.
 *
 * @author mnova
 */
public class BaseFeedProcessorTest {

    private TestProcessor processor = new TestProcessor();

    @Test
    public void testLoadEmptyResource() {
        processor.loadFieldValueMapping();

        Assert.assertNotNull(processor.fieldValueMapping);
        Assert.assertEquals(0, processor.fieldValueMapping.size());
    }

    @Test
    public void testLoadResource() {
        processor.setFieldValueProperties("valueMapping.properties");
        processor.loadFieldValueMapping();

        Assert.assertNotNull(processor.fieldValueMapping);
        Assert.assertEquals(18, processor.fieldValueMapping.size());
    }

    @Test
    public void testContentWriteBuilderWireArticleAspectBean() {
        final DamWireArticleAspectBean bean = new DamWireArticleAspectBean();
        final ContentWriteBuilder<DamContentBean> cwb = processor.createContentWriteBuilder(bean);
        Assert.assertNotNull(cwb);

        final ContentWrite<DamContentBean> cw = cwb.build();
        Assert.assertEquals(bean, cw.getContentData());
        Assert.assertEquals("atex.dam.standard.WireArticle", cw.getContentDataType());
    }

    @Test
    public void testContentWriteBuilderWireImageAspectBean() {
        final DamWireImageAspectBean bean = new DamWireImageAspectBean();
        final ContentWriteBuilder<DamContentBean> cwb = processor.createContentWriteBuilder(bean);
        Assert.assertNotNull(cwb);

        final ContentWrite<DamContentBean> cw = cwb.build();
        Assert.assertEquals(bean, cw.getContentData());
        Assert.assertEquals("atex.dam.standard.WireImage", cw.getContentDataType());
    }

    private class TestProcessor extends BaseFeedProcessor {

        @Override
        protected void processFile(final GenericFile file) throws Exception {

        }

    }
}