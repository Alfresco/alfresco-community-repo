/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.oncrpc;

import java.util.*;

/**
 * RPC Request Queue Class
 * 
 * <p>Provides a request queue for a thread pool of worker threads.
 * 
 * @author GKSpencer
 */
public class RpcRequestQueue {

	//	List of RPC requests
	
	private LinkedList m_queue;
	
	/**
	 * Class constructor
	 */
	public RpcRequestQueue()
	{
		m_queue = new LinkedList();
	}

	/**
	 * Return the number of requests in the queue
	 * 
	 * @return int
	 */
	public final synchronized int numberOfRequests()
	{
		return m_queue.size();
	}

	/**
	 * Add a request to the queue
	 * 
	 * @param req RpcPacket
	 */
	public final synchronized void addRequest(RpcPacket req)
	{

		//	Add the request to the queue

		m_queue.add(req);

		//	Notify workers that there is a request to process

		notifyAll();
	}

	/**
	 * Remove a request from the head of the queue
	 * 
	 * @return RpcPacket
	 * @exception InterruptedException
	 */
	public final synchronized RpcPacket removeRequest()
		throws InterruptedException
	{

		//	Wait until there is a request

		waitWhileEmpty();

		//	Get the request from the head of the queue

		return (RpcPacket) m_queue.removeFirst();
	}

	/**
	 * Wait for a request to be added to the queue
	 * 
	 * @exception InterruptedException
	 */
	public final synchronized void waitWhileEmpty()
		throws InterruptedException
	{

		//	Wait until some work arrives on the queue

		while (m_queue.size() == 0)
			wait();
	}

	/**
	 * Wait for the request queue to be emptied
	 * 
	 * @exception InterruptedException
	 */
	public final synchronized void waitUntilEmpty()
		throws InterruptedException
	{

		//	Wait until the request queue is empty

		while (m_queue.size() != 0)
			wait();
	}
}
