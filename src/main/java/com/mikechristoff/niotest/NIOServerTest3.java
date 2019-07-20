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

	public static void main(String[] args) throws IOException {
		
		final ServerSocket ss = new ServerSocket(8080);
		
		
		/**
		 * - Creates new threads lazily
		 * - unbounded queue
		 * - jobs with no thread, wait in queue
		 *
		 * Creates a thread pool that reuses a fixed number of threads operating off a shared unbounded queue.
		 * At any point, at most nThreads threads will be active processing tasks. If additional tasks are submitted
		 * when all threads are active, they will wait in the queue until a thread is available. If any thread
		 * terminates due to a failure during execution prior to shutdown, a new one will take its place if needed to
		 * execute subsequent tasks. The threads in the pool will exist until it is explicitly shutdown.
		 */
		ExecutorService pool = Executors.newFixedThreadPool(1000);
		
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
