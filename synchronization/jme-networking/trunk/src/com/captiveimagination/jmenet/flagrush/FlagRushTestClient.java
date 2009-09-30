package com.captiveimagination.jmenet.flagrush;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetest.flagrushtut.lesson9.Lesson9;
import jmetest.flagrushtut.lesson9.Vehicle;
import jmetest.renderer.ShadowTweaker;

import com.captiveimagination.jgn.JGN;
import com.captiveimagination.jgn.JGNConfig;
import com.captiveimagination.jgn.clientserver.JGNClient;
import com.captiveimagination.jgn.clientserver.JGNServer;
import com.captiveimagination.jgn.event.MessageListener;
import com.captiveimagination.jgn.message.Message;
import com.captiveimagination.jgn.synchronization.SynchronizationManager;
import com.captiveimagination.jgn.synchronization.message.SynchronizeCreateMessage;
import com.captiveimagination.jmenet.JMEGraphicalController;
import com.jme.app.AbstractGame.ConfigShowMode;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.Node;

public class FlagRushTestClient extends FlagRushTest {
	public FlagRushTestClient() throws Exception {
		// Set up the game just like in the lesson
		ShadowedRenderPass shadowPass = new ShadowedRenderPass();
		final FlagRush app = new FlagRush();
		app.setConfigShowMode(ConfigShowMode.AlwaysShow, FlagRushTestClient.class
                .getClassLoader().getResource(
                        "jmetest/data/images/FlagRush.png"));
        new ShadowTweaker(shadowPass).setVisible(true);
        new Thread() {
        	public void run() {
        		app.start();
        	}
        }.start();
        
        // Define the server address
        InetSocketAddress serverReliable = new InetSocketAddress(InetAddress.getLocalHost(), 9100);
		InetSocketAddress serverFast = new InetSocketAddress(InetAddress.getLocalHost(), 9200);
        
        // Initialize networking
        InetSocketAddress clientReliable = new InetSocketAddress(InetAddress.getLocalHost(), 0);
		InetSocketAddress clientFast = new InetSocketAddress(InetAddress.getLocalHost(), 0);
		JGNClient client = new JGNClient(clientReliable, clientFast);
		
		JGN.createThread(client).start();
		
		// Instantiate an instance of a JMEGraphicalController
		JMEGraphicalController controller = new JMEGraphicalController();
		
		// Create SynchronizationManager instance for this server
		SynchronizationManager clientSyncManager = new SynchronizationManager(client, controller);
		clientSyncManager.addSyncObjectManager(this);
		JGN.createThread(clientSyncManager).start();
		
        // Get the vehicle instance out of the application without making any changes
		Field field = FlagRush.class.getDeclaredField("player");
		field.setAccessible(true);
		Vehicle vehicle = null;
		
		while ((vehicle = (Vehicle)field.get(app)) == null) {
			try {
				Thread.sleep(100);
			} catch(Exception exc) {
				exc.printStackTrace();
			}
		}
		
		// Retrieve the "scene" from the game
		field = FlagRush.class.getDeclaredField("scene");
		field.setAccessible(true);
		Node scene = (Node)field.get(app);
		setScene(scene);
		
		// Connect to the server before we register anything
		System.out.println("Connecting!");
		client.connectAndWait(serverReliable, serverFast, 5000);
		System.out.println("Connected!");
		
		client.getServerConnection().getReliableClient().addMessageListener(new MessageListener() {
			public void messageCertified(Message message) {
				System.out.println("Message Certified: " + message);
			}

			public void messageFailed(Message message) {
				System.out.println("Message Failed: " + message);
			}

			public void messageReceived(Message message) {
				System.out.println("Message Received: " + message);
			}

			public void messageSent(Message message) {
				System.out.println("Message Sent: " + message);
			}
		});
		
		// Register server vehicle
		clientSyncManager.register(vehicle, new SynchronizeCreateMessage(), 50);
	}
	
	public static void main(String[] args) throws Exception {
		new FlagRushTestClient();
	}
}
