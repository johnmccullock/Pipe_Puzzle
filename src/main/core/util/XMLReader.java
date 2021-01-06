package main.core.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Wrapper class for commonly used XML reading functions.
 *
 * Version 1.32 adds hasAttribute() function.
 *
 * Version 1.31.01 simply adds a reminder in the documentation of importXMLElements().
 *
 * Version 1.31 replaced buggy code with new findNode() functions to better assist version 1.3's additions.
 *
 * Version 1.3 adds searchStringAttribute(), searchIntAttribute(), searchFloatAttribute(), searchBooleanAttribute() functions.
 *
 * Version 1.2.01 simply adds a reminder in the documentation of importXMLElements().
 *
 * Version 1.2 adds readStringArrayList (for populating an ArrayList with values of the same tag), and readStringTree (for populating a TreeMap of type String, ArrayList).
 *
 * Version 1.1 includes readFloat() and readDouble() methods, and fixes some laughably ugly code.  Only I can write code THAT bad.
 *
 * @author John McCullock
 * @version 1.32 2017-08-20
 */
public class XMLReader
{
	private DocumentBuilderFactory mFactory = null;
	private DocumentBuilder mBuilder = null;
	private Document mDocument = null;
	private NodeList mRootNode = null;
	private Element mRoot = null;
	
	public XMLReader(String xml, String rootTag)
	{
		try{
			this.mFactory = DocumentBuilderFactory.newInstance();
			this.mBuilder = this.mFactory.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			this.mDocument = this.mBuilder.parse(is);
			this.mRootNode = this.mDocument.getElementsByTagName(rootTag);
			this.mRoot = (Element)this.mRootNode.item(0);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}catch(ParserConfigurationException pce){
			pce.printStackTrace();
		}catch(SAXException se){
			se.printStackTrace();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return;
	}
	
	/**
	 * Copy-constructor.
	 * @param reader XMLReader.
	 */
	public XMLReader(XMLReader reader)
	{
		this.mFactory = reader.getBuilderFactory();
		this.mBuilder = reader.getBuilder();
		this.mDocument = reader.getDocument();
		this.mRootNode = reader.getRootNode();
		this.mRoot = reader.getRoot();
		return;
	}
	
	/**
	 * Remember that imported elements have to be wrapped in document elements in order for this to work!
	 * @param xml
	 * @param rootTag
	 */
	public void importXMLElements(String xml, String rootTag)
	{
		try{
			Node temp = this.createNodes(xml, rootTag);
			Node importedNode = this.mDocument.importNode(temp, true);
			this.mRoot.appendChild(importedNode);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return;
	}
	
	private Node createNodes(String xml, String rootTag)
	{
		Node aNode = null;
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try{
			builder = builderFactory.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			Document doc = builder.parse(is);
			NodeList root = doc.getElementsByTagName(rootTag);
			aNode = (Node)root.item(0);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return aNode;
	}
	
	public Element findNode(String path)
	{
		Element results = null;
		String[] p = path.split("\\."); // Periods or dots have special meaning.  Use double escape.
		Element current = this.mRoot;
		boolean found = true;
		for(int i = 0; i < p.length; i++)
		{
			Element temp = this.findNode(current, p[i]);
			if(temp == null){
				found = false;
				break;
			}else{
				current = temp;
			}
		}
		if(found){
			results = current;
		}
		return results;
	}
	
	private Element findNode(Element current, String tag)
	{
		Element results = null;
		try{
			NodeList nodes = current.getElementsByTagName(tag);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element line = (Element)nodes.item(i);
				if(line.getTagName().equalsIgnoreCase(tag)){
					results = line;
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	public boolean containsNode(String tagName)
	{
		boolean results = false;
		try{
			NodeList nodes = this.mRoot.getElementsByTagName(tagName);
			if(nodes != null && nodes.getLength() > 0){
				results = true;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	public String readString(String tagName)
	{
		try{
			NodeList nodes = this.mRoot.getElementsByTagName(tagName);
			Element line = (Element)nodes.item(0);
			return this.getCharacterDataFromElement(line);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public boolean hasAttribute(String tagName, String targetAttribute)
	{
		boolean results = false;
		try{
			NodeList nodes = this.mRoot.getElementsByTagName(tagName);
			Element line = (Element)nodes.item(0);
			results = line.hasAttribute(targetAttribute);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	/**
	 * Takes all elements with the same tag and reads their values into an ArrayList.
	 * @param tagName String Tag name.
	 * @return ArrayList{@literal <}String{@literal >} containing all values for the specified tag.
	 */
	public ArrayList<String> readStringArrayList(String tagName)
	{
		ArrayList<String> results = new ArrayList<String>();
		try{
			NodeList nodes = this.mRoot.getElementsByTagName(tagName);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element line = (Element)nodes.item(i);
				results.add(this.getCharacterDataFromElement(line));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	public String readStringAttribute(String tagName, String attrName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return line.getAttribute(attrName);
	}
	
	public String searchStringAttribute(String path, String tagName, String attrKey, String keyValue, String attrTarget)
	{
		String results = null;
		try{
			Element node = this.findNode(path);
			NodeList nodes = node.getElementsByTagName(tagName);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element line = (Element)nodes.item(i);
				if(line.getAttribute(attrKey).equalsIgnoreCase(keyValue)){
					results = line.getAttribute(attrTarget);
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	public int searchIntAttribute(String path, String tagName, String attrKey, String keyValue, String attrTarget)
	{
		int results = 0;
		try{
			Element node = this.findNode(path);
			NodeList nodes = node.getElementsByTagName(tagName);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element line = (Element)nodes.item(i);
				if(line.getAttribute(attrKey).equalsIgnoreCase(keyValue)){
					results = Integer.parseInt(line.getAttribute(attrTarget));
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	public float searchFloatAttribute(String path, String tagName, String attrKey, String keyValue, String attrTarget)
	{
		float results = 0;
		try{
			Element node = this.findNode(path);
			NodeList nodes = node.getElementsByTagName(tagName);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element line = (Element)nodes.item(i);
				if(line.getAttribute(attrKey).equalsIgnoreCase(keyValue)){
					results = Float.parseFloat(line.getAttribute(attrTarget));
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	public boolean searchBooleanAttribute(String path, String tagName, String attrKey, String keyValue, String attrTarget)
	{
		boolean results = false;
		try{
			Element node = this.findNode(path);
			NodeList nodes = node.getElementsByTagName(tagName);
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element line = (Element)nodes.item(i);
				if(line.getAttribute(attrKey).equalsIgnoreCase(keyValue)){
					results = Boolean.parseBoolean(line.getAttribute(attrTarget));
					break;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return results;
	}
	
	public TreeMap<String, String> readStringTree(String tagName, String attrKey, String attrItem)
	{
		TreeMap<String, String> results = new TreeMap<String, String>();
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Element line = (Element)nodes.item(i);
			String key = line.getAttribute(attrKey);
			String item = line.getAttribute(attrItem);
			results.put(key, item);
		}
		return results;
	}
	
	/**
	 * Takes all elements with the same tag, and reads them into a TreeMap{@literal <}String, ArrayList{@literal <}String{@literal >}{@literal >}.
	 * Each element must have two attributes: a key and a value.  A new ArrayList is made when a new key is entered, and subsequent
	 * values are added to the ArrayList for that key.
	 * @param tagName String tag name.
	 * @param attrKey String key to be used by TreeMap.
	 * @param attrItem String value to be added to ArrayList for the associated key.
	 * @return TreeMap{@literal <}String, ArrayList{@literal <}String{@literal >}{@literal >}
	 */
	public TreeMap<String, ArrayList<String>> readStringArrayTree(String tagName, String attrKey, String attrItem)
	{
		TreeMap<String, ArrayList<String>> results = new TreeMap<String, ArrayList<String>>();
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Element line = (Element)nodes.item(i);
			String key = line.getAttribute(attrKey);
			String item = line.getAttribute(attrItem);
			
			if(!results.containsKey(key)){
				results.put(key, new ArrayList<String>());
			}
			results.get(key).add(item);
		}
		return results;
	}
	
	public int readInt(String tagName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Integer.parseInt(this.getCharacterDataFromElement(line));
	}
	
	public int readIntAttribute(String tagName, String attrName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Integer.parseInt(line.getAttribute(attrName));
	}
	
	public long readLong(String tagName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Long.parseLong(this.getCharacterDataFromElement(line));
	}
	
	public long readLongAttribute(String tagName, String attrName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Long.parseLong(line.getAttribute(attrName));
	}
	
	public float readFloat(String tagName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Float.parseFloat(this.getCharacterDataFromElement(line));
	}
	
	public float readFloatAttribute(String tagName, String attrName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Float.parseFloat(line.getAttribute(attrName));
	}
	
	public double readDouble(String tagName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Double.parseDouble(this.getCharacterDataFromElement(line));
	}
	
	public double readDoubleAttribute(String tagName, String attrName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Double.parseDouble(line.getAttribute(attrName));
	}
	
	public boolean readBoolean(String tagName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Boolean.parseBoolean(this.getCharacterDataFromElement(line));
	}
	
	public boolean readBooleanAttribute(String tagName, String attrName)
	{
		NodeList nodes = this.mRoot.getElementsByTagName(tagName);
		Element line = (Element)nodes.item(0);
		return Boolean.parseBoolean(line.getAttribute(attrName));
	}
	
	protected DocumentBuilderFactory getBuilderFactory()
	{
		return this.mFactory;
	}
	
	protected DocumentBuilder getBuilder()
	{
		return this.mBuilder;
	}
	
	protected Document getDocument()
	{
		return this.mDocument;
	}
	
	protected NodeList getRootNode()
	{
		return this.mRootNode;
	}
	
	public Element getRoot()
	{
		return this.mRoot;
	}
	
	protected String getCharacterDataFromElement(Element e)
	{
		String value = null;
		try{
			if(e != null){
				Node child = e.getFirstChild();
				if(child instanceof CharacterData){
					CharacterData cd = (CharacterData)child;
					value = cd.getData();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return value;
	}
}
