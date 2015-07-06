# FlagRushTest #

Both the server (FlagRushTestServer) and the client (FlagRushTestClient) extend this class.  You'll find the following important landmarks in the code:

  * buildRemotePlayer()
  * setScene(Node scene)
  * Use of GameTaskQueueManager
  * Use of SyncObjectManager
  * Use of Future
  * Use of Callable

In a nutshell, this FlagRushTest class defines how the client and server will construct synchronized scenes--with the same terrain and the same vehicles.  But it does not deal with how that synchronization gets facilitated via message passing.  Instead, this class deals almost exclusively with attaching, to the scene graph, the spatial that will represent a remote player.  The updating the position and rotation of this spatial, such as those that should happen when a remote player moves, occurs elsewhere (in the SynchronizationManger and the SyncWrapper.)


## Code Copyright ##

CodeCopyrightNotice


## FlagRushTest Code ##

Note: The proceeding documentation will consist of blocks of text, each of which describes the salient features of the code block that immediately follows it.

For example:

The code you are about to see consists of several "import" statements which Java requires in order to find the classes you reference in your program.

```
package com.captiveimagination.jmenet.flagrush;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import jmetest.flagrushtut.lesson8.Lesson8;
import jmetest.flagrushtut.lesson9.Vehicle;

import com.captiveimagination.jgn.synchronization.SyncObjectManager;
import com.captiveimagination.jgn.synchronization.message.SynchronizeCreateMessage;
import com.captiveimagination.jgn.synchronization.message.SynchronizeRemoveMessage;
import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.export.binary.BinaryImporter;

public abstract class FlagRushTest implements SyncObjectManager {
    private Node scene;

    public void setScene(Node scene) {
        this.scene = scene;
    }

```


We need the following "create" and "remove" methods because FlagRushTest implements SyncObjectManager, which manages the creation and deletion of synchronized objects (like Vehicles, for example.)  In our case, the

  1. position,
> 2. rotation,
> 3. and existence

of Vehicles must remain synchronized across the multiplayer environment.  In this class, we deal with the third of these aspects.  For example, when a client logs on, the server and all other clients must add a new remote vehicle object to their scene graphs, so that all vehicles exist on all running applications simultaneously.

The ''create(...)'' method below returns an Object (here, a Vehicle.)  The SynchronizationManager then has access to this Object and performs updates on it, depending on the remote messages received from the SynchronizationManager on the other side of the client/server connection.  We'll discuss the relationship between two (or more) SynchronizationManagers in more detail on the actual SynchronizationManager page.  For now, we should simply note that the Object returned by the create method below ends up in the SynchronizationManager because FlagRushTest implements SyncObjectManager, and because we call the [wiki:FlagRushTestServer#addSyncObjectManager "addSyncObjectManager(this)"] method  on the SynchronizationManager in both the FlagRushTestClient and the FlagRushTestServer.  This attachment of the SyncObjectManager and the SynchronizationManager provides the point of contact between 1) the creation of remote players, which we're about to see, and 2) the synchronization of their movements, which happens in a different thread.



```
#!java

    public Object create(SynchronizeCreateMessage scm) {
        System.out.println("CREATING PLAYER!");
        return buildRemotePlayer();
    }
 
    public boolean remove(SynchronizeRemoveMessage srm, Object obj) {
        System.out.println("REMOVING PLAYER!");
        return removeRemotePlayer((Vehicle)obj);
    }
 
```

The buildRemotePlayer() method undertakes the task of loading the 3D model that represents a player's Vehicle.  Because the FlagRushTestServer and the FlagRushTestClient both extend FlagRushTest, they both end up calling this same method to create a Vehicle.

```
#!java

    private Vehicle buildRemotePlayer() {
        Future<Vehicle> future = GameTaskQueueManager.getManager().update(new Callable<Vehicle> () {
            public Vehicle call() throws Exception {
                Spatial model = null;
                try {
                    URL bikeFile = Lesson8.class.getClassLoader().getResource("jmetest/data/model/bike.jme");
                    BinaryImporter importer = new BinaryImporter();
                    model = (Spatial)importer.load(bikeFile.openStream());
                    model.setModelBound(new BoundingBox());
                    model.updateModelBound();
                    //scale it to be MUCH smaller than it is originally
                    model.setLocalScale(.0025f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
 
                //set the vehicles attributes (these numbers can be thought
                //of as Unit/Second).
                Vehicle player = new Vehicle("Player Node", model);
                player.setAcceleration(15);
                player.setBraking(15);
                player.setTurnSpeed(2.5f);
                player.setWeight(25);
                player.setMaxSpeed(25);
                player.setMinSpeed(15);
 
                player.setLocalTranslation(new Vector3f(100,0, 100));
                scene.attachChild(player);
                scene.updateGeometricState(0, true);
                player.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
                scene.updateRenderState();
                return player;
            }
        });
        try {
            return future.get();
        } catch(Exception exc) {
            exc.printStackTrace();
        }
        return null;
    }

    private boolean removeRemotePlayer(Vehicle vehicle) {
        return vehicle.removeFromParent();
    }
}
```