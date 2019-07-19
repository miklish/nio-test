package com.mikechristoff.niotest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NIOServerTest {

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(8080);
		boolean running = true;
		while(true) {
			Socket s = ss.accept();
			try (
				InputStream in = s.getInputStream();
				OutputStream out = s.getOutputStream()
				)
			{
				int data;
				while((data = in.read()) != -1) {
					if(data == '*') {
						running = false;
						break;
					}
					data = Util.switchCase(data);
					out.write(data);
				}
				if(!running) {
					ss.close();
					break;
				}
			}
		}
	}
}
