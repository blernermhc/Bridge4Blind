package lerner.blindBridge.hardware;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.log4j.Category;

import gnu.io.SerialPortEvent;
import lerner.blindBridge.main.Game;
import lerner.blindBridge.model.BridgeScore;
import lerner.blindBridge.model.Card;
import lerner.blindBridge.model.Contract;
import lerner.blindBridge.model.Direction;
import lerner.blindBridge.model.ErrorCode;
import lerner.blindBridge.model.Rank;
import lerner.blindBridge.model.Suit;

/**********************************************************************
 * Communicates with a Blind players' Keyboard Controller 
 *********************************************************************/

public class KeyboardController extends SerialController implements Runnable
{
	/**
	 * Used to collect logging output for this class
	 */
	private static Category s_cat = Category.getInstance(KeyboardController.class.getName());

	public enum Button {
		PLAY
		, H1, H2, H3, H4, HC
		, D1, D2, D3, D4, DC
		, UP, DOWN
		, REPEAT, STATE
		, FUNCTION
	};
	
	/**
	 *********************************************************************
	 * Parameter-less Messages to send to Keyboard Controller.
	 * The first parameter is the ID of the event to send in the message.
	 * The second parameter is the time in milliseconds we expect the
	 * audio associated with the event will take to play.
	 * The system ensures that once a message is sent, the next message
	 * will be sent no sooner than allowed by the reserve. 
	 **********************************************************************
	 */
	public enum KBD_MESSAGE {
		NOOP						(0, 0)
		, SCAN_HAND				(1, 1500)
		, SCAN_DUMMY				(2, 1500)
		// 3 not used
		, REMIND_PLAY			(4, 1000)
		, REMIND_DUMMY			(5, 1000)
		, HAND_SCAN_COMPLETE		(6, 1500)
		, HAND_COMPLETE			(7, 4000)
		// 8 not used
		, NEW_GAME				(9, 0)
		, NEW_HAND				(10, 0)
		, START_RELOAD			(11, 1500)
		, FINISH_RELOAD			(12, 1500)
		, ENTER_CONTRACT			(13, 1500)
		, CANNOT_PLAY_ALREADY_PLAYED		(14, 1500)
		, CANNOT_PLAY_NOT_IN_HAND		(15, 1500)
		, SEND_POSITION			(16, 1500)
		;
		
		private int m_msgId;
		private int m_reserveInMillis;
		
		KBD_MESSAGE (int p_msgId, int p_reserveInMillis)
		{
			m_msgId				= p_msgId;
			m_reserveInMillis	= p_reserveInMillis;
		}
		
		public int getMsgId() { return m_msgId; }
		public int getReserveInMillis() { return m_reserveInMillis; }
	};

	/**
	 *********************************************************************
	 * Multi-byte Messages to send to Keyboard Controller.
	 * The first parameter is the ID of the event to send in the message.
	 * The second parameter is the time in milliseconds we expect the
	 * audio associated with the event will take to play.
	 * The system ensures that once a message is sent, the next message
	 * will be sent no sooner than allowed by the reserve. 
	 **********************************************************************
	 */
	public enum MULTIBYTE_MESSAGE {
		NOOP					(0, 0)
		, SET_PLAYER			(1, 2000)
		, SET_DUMMY			(2, 2000)
		, SET_NEXT_PLAYER	(3, 2250)
		, SET_CONTRACT		(4, 3000)
		, ADD_CARD_TO_HAND	(5, 1500)
		, PLAY_CARD			(6, 3000)
		, UNPLAY_CARD		(7, 3000)
		, TRICK_TAKEN		(8, 1500)
		, CANNOT_PLAY_WRONG_SUIT		(9, 4000)
		, UNDO				(15, 4000)
		;
		
		private int m_msgId;
		private int m_reserveInMillis;
		
		MULTIBYTE_MESSAGE (int p_msgId, int p_reserveInMillis)
		{
			m_msgId				= p_msgId;
			m_reserveInMillis	= p_reserveInMillis;
		}
		
		public int getMsgId() { return m_msgId; }
		public int getReserveInMillis() { return m_reserveInMillis; }
	};

	/**
	 *********************************************************************
	 * Multi-byte Messages to send to Keyboard Controller.
	 * The first parameter is the ID of the event to send in the message.
	 * The second parameter is the time in milliseconds we expect the
	 * audio associated with the event will take to play.
	 * The system ensures that once a message is sent, the next message
	 * will be sent no sooner than allowed by the reserve. 
	 **********************************************************************
	 */
	public enum UNDO_EVENT {
		NOOP					(0, 0)
		, NEW_HAND			(1, 2000)
		, DEAL_HANDS			(2, 2000)
		, SCAN_CARD			(3, 2250)
		, SCAN_HAND			(4, 3000)
		, SET_CONTRACT		(5, 1500)
		;
		
		private int m_msgId;
		private int m_reserveInMillis;
		
		UNDO_EVENT (int p_msgId, int p_reserveInMillis)
		{
			m_msgId				= p_msgId;
			m_reserveInMillis	= p_reserveInMillis;
		}
		
		public int getMsgId() { return m_msgId; }
		public int getReserveInMillis() { return m_reserveInMillis; }
	};
	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	/** A name to describe this class */
	public static final String CONTROLLER_NAME = "Keyboard";
	
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;

	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	/** Start of messages sent by the Keyboard Hardware during boot-up or firmware reset */
	private static final String IDENT_MSG = "Keyboard(";

	/** End of message sent by the Keyboard Hardware at start of boot-up or firmware reset */
	private static final String RESET_MSG = ": Resetting";

	/** End of final message sent by the Keyboard Hardware during boot-up or firmware reset */
	private static final String READY_MSG = ": Ready!";

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** 
	 * If true, read ASCII newline-terminated messages from
	 * the Keyboard Controller until the final setup message is received.
	 * Then switch back to byte-message mode.
	 * <p>
	 * Start with true, since connecting to the USB port triggers
	 * an Arduino reset.
	 */
	boolean m_readLineEventMode = true;
	
	/**
	 * If true, read one ASCII newline-terminated message from
	 * the Keyboard Controller and then switch back to byte message
	 * mode.
	 */
	boolean m_readOneLineEventMode = false;
	
	/** The position of the partner of this controller */
	Direction m_myPartnersPosition;
	
	/** The position of the dummy */
	Direction m_dummyPosition;
	
	/**
	 * If true, enable delays between messages to Arduino to allow audio to complete.
	 * IMPORTANT NOTE: this member is set/read only within the message queue sending thread.
	 */
	private boolean m_reserveAudioPlaybackTime = true;
	
	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	/***********************************************************************
	 * Represents a message to be sent to the hardware.  Used to queue messages
	 * so a single thread serializes the message.
	 ***********************************************************************/
	class KbdMsg {};
	
	/***********************************************************************
	 * Subclass of KbdMsg that contains queue control messages.
	 * For example, to enable and disable delays between messages.
	 ***********************************************************************/
	class KbdMsg_control extends KbdMsg
	{
		boolean reserveAudioPlaybackTime;
		
		public KbdMsg_control ( boolean p_enable ) { reserveAudioPlaybackTime = p_enable; }
		
		public String toString()
		{
			StringBuilder out = new StringBuilder();
			out.append("KbdMsg_control:");
			out.append("\n  reserveAudioPlaybackTime: " + reserveAudioPlaybackTime);
			out.append("\n");
			
			return out.toString();
		}
	};
	
	/***********************************************************************
	 * Subclass of KbdMsg that contains data to be sent to the hardware.
	 ***********************************************************************/
	class KbdMsg_data extends KbdMsg
	{
		int[] byteValues;
		int reserve;
		String description;
		
		public KbdMsg_data (int p_byte0, int p_reserve, String p_description)
		{
			byteValues = new int[] { p_byte0 };
			reserve = p_reserve;
			description = p_description;
		}
		
		public KbdMsg_data (int p_byte0, int p_byte1, int p_reserve, String p_description)
		{
			byteValues = new int[] { p_byte0, p_byte1 };
			reserve = p_reserve;
			description = p_description;
		}
		
		public KbdMsg_data (int p_byte0, int p_byte1, int p_byte2, int p_reserve, String p_description)
		{
			byteValues = new int[] { p_byte0, p_byte1, p_byte2 };
			reserve = p_reserve;
			description = p_description;
		}
		
		public String toString()
		{
			StringBuilder out = new StringBuilder();
			out.append("KbdMsg_data:");
			out.append("\n  description: " + description);
			out.append("\n  numBytes: " + byteValues.length);
			for (int i = 0; i < byteValues.length; ++i)
			{
				out.append("\n  byte" + i + ": " + byteValues[i] + " (" + Integer.toBinaryString(byteValues[i]) + ")");
			}
			out.append("\n  reserve: " + reserve);
			out.append("\n");
			
			return out.toString();
		}
	};
	
	/** Queue of messages to send to the keyboard controller hardware */
	Queue<KbdMsg>	m_sendMessageQueue = new ArrayDeque<>();;

	/** Maximum delay to insert to ensure reserve time between messages (in milliseconds) */
	private static final long MAX_RESERVE_DELAY_MILLIS = 5000;  

	/**
	 * Time last message was sent to the Keyboard Controller. 
	 * This is used to ensure that we space messages to the Keyboard
	 * Controller sufficiently to allow the blind player time to hear the messages.
	 */
	private long m_timeOfLastMessage = System.currentTimeMillis();
	
	/**
	 * The amount of time to reserve to ensure that the next message can be heard
	 * IMPORTANT NOTE: this member is set/read only within the message queue sending thread.
	 */
	private long m_messageReserveMillis = 0;
	
	/** thread to serialize actual sending of messages to the controller */
	private Thread 			m_thread;

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	/***********************************************************************
	 * Configures and initializes a Keyboard Controller
	 * @param p_game		The object managing the hands
	 * @param p_direction		If non-null, the player position this controller is at.
	 * 							If null, attempts to set position based on hardware settings.
	 * @param p_deviceName		If non-empty, open the device with this name for this controller
	 * 							If null or empty, try each device in turn until you find an appropriate one
	 * @throws IOException if it cannot open a port for this controller.
	 ***********************************************************************/
	public KeyboardController(Game p_game, Direction p_direction, String p_deviceName)
		throws IOException
	{
		super(p_game, p_direction, p_deviceName, true);

		//--------------------------------------------------
		// Start thread to send queued messages to keyboard controller hardware
		//--------------------------------------------------
	    m_thread = new Thread (this);
	    m_thread.start();

	    if (p_direction != null)
	    {
	    		setPlayer(p_direction);
	    }
	}

	//--------------------------------------------------
	// CONFIGURATION METHODS (used by findPortToOpen)
	//--------------------------------------------------

	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getName()
	 */
	public String getName() { return CONTROLLER_NAME; }
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getPortOpenTimeout()
	 */
	public int getPortOpenTimeout() { return TIME_OUT; }

	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getPortDataRate()
	 */
	public int getPortDataRate() { return DATA_RATE; }

	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getIdentMsg()
	 */
	public String getIdentMsg() { return IDENT_MSG; }
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getResetMsg()
	 */
	public String getResetMsg() { return RESET_MSG; }
	
	/* (non-Javadoc)
	 * @see lerner.blindBridge.hardware.SerialController#getReadyMsg()
	 */
	public String getReadyMsg() { return READY_MSG; }
	
	/***********************************************************************
	 * Sends a message to the Keyboard Controller to indicate the playerId of the keyboard controller.
	 * (i.e., which position is the blind player playing)
	 * Logs an error if the message fails.
	 * @param p_direction	the position
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean setPlayer(Direction p_direction)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("setPlayer: entered" + " player: " + p_direction);

		m_myPosition = p_direction;
		m_myPartnersPosition = p_direction.getPartner();
		
		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.SET_PLAYER
		                                        , 0
		                                        , p_direction.ordinal()
		                                        , 0
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("setPlayer: finished");
		return status;
	}
	
	/***********************************************************************
	 * Resends a message to the Keyboard Controller to indicate the playerId of the keyboard controller.
	 * (i.e., which position is the blind player playing)
	 * Logs an error if the message fails.
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean setPlayer()
	{
		if (m_myPosition == null)
		{
			s_cat.error("setPlayer: myPosition is not set.");
			return false;
		}

		if (s_cat.isDebugEnabled()) s_cat.debug("setPlayer: setting player to: " + m_myPosition);
		return setPlayer(m_myPosition);
	}
	
	/***********************************************************************
	 * Changes state to DETERMINE_POSITION to use card scans to specify
	 * the antenna's position.
	 ***********************************************************************/
	public void requestPosition()
	{
		m_myPosition = null;
		send_simpleMessage(KBD_MESSAGE.SEND_POSITION);
	}
	
	
	//--------------------------------------------------
	// MESSAGE QUEUE METHODS FOR SENDING EVENTS TO Keyboard Controller
	//--------------------------------------------------
	
	/***********************************************************************
	 * Queues a message to be sent to the Keyboard Controller hardware
	 * @param p_msg the message
	 ***********************************************************************/
	public void queueMessage (KbdMsg p_msg)
	{
		synchronized (m_sendMessageQueue)
		{
			m_sendMessageQueue.add(p_msg);
			if (s_cat.isDebugEnabled()) s_cat.debug("queueMessage: (" + m_messageReserveMillis + ") queued message: " + p_msg);
			m_sendMessageQueue.notify();
		}
	}
	
	/***********************************************************************
	 * Waits for messages to be queued and sends them 
	 ***********************************************************************/
	public void sendQueuedMessages ()
	{
		while (true)
		{
			KbdMsg msg;

			synchronized (m_sendMessageQueue)
			{
				msg = m_sendMessageQueue.poll();
			}

			if (msg != null)
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("sendQueuedMessages: (" + m_messageReserveMillis + ") dequeued message: " + msg);
				
				if (msg instanceof KbdMsg_control)
				{
					// process control messages locally
					m_reserveAudioPlaybackTime = ((KbdMsg_control)msg).reserveAudioPlaybackTime;
				}
				else if (msg instanceof KbdMsg_data)
				{
					KbdMsg_data dmsg = (KbdMsg_data)msg;
					// send data messages to Arduino
					
					if (m_reserveAudioPlaybackTime)
					{
						ensureMessageReserve(dmsg.reserve);
					}

					try
					{
						for (int byteValue : dmsg.byteValues)
						{
							m_output.write((byte)byteValue);
						}
					}
					catch (Exception e)
					{
						s_cat.error("sendQueuedMessages: failed to send message: " + msg, e);
					}
				}
			}
			
			synchronized (m_sendMessageQueue)
			{
				if (m_sendMessageQueue.peek() == null)
				{
					try
					{
						m_sendMessageQueue.wait();
					}
					catch (InterruptedException e)
					{
						s_cat.error("sendQueuedMessages: wait interrupted", e);
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * Invoked in new thread started in constructor 
	 */
	public void run ()
	{
		sendQueuedMessages();
	}
	
	//--------------------------------------------------
	// PUBLIC METHODS TO SEND EVENTS TO Keyboard Controller
	//--------------------------------------------------
	
	/***********************************************************************
	 * Queues a message to the m_sendMessageQueue to enable or disable
	 * delays to allow audio announcement to complete before starting the
	 * next message.
	 * @param p_enable	true to enable delays, false to disable (e.g., during reset)
	 ***********************************************************************/
	public void send_reserveAudioPlaybackTime ( boolean p_enable )
	{
		queueMessage(new KbdMsg_control(p_enable));
	}
	
	/***********************************************************************
	 * Sends the reload finished message and marks the device as ready.
	 ***********************************************************************/
	public void send_reloadFinished ()
	{
		if (m_myPosition != null) m_deviceReady = true;	// wait until position is known
		send_simpleMessage(KBD_MESSAGE.FINISH_RELOAD);
	}
	
	/***********************************************************************
	 * Sends a parameter-less message to the Keyboard Controller
	 * Logs an error if the message fails.
	 * @param p_msg	the message to send
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_simpleMessage(KBD_MESSAGE p_msg)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_simpleMessage: entered"
		                                        + " msg: " + p_msg);
		boolean status	= true;
		int msg			= p_msg.getMsgId();
		int reserve		= p_msg.getReserveInMillis();
		
		queueMessage(new KbdMsg_data(msg, reserve, p_msg.toString()));

		return status;
	}
	
	/***********************************************************************
	 * Sends a generic multi-byte message to the Keyboard Controller.
	 * Logs an error if the message fails.
	 * @param p_msg		the message to send
	 * @param p_direction	the player position to send in the message
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_multiByteMessage(MULTIBYTE_MESSAGE p_msg, Direction p_direction)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage(p): entered"
		                                        + " msg: " + p_msg
		                                        + " player: " + p_direction
		                                        );
		boolean status = send_multiByteMessage(p_msg, 0, p_direction.ordinal(), 0);
		
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage(p): finished");
		return status;
	}
	
	/***********************************************************************
	 * Sends a generic multi-byte message to the Keyboard Controller.
	 * Logs an error if the message fails.
	 * @param p_msg		the message to send
	 * @param p_direction	the player position to send in the message
	 * @param p_card		a card to include in the message
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_multiByteMessage(MULTIBYTE_MESSAGE p_msg, Direction p_direction, Card p_card)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage(pc): entered"
		                                        + " msg: " + p_msg
		                                        + " player: " + p_direction
		                                        + " card: " + p_card
		                                        );
		boolean status = send_multiByteMessage( p_msg
		                                        , p_card.getSuit().ordinal()
		                                        , p_direction.ordinal()
		                                        , p_card.getRank().ordinal()
		                                        );
		
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage(pc): finished");
		return status;
	}
	
	/***********************************************************************
	 * Sends a message to the Keyboard Controller indicating the current contract.
	 * Logs an error if the message fails.
	 * @param p_contract		the contract
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_contract (Contract p_contract)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("sendContract: entered" + " contract: " + p_contract);

		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.SET_CONTRACT
		                                        , p_contract.getTrump().ordinal()
		                                        , p_contract.getBidWinner().ordinal()
		                                        , p_contract.getContractNum()
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("sendContract: finished");
		return status;
	}
			
	/***********************************************************************
	 * Sends a message to the Keyboard Controller indicating that the requested
	 * card cannot be played.  The suit does not match the current suit and the
	 * hand contains a card with the necessary suit.
	 * Logs an error if the message fails.
	 * @param p_direction	player playing card
	 * @param p_card			card played
	 * @param p_currentSuit	current suit
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_cannotPlay (Direction p_direction, Card p_card, Suit p_currentSuit)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_cannotPlay: entered"
												+ " card: " + p_card
												+ " curSuit: " + p_currentSuit
												);

		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.CANNOT_PLAY_WRONG_SUIT
		                                        , p_currentSuit.ordinal()
		                                        , p_direction.ordinal()
		                                        , 0
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("send_cannotPlay: finished");
		return status;
	}
		
	/***********************************************************************
	 * Sends a message to the Keyboard Controller announcing an undo or redo event.
	 * @param p_confirmed	if true, message is confirming that an undo or redo event was processed
	 * 						if false, message is asking for confirmation before processing the undo or redo event.
	 * @param p_redoFlag		if true, this is a redo. Otherwise, this is an undo. 
	 * @param p_undoEvent	the event being undone or redone.
	 * @param p_direction	position of player involved in event
	 * @param p_card			card involved in event
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_undoAnnouncement (boolean p_confirmed, boolean p_redoFlag, UNDO_EVENT p_undoEvent, Direction p_direction, Card p_card)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_undoAnnouncement: entered"
				+ " confirmed: " + p_confirmed
				+ " undoEvent: " + p_undoEvent
				+ " direction: " + p_direction
				+ " card: " + p_card
				);

		int undoMsg = 0;
		if (p_confirmed)		undoMsg |= 0b10000000;	// confirmed
		if (p_redoFlag)		undoMsg |= 0b01000000;	// redo rather than undo
		undoMsg |= p_undoEvent.getMsgId();			// add undo event id
		
		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.UNDO
		                                        , p_card.getSuit().ordinal()
		                                        , p_direction.ordinal()
		                                        , p_card.getRank().ordinal()
		                                        , undoMsg
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("send_undoAnnouncement: finished");
		return status;
	}
	
	/***********************************************************************
	 * Sends a message to the Keyboard Controller announcing an undo or redo event.
	 * @param p_confirmed	if true, message is confirming that an undo or redo event was processed
	 * 						if false, message is asking for confirmation before processing the undo or redo event.
	 * @param p_redoFlag		if true, this is a redo. Otherwise, this is an undo. 
	 * @param p_undoEvent	the event being undone or redone.
	 * @param p_contract		the contract involved in the event
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_undoAnnouncement (boolean p_confirmed, boolean p_redoFlag, UNDO_EVENT p_undoEvent, Contract p_contract)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_undoAnnouncement: entered"
				+ " confirmed: " + p_confirmed
				+ " undoEvent: " + p_undoEvent
				+ " contract: " + p_contract
				);

		int undoMsg = 0;
		if (p_confirmed)		undoMsg |= 0b10000000;	// confirmed
		if (p_redoFlag)		undoMsg |= 0b01000000;	// redo rather than undo
		undoMsg |= p_undoEvent.getMsgId();			// add undo event id
		
		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.UNDO
		                                        , p_contract.getTrump().ordinal()
		                                        , p_contract.getBidWinner().ordinal()
		                                        , p_contract.getContractNum()
		                                        , undoMsg
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("send_undoAnnouncement: finished");
		return status;
	}
	
	/***********************************************************************
	 * Sends a message to the Keyboard Controller announcing an undo or redo event.
	 * @param p_confirmed	if true, message is confirming that an undo or redo event was processed
	 * 						if false, message is asking for confirmation before processing the undo or redo event.
	 * @param p_redoFlag		if true, this is a redo. Otherwise, this is an undo. 
	 * @param p_undoEvent	the event being undone or redone.
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_undoAnnouncement (boolean p_confirmed, boolean p_redoFlag, UNDO_EVENT p_undoEvent)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_undoAnnouncement: entered"
				+ " confirmed: " + p_confirmed
				+ " undoEvent: " + p_undoEvent
				);

		int undoMsg = 0;
		if (p_confirmed)		undoMsg |= 0b10000000;	// confirmed
		if (p_redoFlag)		undoMsg |= 0b01000000;	// redo rather than undo
		undoMsg |= p_undoEvent.getMsgId();			// add undo event id
		
		boolean status = send_multiByteMessage( MULTIBYTE_MESSAGE.UNDO
		                                        , 0
		                                        , 0
		                                        , 0
		                                        , undoMsg
                								  );

		if (s_cat.isDebugEnabled()) s_cat.debug("send_undoAnnouncement: finished");
		return status;
	}
	
	/***********************************************************************
	 * Sends a generic two-byte message to the Keyboard Controller.
	 * Logs an error if the message fails.
	 * @param p_msg	the message to send
	 * @param p_suit		the suit value (3 bits)
	 * @param p_player	the player position value (4 bits)
	 * @param p_cardNumber	the number of the card (4 bits - 0:Two, 1:Three, ..., 12:Ace)
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_multiByteMessage(MULTIBYTE_MESSAGE p_msg, int p_suit, int p_player, int p_cardNumber)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage: entered"
                + " msg: " + p_msg
                + " suit: " + p_suit
                + " player: " + p_player
                + " cardNum: " + p_cardNumber
                );
		
		boolean status	= true;
		int opId			= p_msg.getMsgId();
		int reserve		= p_msg.getReserveInMillis();

		queueMessage(new KbdMsg_data( ((0b10000000) | ((opId & 0b1111) << 3) | (p_suit & 0b111))
		                              , (((p_player & 0b1111) << 4) | (p_cardNumber & 0b1111))
		                              , reserve
		                              , p_msg.toString()));

		return status;
	}
	
	/***********************************************************************
	 * Sends a generic multi-byte message to the Keyboard Controller.
	 * Logs an error if the message fails.
	 * @param p_msg	the message to send
	 * @param p_suit		the suit value (3 bits)
	 * @param p_player	the player position value (4 bits)
	 * @param p_cardNumber	the number of the card (4 bits - 0:Two, 1:Three, ..., 12:Ace)
	 * @param p_addlByte		an additional byte of data to send (ignore if -1)
	 * @return true if sent, false if failed
	 ***********************************************************************/
	public boolean send_multiByteMessage(MULTIBYTE_MESSAGE p_msg, int p_suit, int p_player, int p_cardNumber, int p_addlByte)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage: entered"
		                                        + " msg: " + p_msg
		                                        + " suit: " + p_suit
		                                        + " player: " + p_player
		                                        + " cardNum: " + p_cardNumber
		                                        + " addlByte: " + p_addlByte
		                                        );
		boolean status	= true;
		int opId			= p_msg.getMsgId();
		int reserve		= p_msg.getReserveInMillis();

		queueMessage(new KbdMsg_data( ((0b10000000) | ((opId & 0b1111) << 3) | (p_suit & 0b111))
		                         , (((p_player & 0b1111) << 4) | (p_cardNumber & 0b1111))
		                         , p_addlByte
		                         , reserve
		                         , p_msg.toString()));

		return status;
	}
	
	/***********************************************************************
	 * Simulates a key press on the Keyboard Controller.
	 * (for testing the Keyboard Controller software).
	 * @param p_buttonName the name of the button to be pressed
	 ***********************************************************************/
	public boolean send_pressButton(String p_buttonName)
	{
		if (s_cat.isDebugEnabled()) s_cat.debug("send_multiByteMessage: entered: button: " + p_buttonName);

		boolean status	= true;
		try
		{
			Button button = Button.valueOf(p_buttonName.toUpperCase());
			
			queueMessage(new KbdMsg_data( (0b01000000 | button.ordinal()), 0, button.toString())); // resulting audio can be interrupted, so no reserve
		}
		catch (Exception e)
		{
			s_cat.error("pressButton: failed to press: " + p_buttonName, e);
			status = false;
		}

		return status;
	}

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------

	/***********************************************************************
	 * Ensures that the previous message reserve is met, sleeping if necessary.
	 * @param p_newReserve	time to reserve for next message, in milliseconds.
	 * @return true if a delay was introduced and false otherwise
	 ***********************************************************************/
	private boolean ensureMessageReserve (int p_newReserve)
	{
		long curTime = System.currentTimeMillis();
		long delayTime = ((m_timeOfLastMessage + m_messageReserveMillis) - curTime);
		boolean delayed = false;
		if (delayTime < 100) delayTime = 100;	// don't flood serial line?
		if (delayTime > 0)
		{
			if (delayTime > MAX_RESERVE_DELAY_MILLIS) delayTime = MAX_RESERVE_DELAY_MILLIS;
			try
			{
	            Thread.sleep(delayTime);
	        }
			catch (InterruptedException e)
			{
	            e.printStackTrace();
	        }
			delayed = true;
		}
		m_timeOfLastMessage = System.currentTimeMillis();
		m_messageReserveMillis = p_newReserve;
		return delayed;
	}
	

	//--------------------------------------------------
	// COMMUNICATION METHODS
	//--------------------------------------------------

	private String binary8 (int p_num)
	{
		return String.format("%8s", Integer.toBinaryString(p_num & 0xff)).replace(" ", "0");
	}
	
	private String binaryMsgToString (int p_num)
	{
		String b = binary8(p_num);
		String a = (p_num >= 32 && p_num <  127) ? " (" + (char) p_num + ")" : "";
		return String.format("%s: %3d%4s", b, p_num, a);
	}
	

	private String readLine (BufferedInputStream p_input)
		throws IOException
	{
		StringBuilder line = new StringBuilder();
		int ch;
		while ((ch = p_input.read()) != '\n')
		{
			// do not include carriage return or newline
			if (ch != 13) line.append((char)ch);
		}
		return line.toString();
	}

	/** Prefix that indicates line from Keyboard Controller is a command to process */
	public static final String CMD_PREFIX = "CMD: ";
	
	/* *******************************************************************
	 * (non-Javadoc)
	 * @see gnu.io.SerialPortEventListener#serialEvent(gnu.io.SerialPortEvent)
	 * Handle an event on the serial port. Read the data and print it.
	 * *******************************************************************/
	public synchronized void serialEvent(SerialPortEvent oEvent)
	{
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
		{
			try
			{
				while (m_input.available() > 0)
				{
					if (m_readLineEventMode || m_readOneLineEventMode)
					{
						String line = readLine(m_input);
						System.out.println("Keyboard(" + m_myPosition + "): " + line);
						if (line.startsWith(CMD_PREFIX))
						{
							processIncomingCommand(line.substring(CMD_PREFIX.length()));
						}
						if (line.endsWith(READY_MSG))
						{
							m_readLineEventMode = false;
							System.out.println("Found " + READY_MSG);
						}
						m_readOneLineEventMode = false;
					}
					else
					{
						// read an 8-bit byte, but operate on it as an int, so it is, in effect, unsigned
						int msg = m_input.read();
						if (msg != 192)
							System.out.println("From Keyboard: " + binaryMsgToString(msg));
						String messageDescription = processIncomingMessage(msg);
						if (messageDescription != null)
							System.out.println("    Operation: " + messageDescription);
					}
				}
			}
			catch (Exception e)
			{
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	/***********************************************************************
	 * Main handler for messages from the Keyboard Controllers.
	 * @param p_msg			the message (only the low 8 bits are considered)
	 * 						using an int rather than byte to avoid sign issues
	 * @return a description of the message
	 * @throws IOException if there are communication problems
	 ***********************************************************************/
	public String processIncomingMessage (int p_msg)
		throws IOException
	{
		int opId = (p_msg >> 6);
		int cardId = (p_msg & 0b00111111);
		Card card = (cardId < 52 ? new Card(cardId) : null);
		
		String cardAbbrev = (card == null ? "" : card.abbreviation());
		
		if (opId == 0)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("processIncomingMessage: play card from hand: " + cardId);
			m_game.getBridgeHand().evt_playCard(getMyPosition(), card);
			return "Play card (" + cardId + "): " + cardAbbrev;
		}
		else if (opId == 1)
		{
			if (s_cat.isDebugEnabled()) s_cat.debug("processIncomingMessage: play card from parner (dummy): " + cardId);
			m_game.getBridgeHand().evt_playCard(getMyPartnersPosition(), card);
			return "Play card (" + cardId + "): " + cardAbbrev;
		}
		else if (opId == 2)
		{
			// remove these, using text command now
			if (cardId == 0)
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("processIncomingMessage: undo play card");
				//TODO: m_bridgeHand.evt_undoPlayCard(p_controller.getMyPosition());
				return "Undo Play card (" + cardId + "): " + cardAbbrev;
			}
			else
			{
				if (s_cat.isDebugEnabled()) s_cat.debug("processIncomingMessage: undo play card");
				//TODO: m_bridgeHand.evt_masterUndo();
				return "Master Undo";
			}
		}
		else if (opId == 3)
		{
			if ((cardId & 0b00100000) != 0)
			{
				int id = (cardId & 0b1111);
				return "Incomplete message: opId: " + Integer.toBinaryString(id) + " (" + id + ")";
			}
			else
			{
				if (cardId == 0)
				{
					setReadOneLineEventMode(true);
					// return "Read line:";
					return null;
				}
				else if (cardId == 1)
				{
					setReadLineEventMode(true);
					return "Restarting";
				}
				else if (cardId == 2)
				{
					System.out.println("    about to initiate reset");
					m_deviceReady = false;
					m_timeOfLastMessage = System.currentTimeMillis();	// Hardware announces reset start
					m_messageReserveMillis = 1500;
					m_game.getBridgeHand().evt_resetKeyboard(this);
					return "Initiate reset";
				}
				else
				{
					return "unknown code: " + cardId;
				}
			}
		}
		return "no code";
	}

	/***********************************************************************
	 * Commands that can be entered in the command interpreter.
	 ***********************************************************************/
	public enum TextCommand {
		  RESET			("Requests Reset")
		, DEBUG			("Debug message")
		, PLAY			("Play card: position cardAbbrev (e.g., N QH)")
		, KBDPOS			("Indicate keyboard position: newPosition)")
		, CONTRACT		("Set contract: position numTricks suit")
		, DEAL			("Asks Game Controller to Deal hands")
		, NEWHAND		("Asks Game Controller to start a new hand")
		, UNDO			("Undo")
		, REDO			("Redo last undo")
		;
		
		private String m_description;
		
		TextCommand (String p_description)
		{
			m_description = p_description;
		}
		
		public String getDescription() { return m_description; } 
	}

	/***********************************************************************
	 * Processes text commands from the keyboard controller.
	 ***********************************************************************/
	public void processIncomingCommand ( String p_line )
	{
		if (p_line.trim().equals("")) return;	// skip empty input
			
		TextCommand cmd = null;
		try
		{
			String[] args = p_line.split(" ");
			if (args.length <= 0) return;
				
			cmd = TextCommand.valueOf(args[0].toUpperCase());
				
			switch (cmd)
			{
				case RESET:
				{
					if (args.length != 1)
						throw new IllegalArgumentException("Wrong number of arguments");
					System.out.println("    about to initiate reset");
					m_deviceReady = false;
					m_timeOfLastMessage = System.currentTimeMillis();	// Hardware announces reset start
					m_messageReserveMillis = 1500;
					m_game.getBridgeHand().evt_resetKeyboard(this);
				}
				break;

				case DEBUG:
				{
					System.out.println(p_line);
				}
				break;
				
				case PLAY:
				{	// position rank suit
					if (args.length != 4)
						throw new IllegalArgumentException("Wrong number of arguments");
					int idx = 0;
					int id;
					id = Integer.parseInt(args[++idx]);
					Direction direction = Direction.values()[id];
					id = Integer.parseInt(args[++idx]);
					Rank rank = Rank.values()[id];
					id = Integer.parseInt(args[++idx]);
					Suit suit = Suit.values()[id];
					Card card = new Card(rank,suit);
					m_game.getBridgeHand().evt_playCard(direction, card);
				}
				break;
					
					
				case KBDPOS:
				{ 	// position
					// identify the position of this keyboard
					if (args.length != 2)
						throw new IllegalArgumentException("Wrong number of arguments");
					int idx = 0;
					int id;
					id = Integer.parseInt(args[++idx]);
					Direction direction = Direction.values()[id];
					m_myPosition = direction;
					m_deviceReady = true;
					send_multiByteMessage(MULTIBYTE_MESSAGE.SET_PLAYER, m_myPosition);
				}
				break;
						
				case CONTRACT:
				{	// winner tricks suit
					if (args.length != 4)
						throw new IllegalArgumentException("Wrong number of arguments");
					int idx = 0;
					int id;
					id = Integer.parseInt(args[++idx]);
					Direction direction = Direction.values()[id];
					int numTricks = Integer.parseInt(args[++idx]);
					if (numTricks < 0 || numTricks > 7)
						throw new IllegalArgumentException("Invalid numTricks: " + numTricks);
					id = Integer.parseInt(args[++idx]);
					Suit suit = Suit.values()[id];
					Contract contract = new Contract(direction, suit, numTricks);
					m_game.getBridgeHand().evt_setContract(contract);
				}
				break;

				case DEAL:
				{
					if (args.length != 1)
						throw new IllegalArgumentException("Wrong number of arguments");
					m_game.getBridgeHand().evt_dealHands(-1);
				}
				break;
						
				case NEWHAND:
				{
					if (args.length != 1)
						throw new IllegalArgumentException("Wrong number of arguments");
					m_game.evt_startNewHand();
				}
				break;
						
				case UNDO:
				{
					if (args.length != 2)
						throw new IllegalArgumentException("Wrong number of arguments");
					boolean confirmed = (args[1].toLowerCase().startsWith("c"));
					m_game.getBridgeHand().evt_undo(confirmed);
				}
				break;
										
				case REDO:
				{
					if (args.length != 2)
						throw new IllegalArgumentException("Wrong number of arguments");
					boolean confirmed = (args[1].toLowerCase().startsWith("c"));
					m_game.getBridgeHand().evt_redo(confirmed);
				}
				break;
										
				default:
						break;
			}
		}
		catch (Exception e)
		{
			System.out.print("Error: ");
			System.out.println(e.getMessage());
			e.printStackTrace(System.out);
			if (cmd != null) System.out.println(cmd.getDescription());
		}
	}

	//--------------------------------------------------
	// GAME LISTENER METHODS
	//--------------------------------------------------

	/* (non-Javadoc)
	 * @see model.GameListener#debugMsg(java.lang.String)
	 */
	@Override
	public void sig_debugMsg ( String p_string )
	{
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see model.GameListener#sig_initializing()
	 */
	public void sig_initializing ()
	{
		// nothing to do
	}
	
	/* (non-Javadoc)
	 * @see model.GameListener#gameReset()
	 */
	@Override
	public void sig_gameReset ()
	{
		send_simpleMessage(KBD_MESSAGE.NEW_HAND);
		setPlayer();
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_gameReset_undo(boolean, boolean)
	 */
	@Override
	public void sig_gameReset_undo ( boolean p_redoFlag, boolean p_confirmed )
	{
		send_undoAnnouncement(p_confirmed, p_redoFlag, UNDO_EVENT.NEW_HAND);
	}
	
	/* (non-Javadoc)
	 * @see model.GameListener#scanBlindHands()
	 */
	@Override
	public void sig_scanBlindHands ()
	{
		send_simpleMessage(KBD_MESSAGE.SCAN_HAND);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#scanDummyHand()
	 */
	@Override
	public void sig_scanDummyHand ()
	{
		send_simpleMessage(KBD_MESSAGE.SCAN_DUMMY);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#cardScanned(model.Direction, model.Card, boolean)
	 */
	@Override
	public void sig_cardScanned ( Direction p_direction, lerner.blindBridge.model.Card p_card, boolean p_handComplete )
	{
		if (p_direction == m_myPosition || p_direction == m_dummyPosition)
		{
			send_multiByteMessage(MULTIBYTE_MESSAGE.ADD_CARD_TO_HAND, p_direction, p_card);
			if (p_handComplete)
			{
				send_simpleMessage(KBD_MESSAGE.HAND_SCAN_COMPLETE);
			}
		}
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_cardScanned_undo(boolean, boolean, lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card, boolean)
	 */
	public void sig_cardScanned_undo ( boolean p_redoFlag, boolean p_confirmed, Direction p_direction, Card p_card, boolean p_handComplete )
	{
		if (p_handComplete)
		{
			send_undoAnnouncement(p_confirmed, p_redoFlag, UNDO_EVENT.SCAN_HAND, p_direction, p_card);
		}
		else
		{
			send_undoAnnouncement(p_confirmed, p_redoFlag, UNDO_EVENT.SCAN_CARD, p_direction, p_card);
		}
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_cardPlayed_undo(boolean, boolean, lerner.blindBridge.model.Direction, lerner.blindBridge.model.Card)
	 */
	@Override
	public void sig_cardPlayed_undo ( boolean p_redoFlag, boolean p_confirmed, Direction p_direction, Card p_card )
	{
		send_undoAnnouncement(p_confirmed, p_redoFlag, UNDO_EVENT.SCAN_CARD, p_direction, p_card);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#blindHandsScanned()
	 */
	@Override
	public void sig_blindHandsScanned ()
	{
		// Nothing to do
		// cardScanned sends hand complete notification, since this goes to only one listener)
	}

	/* (non-Javadoc)
	 * @see model.GameListener#dummyHandScanned()
	 */
	@Override
	public void sig_dummyHandScanned ()
	{
		// Nothing to do
		// cardScanned sends hand complete notification)
	}

	/* (non-Javadoc)
	 * @see model.GameListener#enterContract()
	 */
	@Override
	public void sig_enterContract ()
	{
		send_simpleMessage(KBD_MESSAGE.ENTER_CONTRACT);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#contractSet(model.Contract)
	 */
	@Override
	public void sig_contractSet ( lerner.blindBridge.model.Contract p_contract )
	{
		send_contract(p_contract);
	}

	/* (non-Javadoc)
	 * @see lerner.blindBridge.model.GameListener#sig_contractSet_undo(boolean, boolean, lerner.blindBridge.model.Contract)
	 */
	@Override
	public void sig_contractSet_undo ( boolean p_redoFlag, boolean p_confirmed, Contract p_contract )
	{
		send_undoAnnouncement(p_confirmed, p_redoFlag, UNDO_EVENT.SET_CONTRACT, p_contract);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#setDummyPosition(model.Direction)
	 */
	public void sig_setDummyPosition ( Direction p_direction )
	{
		m_dummyPosition = p_direction;
		send_multiByteMessage(MULTIBYTE_MESSAGE.SET_DUMMY, p_direction);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#setNextPlayer(model.Direction)
	 */
	public void sig_setNextPlayer ( Direction p_direction )
	{
		send_multiByteMessage(MULTIBYTE_MESSAGE.SET_NEXT_PLAYER, p_direction);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#setNextPlayer(model.Direction)
	 */
	public void sig_setCurrentSuit ( Suit p_suit )
	{
		// nothing to do (Arduino sets currentSuit set from first trick)
	}

	/* (non-Javadoc)
	 * @see model.GameListener#cardPlayed(model.Direction, model.Card)
	 */
	@Override
	public void sig_cardPlayed ( Direction p_direction, Card p_card )
	{
		send_multiByteMessage(MULTIBYTE_MESSAGE.PLAY_CARD, p_direction, p_card);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#trickWon(model.Direction)
	 */
	@Override
	public void sig_trickWon ( lerner.blindBridge.model.Direction p_winner )
	{
		send_multiByteMessage(MULTIBYTE_MESSAGE.TRICK_TAKEN, p_winner);
	}

	/* (non-Javadoc)
	 * @see model.GameListener#gameComplete(model.BridgeScore)
	 */
	public void sig_handComplete (BridgeScore p_score )
	{
		send_simpleMessage(KBD_MESSAGE.HAND_COMPLETE);
	}
	
	/* (non-Javadoc)
	 * @see model.GameListener#announceError(lerner.blindBridge.gameController.ErrorCode, model.Direction, model.Card, model.Suit, int)
	 */
	@Override
	public void sig_error (	ErrorCode p_errorCode,
								Direction p_direction,
								Card p_card,
								Suit p_suit,
								int p_num )
	{
		switch (p_errorCode)
		{
			case CANNOT_PLAY_ALREADY_PLAYED:
				send_simpleMessage(KBD_MESSAGE.CANNOT_PLAY_ALREADY_PLAYED);
				break;

			case CANNOT_PLAY_NOT_IN_HAND:
				send_simpleMessage(KBD_MESSAGE.CANNOT_PLAY_NOT_IN_HAND);
				break;

			case CANNOT_PLAY_WRONG_SUIT:
			{
				if ((p_direction != m_dummyPosition && p_direction == m_myPosition)
						||
					(p_direction == m_dummyPosition && p_direction == m_myPartnersPosition))
				{
					send_cannotPlay(p_direction, p_card, p_suit);
				}
			}
			break;

			default:
				s_cat.error("announceError: Unexpected error");
				break;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		
		out.append("Kbd[" + m_myPosition + "]");
		out.append(" dummy: " + m_dummyPosition);
		out.append(" device: " + m_communicationPort.getName());
		
		return out.toString();
	}
	
	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * If true, read ASCII newline-terminated messages from
	 * the Keyboard Controller until the final setup message is received.
	 * Then switch back to byte-message mode.
	 * Default is false (byte-message mode).
	 * @return boolean 
	 ***********************************************************************/
	public boolean isReadLineEventMode ()
	{
		return m_readLineEventMode;
	}

	/***********************************************************************
	 * If true, read ASCII newline-terminated messages from
	 * the Keyboard Controller until the final setup message is received.
	 * Then switch back to byte-message mode.
	 * Default is false (byte-message mode).
	 * @param p_readLineEventMode boolean
	 ***********************************************************************/
	public void setReadLineEventMode ( boolean p_readLineEventMode )
	{
		m_readLineEventMode = p_readLineEventMode;
	}

	/***********************************************************************
	 * If true, read one ASCII newline-terminated message from
	 * the Keyboard Controller and then switch back to byte message
	 * mode.
	 * Default is false (byte-message mode).
	 * @return boolean
	 ***********************************************************************/
	public boolean isReadOneLineEventMode ()
	{
		return m_readOneLineEventMode;
	}

	/***********************************************************************
	 * If true, read one ASCII newline-terminated message from
	 * the Keyboard Controller and then switch back to byte message
	 * mode.
	 * Default is false (byte-message mode).
	 * @param p_readOneLineEventMode boolean
	 ***********************************************************************/
	public void setReadOneLineEventMode ( boolean p_readOneLineEventMode )
	{
		m_readOneLineEventMode = p_readOneLineEventMode;
	}

	/***********************************************************************
	 * The position of this Keyboard Controller
	 * @return player
	 ***********************************************************************/
	public Direction getMyPosition ()
	{
		return m_myPosition;
	}

	/***********************************************************************
	 * The position of this Keyboard Controller's partner
	 * @return player
	 ***********************************************************************/
	public Direction getMyPartnersPosition ()
	{
		return m_myPartnersPosition;
	}

	/***********************************************************************
	 * Indicates if the device has completed initialization or reset
	 * @return true if ready, false otherwise
	 ***********************************************************************/
	public boolean isDeviceReady ()
	{
		return m_deviceReady;
	}

}
