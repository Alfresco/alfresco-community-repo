/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

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
	 * @param evetn NodeEvent
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
