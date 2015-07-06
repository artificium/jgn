## Introduction ##

JGN currently has a BlowFishDataTranslator which allows use of BlowFish for encryption purposes. Before setting up an encrypted connection, a key must be generated. This key generally is used by client and server to decrypt a message. With a incorrect key or no key at all, the message cannot be decrypted. Thus the client and server must have the same key to "talk" with each other. To generate a key take a look at the com.captiveimagination.jgn.test.translation.TestDataTranslation.java inside JGN. In this example a key is generated in byte form.

A snippet of the code:


> KeyGenerator kgen = KeyGenerator.getInstance("Blowfish");
> SecretKey skey = kgen.generateKey();
> byte[.md](.md) raw = skey.getEncoded();
> BlowfishDataTranslator trans = new BlowfishDataTranslator(raw);


The byte[.md](.md) is what we're interested in, since setting up the BlowFishDataTranslator requires this value. A small sidenote, the byte-array consists of 16 entries. Since a byte is 8bits, we have a 128-bits key.


## Creating a key ##

Now to set up a secure client and server requires the same to be on both sides. This means we have to generate a key and somehow make sure it's on the client as well as on the server side. There are numerous ways how this can be achieved. One of the main goals of JGN is not to presume one methodology over another, but rather to enable you to do anything you want. To name a few options you may choose to connect to an HTTPS URL to get the encryption key. An other method might be to pack it in your project or simply send out the key over an insecure connection before establishing a secure connection. It all depends on the project's needs.

For this example I have chosen to pack the key with the client and server and packed the key using the following method:


```
  private byte[] returnKey()
      {[[BR]]
          byte[] key = new byte[16];
          key[0] = -112;
          key[1] = -55;
          key[2] = 14;
          key[3] = 118;
          key[4] = 42;
          key[5] = 21;
          key[6] = 89;
          key[7] = 62;
          key[8] = 58;
          key[9] = 5;
          key[10] = 107;
          key[11] = -114;
          key[12] = 35;
          key[13] = -16;
          key[14] = -76;
          key[15] = 6;
          return key;
      }
```




## Using the DataTranslator ##

After adding this method both client and server have a key. To make use of the encryption functionality a Datatranslator must be added to the MessageServer's. The Datatranslator will use the key for their encryption tasks. The most important parts of setting up a MessageServer with a datatranslator looks something like this:


```
  private JGNServer internalServerManager; 
  ....
  BlowfishDataTranslator trans = new BlowfishDataTranslator(returnKey());
  ....
  internalServerManager.getReliableServer().addDataTranslator(trans);
  internalServerManager.getFastServer().addDataTranslator(trans);
  ....
  JGN.createThread(internalServerManager).start();
```

The same must be done for the client side.
After setting this up we have succesfully created a secure connection. To proof that I have made the following screenshots of the exact same JGN Message being sent with a secure and a insecure MessageServer.


## Testing encryption ##


The insecure connection, as you can see at the bottom, the message is visible. Imagine that to be someone's username or password. You don't want that to be in the open like that if you're serious about your networking[[BR](BR.md)]
![http://img529.imageshack.us/img529/4833/insecconnr4.png](http://img529.imageshack.us/img529/4833/insecconnr4.png)

The secure connection, there appears to be a little bit of overhead in the data , 7 bytes to be exact but the message is now unreadable.[[BR](BR.md)]
![http://img185.imageshack.us/img185/4814/secureconsi3.png](http://img185.imageshack.us/img185/4814/secureconsi3.png)

After this small test one might realise it would be more safe to avoid sending meaningfull strings in your custom messages and rather use integers/shorts to obscure it more. Though security thru obscurity is never a good solution so it's a must-have to have a message validator at the receiving side when there is the slightest doubt a side might be compromised. The message validator would check each message if the values being passed are allowed.


## Try it out for yourself! ##

Added to this page is the code I wrote to test this out. You can download them and execute them with the following commando's.

Start the secure server with the following commando: "java -cp SecureJGNServer.jar:JGN.jar: com.secureJGNServer.server 

&lt;ip&gt;

 

&lt;reliableport&gt;

 

&lt;fastport&gt;

"

To start the insecure server type: "java -cp SecureJGNServer.jar:JGN.jar: com.insecureJGNServer.server 

&lt;ip&gt;

 

&lt;reliableport&gt;

 

&lt;fastport&gt;

"

Start the secure client with the following commando: "java -cp SecureJGNClient.jar:JGN.jar: com.secureJGNClient.client 

&lt;serverIP&gt;

 

&lt;serverReliablePort&gt;

 

&lt;serverFastPort&gt;

 

&lt;clientIp&gt;

 

&lt;clientReliablePort&gt;

 

&lt;clientFastPort&gt;



To start the insecure client type: "java -cp SecureJGNClient.jar:JGN.jar: com.insecureJGNClient.client 

&lt;serverIP&gt;

 

&lt;serverReliablePort&gt;

 

&lt;serverFastPort&gt;

 

&lt;clientIp&gt;

 

&lt;clientReliablePort&gt;

 

&lt;clientFastPort&gt;



note: I've used a ":" to seperate jar's on the classpath (-cp), on windows machines use ";" instead.


For more information visit:
http://forum.captiveimagination.com/index.php?topic=591.0