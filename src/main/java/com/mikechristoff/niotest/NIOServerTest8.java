package com.mikechristoff.niotest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
//import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.logging.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Selector Server
public class NIOServerTest8 {
	private static Logger logger;
	
	static {
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream("src//main//resources//logger.properties"));
		} catch(FileNotFoundException e) {
			System.out.println("Logger File Not Found : " + e.getMessage());
			System.exit(-1);			
		} catch(IOException e) {
			System.out.println("Logger IO Error : " + e.getMessage());
			System.exit(-2);			
		}		
		logger = LoggerFactory.getLogger(NIOServerTest8.class);	
	}	

	private static Map<SocketChannel, java.util.Queue<ByteBuffer>>
		pendingData = new HashMap<>();
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		logger.info("Server 8");
		
		// create a ServerSocketChannel to accept incoming connections
		ServerSocketChannel ssc = ServerSocketChannel.open();	
		ssc.bind(new InetSocketAddress("localhost", 8080));
		
		// The Channel must be in non-blocking mode to be used with a Selector.
		// This means that you cannot use FileChannel's with a Selector since
		// FileChannel's cannot be switched into non-blocking mode. Socket channels
		// will work fine though. 		
		ssc.configureBlocking(false);
		
		// You create a Selector by calling the Selector.open() method, like this: 
		Selector selector = Selector.open();
		
		// In order to use a Channel with a Selector you must register the Channel
		// with the Selector. This is done using the SelectableChannel.register()
		// method, like this: 
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		
		/*		
        Notice the second parameter of the register() method. This is an "interest
        set", meaning what events you are interested in listening for in the Channel,
        via the Selector. There are four different events you can listen for:
         
			1. Connect
			2. Accept
			3. Read
			4. Write
			
		A channel that "fires an event" is also said to be "ready" for that event.
		So, a channel that has connected successfully to another server is "connect
		ready". A server socket channel which accepts an incoming connection is
		"accept" ready. A channel that has data ready to be read is "read" ready. A
		channel that is ready for you to write data to it, is "write" ready.
		 
		These four events are represented by the four SelectionKey constants:
		 
			1. SelectionKey.OP_CONNECT
			2. SelectionKey.OP_ACCEPT
			3. SelectionKey.OP_READ
			4. SelectionKey.OP_WRITE
			
		If you are interested in more than one event, OR the constants together,
		like this:
		 
			int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
			
		 */	
		
		// THE ACCEPT LOOP
		// Here we look for new connections
		while(true) {
			
			/*
			Once you have register one or more channels with a Selector you can call
			one of the select() methods. These methods return the channels that are
			"ready" for the events you are interested in (connect, accept, read or
			write). In other words, if you are interested in channels that are ready
			for reading, you will receive the channels that are ready for reading
			from the select() methods. 
			
			Here are the select() methods:
				
				int select()
				int select(long timeout)
				int selectNow()
				
				* select() blocks until at least one channel is ready for the events
				  you registered for.
				   
				* select(long timeout) does the same as select() except it blocks for
				  a maximum of timeout milliseconds (the parameter).
				   
				* selectNow() doesn't block at all. It returns immediately with
				  whatever channels are ready. 
					
			The int returned by the select() methods tells how many channels are
			ready. That is, how many channels that became ready since last time you
			called select(). If you call select() and it returns 1 because one
			channel has become ready, and you call select() one more time, and one
			more channel has become ready, it will return 1 again. If you have done
			nothing with the first channel that was ready, you now have 2 ready
			channels, but only one channel had become ready between each select()
			call. 			
			
			*/
			
			// select() blocks until at least one channel is ready for the events
			// you registered for
			selector.select();	
			

			/*
			selectedKeys()
			Once you have called one of the select() methods and its return value
			has indicated that one or more channels are ready, you can access the
			ready channels via the "selected key set", by calling the selectors
			selectedKeys() method. Here is how that looks: 
			
				Set<SelectionKey> selectedKeys = selector.selectedKeys();  
				  
			When you register a channel with a Selector the Channel.register() method
			returns a SelectionKey object. This key represents that channels
			registration with that selector. It is these keys you can access via the
			selectedKeySet() method. From the SelectionKey.
			 
			You can iterate this selected key set to access the ready channels. Here
			is how that looks: 
			
				Set<SelectionKey> selectedKeys = selector.selectedKeys();			
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();			
				while(keyIterator.hasNext()) {			    
				    SelectionKey key = keyIterator.next();			
				    if(key.isAcceptable()) {
				        // a connection was accepted by a ServerSocketChannel.			
				    } else if (key.isConnectable()) {
				        // a connection was established with a remote server.			
				    } else if (key.isReadable()) {
				        // a channel is ready for reading			
				    } else if (key.isWritable()) {
				        // a channel is ready for writing
				    }			
				    keyIterator.remove();
				}
			
			This loop iterates the keys in the selected key set. For each key it
			tests the key to determine what the channel referenced by the key is
			ready for. 
			
			Notice the keyIterator.remove() call at the end of each iteration. The
			Selector does not remove the SelectionKey instances from the selected key
			set itself. You have to do this, when you are done processing the
			channel. The next time the channel becomes "ready" the Selector will add
			it to the selected key set again.
			 
			The channel returned by the SelectionKey.channel() method should be cast
			to the channel you need to work with, e.g a ServerSocketChannel or
			SocketChannel etc. 			
			 */
			
			Iterator<SelectionKey> itKeys = selector.selectedKeys().iterator();
			while (itKeys.hasNext()) {				
				SelectionKey key = itKeys.next();
				itKeys.remove();
				
				// check whether key is useful (socket still open etc...)
				if(key.isValid())
				{
					if(key.isAcceptable())	// someone connected to SS channel
						accept(key);
					else if(key.isReadable())
						read(key);
					else if(key.isWritable())
						write(key);
				}				
			}
		}
	}
	
	private static void accept(SelectionKey key) throws IOException {
		/*
		The channel returned by the SelectionKey.channel() method should be cast to
		the channel you need to work with, e.g a ServerSocketChannel or SocketChannel
		etc.
		*/ 		
		
		// SelectionKey.channel() returns the Channel that it was registered with
		// In this case, it will be the original ServerSocketChannel
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		
		// We use the original SSC to NON-BLOCKINGLY accept the new Socket (Channel)
		SocketChannel sc = ssc.accept(); // nonblocking -- never null!
		
		logger.debug("accept called : " + sc);
		
		// We similarly set the SocketChannel to be non-blocking
		sc.configureBlocking(false);
		
		// now that we have the new SocketChannel, we
		// * REGISTER IT WITH THE SAME SELECTOR THAT THE SERVERSOCKETCHANNEL 
		// is using.
		// We register it for READs Only at this point
		// Q: Why not reg it for reads AND writes
		sc.register(key.selector(), SelectionKey.OP_READ);
		//sc.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		
		// recall: LinkedList implements Queue
		pendingData.put(sc, new LinkedList<>());
	}	
	
	// Here we READ the input and WRITE our response
	private static void read(SelectionKey key) throws IOException {
		// socket def. has something to say (not 0). But may be -1
		
		SocketChannel sc = (SocketChannel) key.channel();
		
		logger.info("[ READ ] called : " + sc.getLocalAddress() + " -----------");
		
		ByteBuffer readBuf = ByteBuffer.allocateDirect(3);
		
		// write buffer needs to be at least 3x read buffer in order
		// to fully process READ data
		ByteBuffer writeBuf = ByteBuffer.allocateDirect(9);
		
		// READ data fom channel into buf
		// CHANNEL--read--> --write-->BUF
		int read = sc.read(readBuf);
		if(read == -1) {
			sc.close();
			pendingData.remove(sc);
			return;
		}

		// PREPARE FOR READING from BUF into CHANNEL
		
		// set BUF into READ mode
		// e.g.: sets the limit to 'max read position' and set position to 0
		readBuf.flip();
		
		// BUF--read--> <PROCESS-DATA> --write-->BUF
		/*
		// This version uses manual puts and gets, which means we need to
		// manually keep track of our position in each buffer
		// It also means we can't use flip(), since ABSOLUTE puts and gets
		// do NOT update position()
		int w = 0;
		int r = 0;
		while(r < readBuf.limit() && w < writeBuf.limit()) {
			
			byte b = readBuf.get(r++);			
			if(Character.isLetterOrDigit((char) b) && w+2 < writeBuf.limit()) {
				
				writeBuf.put(w++, (byte) '<');
				writeBuf.put(w++, (byte) Util.switchCase(b));
				writeBuf.put(w++, (byte) '>');				
			}
			else
				writeBuf.put(w++, b);
		}

		// Since we used ABSOLUTE puts and gets, we need to manually
		// flip the write buffer
		writeBuf.position(0);
		writeBuf.limit(w);
		*/

		// This uses RELATIVE puts and RELATIVE gets
		// These automatically update position()
		// This allows us to use flip()
		
		// NOTE: readBuf.hasRemaining ==equivalent-to== readBuf.position() < readBuf.limit()
		//       We can use hasRemaining() because we are using RELATIVE puts and gets
		while(readBuf.hasRemaining() && writeBuf.hasRemaining()) {
			
			byte b = readBuf.get();			
			//if(Character.isLetterOrDigit((char) b) && writeBuf.position()+2 < writeBuf.limit()) {
			
			// NOTE: We actually don't have to check whether writeBuf.position()+2 < writeBuf.limit(),
			//       since our writeBuf size is 3x larger than the readBuf
			//		 This ensures that there is no readBuf that can ever require us to need more
			//       writeBuf space than we have.
			if(Character.isLetterOrDigit((char) b)) {
				
				writeBuf.put((byte) '<');
				writeBuf.put((byte) Util.switchCase(b));
				writeBuf.put((byte) '>');				
			}
			else
				writeBuf.put(b);
		}
		writeBuf.flip();
		

		// note: We only want to write once socket is ready for writing
		// therefore instead make a queue of data to written onto
		// this socket:
		
		// Save processed data until selector says channel is ready for WRITING
		pendingData.get(sc).add(writeBuf);
		
		// Let selector know we want to know when socket is ready to 
		// have pending data written
		
		 sc.register(key.selector(), SelectionKey.OP_WRITE);
		//sc.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		
		// NOTE: For the SocketChannel sc, multiple calls to 
		//       IOServerTest6.read() may have been before it is ready to be
		//       written to. Therefore, this is why we use an ordered Collection
		//       (like a FIFO Queue or LinkedList) to store pending writes, so
		//       that they are written back to SocketChannel in the correct order.		
	}	
	
	private static void write(SelectionKey key) throws IOException {
		// get the SocketChannel
		SocketChannel sc = (SocketChannel) key.channel();
		
		logger.info("write called : " + sc);
		
		// get the order list of pending data buffers
		// NOTE: Last time we used these buffers, we WROTE to them
		//       BUT: I assume the value of limit() and position are still the
		//            the same as they were right after buf.flip(); E.g.:
		//            They are still in the equivalent of READ mode
		//            SINCE our processing just changed values in-place and
		//            didn't change the size of the data.
		Queue<ByteBuffer> queue = pendingData.get(sc);
		
		ByteBuffer buf = null;
		while((buf = queue.peek()) != null) {
			// read contents of current buffer in FIFO Queue,
			// but do NOT remove it from HEAD
			buf = queue.peek();
			
			// write the contents of buf to SocketChannel
			// buf--read--> --write-->CHANNEL
			// KEY POINT! : This write may NOT be able to write the WHOLE buffer
			//   into the CHANNEL!! E.g.: After sc.write(buf), buf.position() may
			//   be less than buf.limit()
			//   This is why we only POLL the head of the Queue until
			//   we're SURE the whole buffer has been written.
			//   If the whole buffer has NOT been written, then we leave
			//   the buffer in the pendingData Queue until the SocketChannel's
			//   output buffer is emptied enough so there is space for the next
			//   WRITE. 
			//   Since SocketChannel.write(buf) updates buf's position(),
			//   we can be assured that the next time NIOServerTest6.write()
			//   is called, we will ONLY write the remaining data in the buf
			//   (since sc.write() starts writing from buf.position()).
			sc.write(buf);
			
			// if buf still has data left, leave it in the Queue at the HEAD
			// until the SocketChannel is available for additional WRITEs.
			if(buf.hasRemaining())
				return;			// leave buf in pendingData Queue
			else
				queue.poll();	// remove buf from HEAD of Queue
								// poll() is identical to remove(), except that it 
								// returns null if called on an empty Queue, rather
								// than throwing an Exception
			
			sc.register(key.selector(), SelectionKey.OP_READ);
			//sc.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
	}	
}

