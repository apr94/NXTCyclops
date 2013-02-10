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

import static com.googlecode.javacv.cpp.opencv_core.*;          //imports
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import lejos.pc.comm.*;
import java.io.*;





public class comp {
    
    IplImage img = new IplImage();
    CvCapture capture = cvCreateCameraCapture(0);  //capture video from camera
    int n = 0;
    String features = "haarcascade_frontalface_alt.xml";  //classifiers to detect eyes and face.
    CvHaarClassifierCascade face = new CvHaarClassifierCascade(cvLoad(features));
    CvMemStorage memory = cvCreateMemStorage(0);
    DataOutputStream dos;//data input and output streams for NXT
    DataInputStream dis;
    CvRect e;




    
    
    public static void main(String[] args) throws Exception
    {
        comp comp = new comp();
        comp.connect();
        comp.getVideo();
    }
    
      public void connect()//Connect to NXT
        {

                NXTConnector conn = new NXTConnector();//create a new NXT connector
                boolean connected = conn.connectTo("btspp://"); //try to connect to any NXT over bluetooth


                if (!connected) {
                    System.out.print("Could not connect to NXT");
                        
                }

                else//success!
                {
                
                dos = conn.getDataOut(); //create data input and output streams to send and recieve data
                dis = conn.getDataIn();
                
                
                }

        }
    
    public void getVideo()
    {
    cvNamedWindow("WebCam"); //New Window named webcam
 
    while(n != 'q') //while key pressed is not q.....
    {    Thread thread1 = new Thread(new sender1()); // start a new thread, this thread analyses the video and send commands to the NXT. The main thread gets and displays the video
         thread1.start();
        img = cvQueryFrame(capture); //get an image from the video every 333ms so as to achieve 30 FPS
        if(img == null) break; //no video
        detectFaces(img);//datect the faces in the video, code below
        cvShowImage("WebCam", img);// show the video with the face highlighted
       
         n = cvWaitKey(33); //wait 33ms before displaying next image.
    }
    
    cvDestroyWindow("WebCam"); //after q is pressed, destroy the Window, release memory used by cvCapture and IplImage.
    cvReleaseCapture(capture);
    cvReleaseImage(img);
    
    try{dos.writeInt(-2); //Also send -2 and -3 to NXT, which tells it to stop all its motors.
        dos.flush();
    dos.writeInt(-3);
    dos.flush();}catch(Exception E){}
        
    }
    
    public void detectFaces(IplImage img)
    {

    /* detect faces */
	CvSeq faces = cvHaarDetectObjects(img, face, memory, 2.0, 3, 0); //Store all detetced faces in CvSeq faces.

    /* return if not found */
    if (faces.total() == 0) return; //no faces found :(
    
    e = new CvRect(cvGetSeqElem(faces, 0)); //following code draws a rectangle around each face, as well as a circle divided into 4 quadrants in the middle of the box.
    cvRectangle (img,cvPoint(e.x(), e.y()),cvPoint(e.x() + e.width(), e.y() + e.height()),CvScalar.RED, 1, CV_AA, 0);
    cvCircle(img,cvPoint( (2*e.x()+e.width())/2,(2*e.y()+e.height())/2), e.width()/4, CvScalar.RED, 1, CV_AA, 0);
    cvLine(img,cvPoint((2*e.x()+e.width())/2-e.width()/4, (2*e.y()+e.height())/2), cvPoint((2*e.x()+e.width())/2+e.width()/4, (2*e.y()+e.height())/2), CvScalar.RED, 1, CV_AA, 0);
    cvLine(img,cvPoint((2*e.x()+e.width())/2, (2*e.y()+e.height())/2-e.width()/4), cvPoint((2*e.x()+e.width())/2, (2*e.y()+e.height())/2+e.width()/4), CvScalar.RED, 1, CV_AA, 0);
    

    cvClearMemStorage(memory); // clear memory for next frame.
    
    }
    
    public class sender1 implements Runnable{ //sender thread class
    public void run()
    {
        int x = e.x()-(img.width()-(e.x()+e.width())); //Difference betweeen the distance from the left edge of the facebox to the left edge of the frame 
        //and distance from the right edge of the framebox to the right edge of the frame. If positive, camera is too much to the left, and if negative, camera is too much to the right.
        int y = e.y()-(img.height()-(e.y()+e.height())); //Difference betweeen the distance from the bottom edge of the facebox to the bottom edge of the frame
        //and distance from the top edge of the framebox to the top edge of the frame. If positive, camera is too much to the bottom, and if negative, camera is too much to the top.
                
                
        try{
      if(x > 80) //camera too much to the left.
       {
        dos.writeInt(39); //flush 39, make camera rotate right
        dos.flush();
        dos.writeInt(-3); //flush -3, stop motor that causes tilt
        dos.flush();  
       }
      else if(x < -80)
      {
        dos.writeInt(37);//flush 37, make camera rotate left
        dos.flush();
        dos.writeInt(-3);//flush -3, stop motor that causes tilt
        dos.flush();  
      }
    
      else if(y > 60)
      {
        dos.writeInt(38);//flush 38, make camera tilt up
        dos.flush();
        dos.writeInt(-2);//flush -2, stop motor that causes rotation
        dos.flush();  
      }
           
        else if(y < -60)
        {
        dos.writeInt(40);//flush 40, make camera tilt down
        dos.flush();
        dos.writeInt(-2);//flush -2, stop motor that causes rotation
        dos.flush();  
        }
      
        else //camera position perfect, no need for any changes
        {
            dos.writeInt(-3);//flush -3, stop motor that causes tilt
        dos.flush();
        dos.writeInt(-2);//flush -2, stop motor that causes rotation
        dos.flush();   
        }
       
    }
        
        catch(Exception E){System.out.print("D'oh!");} //catch exception (__8-()
    }
    }
    
  
}
 