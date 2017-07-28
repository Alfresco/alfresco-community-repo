/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.remote.RepoRemote;
import org.alfresco.service.cmr.remote.RepoRemoteTransport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;

/**
 * The server side implementation of RepoRemoteTransport. It
 * handles ticket validation, and expiration of idle streams.
 * @author britt
 */
public class RepoRemoteTransportService implements RepoRemoteTransport,
        Runnable 
{
    /**
     * The map of handles to open input streams.
     */
    private Map<String, InputStream> fInputStreams;
    
    /**
     * The map of handles to last accesses for input streams.
     */
    private Map<String, Long> fInputLastAccessTimes;
    
    /**
     * The map of handles to busy flags.
     */
    private Map<String, Boolean> fInputBusy;
    
    /**
     * The map of handles to open output streams.
     */
    private Map<String, OutputStream> fOutputStreams;
    
    /**
     * The map of handles to last accesses for output streams.
     */
    private Map<String, Long> fOutputLastAccessTimes;
    
    /**
     * The map of handles to busy flags.
     */
    private Map<String, Boolean> fOutputBusy;
    
    /**
     * The stale handle time.  This is the maximum time a handle
     * can stay idle in milliseconds.
     */
    private long fIdleTimeout;
    
    /**
     * The thread for this Runnable.
     */
    private Thread fThread;
    
    /**
     * Flag for whether this Runnable is done.
     */
    private boolean fDone;
    
    /**
     * The RepoRemote instance.
     */
    private RepoRemote fRepoRemote;
    
    /**
     * The AuthenticationService instance.
     */
    private AuthenticationService fAuthService;

    /**
     * Default constructor.
     */
    public RepoRemoteTransportService()
    {
        fIdleTimeout = 30000;
        fInputStreams = new HashMap<String, InputStream>();
        fInputLastAccessTimes = new HashMap<String, Long>();
        fInputBusy = new HashMap<String, Boolean>();
        fOutputStreams = new HashMap<String, OutputStream>();
        fOutputLastAccessTimes = new HashMap<String, Long>();
        fOutputBusy = new HashMap<String, Boolean>();
    }

    /**
     * Set the Idle Timeout value.
     * @param timeout The value to set.
     */
    public void setIdleTimeout(long timeout)
    {
        fIdleTimeout = timeout;
    }

    /**
     * Set the RepoRemote instance.
     */
    public void setRepoRemote(RepoRemote remote)
    {
        fRepoRemote = remote;
    }
    
    /**
     * Set the AuthenticationService instance.
     */
    public void setAuthenticationService(AuthenticationService service)
    {
        fAuthService = service;
    }

    /**
     * The init method. This fires up a thread to check 
     * for closable streams.
     */
    public void init()
    {
        fThread = new Thread(this);
        fDone = false;
        fThread.start();
    }
    
    /**
     * The body of this Runnable.
     */
    public synchronized void run()
    {
        while (!fDone)
        {
            try
            {
                wait(fIdleTimeout);
            }
            catch (InterruptedException e)
            {
                // Do nothing.
            }
            long now = System.currentTimeMillis();
            List<String> toClose = new ArrayList<String>();
            for (String handle : fInputLastAccessTimes.keySet())
            {
                if (fInputBusy.get(handle))
                {
                    continue;
                }
                if (now - fInputLastAccessTimes.get(handle) > fIdleTimeout)
                {
                    toClose.add(handle);
                }
            }
            for (String handle : toClose)
            {
                try
                {
                    fInputStreams.get(handle).close();
                }
                catch (IOException e)
                {
                    // Do nothing.
                }
                fInputStreams.remove(handle);
                fInputLastAccessTimes.remove(handle);
                fInputBusy.remove(handle);
            }
            toClose.clear();
            for (String handle : fOutputLastAccessTimes.keySet())
            {
                if (fOutputBusy.get(handle))
                {
                    continue;
                }
                if (now - fOutputLastAccessTimes.get(handle) > fIdleTimeout)
                {
                    toClose.add(handle);
                }
            }
            for (String handle : toClose)
            {
                try
                {
                    fOutputStreams.get(handle).close();
                }
                catch (IOException e)
                {
                    // Do nothing.
                }
                fOutputStreams.remove(handle);
                fOutputLastAccessTimes.remove(handle);
                fOutputBusy.remove(handle);
            }
        }
    }
    
    /**
     * Shutdown the Runnable cleanly.
     */
    public void shutDown()
    {
        synchronized (this)
        {
            fDone = true;
            notifyAll();
        }
        try
        {
            fThread.join();
        }
        catch (InterruptedException e)
        {
            // Do nothing.
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#closeInputHandle(java.lang.String, java.lang.String)
     */
    public synchronized void closeInputHandle(String ticket, String handle) 
    {
        fAuthService.validate(ticket);
        InputStream in = fInputStreams.get(handle);
        if (in != null)
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                // Do nothing.
            }
            fInputStreams.remove(handle);
            fInputLastAccessTimes.remove(handle);
            fInputBusy.remove(handle);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#closeOutputHandle(java.lang.String, java.lang.String)
     */
    public synchronized void closeOutputHandle(String ticket, String handle) 
    {
        fAuthService.validate(ticket);
        OutputStream out = fOutputStreams.get(handle);
        if (out != null)
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                // Do nothing.
            }
            fOutputStreams.remove(handle);
            fOutputLastAccessTimes.remove(handle);
            fOutputBusy.remove(handle);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#createDirectory(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public NodeRef createDirectory(String ticket, NodeRef base, String path) 
    {
        fAuthService.validate(ticket);
        return fRepoRemote.createDirectory(base, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#createFile(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public String createFile(String ticket, NodeRef base, String path) 
    {
        fAuthService.validate(ticket);
        OutputStream out = fRepoRemote.createFile(base, path);
        return getOutputHandle(out);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#getListing(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<String, Pair<NodeRef, Boolean>> getListing(String ticket, NodeRef dir) 
    {
        fAuthService.validate(ticket);
        return fRepoRemote.getListing(dir);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#getRoot(java.lang.String)
     */
    public NodeRef getRoot(String ticket) 
    {
        fAuthService.validate(ticket);
        return fRepoRemote.getRoot();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#lookup(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public Pair<NodeRef, Boolean> lookup(String ticket, NodeRef base, String path) 
    {
        fAuthService.validate(ticket);
        return fRepoRemote.lookup(base, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#readFile(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public String readFile(String ticket, NodeRef base, String path) 
    {
        fAuthService.validate(ticket);
        InputStream in = fRepoRemote.readFile(base, path);
        return getInputHandle(in);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#readFile(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public String readFile(String ticket, NodeRef fileRef) 
    {
        fAuthService.validate(ticket);
        InputStream in = fRepoRemote.readFile(fileRef);
        return getInputHandle(in);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#readInput(java.lang.String, java.lang.String, int)
     */
    public byte[] readInput(String ticket, String handle, int count) 
    {
        fAuthService.validate(ticket);
        InputStream in = null;
        synchronized (this)
        {
            in = fInputStreams.get(handle);
            if (in == null)
            {
                throw new AlfrescoRuntimeException("Invalid Input Handle.");
            }
            fInputBusy.put(handle, true);
            fInputLastAccessTimes.put(handle, System.currentTimeMillis());
        }
        byte [] buff = new byte[count];
        try
        {
            int read = in.read(buff);
            if (read == -1)
            {
                read = 0;
            }
            if (read != count)
            {
                byte [] newBuff = new byte[read];
                for (int i = 0; i < read; i++)
                {
                    newBuff[i] = buff[i];
                }
                return newBuff;
            }
            return buff;
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("I/O Error.");
        }
        finally
        {
            synchronized (this)
            {
                fInputBusy.put(handle, false);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#removeNode(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void removeNode(String ticket, NodeRef base, String path) 
    {
        fAuthService.validate(ticket);
        fRepoRemote.removeNode(base, path);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#removeNode(java.lang.String, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeNode(String ticket, NodeRef toRemove) 
    {
        fAuthService.validate(ticket);
        fRepoRemote.removeNode(toRemove);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#rename(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public void rename(String ticket, NodeRef base, String src, String dst) 
    {
        fAuthService.validate(ticket);
        fRepoRemote.rename(base, src, dst);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#writeFile(java.lang.String, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public String writeFile(String ticket, NodeRef base, String path) 
    {
        fAuthService.validate(ticket);
        OutputStream out = fRepoRemote.writeFile(base, path);
        return getOutputHandle(out);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.remote.RepoRemoteTransport#writeOutput(java.lang.String, java.lang.String, byte[], int)
     */
    public void writeOutput(String ticket, String handle, byte[] buff, int count) 
    {
        fAuthService.validate(ticket);
        OutputStream out = null;
        synchronized (this)
        {
            out = fOutputStreams.get(handle);
            if (out == null)
            {
                throw new AlfrescoRuntimeException("Invalid Output Handle.");
            }
            fOutputBusy.put(handle, true);
            fOutputLastAccessTimes.put(handle, System.currentTimeMillis());
        }
        try
        {
            out.write(buff, 0, count);
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException("I/O Errror.");
        }
        finally
        {
            synchronized (this)
            {
                fOutputBusy.put(handle, false);
            }
        }
    }
    
    private synchronized String getOutputHandle(OutputStream out)
    {
        String handle = GUID.generate();
        fOutputStreams.put(handle, out);
        fOutputLastAccessTimes.put(handle, System.currentTimeMillis());
        fOutputBusy.put(handle, false);
        return handle;
    }
    
    private synchronized String getInputHandle(InputStream in)
    {
        String handle = GUID.generate();
        fInputStreams.put(handle, in);
        fInputLastAccessTimes.put(handle, System.currentTimeMillis());
        fInputBusy.put(handle, false);
        return handle;
    }
}
