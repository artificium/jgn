/**
 * Copyright (c) 2005-2006 JavaGameNetworking
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'JavaGameNetworking' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created: Jul 22, 2006
 */
package com.captiveimagination.jgn.test.clientserver;

import java.net.*;

import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.clientserver.*;

/**
 * @author Matthew D. Hicks
 */
public class BasicClientServer {
	public static void main(String[] args) throws Exception {
		// Create Server
		SocketAddress serverReliableAddress = new InetSocketAddress(InetAddress.getLocalHost(), 1000);
		SocketAddress serverFastAddress = new InetSocketAddress(InetAddress.getLocalHost(), 2000);
		JGNServer server = new JGNServer(serverReliableAddress, serverFastAddress);
		server.addClientConnectionListener(new ClientConnectionListener() {
			public void connected(JGNConnection connection) {
				System.out.println("Client connected on server: " + connection.getPlayerId());
			}

			public void disconnected(JGNConnection connection) {
				System.out.println("Client disconnected on server: " + connection.getPlayerId());
			}
		});
		JGN.createThread(server).start();
		
		// Create Client1
		JGNClient client1 = new JGNClient(new InetSocketAddress(InetAddress.getLocalHost(), 1100), new InetSocketAddress(InetAddress.getLocalHost(), 2100));
		client1.addClientConnectionListener(new ClientConnectionListener() {
			public void connected(JGNConnection connection) {
				System.out.println("Client connected on client1: " + connection.getPlayerId());
			}

			public void disconnected(JGNConnection connection) {
				System.out.println("Client disconnected on client1: " + connection.getPlayerId());
			}
		});
		JGN.createThread(client1).start();
		client1.connectAndWait(serverReliableAddress, serverFastAddress, 15000);
		System.out.println("Connected!");
		client1.disconnect();
	}
}
