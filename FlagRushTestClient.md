# FlagRushTestClient #

Several major events occur in FlagRushTestClient:

  * We start the jME FlagRush application in its own thread.
  * We define the location of the server and create appropriate connections.
  * We start a [wiki:JGNClient] in its own thread.
  * We start a SynchronizationManager in its own thread.
  * We extract the vehicle object from the FlagRush application and [wiki:SynchronizationManager#register register] it.
  * We extract the scene Node from the FlagRush application so we can use it to attach the remote vehicle.  (This attachment process occurs in FlagRushTest.)
  * We add a MessageListener to the client so we can keep track of the messages that it receives.

Note: Many of these events also occur in FlagRushTestServer.  Points of difference include:

  1. explicitly defining the server's address as well as the client's (whereas in FlagRushTestServer, we don't need to define the client's address.)
> 2. starting a client instead of a server (naturally.)
> 3. adding a MessageListener (which isn't vital, and which you can do just as easily on the server.)

Aside from these points, the client and server applications bear striking similarities.  For this reason, I have only gone into detail about the portions of this code that differ significantly from the server code.  If something remains mysterious after reading this page, the answers probably lie on the FlagRushTestServer page.


## Code Copyright ##

CodeCopyrightNotice


## FlagRushTestClient Code ##

```
#!java

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
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.Node;
 
public class FlagRushTestClient extends FlagRushTest {
    public FlagRushTestClient() throws Exception {
        // Set up the game just like in the lesson
        ShadowedRenderPass shadowPass = new ShadowedRenderPass();
        final FlagRush app = new FlagRush();
        app.setDialogBehaviour(2, Lesson9.class.getClassLoader().getResource("jmetest/data/images/FlagRush.png"));
        new ShadowTweaker(shadowPass).setVisible(true);
        new Thread() {
            public void run() {
                app.start();
            }
        }.start();
```

Below, we define the address of the server and the port on which to connect in a InetSocketAddress object.  Later, we'll use this socket to connect to the server.

```
#!java
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

```

Now we've set the vehicle and scene node, we can connect to the server.  Note: If we had tried to connect earlier, we would have begun to receive messages from the server telling the client to create a remote player (the server's Vehicle) before we were ready to actually do so--because the scene Node would have been null.  (See the create method in FlagRushTest.)

```
#!
 
        // Connect to the server before we register anything
        System.out.println("Connecting!");
        client.connectAndWait(serverReliable, serverFast, 5000);
        System.out.println("Connected!");

```

Next we attach a MessageListener, which can serve as a valuable tool in debugging code, creating logs, or for simply assuaging your curiosity about what kinds of messages a client or server receives.  You can use the following code as a reference for creating your own custom MessageListener.  Or you can use a default listener with a line such as this: ''client.addMessageListener(new DebugListener("Client"));''

```
#!java      
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
```