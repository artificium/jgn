# FlagRushTestServer #

Several major events occur in FlagRushTestServer:

  * We start the jME FlagRush application in its own thread.
  * We start a [wiki:JGNServer] in its own thread.
  * We start a [wiki:SynchronizationManager] in its own thread.
  * We extract the vehicle object from the FlagRush application and [wiki:SynchronizationManager#Registration register] it.
  * We extract the scene Node from the FlagRush application so we can use it to attach the remote vehicle.  (This attachment process occurs in FlagRushTest.)

## Code Copyright ##

CodeCopyrightNotice


## FlagRushTestServer Code ##

Note: The proceeding documentation will consist of blocks of text, each of which describes the salient features of the code block that immediately follows it.

For example:

The code you are about to see consists of several "import" statements which Java requires in order to find the classes you reference in your program.  You will also notice the construction of a ShadowRenderPass, which you can read about in jME's documentation.

```
package com.captiveimagination.jmenet.flagrush;
 
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
 
import com.captiveimagination.jgn.JGN;
import com.captiveimagination.jgn.clientserver.JGNServer;
import com.captiveimagination.jgn.synchronization.SynchronizationManager;
import com.captiveimagination.jgn.synchronization.message.SynchronizeCreateMessage;
import com.captiveimagination.jmenet.JMEGraphicalController;
import com.jme.renderer.pass.ShadowedRenderPass;
import com.jme.scene.Node;
 
import jmetest.flagrushtut.lesson9.Lesson9;
import jmetest.flagrushtut.lesson9.Vehicle;
import jmetest.renderer.ShadowTweaker;
 
public class FlagRushTestServer extends FlagRushTest {
    public FlagRushTestServer() throws Exception {
        // Set up the game just like in the lesson
        ShadowedRenderPass shadowPass = new ShadowedRenderPass();
```

### Start the FlagRush Application ###

First, we run the FlagRush application in a seperate thread.  It will run in the background while the rest of the code deals with synchronizing what happens in it and what happens in the client's version of the application.  (Note: As we will see later on, we can grab the application's relevant details--such as the Vehicle object--by using Java's Reflection API.)


```
        final FlagRush app = new FlagRush();
        app.setDialogBehaviour(2, Lesson9.class.getClassLoader().getResource("jmetest/data/images/FlagRush.png"));
        new ShadowTweaker(shadowPass).setVisible(true);
        new Thread() {
            public void run() {
                app.start();
            }
        }.start();
```

### Start Server ###

Next, we need to initialize the server.  The [wiki:JGNServer]'s constructor takes two addresses (either of which you can make "null.")  The server will use TCP protocol for traffic on the first socket you pass to its constructor and UDP protocol for traffic on the second.  (It decides internally what kind of messages get sent on which socket.)

```
        // Initialize networking
        InetSocketAddress serverReliable = new InetSocketAddress(InetAddress.getLocalHost(), 9100);
        InetSocketAddress serverFast = new InetSocketAddress(InetAddress.getLocalHost(), 9200);
        JGNServer server = new JGNServer(serverReliable, serverFast);
 
```

### Create JMEGraphicalController ###

The JMEGraphicalController helps to keep the location and rotation of the Spatials in a scene synchronized by creating and applying messages that carry information about a Spatial's local translation and local rotation.  The JMEGraphicalController represents the main point of intersection between jME and JGN.  It deals with transforming information about a Spatial (something specific to jME) into a JGN message, and vice versa.


```
        // Instantiate an instance of a JMEGraphicalController
        JMEGraphicalController controller = new JMEGraphicalController();

```

### Start Synchronization Manager ###

Whereas the [wiki:JMEGraphicalController] deals with the generation and application of messages, the actual sending and receiving of messages happens in the [wiki:JGNServer] and in the SynchronizationManager.  The latter keeps track of objects in need of network synchronization, by wrapping them each in a SyncWrapper (to hold information about their id, time of last update, owner, etc.)  We'll create a SynchronizationManager in the code below.  And later we'll see how we can tell the manager to register and begin synchronizing an object.

```
        // Create SynchronizationManager instance for this server
        SynchronizationManager serverSyncManager = new SynchronizationManager(server, controller);
        serverSyncManager.addSyncObjectManager(this);
 
        JGN.createThread(server).start();
 
        JGN.createThread(serverSyncManager).start();

```

### Reflection ###

"Reflection is commonly used by programs which require the ability to examine or modify the runtime behavior of applications running in the Java virtual machine. This is a relatively advanced feature and should be used only by developers who have a strong grasp of the fundamentals of the language. With that caveat it mind, reflection is a powerful technique and can enable applications to perform operations which would otherwise be impossible" (http://java.sun.com/docs/books/tutorial/reflect/index.html).

Even though the "player" variable has private access in the FlagRush class, we can make our own Vehicle (identical to the one currently running around in the FlagRush app) by using Java's Field class.  The while-loop below keeps trying to get the Vehicle every tenth of a second until it gets one that isn't null.

NOTE: We can't just yank the Vehicle out without first checking to see if it exists.  The FlagRush application runs in its own thread and, by this time, will probably have just begun its setup procedure, having not yet instantiated the "player" object.  Our current thread needs to wait for the FlagRush application.  Hence: the while-loop.

Than, after getting the Vehicle, we grab the scene node.

```
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

### Registration ###

Finally, we register the vehicle object with the SynchronizationManager.  As mentioned before, the object will go into a SyncWrapper, which will in turn go into a queue whose contents get periodically updated.  When the client logs on, our server-side SynchronizationManager will send a SynchronizeCreateMessage to the client-side SynchronizeManager, eventually telling the client to place a Vehicle (which we'll call "Vehicle X") on the client's scene graph.  Let's call the Vehicle we just grabbed from our own scene graph "Vehicle Y."  The server-side SynchronizationManager will send periodic updates about Vehicle Y, which will tell the client-side SynchronizationManager to update the position and rotation of Vehicle X.

```
        // Register server vehicle
        serverSyncManager.register(vehicle, new SynchronizeCreateMessage(), 50);
    }
 
    public static void main(String[] args) throws Exception {
        new FlagRushTestServer();
    }
}

```