# Diving into the Multiplayer Flag Rush Tutorial #

If you've never heard of jME's Flag Rush tutorials, you can find information about them here:

  * FlagRush

I recommend taking a look at these three classes to see what JGN can do, especially with regard to JME:

  * FlagRushTest
  * FlagRushTestServer
  * FlagRushTestClient

The above classes exhibit points of contact between JGN and JME, as does this one:

  * JMEGraphicalController

After examining the files listed above, you may wish to understand the following important classes:

  * [JGN](JGN.md)
  * [JGNClient](JGNClient.md)
  * [JGNServer](JGNServer.md)
  * SynchronizationManager
  * GameTaskQueueManager
  * MessageListener
  * SyncObjectManager
  * SynchronizeMessage
  * SyncWrapper
  * [Future](Future.md)
  * [Callable](Callable.md)
  * [Field](Field.md)

Some of these classes (such as [Future](Future.md) and [Callable](Callable.md)) do not lie within the explicit domain of the JGN software.  Nevertheless, an understanding of each of them will yield a deeper understanding of concurrency, network programming, reflection, and how JGN and JME relate.