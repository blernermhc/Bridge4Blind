package idAntennaTester;

import gui.GameGUI;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import audio.AudibleGameListener;
import controller.AntennaHandler;
import main.BridgeActualGame;
import main.BridgeMode;
import main.SyncPipe;
import model.CardDatabase;
import model.Game;

public class BridgeGameIDTest  implements BridgeMode {
	
	// path where the .exe is
		private static final String PATH = "C://Users//orche22h//Documents//Bridge-IndependentStudy//Bridge-Workspace//Bridge4BlindNew//Server//bin//Debug//SkyeTekReader";

		// command to start the .exe file
		// the space at the end is important
		private static final String COMMAND = "start ";
		
		//private static Game game ;


		public BridgeGameIDTest() throws IOException, InterruptedException {

			startServer();

			// start the game 15 seconds after starting the C# Server
			TimerTask timerTask = new TimerTask() {

				@Override
				public void run() {

					start();

				}

			};

			Timer timer = new Timer(true);
			timer.schedule(timerTask, 5000);

		}

		@Override
		public void start() {

			try {
				Game ameIDAntennaTest = new GameIDAntennaTest(new AntennaHandler(new CardDatabase()), false);
				ameIDAntennaTest.activateAntennas();
				GameGUI gui = new GameGUI(ameIDAntennaTest);
				ameIDAntennaTest.addListener(new AudibleGameListener());
				ameIDAntennaTest.addListener(gui);

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
		public static void startServer() throws IOException, InterruptedException {

			// got code from
			// http://stackoverflow.com/questions/4157303/how-to-execute-cmd-commands-via-java

			Process process = Runtime.getRuntime().exec("cmd");

			new Thread(new SyncPipe(process.getErrorStream(), System.err)).start();
			new Thread(new SyncPipe(process.getInputStream(), System.out)).start();

			PrintWriter stdin = new PrintWriter(process.getOutputStream());

			// stdin.println("dir");

			stdin.println(COMMAND + PATH);

			stdin.close();

			System.out.println("return code " + process.waitFor());
		}
		
		

//		public static Game getGame() {
//			return game;
//		}

		public static void main(String[] args) throws IOException,
				InterruptedException {

			new BridgeActualGame();
		}

}
