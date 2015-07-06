This patch will give the users an interface what to do in certain "cheating" situations. Currently the only "cheat"-event handled is when duplicate ID's are found.

To apply the patch the following code must be added:

First we create the CheatHandler interface
```
package com.captiveimagination.jgn;

/**
 * CheatHandler is an open interface which can be used to define
 * what action should be taken when "cheat" situations occur.
 * @author Jeffrey Pijnappels
 */
public interface CheatHandler {
    
    	public static enum CHEAT_EVENT {
		DUPLICATE_ID
	}
    
        /**
         * This method is invoked when a duplicate playerID has been found
         * on the MessageServer.
         * @param cheatingId The playerId of the cheating user.
         * @param cheatingClient The MessageClient of the cheating user.
         */
    public void handleDuplicateId(short cheatingId, MessageClient cheatingClient);
    
}
```


Now that we have the interface, we want to have acces to it. We do that by adding the following
code to **JGNServer**

```
    /**
     * Allows a custom to solution to handle certain cheat-situations
     * @param cheatHandler A new CheatHandler instance.
     */
    public void addCheatHandler(CheatHandler cheatHandler)
    {
                
        if (reliableServer != null)
        {
            reliableServer.addCheatHandler(cheatHandler);
        }
        if (fastServer != null)
        {
            fastServer.addCheatHandler(cheatHandler);
        }
    }

    /**
     * Removes the CheatHandler from the JGNServer
     * @param cheatHandler 
     */
    public void removeCheatHandler(CheatHandler cheatHandler)
    {
        LOG.log(Level.FINEST, "remove CheatHandler {0}", cheatHandler);
        if (reliableServer != null)
        {
            reliableServer.removeCheatHandler(cheatHandler);
        }
        if (fastServer != null)
        {
            fastServer.removeCheatHandler(cheatHandler);
        }
    }


```

The underlying **MessageServer** will need the following code.
```

private final ConcurrentLinkedQueue<CheatHandler> cheatHandlers;
...
...
...
/**
 * Adds a CheatHandler to the MessageServer
 * @param handler
 */
    public void addCheatHandler(CheatHandler handler)
    {
        synchronized (cheatHandlers)
        {
            cheatHandlers.add(handler);
        }
        log.finest("added CheatHandler: " + handler);
    }

    /**
     * Removed a CheatHandler from the MessageServer
     * @param handler
     * @return A <code>boolean<code> whether the removal was succesfull.
     */
    public boolean removeCheatHandler(CheatHandler handler)
    {
        boolean result;
        synchronized (cheatHandlers)
        {
            result = cheatHandlers.remove(handler);
        }
        if (result)
        {
            log.finest("removed CheatHandler: " + handler);
        } else
        {
            log.finest("NOT removed CheatHandler: " + handler);
        }
        return result;
    }
```

With the above set up, the handler is done. Now we can add a cheat situation to JGN's code.

Inside **ServerClientConnectionController** we _replace_ this:

```
 if ((message.getPlayerId() != -1) && (message.getPlayerId() != server.getConnection(message.getMessageClient()).getPlayerId()))
            {
                LOG.log(Level.WARNING, "MessageClient tried to send a message with the wrong playerId, ignoring (Received: " + message.getPlayerId() + ", Expected: " + server.getConnection(message.getMessageClient()).getPlayerId() + ")!");
                // TODO send something to cheat handler
                //return;
            }

```

with this:

```
 if ((message.getPlayerId() != -1) && (message.getPlayerId() != server.getConnection(message.getMessageClient()).getPlayerId()))
            {
                LOG.log(Level.WARNING, "MessageClient tried to send a message with the wrong playerId, ignoring (Received: " + message.getPlayerId() + ", Expected: " + server.getConnection(message.getMessageClient()).getPlayerId() + ")!");
             
             
                server.executeCheatHandling(message.getPlayerId(),message.getMessageClient(), CheatHandler.CHEAT_EVENT.DUPLICATE_ID);
                //return;
            }
```


We add this to **JGNServer**:

```
    public void executeCheatHandling(short cheatingId, MessageClient cheatingClient, CheatHandler.CHEAT_EVENT type)
    {
        if (reliableServer != null)
        {
            reliableServer.executeCheatHandler(cheatingId, cheatingClient, type);
        } else if (fastServer != null)
        {
            fastServer.executeCheatHandler(cheatingId, cheatingClient, type);
        }
    }
```

And these 2 methods to **MessageServer**
```
    public void executeCheatHandler(short cheatingId, MessageClient cheatingClient, CheatHandler.CHEAT_EVENT type)
    {
        synchronized (cheatHandlers)
        {
            for (CheatHandler handler : cheatHandlers)
            {
                sendToCheatHandler(cheatingId, cheatingClient, handler, type);
            }
        }
    }
```

```

    private static void sendToCheatHandler(short cheatingId, MessageClient cheatingClient, CheatHandler handler, CheatHandler.CHEAT_EVENT type)
    {
        switch(type)
        {
            case DUPLICATE_ID:
                handler.handleDuplicateId(cheatingId, cheatingClient);
                break;
        }
        
    }

```



Now when we instantiate a JGNServer we will have the option to handle cheat events.
The instantiation code would look like this:

```

InetSocketAddress serverExternalReliable = new InetSocketAddress((InetAddress)null, 5000);
        InetSocketAddress serverExternalFast = new InetSocketAddress((InetAddress)null, 5001);
        JGNServer serverExternalManager;
        try
        {
            serverExternalManager = new JGNServer(serverExternalReliable, serverExternalFast);
            
            serverExternalManager.addCheatHandler( new CheatHandler()
            {
                public void handleDuplicateId(short duplicateId, MessageClient duplicateClient)
                {
                    System.out.println("We have a cheater with ID:"+duplicateId);
                    duplicateClient.disconnect();
                }
            });
            
            JGN.createThread(serverExternalManager).start();

```