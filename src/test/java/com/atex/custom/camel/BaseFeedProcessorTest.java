package com.atex.custom.camel;


import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import com.atex.onecms.app.dam.standard.aspects.OneContentBean;
import org.apache.camel.component.file.GenericFile;
import org.junit.Assert;
import org.junit.Test;


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
        final OneArticleBean bean = new OneArticleBean();
        final ContentWriteBuilder<OneContentBean> cwb = processor.createContentWriteBuilder(bean);
        Assert.assertNotNull(cwb);

        final ContentWrite<OneContentBean> cw = cwb.build();
        Assert.assertEquals(bean, cw.getContentData());
        Assert.assertEquals("atex.dam.standard.WireArticle", cw.getContentDataType());
    }

    @Test
    public void testContentWriteBuilderWireImageAspectBean() {
        final OneArticleBean bean = new OneArticleBean();
        final ContentWriteBuilder<OneContentBean> cwb = processor.createContentWriteBuilder(bean);
        Assert.assertNotNull(cwb);

        final ContentWrite<OneContentBean> cw = cwb.build();
        Assert.assertEquals(bean, cw.getContentData());
        Assert.assertEquals("atex.dam.standard.WireImage", cw.getContentDataType());
    }

    private class TestProcessor extends BaseFeedProcessor {

        @Override
        protected void processFile(final GenericFile file) throws Exception {

        }

    }
}