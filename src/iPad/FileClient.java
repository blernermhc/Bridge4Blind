package iPad;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A client for our simple file server that sends the "ls" command to the server and
 * displays the result returned by the server on standard output.
 * 
 * @author Barbara Lerner
 *
 */
public class FileClient {
	public static void main (String[] args) throws UnknownHostException, IOException {
		int numClients= 1;
		
	    //Loop in order to create many clients
		for(int i=0;i<numClients; i++)
		{
		
		// Connect to the server.  127.0.0.1 will look for the server on the same
		// computer the client is running on.  You can test this on multiple computers
		// by changing the IP address here to the one where the server is running.
		// The port number should be the same as you use when creating the ServerSocket 
		// in the server.
		Socket socket = new Socket("138.110.173.186", 5000);
		
		// You will use this to read the output sent by the server
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		// You will use this to send commands to the server.  Be sure to send true for the
		// second parameter to the PrintWriter constructor so that it flushes the output stream
		// on each write.
		PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

		//If this is the last client
		if((i+1)==numClients)
		{
			out.println("quit");
		}
		else
		{
			// Send the ls command
			out.println("ls");
		}

		// Get the output sent from the server and display it.
		String nextLine = in.readLine();
		while (nextLine != null) {
			System.out.println(nextLine);
			nextLine = in.readLine();
		}

		// Close the socket.
		socket.close();
		}
	}
}
