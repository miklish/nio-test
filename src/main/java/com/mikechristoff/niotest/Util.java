package com.mikechristoff.niotest;

/*
 * 		https://www.youtube.com/watch?v=vkjNjZiMt4w
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.LogManager;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Util {
	
	private static Logger logger;
	
	static {
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream("src//main//resources//logger.properties"));
		} catch(FileNotFoundException e) {
			System.exit(1);
		} catch(IOException e) {
			System.exit(2);			
		}		
		logger = LoggerFactory.getLogger(Util.class);	
	}
	
	public static int switchCase(int data) {
		if(Character.isLetter(data)) return data ^ ' ';
		return data;
	}
	
	public static void process(Socket s) {
		try (
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream()
			)
		{
			int data;
			while((data = in.read()) != -1) {
				if(data == '*') {
					in.close();					
					out.close();
					logger.info("Closing : " + s);
					break;
				}
				data = Util.switchCase(data);
				out.write(data);
			}
		} catch(IOException e) {
			logger.error(e.getMessage());
		}
	}	

	public static void process(SocketChannel sc) {
		try {
			// position == 0
			//limit == capacity == 1024
			ByteBuffer buf = ByteBuffer.allocateDirect(1024);
			
			// assume read in 'hello world'
			while(sc.read(buf) != -1) {
				// byte array with some additional information
				// position == 11
				// length (limit) == 1024
				// capacity == 1024
				
				// want to give data to someone else to process
				// set position to 0 and limit to 11
				//buf.limit(buf.position());
				//buf.position(0);
				
				// equivalent to:
				// buf.limit(buf.position()).position(0);
				
				// equivalent to:
				buf.flip();
				for(int i = 0; i < buf.limit(); ++i) {
					buf.put(i, (byte) switchCase(buf.get(i)));
				}
				sc.write(buf);
				buf.clear();
			}
			logger.info("Channel closed : " + sc);
		} catch(IOException e) {
			logger.error(e.getMessage());
		}
	}		
	
}
