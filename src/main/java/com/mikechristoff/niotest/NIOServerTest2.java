package com.mikechristoff.niotest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NIOServerTest2 {
	private static Logger logger;
	
	public static void main(String[] args) throws IOException {
		
		/**
		 * Multi-threaded - Can now process many connections
		 *
		 * Problem: Limit to how many threads you can have per process.
		 *          This implementation is not managing its threads well
		 *
		 */
		final ServerSocket ss = new ServerSocket(8080);
		while(true) {

			final Socket s = ss.accept();
			logger.info("Connection from " + s);
			
			new Thread(() -> Util.process(s)).start();
		}
	}
	
	
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
}
