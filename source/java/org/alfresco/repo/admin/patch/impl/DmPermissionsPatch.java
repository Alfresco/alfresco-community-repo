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
package org.alfresco.repo.admin.patch.impl;

import java.util.Map;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.permissions.AccessControlListDAO;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate permissions from the OLD format to defining, shared and layered
 */
public class DmPermissionsPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.updateDmPermissions.result";
    
    private static Log logger = LogFactory.getLog(DmPermissionsPatch.class);
    
    private AccessControlListDAO accessControlListDao;
    private PatchDAO patchDAO;
    private ControlDAO controlDAO;
    
    public void setAccessControlListDao(AccessControlListDAO accessControlListDao)
    {
        this.accessControlListDao = accessControlListDao;
    }
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        Thread progressThread = null;
        progressThread = new Thread(new ProgressWatcher(), "DMPatchProgressWatcher");
        progressThread.start();

        try
        {
            Map<ACLType, Integer> summary = this.accessControlListDao.patchAcls();
            // build the result message
            String msg = I18NUtil.getMessage(DmPermissionsPatch.MSG_SUCCESS, summary.get(ACLType.DEFINING));
            // done
            return msg;
        }
        finally
        {
            progressThread.interrupt();
            progressThread.join();
        }
    }

    private class ProgressWatcher implements Runnable
    {
        private boolean running = true;
        Long toDo;
        Long max;

        public void run()
        {
            while (this.running)
            {
                if (this.running)
                {
                    RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
                    txHelper.setMaxRetries(1);
                    txHelper.setForceWritable(true);
                    RetryingTransactionCallback<Long> callback = new RetryingTransactionCallback<Long>()
                    {
                        public Long execute() throws Throwable
                        {
                            // Change isolation level
                            try
                            {
                                controlDAO.setTransactionIsolationLevel(1);
                            }
                            catch (IllegalStateException e)
                            {
                                // Can't be set.  We're done here.
                                toDo = 0L;
                                running = false;
                                return 0L;
                            }
                            
                            if (toDo == null)
                            {
                                toDo = patchDAO.getDmNodeCount();
                                max = patchDAO.getMaxAclId();
                            }
                            return patchDAO.getDmNodeCountWithNewACLs(ProgressWatcher.this.max);
                        }
                    };
                    try
                    {
                        Long done = txHelper.doInTransaction(callback, true, true);
                        reportProgress(this.toDo, done);
                    }
                    catch (Throwable e)
                    {
                        logger.error("Failure in ProgressWatcher", e);
                        this.running = false;
                    }
                }
                
                try
                {
                    Thread.sleep(60000);
                }
                catch (InterruptedException e)
                {
                    this.running = false;
                }
            }
        }
    }
}
