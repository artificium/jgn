You can get the latest source code by typing, "svn co http://jgn.googlecode.com/svn/ jgn," on your command line.  If you don't have SVN, Google will help you get it.

I'm not going to lie: when I began using JGN, I had a fair amount of trouble getting JGN's jme-networking component to compile.  Perhaps I made a critical oversight of some sort; or perhaps the jme-networking source code has become out-of-sync with JME's development.  In any event, several dependencies did not go where they were supposed to go during the build process; and I had to place them there manually.  This page will probably not serve as an exhaustive list, but rather as a list of all the gotchas that I can remember at the moment.  Someone who knows more about the process (and perhaps can see that I did indeed make a critical oversight) should edit this page and give a simpler way to build the jme-networking component.

1. The build.xml file in jgn/sychronization/jme-networking/trunk tries to place things in a lib directory which doesn't exist.  You'll have to make it yourself. ("jgn/sychronization/jme-networking/trunk/lib").

2. Hundreds of compile errors will result if you try to build at this point.  You need the following in your newly created lib directory:
> A. The file magicbeans.jar.  You can find this file in jgn/core/trunk/lib/magicbeans.jar.
> B. The classes from jmetest.  I assume you've already built the JME source code.  If so, plop the jmetest directory into the jgn/sychronization/jme-networking/trunk/lib directory.
> C. Junit stuff.  Get it online.  Google will help.
> D. All of the jme classes, including those in com.jme and com.jmex.  Note: the build.xml file downloads JME's nightly build--a file called jme.jar.  This file, to my knowledge, doesn't contain everything you need.  I solved the problem by sticking all the code from JME's build directory (again, I assume you already have JME working) into the lib directory.  I think I even had to comment out the line in the build.xml file that downloaded JME's nightly build.  But perhaps you don't need to do this.

3. After everything compiles, you still need to copy the resources folder from the jgn/sychronization/jme-networking/trunk/src directory into the jgn/sychronization/jme-networking/trunk/bin directory in order to run the FlagRushTestServer and FlagRushTestClient.