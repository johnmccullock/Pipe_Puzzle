package main.core.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.File;

/**
 * Class for reading and writing String contents to text files.
 * 
 * Version 3 included a new write function that accepts StringBuilder objects.
 * Version 3.1's write function accepts String objects.
 * 
 * @author John McCullock
 * @version 3.1 2013-09-17
 */

public class ReadWriteFile
{
	public ReadWriteFile()	{ return; }
	
	public static String read(String FileName) throws Exception, FileNotFoundException, IOException
    {
		String fileContents = null;
		
		try{
			fileContents = ReadWriteFile.read(new File(FileName));
		}catch(FileNotFoundException fnfe){
    		throw fnfe;
    	}catch(IOException ioe){
    		throw ioe;
    	}catch(Exception ex){
    		throw ex;
    	}
		
		return fileContents;
    }
	
	public static String read(File aFile) throws Exception, FileNotFoundException, IOException
    {
		String fileContents = null;
		StringBuffer bufferText = null;
		
    	try{
    		BufferedReader fr = new BufferedReader(new FileReader(aFile));
    		bufferText = new StringBuffer();
    		String line = null;
    		
            do
            {
                line = fr.readLine();
                if(line != null){
                	bufferText.append(line);
                	bufferText.append("\n");
                }
            }while(line != null);
            
            fileContents = bufferText.toString();
    	}catch(FileNotFoundException fnfe){
    		throw fnfe;
    	}catch(IOException ioe){
    		throw ioe;
    	}catch(Exception ex){
    		throw ex;
    	}finally{
    		bufferText = null;
    	}
    	
    	return fileContents;
    }
	
	public static void write(String FileName, StringBuffer bufferText) throws Exception, IOException
    {
		FileOutputStream fout = null;		

		try{
		    fout = new FileOutputStream(FileName);
		    new PrintStream(fout).print(bufferText);
		    fout.close();		
		}catch(IOException ioe){
			throw ioe;
		}catch(Exception ex){
			throw ex;
		}

    	return;
    }
	
	public static void write(String FileName, StringBuilder bufferText) throws Exception, IOException
    {
		FileOutputStream fout = null;		

		try{
		    fout = new FileOutputStream(FileName);
		    new PrintStream(fout).print(bufferText);
		    fout.close();		
		}catch(IOException ioe){
			throw ioe;
		}catch(Exception ex){
			throw ex;
		}

    	return;
    }
	
	public static void write(String FileName, String bufferText) throws Exception, IOException
    {
		FileOutputStream fout = null;		

		try{
		    fout = new FileOutputStream(FileName);
		    new PrintStream(fout).print(bufferText);
		    fout.close();		
		}catch(IOException ioe){
			throw ioe;
		}catch(Exception ex){
			throw ex;
		}

    	return;
    }
}

