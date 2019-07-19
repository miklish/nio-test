package com.mikechristoff.niotest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.SynchronousQueue;

import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NIOServerTest5 {
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

	private static Collection<SocketChannel> _sockets =
			Collections.newSetFromMap(
					new HashMap<SocketChannel, Boolean>());
	public static void main(String[] args) throws IOException {
		
		logger.info("Connection 5");
		
		final ServerSocketChannel ssc = ServerSocketChannel.open();		
		ssc.bind(new InetSocketAddress("localhost", 8080));
		
		ssc.configureBlocking(false);		
		
		while(true) {
			// now non-blocking. Returns null if no connection -- usually null!
			final SocketChannel sc = ssc.accept();
			
			if(sc != null) {
				logger.info("Connection from " + sc);
				sc.configureBlocking(false);
				_sockets.add(sc);
			}	
			
			for (Iterator<SocketChannel> it = _sockets.iterator(); it.hasNext();) {
				
				SocketChannel socket = it.next();
				try {
					ByteBuffer buf = ByteBuffer.allocateDirect(1024);
					
					int read = sc.read(buf);
					if(read == -1) {
						
					}
					
					while(socket.read(buf) != -1) {

						buf.flip();
						for(int i = 0; i < buf.limit(); ++i) {
							buf.put(i, (byte) Util.switchCase(buf.get(i)));
						}
						socket.write(buf);
						
						// get rid of this since we make a new buf every time
						// -- WASTEFUL -- Fix later
						//buf.clear();
					}
				} catch(IOException e) {
					logger.error(e.getMessage());
					it.remove();
				}								
				
			}				
		}
		
		/*
		while(true) {
			// now non-blocking. Returns null if no connection -- usually null!
			final SocketChannel sc = ssc.accept();
			
			if(sc != null) {
				logger.info("Connection from " + s);
				s.configureBlocking(false);
				_sockets.add(s);
			}
			
			for (Iterator<SocketChannel> it = _sockets.iterator()) {
				
				try {
					// position == 0
					//limit == capacity == 1024
					ByteBuffer buf = ByteBuffer.allocateDirect(1024);
					
					// assume read in 'hello world'
					int read = sc.read(buf);
					if(read == -1) {
						
					}
					
					while(sc.read(buf) != -1) {

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
			
			//pool.submit(() -> Util.process(s));
		}
		*/
	}
}
