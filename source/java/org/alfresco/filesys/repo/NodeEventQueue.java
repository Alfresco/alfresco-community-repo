
package org.alfresco.filesys.repo;

import java.util.LinkedList;

/**
 * Node Event Queue Class
 * 
 * @author gkspencer
 */
public class NodeEventQueue {

	// List of node events

	private LinkedList<NodeEvent> m_queue;

	/**
	 * Class constructor
	 */
	public NodeEventQueue() {
		m_queue = new LinkedList<NodeEvent>();
	}

	/**
	 * Return the number of events in the queue
	 * 
	 * @return int
	 */
	public final synchronized int numberOfEvents() {
		return m_queue.size();
	}

	/**
	 * Add an event to the queue
	 * 
	 * @param event NodeEvent
	 */
	public final synchronized void addEvent(NodeEvent event) {

		// Add the event to the queue

		m_queue.add( event);

		// Notify a listener that there is an event to process

		notify();
	}

	/**
	 * Remove an event from the head of the queue
	 * 
	 * @return NodeEvent
	 * @exception InterruptedException
	 */
	public final synchronized NodeEvent removeEvent()
		throws InterruptedException {

		// Wait until there is an event

		waitWhileEmpty();

		// Get the event from the head of the queue

		return m_queue.removeFirst();
	}

	/**
	 * Remove an event from the queue, without waiting if there are no events in the queue
	 * 
	 * @return NodeEvent
	 */
	public final synchronized NodeEvent removeSessionNoWait() {
		
		NodeEvent event = null;
		
		if ( m_queue.size() > 0)
			event = m_queue.removeFirst();
			
		return event;
	}
	
	/**
	 * Wait for an event to be added to the queue
	 * 
	 * @exception InterruptedException
	 */
	public final synchronized void waitWhileEmpty()
		throws InterruptedException {

		// Wait until an event arrives on the queue

		while (m_queue.size() == 0)
			wait();
	}

	/**
	 * Wait for the event queue to be emptied
	 * 
	 * @exception InterruptedException
	 */
	public final synchronized void waitUntilEmpty()
		throws InterruptedException {

		// Wait until the event queue is empty

		while (m_queue.size() != 0)
			wait();
	}
}
