package iPad;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * This is a simple server that runs on port 3220.  It understands 2 commands:
 * - user returns to the client the login name of the user who is running the server
 * - os returns to the client the operating system name and version.
 * 
 * @author Barbara Lerner
 * @version September 2010
 * 
 * Edited by Jessie Hamelin 9/22/2012
 *
 */
public class FileServer {
	
	/**
	 * Runs the server in an infinite loop.  The server must be killed to stop it.
	 * @param args None expected
	 * @throws IOException if there is a problem with the socket communication
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		// Attach the server to port 3220
		ServerSocket serverSocket = new ServerSocket(5000);

		// Loop forever accepting connections from clients and processing their requests.
		int requestNum = 0;
		while (true) {
			System.out.println("Waiting in correct program");
			Socket socket = serverSocket.accept();

			System.out.println("Done waiting");
			handleRequest(socket, requestNum);
			//socket.close();
			requestNum++;
		}
	}

	/**
	 * Handle an individual client connection
	 * @param socket the socket to communicate with the client on
	 * @param id a unique id for this connection, used for I/O purposes only
	 */
	private static void handleRequest(Socket socket, int id) throws InterruptedException{

		System.out.println("CONNECTION MADE");
		
		//Create a new childProcess
		Thread tid;

		tid = new Thread (new ChildProc(socket, id));
		tid.start();
		
	}

}