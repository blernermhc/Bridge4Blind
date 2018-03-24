// -*- mode: java; standard-indent: 4; tab-width: 4; -*-
// Copyright, (c) 2008 Clickshare Service Corp., All Rights Reserved.
//----------------------------------------------------------------------

package lerner.blindBridge.model;

import lerner.blindBridge.stateMachine.BridgeHandState;

/***********************************************************************
 * Represents an event received that can be undone.
 ***********************************************************************/
public class UndoEvent
{

	/**
	 * Used to collect logging output for this class
	 */
	// private static Category s_cat = Category.getInstance(UndoEvent.class.getName());

	public interface UndoMethod
	{
		public void undo(UndoEvent p_event, boolean p_confirmed);
		public void redo(UndoEvent p_event, boolean p_confirmed);
	};
	
	//--------------------------------------------------
	// CONSTANTS
	//--------------------------------------------------

	//--------------------------------------------------
	// CONFIGURATION MEMBER DATA
	//--------------------------------------------------
	
	/** The hand to which this event relates. */
	private BridgeHand		m_bridgeHand;
	
	/** The state at the time the event was received. */
	private BridgeHandState	m_currentState;
	
	/** The name of this event. */
	private String			m_eventName;
	
	/** An array of objects for context to be used by the undo/redo methods. */
	private Object[]			m_objects;
	
	/** An array of int values for context to be used by the undo/redo methods. */
	private int[]			m_ints;
	
	/** The implementation of undo and redo for this event. */ 
	private UndoMethod		m_undoMethod;

	//--------------------------------------------------
	// INTERNAL MEMBER DATA
	//--------------------------------------------------

	//--------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------
	
	public UndoEvent (BridgeHand p_bridgeHand, String p_eventName, Object[] p_objects, int[] p_ints, UndoMethod p_undoMethod)
	{
		m_bridgeHand		= p_bridgeHand;
		m_currentState	= p_bridgeHand.getGame().getStateController().getCurrentState();
		m_eventName		= p_eventName;
		m_objects		= p_objects;
		m_ints			= p_ints;
		m_undoMethod		= p_undoMethod;
	}

	//--------------------------------------------------
	// METHODS
	//--------------------------------------------------
	
	/***********************************************************************
	 * If p_confirmed is true, undoes the effects of the given event and notifies the listeners.
	 * If p_confirmed is false, just notifies the listeners that confirmation is required.
	 * @param p_confirmed true if confirmation received.
	 ***********************************************************************/
	public void undo(boolean p_confirmed)
	{
		m_undoMethod.undo(this, p_confirmed);
	}
	
	/***********************************************************************
	 * If p_confirmed is true, redoes the effects of the given event and notifies the listeners.
	 * If p_confirmed is false, just notifies the listeners that confirmation is required.
	 * @param p_confirmed true if confirmation received.
	 ***********************************************************************/
	public void redo(boolean p_confirmed)
	{
		m_undoMethod.undo(this, p_confirmed);
	}
	

	//--------------------------------------------------
	// HELPER METHODS
	//--------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString ()
	{
		StringBuffer out = new StringBuffer();
		out.append("UndoEvent: " + m_eventName);
		out.append("\n  state: " + m_currentState);
		
		out.append("\n  objects:");
		if (m_objects == null)
		{
			out.append("null");
		}
		else
		{
			for (int idx = 0; idx < m_objects.length; ++idx)
			{
				Object obj = m_objects[idx];
				out.append("\n    " + idx + ": ");
				out.append(obj == null ? "null" : obj.toString());
			}
		}
		
		out.append("\n  ints:");
		if (m_ints == null)
		{
			out.append("null");
		}
		else
		{
			for (int idx = 0; idx < m_ints.length; ++idx)
			{
				out.append("\n    " + idx + ": " + m_ints[idx]);
			}
		}
		
		return out.toString();
	}

	//--------------------------------------------------
	// ACCESSORS
	//--------------------------------------------------

	/***********************************************************************
	 * The hand to which this event relates. 
	 * @return a bridge hand object
	 ***********************************************************************/
	public BridgeHand getBridgeHand ()
	{
		return m_bridgeHand;
	}

	/***********************************************************************
	 * The state at the time the event was received.
	 * @return The bridge state
	 ***********************************************************************/
	public BridgeHandState getCurrentState ()
	{
		return m_currentState;
	}

	/***********************************************************************
	 * The name of this event.
	 * @return the event name
	 ***********************************************************************/
	public String getEventName ()
	{
		return m_eventName;
	}

	/***********************************************************************
	 * An array of objects for context to be used by the undo/redo methods.
	 * @return the object array
	 ***********************************************************************/
	public Object[] getObjects ()
	{
		return m_objects;
	}

	/***********************************************************************
	 * An array of objects for context to be used by the undo/redo methods.
	 * @param p_objects the object array
	 ***********************************************************************/
	public void setObjects ( Object[] p_objects )
	{
		m_objects = p_objects;
	}

	/***********************************************************************
	 * An array of int values for context to be used by the undo/redo methods.
	 * @return the int array
	 ***********************************************************************/
	public int[] getInts ()
	{
		return m_ints;
	}

	/***********************************************************************
	 * An array of int values for context to be used by the undo/redo methods.
	 * @param p_ints the int array
	 ***********************************************************************/
	public void setInts ( int[] p_ints )
	{
		m_ints = p_ints;
	}

	/***********************************************************************
	 * The implementation of undo and redo for this event.
	 * @return the implementation object
	 ***********************************************************************/
	public UndoMethod getUndoMethod ()
	{
		return m_undoMethod;
	}

}
