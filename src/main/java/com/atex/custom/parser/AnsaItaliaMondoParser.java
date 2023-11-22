package com.atex.custom.parser;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import com.atex.plugins.structured.text.StructuredText;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.util.StringUtil;

public class AnsaItaliaMondoParser extends AnsaParser {

	public AnsaItaliaMondoInfo parseFile(BufferedInputStream bis) throws Exception {
		AnsaItaliaMondoInfo ansaItaliaMondoInfo = new AnsaItaliaMondoInfo();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
		//an instance of builder to parse the specified xml file  
		DocumentBuilder db = dbf.newDocumentBuilder();  
		Document document = db.parse(bis);

		OneArticleBean articleBean = new OneArticleBean();
		articleBean.setInputTemplate(OneArticleBean.INPUT_TEMPLATE_WIRE);
		String headline = "";
		String lead = "";
		String body = "";
		String location = "";
		String category = "";
		String keywords = "";
		String date = "";
		String ANSA_SOURCE = "ANSA";
		ArrayList<String> fileImages = new ArrayList<String>();
		Element root = document.getDocumentElement();
		final Node bodyNode = getChildElement(root, "body");
		if (bodyNode != null) {
			final Node bodyheadNode = getChildElement(bodyNode, "body.head");
			if (bodyheadNode != null) {
				final Node hedlineNode = getChildElement(bodyheadNode, "hedline");
				if (hedlineNode != null) {
					headline = getNodeText(hedlineNode);
				}
				final Node bylineNode = getChildElement(bodyheadNode, "byline");
				if (bylineNode != null) {
					lead = getNodeText(bylineNode);
				}
				final Node datelineNode = getChildElement(bodyheadNode, "dateline");
				if (datelineNode != null) {
					final Node locationNode = getChildElement(datelineNode, "location");
					if (locationNode != null) {
						location = getNodeText(locationNode);
						location = location.replace("&quot;", "\"");
						location = location.trim();
					}
				}
				final Node bodyContentNode = getChildElement(bodyNode, "body.content");
				if (bodyContentNode != null) {
					final Node blockNode = getChildElement(bodyContentNode, "block");
					if (blockNode != null) {
						final Node pNode = getChildElement(blockNode, "p");
						if (pNode != null) {
							body = getNodeText(pNode);
							if (!StringUtil.isEmpty(body)) {
								body = body.replaceAll("\\.\r\n|\\.\r|\\.\n", ".</p><p>");
								body = body.replaceAll("\r\n|\r|\n", " ");
								body = "<p>" + body + "</p>";
							}
						}
						final ArrayList<Node> imgsNode = getChildsElement(blockNode, "img");
						for(Node node : imgsNode) {
							fileImages.add(getAttributeValue(node, "source"));
						}
					}
				}
			}
		}

		final Node headNode = getChildElement(root, "head");
		if (headNode != null) {
			Node meta_category = getChildElement(headNode, "meta", "name", "category");
			if (meta_category != null) {
				category = getAttributeValue(meta_category, "content");
			}
			Node meta_keyword = getChildElement(headNode, "meta", "name", "keyword");
			if (meta_keyword != null) {
				keywords = getAttributeValue(meta_keyword, "content");
				keywords = keywords.replace("&quot;", "\"");
				keywords = keywords.trim();
			}
			final Node docdataNode = getChildElement(headNode, "docdata");
			if (docdataNode != null) {
				Node dateIssueNode = getChildElement(docdataNode, "date.issue");
				if (dateIssueNode != null) {
					date = getAttributeValue(dateIssueNode, "norm");
				}
			}
		}

		articleBean.setHeadline(new StructuredText(headline));
		articleBean.setLead(new StructuredText(lead));
		articleBean.setBody(new StructuredText(body));

		Date d_pubDate = formatterPubDate.parse(date);
		articleBean.setPublicationDate(d_pubDate.getTime());
		articleBean.setSource(ANSA_SOURCE);

		ansaItaliaMondoInfo.setBean(articleBean);

		final Metadata metadata = new Metadata();
		if (!StringUtil.isEmpty(location)) {
			Dimension dimension = Optional
					.ofNullable(metadata.getDimensionById("dimension.Location"))
					.orElse(new Dimension("dimension.Location", "Location", false));
			if (metadata.getDimensionById("dimension.Location") == null) {
				dimension.addEntity(new Entity(location));
				metadata.addDimension(dimension);
			} else {
				dimension.mergeEntity(new Entity(location));
			}
		}
		String[] tag_list = keywords.split(",");
		List<String> tags = new ArrayList<String>( Arrays.asList( tag_list ) );
		if (tag_list.length > 0) {
			Dimension dimension = Optional
					.ofNullable(metadata.getDimensionById("dimension.Tag"))
					.orElse(new Dimension("dimension.Tag", "Tag", false));
			if (metadata.getDimensionById("dimension.Tag") == null) {
				tags.forEach(t -> dimension.addEntity(new Entity(t.trim())));
				metadata.addDimension(dimension);
			} else {
				tags.forEach(t -> dimension.mergeEntity(new Entity(t.trim())));
			}
		}
		ansaItaliaMondoInfo.setMetadata(metadata);
		ansaItaliaMondoInfo.setFileImages(fileImages);
		return ansaItaliaMondoInfo;
	}

}
