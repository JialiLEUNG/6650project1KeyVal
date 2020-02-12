Compilation and Running Instructions
Compiling the code
Use javac to compile all 4 programs.
 > javac Sorter.java
 > javac SorterImpl.java
 > javac SorterServer.java
 > javac SorterClient.java
Now use rmic to create the stub and skeleton class files.
 > rmic SorterImpl
Running the RMI System

You are now ready to run the system! You need to start three consoles, one for the server, one for the client, and one for the RMIRegistry.

Start with the Registry. You must be in the directory that contains the classes you have written. From there, enter the following:

 > rmiregistry

If all goes well, the registry will start running and you can switch to the next console.

In the second console start the server hosting the SorterService, and enter the following:

 > java SorterServer
It will start, load the implementation into memory and wait for a client connection.

In the last console, start the client program.

 > java SorterClient
If all goes well you will see the following output:

Original array: 10 2 3 4 5 6 7 8 9 1 
Sorted Array: 1 2 3 4 5 6 7 8 9 10 

That's it; you have created a working RMI system. Even though you ran the three consoles on the same computer, RMI uses your network stack and TCP/IP to communicate between the three separate JVMs. This is a full-fledged RMI system.