package com.captiveimagination.jgn.server;

import java.io.*;
import java.util.*;

import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.event.*;
import com.captiveimagination.jgn.message.*;
import com.captiveimagination.jgn.message.player.*;

/**
 * NetworkingServer is a convenience class for servers handling
 * clients in a client/server architecture.
 * 
 * @author Matthew D. Hicks
 */
public class NetworkingServer implements Runnable {
	private UDPMessageServer messageServerUDP;
    private TCPMessageServer messageServerTCP;
	private ServerSession serverSession;
	private ServerPlayerMessageListener playerMessageListener;
    private ArrayList playerListeners;
	
	private boolean keepAlive;
	
	public NetworkingServer(int udpPort, int tcpPort) throws IOException {
		UDPMessageServer serverUDP = null;
		TCPMessageServer serverTCP = null;
		if (udpPort > -1) {
			serverUDP = new UDPMessageServer(null, udpPort);
		}
		if (tcpPort > -1) {
			serverTCP = new TCPMessageServer(null, tcpPort);
		}
		init(serverUDP, serverTCP, null);
	}
	
    public NetworkingServer(UDPMessageServer messageServer) {
        this(messageServer, null);
    }
    
    public NetworkingServer(TCPMessageServer messageServer) {
        this(null, messageServer);
    }
    
	public NetworkingServer(UDPMessageServer messageServerUDP, TCPMessageServer messageServerTCP) {
		this(messageServerUDP, messageServerTCP, null);
	}
	
	public NetworkingServer(UDPMessageServer messageServerUDP, TCPMessageServer messageServerTCP, ServerSession serverSession) {
		init(messageServerUDP, messageServerTCP, serverSession);
	}
	
	private void init(UDPMessageServer messageServerUDP, TCPMessageServer messageServerTCP, ServerSession serverSession) {
		if (serverSession == null) serverSession = new DefaultServerSession();
		
		this.messageServerUDP = messageServerUDP;
        this.messageServerTCP = messageServerTCP;
		this.serverSession = serverSession;

		// Register Necessary Player Messages
		JGN.registerMessage(PlayerJoinRequestMessage.class, (short)(Short.MIN_VALUE + 1));
		JGN.registerMessage(PlayerJoinResponseMessage.class, (short)(Short.MIN_VALUE + 2));
		JGN.registerMessage(PlayerDisconnectMessage.class, (short)(Short.MIN_VALUE + 3));
		JGN.registerMessage(ServerDisconnectMessage.class, (short)(Short.MIN_VALUE + 4));
		JGN.registerMessage(PlayerNoopMessage.class, (short)(Short.MIN_VALUE + 5));
		
		// Add Message Listeners
		playerMessageListener = new ServerPlayerMessageListener(this);
        if (messageServerTCP != null) {
            messageServerTCP.addMessageListener(playerMessageListener);
        }
        if (messageServerUDP != null) {
            messageServerUDP.addMessageListener(playerMessageListener);
        }
        playerListeners = new ArrayList();
	}
	
	/**
	 * Called when a join request is received.
	 * 
	 * @param message
	 * @throws IOException 
	 */
	public void joinRequest(PlayerJoinRequestMessage message) throws IOException {
		PlayerJoinResponseMessage response = serverSession.receivedJoinRequest(message);
        try {
            getPlayerMessageServer().sendMessage(response, message.getRemoteAddress(), message.getRemotePort());
        } catch(IOException exc) {
            PlayerDisconnectMessage pdm = new PlayerDisconnectMessage();
            pdm.setPlayerId(response.getPlayerId());
            disconnectRequest(pdm);
        }
        if (response.isAccepted()) {
            // Send message to current clients about new client
            PlayerJoinRequestMessage request = serverSession.createClientJoinRequest(response.getPlayerId());
            sendToAllClientsExcept(request, response.getPlayerId());
            
            // Send messages to new client about currently connected players
            Player[] players = serverSession.getPlayers();
            for (int i = 0; i < players.length; i++) {
                if (players[i].getPlayerId() != response.getPlayerId()) {
                    request = serverSession.createClientJoinRequest(players[i].getPlayerId());
                    sendToClient(request, response.getPlayerId());
                }
            }
            
            // Call all listeners
            message.setPlayerId(response.getPlayerId());
            PlayerListener listener;
            for (int i = 0; i < playerListeners.size(); i++) {
                listener = (PlayerListener)playerListeners.get(i);
                listener.createRemotePlayer(message);
            }
        }
	}
	
	/**
	 * Call this method to handle management of server in a single threaded
	 * environment. You can new Thread(networkingServer).start() to run threaded.
	 */
	public void update() {
		updateIncoming();
		
		updateEvents();
		
		// Check for expired players
		updateExpired();
	}
	
	public void updateIncoming() {
		if (messageServerTCP != null) {
			messageServerTCP.updateIncoming();
		}
        if (messageServerUDP != null) {
        	messageServerUDP.updateIncoming();
        }
	}
	
	public void updateEvents() {
		if (messageServerTCP != null) {
			messageServerTCP.updateEvents();
		}
        if (messageServerUDP != null) {
        	messageServerUDP.updateEvents();
        }
	}
	
	public void updateExpired() {
		Player[] players = getPlayers();
		for (int i = 0; i < players.length; i++) {
			if (players[i].getLastHeardFrom() + serverSession.getPlayerTimeout() < System.currentTimeMillis()) {
				PlayerDisconnectMessage message = new PlayerDisconnectMessage();
				message.setPlayerId(players[i].getPlayerId());
				message.setRemoteAddress(players[i].getAddress());
				message.setRemotePort(players[i].getPlayerPort());
				disconnectRequest(message);
			}
		}
	}
	
	public void run() {
		keepAlive = true;
		while (keepAlive) {
			try {
				Thread.sleep(1);
			} catch(InterruptedException exc) {
			}
			update();
		}
	}
	
	/**
	 * Called when a disconnect request is received.
	 * 
	 * @param message
	 */
	public void disconnectRequest(PlayerDisconnectMessage message) {
        removePlayer(message.getPlayerId(), message);
	}
    
    /**
     * Should be called when a player needs to be removed.
     * 
     * @param playerId
     * @param message - this is the disconnect message received. This is optionally
     *                  null and will be generated by the server.
     */
    public void removePlayer(short playerId, PlayerDisconnectMessage message) {
        if (message == null) {
            message = new PlayerDisconnectMessage();
            message.setPlayerId(playerId);
        }
        
        // Send message to all clients
        sendToAllClientsExcept(message, message.getPlayerId());
        
        PlayerListener listener;
        for (int i = 0; i < playerListeners.size(); i++) {
            listener = (PlayerListener)playerListeners.get(i);
            listener.removeRemotePlayer(message);
        }
        if (messageServerTCP != null) {
            messageServerTCP.disconnect(message.getRemoteAddress(), message.getRemotePort());
        }
        serverSession.expirePlayer(message.getPlayerId());
    }
	
	/**
	 * Changes the ServerSession implementation being used by this
	 * instance of NetworkingServer. DefaultServerSession is used if
	 * none is defined.
	 * 
	 * @param serverSession
	 */
	public void setServerSession(ServerSession serverSession) {
		this.serverSession = serverSession;
	}
	
    /**
     * @return
     *      The ServerSession associated with this NetworkingServer.
     */
    public ServerSession getServerSession() {
        return serverSession;
    }
    
	/**
	 * @param playerId
	 * @return
	 * 		returns the player corresponding to <code>playerId</code>
	 */
	public Player getPlayer(short playerId) {
		return serverSession.getPlayer(playerId);
	}
	
	/**
	 * @return
	 * 		Player[] of all the players currently connected to the
	 * 		server.
	 */
	public Player[] getPlayers() {
		return serverSession.getPlayers();
	}
    
    /**
     * @return
     *      The local TCPMessageServer being used to send
     *      and receive messages or null if one is not set.
     */
    public TCPMessageServer getTCPMessageServer() {
        return messageServerTCP;
    }
    
    /**
     * @return
     *      The local UDPMessageServer being used to send
     *      and receive messages or null if one is not set.
     */
    public UDPMessageServer getUDPMessageServer() {
        return messageServerUDP;
    }
    
    /**
     * @return
     *      The local MessageServer being used to send
     *      and receive player messages. This favors
     *      TCPMessageServer if it is set.
     */
    public MessageServer getPlayerMessageServer() {
        if (messageServerTCP != null) {
            return messageServerTCP;
        }
        return messageServerUDP;
    }
    
    /**
     * Send to client using the player message server.
     * 
     * @param message
     * @param playerId
     * @throws IOException 
     */
    public void sendToClient(Message message, short playerId) throws IOException {
        sendToClient(message, playerId, getPlayerMessageServer());
    }
    
    /**
     * Send to client using the TCPMessageServer.
     * 
     * @param message
     * @param playerId
     * @throws IOException 
     */
    public void sendToClientTCP(Message message, short playerId) throws IOException {
        sendToClient(message, playerId, getTCPMessageServer());
    }
    
    /**
     * Send to client using the UDPMessageServer.
     * 
     * @param message
     * @param playerId
     * @throws IOException 
     */
    public void sendToClientUDP(Message message, short playerId) throws IOException {
        sendToClient(message, playerId, getUDPMessageServer());
    }
    
    public void sendToClient(Message message, short playerId, MessageServer server) throws IOException {
        Player player = getPlayer(playerId);
        if ((server instanceof TCPMessageServer) && (player.getTCPPort() == -1)) {
            if (getUDPMessageServer() != null) {
                server = getUDPMessageServer();
            } else {
                System.err.println("Unable to send to client TCP " + playerId + " as there is no compatible server.");
            }
        }
        if ((server instanceof UDPMessageServer) && (player.getUDPPort() == -1)) {
            if (getTCPMessageServer() != null) {
                server = getTCPMessageServer();
            } else {
                System.err.println("Unable to send to client UDP " + playerId + " as there is no compatible server.");
            }
        }
        
        int port;
        if (server instanceof TCPMessageServer) {
            port = player.getTCPPort();
        } else {
            port = player.getUDPPort();
        }
        server.sendMessage(message, player.getAddress(), port);
    }
    
    public void sendToAllClients(Message message) {
        sendToAllClients(message, getPlayerMessageServer());
    }
    
    public void sendToAllClientsTCP(Message message) {
        sendToAllClients(message, getTCPMessageServer());
    }
    
    public void sendToAllClientsUDP(Message message) {
        sendToAllClients(message, getUDPMessageServer());
    }
    
	/**
	 * Sends <code>message</code> to all connected clients.
	 * 
	 * @param message
	 */
	public void sendToAllClients(Message message, MessageServer messageServer) {
		Player[] players = getPlayers();
        int port;
		for (int i = 0; i < players.length; i++) {
            if (messageServer instanceof TCPMessageServer) {
                port = players[i].getTCPPort();
            } else {
                port = players[i].getUDPPort();
            }
            //System.out.println("S> sendToAllClients: " + messageServer.getClass().getName() + ", " + port + ", " + message.getClass().getName() + ", " + players[i].getAddress());
            try {
                messageServer.sendMessage(message, players[i].getAddress(), port);
            } catch(IOException exc) {
                System.err.println("Unable to send " + messageServer.getClass().getName() + " message to " + players[i].getAddress() + ":" + port);
                exc.printStackTrace();
                removePlayer(players[i].getPlayerId(), null);
            }
		}
	}
	
    public void sendToAllClientsExcept(Message message, short playerId) {
        sendToAllClientsExcept(message, playerId, getPlayerMessageServer());
    }
    
    public void sendToAllClientsExceptTCP(Message message, short playerId) {
        sendToAllClientsExcept(message, playerId, getTCPMessageServer());
    }
    
    public void sendToAllClientsExceptUDP(Message message, short playerId) {
        sendToAllClientsExcept(message, playerId, getUDPMessageServer());
    }
    
    /**
     * Sends <code>message</code> to all players except defined by
     * <code>playerId</code>.
     * 
     * @param message
     * @param playerId
     */
    public void sendToAllClientsExcept(Message message, short playerId, MessageServer messageServer) {
        Player[] players = getPlayers();
        int port;
        for (int i = 0; i < players.length; i++) {
            if (players[i].getPlayerId() != playerId) {
                if (messageServer instanceof TCPMessageServer) {
                    port = players[i].getTCPPort();
                } else {
                    port = players[i].getUDPPort();
                }
                try {
                    messageServer.sendMessage(message, players[i].getAddress(), port);
                } catch(IOException exc) {
                    PlayerDisconnectMessage pdm = new PlayerDisconnectMessage();
                    pdm.setPlayerId(playerId);
                    disconnectRequest(pdm);
                }
            }
        }
    }
    
    /**
     * @param playerId
     * @return
     *      true if the player <code>playerId</code> is currently
     *      connected to the server and has not expired.
     */
    public boolean isPlayerConnected(short playerId) {
        if (getPlayer(playerId) != null) return true;
        return false;
    }
    
    /**
     * Add a PlayerListener to receive events on the server.
     * 
     * @param listener
     */
    public void addPlayerListener(PlayerListener listener) {
        playerListeners.add(listener);
    }
    
    /**
     * Adds a listener to both message servers.
     * 
     * @param listener
     */
    public void addMessageListener(MessageListener listener) {
        if (messageServerUDP != null) {
            messageServerUDP.addMessageListener(listener);
        }
        if (messageServerTCP != null) {
            messageServerTCP.addMessageListener(listener);
        }
    }
    
    /**
     * Adds a sent listener to both message servers.
     * 
     * @param listener
     */
    public void addMessageSentListener(MessageSentListener listener) {
        if (messageServerUDP != null) {
            messageServerUDP.addMessageSentListener(listener);
        }
        if (messageServerTCP != null) {
            messageServerTCP.addMessageSentListener(listener);
        }
    }
    
    /**
	 * Disconnects all clients and kills all threads started by the
	 * NetworkingServer.
	 */
	public void shutdown() {
		keepAlive = false;
		ServerDisconnectMessage message = new ServerDisconnectMessage();
		sendToAllClients(message);
        if (messageServerTCP != null) {
            messageServerTCP.shutdown();
        }
        if (messageServerUDP != null) {
            messageServerUDP.shutdown();
        }
	}
}
