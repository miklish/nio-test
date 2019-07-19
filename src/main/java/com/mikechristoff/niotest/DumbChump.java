package com.mikechristoff.niotest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DumbChump {
	
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
				logger.info("Connection " + i);
			} catch (IOException e) {
				logger.error("Could not connect : " + e);
			}
			
		}
			
	}
}
