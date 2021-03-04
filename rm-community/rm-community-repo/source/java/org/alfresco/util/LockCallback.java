package org.alfresco.util;

import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;

public class LockCallback implements JobLockRefreshCallback
{
    private final AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public boolean isActive()
    {
        return running.get();
    }

    @Override
    public void lockReleased()
    {
        running.set(false);
    }

    public void setIsRunning(boolean isRunning)
    {
        this.running.set(isRunning);
    }
}
