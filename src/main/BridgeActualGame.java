package main;

import main.SyncPipe;
import gui.GameGUI;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import controller.AntennaHandler;
import model.CardDatabase;
import model.Game;
import audio.AudibleGameListener;

public class BridgeActualGame implements BridgeMode {

	// path where the .exe is
	private static final String PATH = "C://Users//orche22h//Documents//Bridge-IndependentStudy//Bridge-Workspace//Bridge4Blind//Server//bin//Debug//SkyeTekReader";

	// command to start the .exe file
	// the space at the end is important
	private static final String COMMAND = "start ";

	public BridgeActualGame() {

		startServer();

		// start the game 15 seconds after starting the C# Server
		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {

				start();

			}

		};

		// wait 4 seconds after starting the server to start the game
		Timer timer = new Timer(true);
		timer.schedule(timerTask, 4000);

	}

	@Override
	public void start() {

		try {
			Game game = new Game(new AntennaHandler(new CardDatabase()), false);
			game.activateAntennas();
			GameGUI gui = new GameGUI(game);
			game.addListener(new AudibleGameListener());
			game.addListener(gui);

			gui.debugMsg("main run");

		} catch (UnknownHostException e) {
			System.err.println("Could not connect to server.  Host unknown.");
		} catch (ConnectException connectExc) {
			System.err.println("The server is not running!");
		} catch (SocketException socketEsc) {
			System.err
					.println("Check that there is no virus scanner blocking IRC connections.");
			socketEsc.printStackTrace();
		} catch (IOException e) {
			System.err.println("Could not connect to server.");
			e.printStackTrace();
		}

	}

	/**
	 * Starts the C# Server
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void startServer() {

		System.out.println("Starting Server");

		// got code from
		// http://stackoverflow.com/questions/4157303/how-to-execute-cmd-commands-via-java
		try {
			Process process;

			process = Runtime.getRuntime().exec("cmd");

			new Thread(new SyncPipe(process.getErrorStream(), System.err))
					.start();
			new Thread(new SyncPipe(process.getInputStream(), System.out))
					.start();

			PrintWriter stdin = new PrintWriter(process.getOutputStream());

			// stdin.println("dir");

			stdin.println(COMMAND + PATH);

			stdin.close();

			System.out.println("return code " + process.waitFor());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public static void closeServerWindow() {
		try {

			Runtime.getRuntime().exec("taskkill /f /im " + "SkyeTekReader.exe");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		new BridgeActualGame();
	}

}
