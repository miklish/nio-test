package com.mikechristoff.niotest;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadTester
{
	
	static Logger logger;
	
	static {
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream("src//main//resources//logger.properties"));
		} catch(FileNotFoundException e) {
			System.out.println("Logger File Not Found : " + e.getMessage());
			System.exit(1);			
		} catch(IOException e) {
			System.out.println("Logger IO Error : " + e.getMessage());
			System.exit(2);			
		}		
		logger = LoggerFactory.getLogger(Util.class);	
	}		
	
	public static void main(String[] args) {
		for(int i = 0; i < 3000; ++i) {
			try (
					Socket s = new Socket("localhost", 8080)
				) {
			 
				//s.getOutputStream().write("CAPITALS_lowercase".getBytes(Charset.forName("UTF-8")));
                //String retVal = getString(s.getInputStream());
                //logger.info("Connection " + i + " : RESULT = " + retVal);
                
                logger.info("Connection " + i);
                
			} catch (IOException e) {
				logger.error("Could not connect : " + e);
			}
			
		}
	}
	
	public static String getString(InputStream inputStream)
	{
	    String retVal = null;
		try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            // StandardCharsets.UTF_8.name() > JDK 7
            retVal = result.toString("UTF-8");
        } catch(Exception e) {
        }
        
		return retVal;
	}
}
