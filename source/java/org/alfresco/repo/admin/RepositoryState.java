package org.alfresco.repo.admin;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A class that maintains a thread-safe ready indicator on the current bootstrap state of the repository.
 * 
 * @author Andy
 *
 */
public class RepositoryState
{
    private boolean bootstrapping;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Determine if the repository is ready to use.
     * 
     * @return                  <tt>true</tt> if the repository bootstrap process is still going,
     *                          or <tt>false</tt> if the repository is ready to use
     */
    public boolean isBootstrapping()
    {
        this.lock.readLock().lock();
        try
        {
            return bootstrapping;
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    public void setBootstrapping(boolean bootstrapping)
    {
        this.lock.writeLock().lock();
        try
        {
            this.bootstrapping = bootstrapping;
        }
        finally
        {
            this.lock.writeLock().unlock();
        }
    }
    
}
