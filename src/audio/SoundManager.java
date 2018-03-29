package audio;

import java.util.ArrayList;

public class SoundManager {
	// the sounds to play
	// Need to synchronize methods that use this variable. Sounds
	// are added to toPlay in one thread and removed in another.
	private ArrayList<String> toPlay = new ArrayList<String>();

	// the sounds last played. Save these so that they can be repeated.
	private ArrayList<String> lastPlayed = new ArrayList<String>();

	// the AudioPlayer used for playing sounds
	private AudioPlayer ap = new AudioPlayer();

	// The thread that the sounds play in.
	private Thread soundThread;

	// The singleton object
	private static SoundManager instance;

	// Set to true to interrupt the sounds being played.
	private boolean stopRequested = false;

	// sets to true when the the thread playing the sound is done playing
	private boolean soundThreadDone = true;

	private SoundManager() {

	}

	public static SoundManager getInstance() {
		if (instance == null) {
			instance = new SoundManager();
		}
		return instance;
	}

	public synchronized void addSound(String filename) {
		// System.out.println("SoundManager.addSound: Thread " +
		// Thread.currentThread().getName() + " got lock in playSounds");
		
		toPlay.add(filename);
		
		// System.out.println("SoundManager.addSound: Thread " +
		// Thread.currentThread().getName() + " releasing lock");
	}

	public void playSounds() {
		// System.out.println("Entering playSounds");
		// System.out.println("SoundManager.playSounds: Thread " +
		// Thread.currentThread().getName() +
		// " trying to get lock in playSounds");
		
		synchronized (this) {
		
			// System.out.println("SoundManager.playSounds: Thread " +
			// Thread.currentThread().getName() + " got lock in playSounds");
			// System.out.println("SoundManager.playSounds got the lock");
			// Do not start a new thread if there is one already running. The
			// existing thread will play all the sounds that are queued up.
			
			if (soundThread != null) {
				// while(soundThread.isAlive()) {

				try {

					while (!soundThreadDone) {
						System.out
								.println("Sleeping playSounds - thread alive");

						this.wait();

					}

				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}

			// Need to keep a separate thread so that it can be stopped.
			// System.out.println("SoundManager creating Sound Player thread");

			soundThread = new Thread("Sound Player") {

				public void run() {
					// System.out.println("SoundManager.playSounds: Thread " +
					// Thread.currentThread().getName() +
					// " trying to get lock in soundThread's run");

					synchronized (SoundManager.this) {

						// System.out.println("SoundManager.playSounds: Thread "
						// + Thread.currentThread().getName() +
						// " got lock in soundThread's run");
						// System.out.println("Running sound thread");

						lastPlayed.clear();
						int nextSound = 0;

						while (nextSound < toPlay.size() && !stopRequested) {

							String nextSoundFile = toPlay.get(nextSound);
							lastPlayed.add(nextSoundFile);
							playNextSound(nextSoundFile);
							nextSound++;
						}

						// reset the next sound
						toPlay.clear();

						// System.out.println("Done running sound thread");

						stopRequested = false;
						
						soundThreadDone = true ;

						SoundManager.this.notify();
						
					}
				}
			};

			soundThreadDone = false;
			soundThread.start();
		}
	}

	// Plays the next sound in line in the toPlay vector.
	private void playNextSound(String nextSoundFile) {
		// initialize the audio player with the correct sound

		debugMsg("Checking if audio player is busy.  ");
		while (ap.isPlaying()) {
			if (Thread.currentThread().isInterrupted()) {
				ap.stop();
				return;
			}
			// System.out.println(Thread.currentThread().getName() +
			// " is yieldeing");
			Thread.yield();

		}
		debugMsg("Audio player is free.  ");

		if (!ap.init(nextSoundFile)) {
			debugMsg("Error initializing player: " + ap.m_error);
			return;
		}

		debugMsg("Player initialized");

		// play the sound
		ap.play();

		debugMsg("Waiting for player to start " + nextSoundFile);

		// Make sure it starts playing
		while (!ap.isPlaying()) {
			// debugMsg("Not started");
			// System.out.println(Thread.currentThread().getName() +
			// " is yieldeing");
			Thread.yield();
		}

		debugMsg("Waiting for player to finish");

		// Wait for the sound to finish
		do {
			if (Thread.currentThread().isInterrupted()) {
				debugMsg("Playing thread interrupted");
				ap.stop();
				return;
			}
			// System.out.println(Thread.currentThread().getName() +
			// " is yieldeing");
			Thread.yield();

		} while (ap.isPlaying());

		debugMsg("Done playing.");
	}

	/**
	 * Repeat the last sound played
	 */
	public void playLastSound() {
		// System.out.println("Entering playLastSound");
		synchronized (this) {
			if (lastPlayed.size() == 0) {
				// System.out.println("Exiting playLastSound - nothing to play");
				return;
			}

			toPlay.addAll(lastPlayed);
		}
		playSounds();
		// System.out.println("Exiting playLastSound");
	}

	public void debugMsg(String msg) {
		System.out.println(msg);
	}

	public void pauseSounds() {

		while (soundThread != null && soundThread.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Continue
			}
		}
	}

	public synchronized void clearSounds() {
		// System.out.println("SoundManager.clearSounds: Thread " +
		// Thread.currentThread().getName() + " got lock in clearSounds");
		toPlay.clear();
		// System.out.println("SoundManager.clearSounds: Thread " +
		// Thread.currentThread().getName() + " releasing lock");
	}

	public boolean isPlaying() {
		return ap.isPlaying();
	}

	public void requestStop() {
		// soundThread.interrupt();
		// System.out.println("stop requested");
		stopRequested = true;
	}

	public static void main(String[] args) {
		SoundManager soundMgr = new SoundManager();

		System.out.println("Should say \"North South team has won 2\"");
		soundMgr.addSound("/sounds/bidding/northsouth.WAV");
		soundMgr.addSound("/sounds/bidding/2.WAV");
		soundMgr.playSounds();
		soundMgr.pauseSounds();

		System.out.println("Should say \"North South team has won 2\"");
		soundMgr.playLastSound();
		soundMgr.pauseSounds();

		soundMgr.clearSounds();
		soundMgr.playSounds();
		soundMgr.pauseSounds();

		System.out.println("Should say \"North South team has won\"");
		soundMgr.addSound("/sounds/bidding/northsouth.WAV");
		soundMgr.addSound("/sounds/bidding/northsouth.WAV");
		soundMgr.addSound("/sounds/bidding/northsouth.WAV");
		soundMgr.addSound("/sounds/bidding/northsouth.WAV");
		soundMgr.addSound("/sounds/bidding/northsouth.WAV");
		soundMgr.addSound("/sounds/bidding/2.WAV");
		soundMgr.playSounds();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {

		}
		soundMgr.requestStop();
	}
}
