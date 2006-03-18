/*
 * Created on Jan 23, 2006
 */
package com.captiveimagination.jgn.register;

import java.io.*;
import java.net.*;
import java.util.*;

import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.register.listener.*;
import com.captiveimagination.jgn.register.message.*;

/**
 * @author Matthew D. Hicks
 */
public class ServerRegister extends Thread {
    public static long TIMEOUT = 2 * 60 * 1000; // Default timeout set to 2 minutes
    
    private static Server debugServer;
    
    private HashMap servers;
    private MessageServer server;
    
    public ServerRegister(MessageServer server) throws IOException {
    	registerMessages();
    	
        servers = new HashMap();
        if (debugServer != null) {
        	addServer(debugServer);
        }
        
        this.server = server;
        
        // The listener that registers/unregisters servers to the server list
        ServerListener sl = new ServerListener(this);
        server.addMessageListener(sl);
        
        // The listener that receives server list requests and fulfills them
        ClientListener cl = new ClientListener(this);
        server.addMessageListener(cl);
        
        System.out.println("Successfully started " + server.getClass().getName() + " register at " + server.getAddress().toString() + ":" + server.getPort() + "...");
    }
    
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch(InterruptedException exc) {
                exc.printStackTrace();
            }
            validateExpires();
            
            server.update();
        }
    }
    
    private void validateExpires() {
        Server[] temp = getServers();
        Long created;
        for (int i = 0; i < temp.length; i++) {
            created = (Long)servers.get(temp[i]);
            if (System.currentTimeMillis() > (created.longValue() + TIMEOUT)) {
            	if ((debugServer != null) && (temp[i] == debugServer)) continue;
                System.out.println("Server has expired, removing: " + temp[i].getServerName());
                servers.remove(temp[i]);
            }
        }
    }
    
    public synchronized boolean addServer(Server server) {
        boolean existed = removeServer(server.getAddress(), server.getPortUDP(), server.getPortTCP());
        servers.put(server, new Long(System.currentTimeMillis()));
        return existed;
    }
    
    public boolean removeServer(byte[] address, int portUDP, int portTCP) {
        Server server;
        Server[] temp = getServers();
        for (int i = 0; i < temp.length; i++) {
            server = (Server)temp[i];
            if ((server.getAddress()[0] == address[0]) &&
                (server.getAddress()[1] == address[1]) &&
                (server.getAddress()[2] == address[2]) &&
                (server.getAddress()[3] == address[3]) &&
                (server.getPortUDP() == portUDP) &&
                (server.getPortTCP() == portTCP)) {
                    servers.remove(server);
                    return true;
            }
        }
        return false;
    }
    
    public Server[] getServers() {
        return (Server[])servers.keySet().toArray(new Server[servers.size()]);
    }
    
    public MessageServer getMessageServer() {
        return server;
    }
    
    public static void registerMessages() {
    	JGN.registerMessage(RegisterServerMessage.class, (short)-1);
        JGN.registerMessage(RequestServersMessage.class, (short)-2);
        JGN.registerMessage(ServerStatusMessage.class, (short)-3);
        JGN.registerMessage(UnregisterServerMessage.class, (short)-4);
    }
    
    public static void main(String[] args) throws Exception  {
    	MessageServer server = null;
    	
    	for (int i = 0; i < args.length; i++) {
    		if (args[i].equalsIgnoreCase("tcp")) {
    			server = new TCPMessageServer(new InetSocketAddress((InetAddress)null, 9801));
    		} else if (args[i].equalsIgnoreCase("udp")) {
    			server = new UDPMessageServer(new InetSocketAddress((InetAddress)null, 9801));
    		} else if (args[i].equalsIgnoreCase("debug")) {
    			InetAddress address = InetAddress.getByName("captiveimagination.com");
    			debugServer = new Server("DebugServer", address.getHostName(), address.getAddress(), 9901, 9902, "DebugGame", "DebugMap", "DebugInfo", 2, 16);
    		}
    	}
    	if (server == null) {
    		server = new UDPMessageServer(new InetSocketAddress((InetAddress)null, 9801));
    		
    	}
        ServerRegister register = new ServerRegister(server);
        register.start();
    }
}
