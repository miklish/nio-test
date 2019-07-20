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


	private static Collection<SocketChannel> _sockets =
			Collections.newSetFromMap(
					new HashMap<SocketChannel, Boolean>());
	
	
	public static void main(String[] args) throws IOException
    {
        
        logger.info("Connection 5");
        
        /**
         * The accept() method is now non-blocking
         * BUT: - The server now spins on ssc.accept() which increases CPU load
         *      - Takes a long time for new clients to connect (as # of client sockets increases),
         *        since every time the server checks for a new connection, it ALSO iterates through
         *        every socketchannel in the _sockets Set
         *
         * ALSO: Socket is non-blocking as well!
         */
        
        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("localhost", 8080));
        
        /**
         * Server socket is non-blocking
         * - this means ssc.accept() is non-blocking
         */
        ssc.configureBlocking(false);
        
        
        while (true) {
            /** now non-blocking. Returns null if no connection -- *usually* null! */
            final SocketChannel newSocketChannel = ssc.accept();
            
            if (newSocketChannel != null) {
                logger.info("Connection from " + newSocketChannel);
                
                /**
                 * Client connection socket is ALSO non-blocking
                 * - This means that if there is no new data to read, the client socket will also return null
                 */
                newSocketChannel.configureBlocking(false);
                
                /** Add new client socket to a new Collection of sockets */
                _sockets.add(newSocketChannel);
            }
            
            for (Iterator<SocketChannel> it = _sockets.iterator(); it.hasNext(); ) {
                
                SocketChannel iteratedSocketChannel = it.next();
                try {
                    /** Hugely inefficient to allocate ByteBuffer to read each new chunk of data from client socket.
                     *  However this is just to simpligy code here.
                     */
                    ByteBuffer buf = ByteBuffer.allocateDirect(1024);
                    
                    
                    /**
                     * Reads a sequence of bytes from this channel into the given buffer.
                     * An attempt is made to read up to r bytes from the channel, where r is the number of bytes
                     * remaining in the buffer, that is, dst.remaining(), at the moment this method is invoked.
                     * Suppose that a byte sequence of length n is read, where 0 <= n <= r. This byte sequence will be
                     * transferred into the buffer so that the first byte in the sequence is at index p and the last
                     * byte is at index p + n - 1, where p is the buffer's position at the moment this method is
                     * invoked. Upon return, the buffer's position will be equal to p + n; its limit will not have
                     * changed.
                     *
                     * A read operation might not fill the buffer, and in fact it might not read any bytes at all.
                     * Whether or not it does so depends upon the nature and state of the channel. A socket channel in
                     * non-blocking mode, for example, cannot read any more bytes than are immediately available from
                     * the socket's input buffer; similarly, a file channel cannot read any more bytes than remain in
                     * the file. It is guaranteed, however, that if a channel is in blocking mode and there is at least
                     * one byte remaining in the buffer then this method will block until at least one byte is read.
                     * This method may be invoked at any time. If another thread has already initiated a read operation
                     * upon this channel, however, then an invocation of this method will block until the first
                     * operation is complete.
                     *
                     * Returns: The number of bytes read, possibly zero, or -1 if the channel has reached end-of-stream
                     */
                    int read = iteratedSocketChannel.read(buf);
                    if (read == -1) {
                        it.remove();    // remove this socket from our Set if it has been closed
                        continue;
                    } else if (read == 0)
                        continue;
                    
                    
                    buf.flip();
                    for (int i = buf.position(); i < buf.limit(); ++i) {
                        buf.put(i, (byte) Util.switchCase(buf.get(i)));
                    }
                    iteratedSocketChannel.write(buf);
                    
                    // get rid of this since we make a new buf every time
                    // -- WASTEFUL -- Fix later
                    //buf.clear();
                    
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    it.remove();
                }
            }
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
