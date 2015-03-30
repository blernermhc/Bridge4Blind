package main;

import java.io.InputStream;
import java.io.OutputStream;

class SyncPipe implements Runnable {
	/**
	 * Needed to give commands to the command line through the Java application
	 * Code from
	 * http://stackoverflow.com/questions/4157303/how-to-execute-cmd-commands
	 * -via-java
	 * 
	 * @param istrm
	 * @param ostrm
	 */
	public SyncPipe(InputStream istrm, OutputStream ostrm) {
		istrm_ = istrm;
		ostrm_ = ostrm;
	}

	public void run() {
		try {
			final byte[] buffer = new byte[1024];
			for (int length = 0; (length = istrm_.read(buffer)) != -1;) {
				ostrm_.write(buffer, 0, length);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final OutputStream ostrm_;
	private final InputStream istrm_;
}