package com.mikechristoff.niotest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.SynchronousQueue;

import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NIOServerTest4 {
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
		
		logger.info("Connection 4");
		
		final ServerSocketChannel ssc = ServerSocketChannel.open();		
		ssc.bind(new InetSocketAddress("localhost", 8080));
		
		ExecutorService pool = Executors.newFixedThreadPool(100);
		
		while(true) {
			final SocketChannel s = ssc.accept();
			logger.info("Connection from " + s);
			
			pool.submit(() -> Util.process(s));
		}
	}
}
