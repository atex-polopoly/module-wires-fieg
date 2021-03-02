package com.atex.custom.parser;

import java.io.BufferedInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atex.onecms.app.dam.standard.aspects.OneArticleBean;
import com.polopoly.metadata.Metadata;
import com.polopoly.util.StringUtil;

public abstract class AnsaParser {

	public static Pattern XML_COMMENT = Pattern.compile("<!--.*-->");
	public static SimpleDateFormat formatterPubDate= new SimpleDateFormat("yyyy-MM-dd HH:mm");
	
	public abstract AnsaItaliaMondoInfo parseFile(BufferedInputStream bis) throws Exception;

	public static Node getChildElement(Node element, String name, String attributeName, String attributeValue) {
		final NodeList nodeList = element.getChildNodes();
		for (int idx = 0; idx < nodeList.getLength(); idx++) {
			final Node node = nodeList.item(idx);
			if (isTag(node, name) && getAttributeValue(node, attributeName).equals(attributeValue)) {
				return node;
			}
		}
		return null;
	}

	public static Node getChildElement(final Node element, final String name) {
		final NodeList nodeList = element.getChildNodes();
		for (int idx = 0; idx < nodeList.getLength(); idx++) {
			final Node node = nodeList.item(idx);
			if (isTag(node, name)) {
				return node;
			}
		}
		return null;
	}

	public static ArrayList<Node> getChildsElement(final Node element, final String name) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		final NodeList nodeList = element.getChildNodes();
		for (int idx = 0; idx < nodeList.getLength(); idx++) {
			final Node node = nodeList.item(idx);
			if (isTag(node, name)) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	public static boolean isNode(final Node n) {
		return (n != null) && (n.getNodeType() == Node.ELEMENT_NODE);
	}

	public static boolean isTag(final Node n, final String tagName) {
		return isNode(n) && n.getNodeName().equals(tagName);
	}

	public static String getChildText(final Node element, final String name) {
		return getNodeText(getChildElement(element, name));
	}

	public static String getNodeText(final Node node) {
		if (node != null) {
			final NodeList nodeList = node.getChildNodes();
			if (nodeList != null && nodeList.getLength() > 0) {
				final StringBuilder sb = new StringBuilder();
				for (int idx = 0; idx < nodeList.getLength(); idx++) {
					final Node n = nodeList.item(idx);
					final String s = nodeToString(n);
					if (!StringUtil.isEmpty(s)) {
						sb.append(s.trim());
					}
				}
				return XML_COMMENT.matcher(sb).replaceAll("");
				//return sb.toString();
			} else {
				return nodeToString(node);
			}
		}
		return null;
	}

	public static String getAttributeValue(final Node element, final String name) {
		final NamedNodeMap attributes = element.getAttributes();
		if (attributes != null) {
			final Node node = attributes.getNamedItem(name);
			if (node != null) {
				return node.getNodeValue();
			}
		}
		return null;
	}

	public static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
		return XML_COMMENT.matcher(sw.toString()).replaceAll("");
		//return sw.toString();
	}

	public class AnsaItaliaMondoInfo {
		private OneArticleBean bean = null;
		private String securityParent = null;
		private Metadata metadata = null;
		private ArrayList<String> fileImages = null;

		public OneArticleBean getBean() {
			return bean;
		}

		public void setBean(OneArticleBean bean) {
			this.bean = bean;
		}

		public String getSecurityParent() {
			return securityParent;
		}

		public void setSecurityParent(String securityParent) {
			this.securityParent = securityParent;
		}

		public Metadata getMetadata() {
			return metadata;
		}

		public void setMetadata(Metadata metadata) {
			this.metadata = metadata;
		}

		public ArrayList<String> getFileImages() {
			return fileImages;
		}

		public void setFileImages(ArrayList<String> fileImages) {
			this.fileImages = fileImages;
		}
	}
}
