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
package org.alfresco.repo.search.transaction;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.alfresco.service.cmr.repository.StoreRef;

public class LuceneIndexLock 
{
    private HashMap<StoreRef, ReentrantLock> locks = new HashMap<StoreRef, ReentrantLock> ();

    public LuceneIndexLock()
    {
        super();
    }
    
    public void getReadLock(StoreRef ref)
    {
      return;
    }
    
    public void releaseReadLock(StoreRef ref)
    {
      return;
    }
    
    public void getWriteLock(StoreRef ref)
    {
        ReentrantLock lock;
        synchronized(locks)
        {
            lock = locks.get(ref);
            if(lock == null)
            {
                lock = new ReentrantLock(true);
                locks.put(ref, lock);
            }
        }
        lock.lock();
    }
    
    public void releaseWriteLock(StoreRef ref)
    {
        ReentrantLock lock;
        synchronized(locks)
        {
            lock = locks.get(ref); 
        }
        if(lock != null)
        {
           lock.unlock();
        }
      
    }
}
