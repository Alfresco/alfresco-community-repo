/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.node.cleanup;

import java.util.Collections;
import java.util.List;

import org.alfresco.error.StackTraceUtil;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for Node cleaners.  This class ensures calls through
 * after having created a read-write transaction that is authenticated
 * as system.
 * 
 * @author Derek Hulley
 * @since 2.2 SP2
 */
public abstract class AbstractNodeCleanupWorker implements NodeCleanupWorker
{
    /** Lock key: system:NodeCleanup */
    private static final QName LOCK = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "NodeCleanup");
    /** Default Lock time to live: 1 minute */
    private static final long LOCK_TTL = 60*1000L;
    
    protected final Log logger;
    
    private NodeCleanupRegistry registry;
    protected TransactionService transactionService;
    protected JobLockService jobLockService;
    protected DbNodeServiceImpl dbNodeService;
    protected NodeDAO nodeDAO;
    
    private ThreadLocal<String> lockToken = new ThreadLocal<String>();
    private VmShutdownListener shutdownListener = new VmShutdownListener("NodeCleanup");
    
    /**
     * Default constructor
     */
    public AbstractNodeCleanupWorker()
    {
        logger = LogFactory.getLog(this.getClass());
    }
    
    public void setRegistry(NodeCleanupRegistry registry)
    {
        this.registry = registry;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    public void setDbNodeService(DbNodeServiceImpl dbNodeService)
    {
        this.dbNodeService = dbNodeService;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void register()
    {
        PropertyCheck.mandatory(this, "registry", registry);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "jobLockService", jobLockService);
        PropertyCheck.mandatory(this, "dbNodeService", dbNodeService);
        PropertyCheck.mandatory(this, "nodeDAO", nodeDAO);

        registry.register(this);
    }

    /**
     * Calls {@link #doCleanInternal()} in a System-user authenticated read-write transaction.
     * This method is non-blocking but passes all second and subsequent concurrent invocations
     * straight through.
     */
    public List<String> doClean()
    {
        try
        {
            // Get a lock
            lockToken.set(null);
            String token = jobLockService.getLock(LOCK, LOCK_TTL);
            lockToken.set(token);
            
            // Do the work
            return doCleanAsSystem();
        }
        catch (LockAcquisitionException e)
        {
            // Some other process was busy
            return Collections.singletonList("Node cleanup in process: " + e.getMessage());
        }
        catch (Throwable e)
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder sb = new StringBuilder(1024);
                StackTraceUtil.buildStackTrace(
                        "Node cleanup failed: " +
                        "   Worker: " + this.getClass().getName() + "\n" +
                        "   Error:  " + e.getMessage(),
                        e.getStackTrace(),
                        sb,
                        Integer.MAX_VALUE);
                logger.debug(sb.toString());
            }
            StringBuilder sb = new StringBuilder(1024);
            StackTraceUtil.buildStackTrace(
                "Node cleanup failed: " +
                "   Worker: " + this.getClass().getName() + "\n" +
                "   Error:  " + e.getMessage(),
                e.getStackTrace(),
                sb,
                20);
            return Collections.singletonList(sb.toString());
        }
        finally
        {
            String token = this.lockToken.get();
            if (token != null)
            {
                jobLockService.releaseLock(token, LOCK);
            }
        }
    }
    
    private List<String> doCleanAsSystem()
    {
        final RunAsWork<List<String>> doCleanRunAs = new RunAsWork<List<String>>()
        {
            public List<String> doWork() throws Exception
            {
                try
                {
                    return doCleanInternal();
                }
                catch (Throwable e)
                {
                    logger.error(e);
                    return Collections.emptyList();
                }
            }
        };
        return AuthenticationUtil.runAs(doCleanRunAs, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Helper method to refresh the current job's lock token
     */
    protected void refreshLock() throws LockAcquisitionException
    {
        String token = this.lockToken.get();
        if (token != null && !shutdownListener.isVmShuttingDown())
        {
            // We had a lock token AND the VM is still going
            jobLockService.refreshLock(token, LOCK, LOCK_TTL);
        }
        else
        {
            // There is no lock token on this thread, so we trigger a deliberate failure
            jobLockService.refreshLock("lock token not available", LOCK, LOCK_TTL);
        }
    }
    
    /**
     * Do the actual cleanup.  Any errors are handled by this base class.
     * 
     * @return      Returns the cleanup messages.
     */
    protected abstract List<String> doCleanInternal() throws Throwable;
}