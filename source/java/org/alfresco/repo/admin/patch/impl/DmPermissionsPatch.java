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
import org.alfresco.repo.domain.AccessControlListDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Migrate permissions from the OLD format to defining, shared and layered
 */
public class DmPermissionsPatch extends AbstractPatch
{

    private static final String MSG_SUCCESS = "patch.updateDmPermissions.result";
    
    private AccessControlListDAO accessControlListDao;
    
    private PatchDAO patchDAO;
    
    @Override
    protected String applyInternal() throws Exception
    {
        Thread progressThread = null;
        if (this.patchDAO.supportsProgressTracking())
        {
            progressThread = new Thread(new ProgressWatcher(), "DMPatchProgressWatcher");
            progressThread.start();
        }

        Map<ACLType, Integer> summary = this.accessControlListDao.patchAcls();

        if (progressThread != null)
        {
            progressThread.interrupt();
            progressThread.join();
        }

        // build the result message
        String msg = I18NUtil.getMessage(DmPermissionsPatch.MSG_SUCCESS, summary.get(ACLType.DEFINING));
        // done
        return msg;
    }

    /**
     * Set the access control list dao
     * 
     * @param accessControlListDao
     */
    public void setAccessControlListDao(AccessControlListDAO accessControlListDao)
    {
        this.accessControlListDao = accessControlListDao;
    }
    
    /**
     * Set the patch dao
     * 
     * @param patchDAO
     */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
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
                try
                {
                    Thread.sleep(60000);
                }
                catch (InterruptedException e)
                {
                    this.running = false;
                }

                if (this.running)
                {
                    RetryingTransactionHelper txHelper = DmPermissionsPatch.this.transactionService
                            .getRetryingTransactionHelper();
                    txHelper.setMaxRetries(1);
                    Long done = txHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {

                        public Long execute() throws Throwable
                        {
                            if (ProgressWatcher.this.toDo == null)
                            {
                                ProgressWatcher.this.toDo = DmPermissionsPatch.this.patchDAO
                                        .getDmNodeCount();
                                ProgressWatcher.this.max = DmPermissionsPatch.this.patchDAO.getMaxAclId();
                            }
                            return DmPermissionsPatch.this.patchDAO
                                    .getDmNodeCountWithNewACLs(ProgressWatcher.this.max);
                        }
                    }, true, true);

                    reportProgress(this.toDo, done);
                }
            }
        }

    }

}
