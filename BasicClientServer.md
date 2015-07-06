# Introduction #

When starting out with JGN it can be daunting and sometimes difficult to know where to begin.
FlagRush is a very good starting point, but as an alternative I here go through the clientServer.vm test in the JGN source files.


# Details #

```
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
 * Created: Sep 14, 2006
 */
package com.captiveimagination.jgn.test.clientserver.vm;

/**
 * @author Matthew D. Hicks
 *
 */
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.captiveimagination.jgn.JGN;
import com.captiveimagination.jgn.clientserver.JGNConnectionListener;
import com.captiveimagination.jgn.clientserver.JGNConnection;
import com.captiveimagination.jgn.clientserver.JGNServer;

public class BasicServer extends Thread implements JGNConnectionListener {
	private JGNServer server;

	public BasicServer() {
		try {
			server = new JGNServer(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2000), new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2100));
			server.addClientConnectionListener(this);
			JGN.createThread(server).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (server.isAlive()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void connected(JGNConnection connection) {
		System.out.println(connection + " connected on server");
	}

	public void disconnected(JGNConnection connection) {
		System.out.println(connection + " disconnected on server");
	}

	public static void main(String[] args) {
		new BasicServer();
	}
}
```
To begin with we have the JGNServer, which works as a server in the client-server sense.
It has two MessageServers for relaying Messages - a reliable server and a fast server.
The reliable server is a TCPMessageServer and the fast one is a UDPMessageServer.
The server can use either of these if connected to send messages by different protocols, depending on your need for reliability or speed.

```
		try {
			server = new JGNServer(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2000), new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2100));
			server.addClientConnectionListener(this);
			JGN.createThread(server).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
```
Here a new JGNServer is instantiated. It takes two InetSocketAddresses (with InetAddress and port number to bind to), one for the reliable MessageServer and the other for the fast MessageServer.
`server.addClientConnectionListener(this);` adds a ClientConnectionListener to this server to listen for clients connecting to this server. In this case the Listener is implemented in the same class.
`JGN.createThread(server).start();` takes an Updatable object (in this case the server) and creates a Thread for it behind the scenes. With `start();` we start this thread (the server thread).

```
public void connected(JGNConnection connection) {
		System.out.println(connection + " connected on server");
	}

	public void disconnected(JGNConnection connection) {
		System.out.println(connection + " disconnected on server");
	}
```
Here are the methods implemented by the ClientConnectionListener interface. Pretty straight forward - the connected gets called when a client connects to this server and the disconnected gets called when a client disconnects from this server.

The rest of the code should be obvious for any java programmer.

---


Ok, so on to the client!

```
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
 * Created: Sep 14, 2006
 */
package com.captiveimagination.jgn.test.clientserver.vm;

import java.io.IOException;
import java.net.*;

import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.clientserver.*;

/**
 * @author Matthew D. Hicks
 */
public class BasicClient implements JGNConnectionListener {
	private JGNClient client;
	
	public void init(){
		try {
			client = new JGNClient(new InetSocketAddress(InetAddress.getLocalHost(), 0), new InetSocketAddress(InetAddress.getLocalHost(), 0));
			client.addServerConnectionListener(this);
			JGN.createThread(client).start();
			
			client.connectAndWait(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2000), new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2100), 5000);
			System.out.println("Connected!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException{
		System.out.println("Disconnecting!");
		client.close();
	}

	public void connected(JGNConnection connection) {
		System.out.println("logged in as Player "+connection.getPlayerId());
	}

	public void disconnected(JGNConnection connection) {
		System.out.println("logged off");
	}
	
	public static void main(String[] args) throws Exception {
		BasicClient client = new BasicClient();
		client.init();
		System.out.println("**** Sleeping");
		Thread.sleep(5000);
		client.close();
	}
}
```

The JGNClient acts as the client in the client-server relationship.

```
client = new JGNClient(new InetSocketAddress(InetAddress.getLocalHost(), 0), new InetSocketAddress(InetAddress.getLocalHost(), 0));
			client.addServerConnectionListener(this);
			JGN.createThread(client).start();
```
The JGNClient needs two InetSocketAddresses (you can give a null address if you don't want to use one of the protocols though), one for a TCPMessageServer and one for a UDPMessageServer, just like the JGNServer.
With `client.addServerConnectionListener(this);` we add a Listener for server connections, similarly to what we did for the server.
After that we create a new thread for the client to run in and start it with `JGN.createThread(client).start();`.

```
client.connectAndWait(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2000), new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 2100), 5000);
			System.out.println("Connected!");
```
Here we try to connect this client to the server. We supply the server address and port numbers for both TCP and UDP and also a wait time in milliseconds (5000 in this case) after which the client should give up trying to connect if it hasn't successfully done so by that time.
I've left out the try-catch here, but that's important too, of course.

```
public void close() throws IOException{
		System.out.println("Disconnecting!");
		client.close();
	}

	public void connected(JGNConnection connection) {
		System.out.println("logged in as Player "+connection.getPlayerId());
	}

	public void disconnected(JGNConnection connection) {
		System.out.println("logged off");
	}
```
With the `close()` method we close the client, which disconnects it from the server.
The other two methods are like the ones in the server above and are implemented by the interface ServerConnectionListener.

```
public static void main(String[] args) throws Exception {
		BasicClient client = new BasicClient();
		client.init();
		System.out.println("**** Sleeping");
		Thread.sleep(5000);
		client.close();
	}
```
Finally, in the main method we see that the BasicClient is instantiated and the init() method run, which connects the client to the server (see above). The thread then sleeps for 5 seconds and then closes the client, disconnecting it from the server. End of test.
I hope this very basic walkthrough was any help at all and that you get alot of enjoyment from JGN!