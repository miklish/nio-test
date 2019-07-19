package com.mikechristoff.niotest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.SynchronousQueue;

import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NIOServerTest3 {
	private static Logger logger;
	
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

	public static void main(String[] args) throws IOException {
		
		final ServerSocket ss = new ServerSocket(8080);		
		
//1		ExecutorService pool = Executors.newCachedThreadPool().;
		
//2		ExecutorService pool = new java.util.concurrent.ThreadPoolExecutor(
//				0,
//				//Integer.MAX_VALUE,	// max threads
//				1,					// max threads
//				60L,					// stick around for x TimeUnits before reused
//				java.util.concurrent.TimeUnit.SECONDS,
//				new java.util.concurrent.SynchronousQueue<Runnable>(),
//				new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

		ExecutorService pool = Executors.newFixedThreadPool(1);
		
		while(true) {
			final Socket s = ss.accept();
			logger.info("Connection from " + s);
			
			/*
			if(!Util.process(ss, s)) {
				ss.close();
				break;
			}
			*/
			
			pool.submit(() -> Util.process(s));
		}
	}
}
