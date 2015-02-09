package iPad;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;


public class ChildProc implements Runnable {
	private int id;
	private Socket socket;

	//Constructor
	public ChildProc(Socket socket, int id)
	{
		this.socket = socket;
		this.id = id;
	}


	@Override
	public void run()
	{
		System.out.println("Starting to handle request " + id);
		
		try {
			// Set up the streams to allow 2-way communication with the client.
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);		

			while(true){
				// Get and execute the client's commands.
				
				//Waits about 9 seconds
				Thread.currentThread().sleep(10000);
				
				//Send the message
				out.print("DH2,3,4/EW1/B4 of Hearts/");
				out.flush();
				
				//Waits about 9 seconds
				Thread.currentThread().sleep(10000);
				
				out.print("DH4/DC45/EW2/NS1/C5 of Hearts/");
				out.flush();
				
			}

			//			in.close();
			//			out.close();

		} catch (Exception e) {
			// Give up on this client connection if there is an exception.
			System.err.println("Connection with client " + id + " failed.");
			return;
		}
		

//		try{
//			socket.close();
//		} catch (IOException e1){
//			System.out.println("Error with closing socket.");
//		}
//		
//		try{
//			Thread.sleep(1000);
//		}
//		catch(InterruptedException e2){
//			System.out.println("Error with Thread.sleep()");
//		}
		
//		System.out.println("Done handling request " + id);
//		numConnections--;
//		}
//		else{
//			System.out.println("Connection denied. There are already 5 connections running.");
//		}
		}
}
