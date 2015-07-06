**This will show how to run the examples in the Test folder of the jgn.jar download using windows XP.**

To run the examples in the test folder located in jgn.jar download, which should be unarchived using winRar or other compatible archiving program.
  * Copy the JGN.jar to your java root folder, for example, C:\Program Files\Java\jdk1.6.0\_10\bin.
  * Now add the path, C:\Program Files\Java\jdk1.6.0\_10\bin\jgn.jar to the classpath variable in MyComputer->properties->advanced->enviroment Variables.
    * click on the classpath variable
    * click edit
    * then paste the above path to the end of whatevers there
    * add a semicolon (;) to the end.

**For starting the server in the jgn\com\captiveimagination\jgn\test\clientserver\vm folder**
  * Create a batch file by opening a blank text document in notepad and adding the following lines to it...
> java com.captiveimagination.jgn.test.clientserver.vm.BasicServer<br>
<blockquote>pause<br>
</blockquote><ul><li>Now save it in the vm folder to test the BasicServer.class program as BasicServ.bat.<br>
</li><li>after double clicking the BasicServ.bat, the server started.</li></ul>

<b>For starting the client in jgn\com\captiveimagination\jgn\test\clientserver\vm folder</b>
<ul><li>Create another batch file as outline above and put the following lines in it...<br>
</li></ul><blockquote>java com.captiveimagination.jgn.test.clientserver.vm.BasicClient<br>
pause<br>
</blockquote><ul><li>Now save it in the vm folder to test the BasicClient.class program as BasicClient.bat<br>
</li><li>after double clicking the BasicClient.bat, the client started, the server will catch it and everything will be merry andjoyous!