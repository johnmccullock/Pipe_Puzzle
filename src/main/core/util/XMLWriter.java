package main.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;

/**
 * Wrapper class for commonly used XML writing functions.
 *
 * Version 1.2 adds nonNull() to avoid NullPointerException.
 *
 * Version 1.1 includes a quick and dirty XML serializer (pretty-print) function.
 *
 * @author John McCullock
 * @version 1.2 2017-10-16
 */
public class XMLWriter
{
	private final boolean DEFAULT_OUTPUTKEYS_INDENT = true;
	private final int DEFAULT_INDENT_SPACES = 4;
	private final boolean DEFAULT_INTERCEPT_DECLARATION = true;
	private final boolean DEFAULT_MANUAL_INSERT_DECLARATION = true;
	private final String DEFAULT_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	protected StringWriter mStringWriter = null;
	protected XMLOutputFactory mOutputFactory = null;
	protected XMLEventWriter mEventWriter = null;
	protected XMLEventFactory mEventFactory = null;
	protected boolean mIndent = DEFAULT_OUTPUTKEYS_INDENT;
	protected int mSpaces = DEFAULT_INDENT_SPACES;
	protected boolean mInterceptDeclaration = DEFAULT_INTERCEPT_DECLARATION;
	protected boolean mManualInsertDeclaration = DEFAULT_MANUAL_INSERT_DECLARATION;
	protected String mDeclaration = DEFAULT_DECLARATION;
	
	public XMLWriter()
	{
		try{
			this.mStringWriter = new StringWriter();
			this.mOutputFactory = XMLOutputFactory.newInstance();
			this.mEventWriter = this.mOutputFactory.createXMLEventWriter(this.mStringWriter);
			this.mEventFactory = XMLEventFactory.newInstance();
		}catch(XMLStreamException xmlse){
			xmlse.printStackTrace();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return;
	}
	
	/**
	 * Adds a complete element to EventWriter.
	 * Example: {@literal <}tag{@literal >}value{@literal <}/tag{@literal >}
	 * @param tag String name of tag.
	 * @param value String value of the element.
	 */
	public void addXMLElement(final String tag, final String value)
	{
		try{
			StartElement start = this.mEventFactory.createStartElement("", "", tag);
			this.mEventWriter.add(start);
			
			Characters characters = this.mEventFactory.createCharacters(value);
			this.mEventWriter.add(characters);
			
			EndElement end = this.mEventFactory.createEndElement("", "", tag);
			this.mEventWriter.add(end);
		}catch(XMLStreamException xmlse){
			xmlse.printStackTrace();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return;
	}
	
	public StringWriter getStringWriter()
	{
		return this.mStringWriter;
	}
	
	public XMLEventWriter getEventWriter()
	{
		return this.mEventWriter;
	}
	
	public XMLEventFactory getEventFactory()
	{
		return this.mEventFactory;
	}
	
	/**
	 * Works in conjunction with serializeXML() to "pretty-print" XML output.
	 * @param serializer Transformer object
	 * @return Transformer object originally passed into this method.
	 */
	public Transformer configSerializer(Transformer serializer)
	{
		if(this.mIndent){
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(this.mSpaces));
		}else{
			serializer.setOutputProperty(OutputKeys.INDENT, "no");
		}
		if(this.mInterceptDeclaration){
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}else{
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		}
		return serializer;
	}
	
	/**
	 * Quick and dirty XML serializer.
	 * @param xml String containing XML to format.
	 * @return String serialized (pretty-print) XML.
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	public String serializeXML(String xml)
	{
		StreamResult res = null;
		try{
			Transformer serializer = SAXTransformerFactory.newInstance().newTransformer();
			serializer = this.configSerializer(serializer);
			Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
			res = new StreamResult(new ByteArrayOutputStream());
			serializer.transform(xmlSource, res);
			
			if(this.mManualInsertDeclaration){
				return this.mDeclaration + "\n" + new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());
			}
		}catch(TransformerConfigurationException tce){
			tce.printStackTrace();
		}catch(TransformerException te){
			te.printStackTrace();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());
	}
	
	/**
	 * Replaces null values with empty Strings.  This keeps EventFactory.createAttribute() from throwing an exception.
	 * @param value String
	 * @return empty String if value parameter is null, otherwise the original value is returned.
	 */
	public String nonNull(String value)
	{
		return value == null ? new String() : value;
	}
	
	@Override
	public String toString()
	{
		return new String("XMLWriter class: " + this.getClass().hashCode());
	}
}
