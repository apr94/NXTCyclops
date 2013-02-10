/*Copyright (c) 2011 Aravind Rao

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation 
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT 
 * OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/


import lejos.nxt.*; //imports
import lejos.nxt.comm.*;
import lejos.robotics.navigation.*;
import java.io.*;

public class nxt  {

  BTConnection btc;//a bluetooth connection
  DataInputStream dis;//data input and output sreams
  DataOutputStream dos;
  int command;

public static void main(String [] args)  throws Exception 
{
nxt nxt= new nxt();
nxt.start();
}
	
public void start ()
{Motor.A.setSpeed(70);//set speed of the motors, too fast makes tracking less efficient
Motor.C.setSpeed(70);
  try
     {
	    LCD.drawString("waiting",0,0);
	    LCD.refresh();
	    btc = Bluetooth.waitForConnection();//wait until a connection is opened with the PC
	    LCD.clear();
	    LCD.drawString("connected",0,0);
	    LCD.refresh();  
	    dis = btc.openDataInputStream(); //create the data input and output streams
	    dos = btc.openDataOutputStream();
	while(true)//infinite loop to read in commands from the PC
	{
	 try{
	   command = dis.readInt();//read in command from PC
	    if(command == -1){stop();}//if command is -1, then shutdown the NXT(stop() method)
	    else if(command == 0){ //if command is 0, terminate connection and wait for a new one   
	    dis.close();
	    dos.close();
	    LCD.clear();
	    btc.close();
	    start();//start the process of reconnecting again
	    }
		    
	    else
	    {
	    LCD.clear();
	    LCD.drawInt(command, 0,0);
	    if(command == 37){Motor.A.forward();Motor.C.stop();} //rotate camera left, stop tilting
	    else if(command == 38){Motor.C.forward();Motor.A.stop();}//tilt camera up, stop rotating
	    else if(command == 39){Motor.A.backward();Motor.C.stop();}//rotate camera right, stop tilting
	    else if(command == 40){Motor.C.backward();Motor.A.stop();}//tilt camera down, stop rotating
	    else if(command == -2){Motor.A.stop();}//stop rotating
	    else{Motor.C.stop();}//-3, stop tilting
	    }
	}
	catch(Exception E){}
	}
     }
    catch(Exception E){}
}
	
public void stop()//stop method to shutdown the NXT
{
   try
       {
	   dis.close();//close all connections
	   dos.close();
	   Thread.sleep(100); 
	   LCD.clear();
	   LCD.drawString("closing",0,0);
	   LCD.refresh();
	   btc.close();
	   LCD.clear();
       }
	    
    catch(Exception e){}
}

}

