# Introduction #

Synchronization is a great thing to have in a networked game, don't you think?
Well, here's a short and basic explanation of how it works to get you started.


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
 * Created: Jul 29, 2006
 */
package com.captiveimagination.jgn.test.sync;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;

import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.clientserver.*;
import com.captiveimagination.jgn.synchronization.*;
import com.captiveimagination.jgn.synchronization.message.SynchronizeCreateMessage;
import com.captiveimagination.jgn.synchronization.message.SynchronizeRemoveMessage;
import com.captiveimagination.jgn.synchronization.swing.*;

/**
 * @author Matthew D. Hicks
 *
 */
public class SimpleSynchronization extends JFrame implements KeyListener {
	private static final long serialVersionUID = 1L;
	private static final long SERVER_OBJECT = 1;
	private static final long CLIENT_OBJECT = 2;
	
	private JPanel panel;
	private JPanel serverPanel;
	private JPanel clientPanel;
	private long type;
	
	public SimpleSynchronization(long type) {
		if (type == SERVER_OBJECT) {
			setTitle("Server");
		} else {
			setTitle("Client");
		}
		setSize(400, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(this);
		
		panel = new JPanel();
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(BorderLayout.CENTER, panel);
		
		panel.setLayout(null);
		panel.addKeyListener(this);
		
		if (type == SERVER_OBJECT) {
			serverPanel = new JPanel();
			serverPanel.addKeyListener(this);
			serverPanel.setBounds(0, 0, 50, 50);
			serverPanel.setBackground(Color.BLUE);
			panel.add(serverPanel);
		} else {
			clientPanel = new JPanel();
			clientPanel.addKeyListener(this);
			clientPanel.setBounds(300, 300, 50, 50);
			clientPanel.setBackground(Color.RED);
			panel.add(clientPanel);
		}
		
		this.type = type;
	}
	
	public JPanel createPanel(int x, int y) {
		JPanel panel = new JPanel();
		panel.addKeyListener(this);
		panel.setBounds(x, y, 50, 50);
		panel.setBackground(Color.GREEN);
		panel.setVisible(true);
		this.panel.add(panel);
		this.panel.repaint();
		return panel;
	}
	
	public void removePanel(JPanel panel) {
		this.panel.remove(panel);
	}
	
	protected JPanel getServerPanel() {
		return serverPanel;
	}
	
	protected JPanel getClientPanel() {
		return clientPanel;
	}
	
	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		int x = 0;
		int y = 0;
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			x -= 5;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			x += 5;
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			y -= 5;
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			y += 5;
		}
		
		if (type == SERVER_OBJECT) {
			serverPanel.setBounds(serverPanel.getX() + x, serverPanel.getY() + y, 50, 50);
		} else {
			clientPanel.setBounds(clientPanel.getX() + x, clientPanel.getY() + y, 50, 50);
		}
	}
	
	public static void main(String[] args) throws Exception {
		// Instantiate the SimpleSynchronization GUI for the server
		final SimpleSynchronization ssServer = new SimpleSynchronization(SERVER_OBJECT);
		ssServer.setVisible(true);
		
		// Instantiate an instance of a SwingGraphicalController
		SwingGraphicalController controller = new SwingGraphicalController();
		
		// Start the server
		InetSocketAddress serverReliable = new InetSocketAddress(InetAddress.getLocalHost(), 1000);
		InetSocketAddress serverFast = new InetSocketAddress(InetAddress.getLocalHost(), 2000);
		JGNServer server = new JGNServer(serverReliable, serverFast);
		SynchronizationManager serverSyncManager = new SynchronizationManager(server, controller);
		serverSyncManager.addSyncObjectManager(new SyncObjectManager() {
			public Object create(SynchronizeCreateMessage scm) {
				return ssServer.createPanel(300, 300);
			}

			public boolean remove(SynchronizeRemoveMessage srm, Object object) {
				ssServer.removePanel((JPanel)object);
				return true;
			}
		});
		JGN.createThread(server, serverSyncManager).start();
		
		// Register our server object with the synchronization manager
		serverSyncManager.register(ssServer.getServerPanel(), new SynchronizeCreateMessage(), 50);
		
		// Instantiate the SimpleSynchronization GUI for the client
		final SimpleSynchronization ssClient = new SimpleSynchronization(CLIENT_OBJECT);
		ssClient.setLocation(410, 0);
		ssClient.setVisible(true);
		
		// Start the client
		JGNClient client = new JGNClient(new InetSocketAddress(InetAddress.getLocalHost(), 0), new InetSocketAddress(InetAddress.getLocalHost(), 0));
		SynchronizationManager clientSyncManager = new SynchronizationManager(client, controller);
		clientSyncManager.addSyncObjectManager(new SyncObjectManager() {
			public Object create(SynchronizeCreateMessage scm) {
				return ssClient.createPanel(0, 0);
			}

			public boolean remove(SynchronizeRemoveMessage srm, Object object) {
				ssClient.removePanel((JPanel)object);
				return true;
			}
		});
		JGN.createThread(client, clientSyncManager).start();
		client.connectAndWait(serverReliable, serverFast, 5000);
		
		// Register our client object with the synchronization manager
		clientSyncManager.register(ssClient.getClientPanel(), new SynchronizeCreateMessage(), 50);

(...)
	}
}
```
This is the class in the test package in the JGN source called SimpleSynchronization.
It uses Swing to show how the sync'ing works, but you don't need to know Swing very well to understand how it's all done in this example.

What we're interested in is what's happening in the main method.
First we create an instance of this class as a server object (ssServer). You will get two windows when you run this test and one of them will act as the server window and shows what the server 'sees'.

```
// Instantiate an instance of a SwingGraphicalController
		SwingGraphicalController controller = new SwingGraphicalController();
```
Then we create a SwingGraphicalController. A GraphicalController (which SwingGraphicalController extends) handles what happens when a party receives a SynchronizeMessage and how a SynchronizeMessage is put together. It also handles a few other things. Here's the code for the SwingGraphicalController:

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
 * Created: Jul 29, 2006
 */
package com.captiveimagination.jgn.synchronization.swing;

import java.awt.*;

import com.captiveimagination.jgn.synchronization.*;
import com.captiveimagination.jgn.synchronization.message.*;

/**
 * This is an example implementation of the GraphicalController for use
 * with Swing. The objects specified are JPanels.
 * 
 * @author Matthew D. Hicks
 */
public class SwingGraphicalController implements GraphicalController {
	public void applySynchronizationMessage(SynchronizeMessage message, Object obj) {
		Component component = (Component)obj;
		Synchronize2DMessage m = (Synchronize2DMessage)message;
		component.setBounds((int)m.getPositionX(), (int)m.getPositionY(), 50, 50);
	}

	public SynchronizeMessage createSynchronizationMessage(Object obj) {
		Component component = (Component)obj;
		Synchronize2DMessage message = new Synchronize2DMessage();
		message.setPositionX(component.getX());
		message.setPositionY(component.getY());
		return message;
	}

	public float proximity(Object obj, short playerId) {
		return 1.0f;
	}

	public boolean validateMessage(SynchronizeMessage message, Object obj) {
		return true;
	}

	public boolean validateCreate(SynchronizeCreateMessage message) {
		return true;
	}

	public boolean validateRemove(SynchronizeRemoveMessage message) {
		return true;
	}
}
```
`public void applySynchronizationMessage(SynchronizeMessage message, Object obj)` applies a received SynchronizeMessage to the object given in the argument (this is handled by other classes calling this controller). The object is the sync'ed object that is set up to receive sync messages and the SynchronizeMessage is the message that contains the info from the sending party that controls the registered object that the object on this party represents.
In this case the message contains coords for a 2D position (x and y) which are applied to the object.

`public SynchronizeMessage createSynchronizationMessage(Object obj)` on the other hand creates a SynchronizeMessage. This is done on the side of the party that controls the object in question. The message that is returned from this method is sent to the connected parties to update the representations of this object there.

```
public float proximity(Object obj, short playerId) {
		return 1.0f;
	}
```
This method is used to determine how close a certain object is to the specified player. Here you can calculate a distance and then return a value to say how often a sync'ed object should be updated. If it returns 1 (as it always does in this test) the object is sync'ed at the full update rate (given when registering). If it returns a value 0 < x < 1 the object is updated less often and if you return 0 it isn't updated at all (since it's too far away is the idea). Objects that are far away don't need to be updated as often, so this can be very useful.

```
public boolean validateMessage(SynchronizeMessage message, Object obj) {
		return true;
	}

	public boolean validateCreate(SynchronizeCreateMessage message) {
		return true;
	}

	public boolean validateRemove(SynchronizeRemoveMessage message) {
		return true;
	}
```
These three methods are validation methods used to check if the different kinds of sync messages are valid. Here you can check for cheating and other errors.

Ok, back to the main method of the test class!

```
SynchronizationManager serverSyncManager = new SynchronizationManager(server, controller);
		serverSyncManager.addSyncObjectManager(new SyncObjectManager() {
			public Object create(SynchronizeCreateMessage scm) {
				return ssServer.createPanel(300, 300);
			}

			public boolean remove(SynchronizeRemoveMessage srm, Object object) {
				ssServer.removePanel((JPanel)object);
				return true;
			}
		});
		JGN.createThread(server, serverSyncManager).start();
```
I'm skipping the server setup since that's covered in another tutorial and go straight to the sync'ing. First we set up a SynchronizationManager (it needs the server to sync through and a GraphicalController to do what we discussed above).

The SynchronizationManager is the main class for the sync stuff, it handles SyncWrappers, updating and other things that I won't get into detail about here now.
It also needs a SyncObjectManager. This class is an interface between the SynchronizationManager and you class.

In the first method `public Object create(SynchronizeCreateMessage scm)` you receive a SynchronizeCreateMessage from another party asking you to create a representation of their object on your side. You can extend this message to contain information that you need to build this representation, but in this example we're just making panels (squares) so it's not needed. The object you return here is stored in the SynchronizationManager and synced from the other party's object.
The `public boolean remove(SynchronizeRemoveMessage srm, Object object)` method does the opposite and here you are also given the object that wants to get removed.

Notice in the last line of this code snippet that we put both the server and serverSyncManager objects in as arguments to `JGN.createThread`. This method can take several Updatables at once.

```
// Register our server object with the synchronization manager
		serverSyncManager.register(ssServer.getServerPanel(), new SynchronizeCreateMessage(), 50);
```
Here we do an important thing, we register our object from this side with the SynchronizationManager. This object is then 'authorative' from this side and sync'ed to the connected parties. We specify our server object in this case, a SynchronizeCreateMessage and an update rate in milliseconds (this is the time between sync updates if proximity returns 1).

In the rest of the code we do the same thing for the client, so we get one server window with a server object and a representation of the client object (after the client connects) and one client window with a client object and a representation of the server object (after the client connects). The are continually updated as you move one of them around.

Note:
If you want to do more advanced sync'ing than this, you can extend SynchronizeCreateMessage to add your own fields and methods.
Some rules apply that you need to be aware of:
**All messages need to have a "no-args constructor" (a constructor with no arguments) for the reflection used by JGN.**All messages need to have getters for every field (possibly also setters).
**All messages need to be registered with JGN:
```
JGN.register(MyMessage.class);
```**

With your extended message you can do the following e.g. (compare with the code above):
```
public Object create(SynchronizeCreateMessage scm) {
   if(scm instanceof MySyncCreateMessage) {
      MySyncCreateMessage m = (MySyncCreateMessage) scm;
      return new MyObject(m.getObjectId(), m.getModelPath());
   }
   else if(scm instanceof OtherSyncMessage) {
      return new OtherObject();
   }
}

public boolean remove(SynchronizeRemoveMessage srm, Object object) {
   if(object == thisObject) {
      thisObject.remove();
      return true;
   }
   else if(object == thatObject) {
      thatObject.removeFromParent();
      return true;
   }
   return false;
}
```
The object you return in the create-method is the object that gets sync'ed.
The boolean you return in the remove-method tells the sync manager whether the object was removed or not.

An example of an extended SynchronizeCreateMessage could be:
```
import com.captiveimagination.jgn.synchronization.message.SynchronizeCreateMessage;

public class MySyncCreateMessage extends SynchronizeCreateMessage{
	private int objectId;
	private String modelPath;
	
	public MySyncCreateMessage() {
		//No-args constructor for reflection
	}
	
	public MySyncCreateMessage(int objectId, String modelPath) {
		this.objectId = objectId;
		this.modelPath = modelPath;
	}
	
	public void setObjectId(int num) {
		this.objectId = num;
	}
	
	public int getObjectId() {
		return objectId;
	}
	
	public void setModelPath(String str) {
		this.modelPath = str;
	}
	
	public String getModelPath() {
		return modelPath;
	}
}
```

I hope this has been a useful tutorial and that you enjoy using JGN!