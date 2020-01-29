# 5700_ComputerNetwork

# project1:
## I.	Java Socket Programming implementation   
There are two java files: Server.java and Client.java.

Assume that you start a server your localHost (127.0.0.1), listening to port number 32000. 


In your terminal (terminal 1), you should type:

*java Server.java 32000 <enter>* 

and then open a new terminal (terminal 2), and type:

*java Client.java 127.0.0.1 32000 <enter>*

In terminal 2, you can type your message and then hit enter:
For example, if you enter "This is my text to be changed by the SERVER <enter>"

Then in terminal 2, you will get:

"Response from server: revres EHT YB DEGNAHC EB OT TXET YM SI SIHt"

 At this point (after receiving one line to be reversed), the server and client should both exit.