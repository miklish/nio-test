package com.mikechristoff.niotest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

public class NIOServerTest2C
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
		 * There a different policies that can be set when threads run out
		 *
		 *   https://letslearnjavaj2ee.blogspot.com/2013/08/threadpoolexecutor-handler-policies-for.html
		 *   https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html
		 *
		 * The default policy is AbortPolicy
		 *
		 * AbortPolicy :         the handler throws a runtime RejectedExecutionException upon rejection.
		 * CallerRunsPolicy:     the thread that invokes execute itself runs the task. This provides a simple feedback
		 *                       control mechanism that will slow down the rate that new tasks are submitted.
		 * DiscardPolicy :       a task that cannot be executed is simply dropped.
		 * DiscardOldestPolicy : if the executor is not shut down, the task at the head of the work queue is dropped,
		 *                       and then execution is retried (which can fail again, causing this to be repeated.)
		 *
		 */
		ExecutorService pool = new ThreadPoolExecutor(
			0,						// core pool size
			Integer.MAX_VALUE,					// max pool size
			60L,					// time for thread to live without a job
			TimeUnit.SECONDS,					// - (time unit)
			new SynchronousQueue<Runnable>(),
			new ThreadPoolExecutor.CallerRunsPolicy()	// This has the connection acceptor thread run the task
			                                            // This prevents new connections from being accepted until
			                                            // thread pool has enough free threads for new jobs
		);

		
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
