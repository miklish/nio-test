package com.mikechristoff.niotest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NIOServerTest4 {
	private static Logger logger;


	public static void main(String[] args) throws IOException {
		
		logger.info("Connection 4");
		
		final ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.bind(new InetSocketAddress("localhost", 8080));
		
		ExecutorService pool = Executors.newFixedThreadPool(100);
		
		/**
		 * This is now using NIO ByteBuffers, but it is still blocking
		 * This means it behaves like the previous solution
		 */
		while(true) {
			final SocketChannel s = ssc.accept();		// blocking call -- blocks until new connection comes in
			logger.info("Connection from " + s);
			
			pool.submit(() -> {
				
				/**
				 * Notice now that we are reading and writing to/fro ByteBuffer, and not to IOStreams
				 */
				try {
					// position == 0
					// limit == capacity == 1024
					ByteBuffer buf = ByteBuffer.allocateDirect(1024);
					
					// assume read in 'hello world'
					while(s.read(buf) != -1) {
						
						// Write mode (before buf.flip() or after buf.clear())

						// byte array with some additional information
						// position == 11
						// length (limit) == 1024
						// capacity == 1024
						
						//    h   e         d
						//  [ 0 | 1 | ... | 10 | 11 | ... | 1023 ] (1024)
						//                       ^                  ^
						//                       position           capacity
						//                                          limit
						
						// ------------
						
						// Read mode (after buf.flip())
						
						// want to give data to someone else to process
						// set position to 0 and limit to 11
						// buf.limit(buf.position());
						// buf.position(0);
						
						// equivalent to:
						// buf.limit(buf.position()).position(0);
						
						//    h   e         d
						//  [ 0 | 1 | ... | 10 | 11 | ... | 1023 ] (1024)
						//    ^                  ^                  ^
						//    position           limit              capacity
						
						// equivalent to:
						buf.flip();
						//for(int i = 0; i < buf.limit(); ++i) {
						for(int i = buf.position(); i < buf.limit(); ++i) {
							buf.put(i, (byte) Util.switchCase(buf.get(i)));
						}
						s.write(buf);
						buf.clear();
					}
					logger.info("Channel closed : " + s);
				} catch(IOException e) {
					logger.error(e.getMessage());
				}
			});
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
