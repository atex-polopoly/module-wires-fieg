package com.atex.custom.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import org.junit.Assert;
import org.junit.Test;

import com.atex.onecms.app.dam.standard.aspects.DamWireArticleAspectBean;

/**
 * Unit test for {@link TextParserWcTagged}.
 *
 * @author mnova
 */
public class TextParserWcTaggedTest {

    private TextParserWcTagged parser = new TextParserWcTagged();

    @Test
    public void testArticle() throws Exception {
        final InputStream is = this.getClass().getResourceAsStream("/wire/160429113517B");
        Assert.assertNotNull(is);

        final OneArticleBean article = parser.parseFile(new InputStreamReader(is));
        Assert.assertNotNull(article);

        Assert.assertEquals("RADCOR", article.getSource());
        Assert.assertEquals("CONFCOMMERCIO: SANGALLI, L'ILLEGALITÀ SOTTRAE AL SETTORE 27 MILIARDI DI EUROE METTE A RISC", article.getHeadline());
        Assert.assertEquals("CONFCOMMERCIO...", article.getBody());
        Assert.assertEquals("ECONOMIA", article.getSection());

        final Calendar c = Calendar.getInstance();
        c.set(2015, Calendar.NOVEMBER, 25, 15, 27, 0);

        Assert.assertEquals(c.getTime().toString(), article.getCreationdate().toString());
    }

}