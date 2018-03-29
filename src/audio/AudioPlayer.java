package audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * <p>
 * ModifiedAudioPlayer is an open source Java class that manages WAV, AIFF and AU audio files
 * playback.<br>
 * It provides basic functionality like play, stop, mute and loop.<br>
 * </p>
 * <p>
 * It's written using the JavaSound, a standard package included in J2SE since version 1.3, so you
 * don't need to distribute any extra package like JMF (JavaMediaFramework) with your
 * application.<br>
 * JavaSound provides the lowest level of audio support on the Java platform and AudioPlayer
 * represent higher-level user interfaces built on top of Java Sound.<br>
 * The Java Sound engine can render 8 or 16 bit audio data, in mono or stereo, with sample rates
 * from 8KHz to 48KHz.
 * </p>
 * <p>
 * AudioPlayer can play file of "any" size with low memory usage. Each AudioPlayer object run as
 * separated thread so your application main flow will not freeze during playback.
 * </p>
 * <p>
 * AudioPlayer is licensed under the terms of the <a href="http://www.gnu.org/licenses/gpl.html">GNU
 * General Public License</a>.
 *
 * @author Code4Fun Team <a href="http://code4fun.org">http://code4fun.org</a> Modified by Melissa
 *         Frechette
 * @version 1.0 , 2005/08/17
 */
public class AudioPlayer implements Runnable
{

	private Thread				m_runner				= null;

	private InputStream			m_audioStream			= null;

	private AudioInputStream		m_audioInputStream	= null;

	private SourceDataLine		m_sourceDataLine		= null;

	private AudioFormat			m_audioFormat			= null;

	private boolean				m_stop				= false;

	private boolean				m_loop				= false;

	private BooleanControl		m_mute;

	private int					m_externalBuffer		= 10000;

	private int					m_internalBuffer		= 8192;

	/**
	 * String that handle errors occurred during initialization.<br>
	 * If init() method fail this variable will be filled with a report.
	 */
	public String				m_error				= "";

	/**
	 * Default class constructor.
	 */
	public AudioPlayer ()
	{
	}

	/**
	 * Initialization routine: check if audio file format is supported and asks the hardware for the
	 * needed resources to play it.<br>
	 * If all steps success the audio file will be ready to play, otherwise an exhaustive
	 * explanation of the occurred error will be stored in the public variable "error".
	 *
	 * @param p_audioFile
	 *            - audio file path
	 * @return <code>true</code> if audio file is successfully loaded; <code>false</code> otherwise.
	 */
	public boolean init ( String p_audioFile )
	{

		// Check if thread is currently executing
		if (isPlaying())
		{

			m_error = "A sound file is already playing for this AudioPlayer "
					+ "object, stop it first.";
			return false;
		}

		// Try to creates a new File instance by converting the given pathname
		/*
		 * try { audioFile = new File(f); } catch (NullPointerException npe) {
		 * 
		 * error = "Null reference passed to init."; return false; }
		 */

		m_audioStream = new BufferedInputStream(getClass().getResourceAsStream(p_audioFile));

		try
		{

			// Obtains an AudioInputStream from the provided InputStream
			m_audioInputStream = AudioSystem.getAudioInputStream(m_audioStream);

			/*
			 * Obtains the audio format of the sound data in this audio input stream
			 */
			m_audioFormat = m_audioInputStream.getFormat();
		}
		catch (IOException e)
		{
			// Failed or interrupted I/O operations
			m_error = "Failed or interrupted I/O operations.";
			e.printStackTrace();
			return false;
		}
		catch (UnsupportedAudioFileException e)
		{
			/*
			 * File did not contain valid data of a recognized file type and format
			 */
			m_error = "Invalid audio file format.";
			return false;
		}

		/*
		 * We have to construct an Info object that specifies the desired properties for the line we
		 * want. First, we have to say what kind of line we want. The possibilities are:
		 * SourceDataLine (for playback), and TargetDataLine (for recording). Here, we ask for a
		 * SourceDataLine. Then, we have to pass an AudioFormat object, so that the Line knows which
		 * format the data passed to it will have. Furthermore, we can give Java Sound a hint about
		 * how big the internal buffer for the line should be.
		 */
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, m_audioFormat,
				m_internalBuffer);

		try
		{

			/*
			 * Obtains a line that matches the description in the specified Line.Info object
			 */
			m_sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

			/*
			 * Opens the line with the specified format and suggested buffer size, causing the line
			 * to acquire any required system resources and become operational
			 */
			m_sourceDataLine.open(m_audioFormat, m_internalBuffer);
		}
		catch (LineUnavailableException e)
		{

			m_error = "Line is not available due to resource restrictions.";
			return false;
		}
		catch (IllegalArgumentException e)
		{

			m_error = "Selected mixer does not support any lines matching " + "the description.";
			return false;
		}

		// Gets mute control from our SourceDataLine
		try
		{
			m_mute = (BooleanControl) m_sourceDataLine.getControl(BooleanControl.Type.MUTE);
		}
		catch (Exception e)
		{

			m_error = e.getMessage();
			return false;
		}

		// Initializing post-load status
		m_stop = false;

		// Initialization successfully completed
		return true;
	}

	/**
	 * Open {@link SourceDataLine}, allows the line to engage in data I/O and start the play
	 * routine.
	 */
	public void run ()
	{
		// System.out.println("in AudioPlayer.run()");
		/*
		 * Opens the line with the specified format and suggested buffer size, causing the line to
		 * acquire any required system resources and become operational
		 */
		if (!m_sourceDataLine.isOpen())
		{

			try
			{

				/*
				 * Opens the line with the specified format and suggested buffer size, causing the
				 * line to acquire any required system resources and become operational
				 */
				m_sourceDataLine.open(m_audioFormat, m_internalBuffer);

				rawplay(); // Start rawplay routine
			}
			catch (LineUnavailableException e)
			{

				m_error = "Line unavailable";
				abort();
			}
		}
		else
		{

			rawplay(); // Start rawplay routine
		}

		// System.out.println("audioplayer's run method exiting");
	}

	/**
	 * Play routine: reads up to a specified maximum number of bytes of data from audioInputStream
	 * and writes them to the mixer via the sourceDataLine until the audioInputStream is empty or
	 * user stop playing.
	 */
	private void rawplay ()
	{

		// Allows the line to engage in data I/O
		m_sourceDataLine.start();

		// Ram external buffer
		byte[] tempBuffer = new byte[m_externalBuffer];
		int cnt;

		try
		{

			while ((cnt = m_audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1 && !m_stop)
			{

				if (cnt > 0)
				{
					m_sourceDataLine.write(tempBuffer, 0, cnt);
				}

			} // end while loop
		}
		catch (IOException e1)
		{

			m_error = "Failed or interrupted I/O operations";
			abort();
		}

		// Reset AudioInputStream to beginning
		try
		{

			m_audioInputStream = AudioSystem.getAudioInputStream(m_audioStream);
			// If loop mode is enabled and audio file is ended restart playing
			if (m_loop && !m_stop) rawplay();
			// Stop all I/O activity, flush line and free resources
			abort();
		}

		catch (UnsupportedAudioFileException e)
		{
			abort();
		}
		catch (IOException e)
		{
			abort();
		}

	} // end rawplay method

	/**
	 * Causes this thread to begin execution.<br>
	 * The Java Virtual Machine calls the run method. If init method is never called or failed this
	 * method does nothing.<br>
	 * If invoked while a thread is already running, this method does nothing.
	 */
	public void play ()
	{

		// rick: simply ignoring play() seems wrong. Removing test for live thread seems to work
		// if (runner == null || !runner.isAlive()) {

		m_runner = new Thread(this, "AudioPlayer");
		m_runner.start();
		// }
	}

	/**
	 * Stop all I/O activity, flush {@link SourceDataLine} and free resources, thread is terminated.
	 */
	private void abort ()
	{

		// stop I/O activity
		m_sourceDataLine.stop();
		// empty sourceDataLine buffer
		m_sourceDataLine.flush();
		// free allocated resources
		m_sourceDataLine.close();
		// make thread available for garbage collection
		m_runner = null;
	}

	/**
	 * Check if audio file is playing.
	 *
	 * @return <code>true</code> if audio file is playing; <code>false</code> otherwise.
	 */
	public boolean isPlaying ()
	{

		if (m_sourceDataLine != null) { return m_sourceDataLine.isRunning(); }
		return false;
	}

	/**
	 * Stop playing.
	 */
	public void stop ()
	{

		if (this.isPlaying()) m_stop = true;
		if (m_runner != null)
		{
			abort();
		}
	}

} // end class