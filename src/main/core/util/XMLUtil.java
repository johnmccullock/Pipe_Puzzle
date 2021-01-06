package main.core.util;

public class XMLUtil
{
	public static XMLReader getReaderResource(String resourcePath, String rootTag)
	{
		XMLReader reader = null;
		try{
			String xml = ReadWriteFile.read(XMLUtil.class.getResource(resourcePath).getFile());
			reader = new XMLReader(xml, rootTag);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return reader;
	}
	
	public static XMLReader getReaderAbsolute(String absolutePath, String rootTag)
	{
		XMLReader reader = null;
		try{
			String xml = ReadWriteFile.read(absolutePath);
			reader = new XMLReader(xml, rootTag);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return reader;
	}
}
