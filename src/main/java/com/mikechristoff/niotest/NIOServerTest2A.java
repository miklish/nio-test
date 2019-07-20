package com.mikechristoff.niotest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

public class NIOServerTest2A
{
	private static Logger logger;

	public static void main(String[] args) throws IOException {
		
		final ServerSocket ss = new ServerSocket(8080);
		
		/**
		 * Queue of jobs for threads
		 *
		 * Pool of threads - will create new threads as needed
		 *
		 * Queue for cached thread pool is synchronous (SynchronousQueue<Runnable>) - meaning that:
		 * -   if there is no available thread for a new job,
		 *     it will block until it has created a new thread to run the job
		 *
		 * Has useful diagrams:
		 *   https://www.callicoder.com/java-executor-service-and-thread-pool-tutorial/
		 */
		ExecutorService pool = Executors.newCachedThreadPool();
		
		while(true) {
			
			final Socket s = ss.accept();
			logger.info("Connection from " + s);
			
			pool.submit(() -> Util.process(s));
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
