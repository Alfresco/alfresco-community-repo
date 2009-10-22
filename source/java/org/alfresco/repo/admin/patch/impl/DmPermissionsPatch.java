/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.Map;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.AccessControlListDAO;
import org.alfresco.repo.domain.hibernate.AclDaoComponentImpl;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.impl.AclDaoComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;

/**
 * Migrate permissions from the OLD format to defining, shared and layered
 */
public class DmPermissionsPatch extends AbstractPatch
{

    private static final String MSG_SUCCESS = "patch.updateDmPermissions.result";

    private AccessControlListDAO accessControlListDao;

    private AclDaoComponent aclDaoComponent;

    @Override
    protected String applyInternal() throws Exception
    {
        Thread progressThread = null;
        if (this.aclDaoComponent.supportsProgressTracking())
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
     * Set the acl dao component
     * 
     * @param aclDaoComponent
     */
    public void setAclDaoComponent(AclDaoComponent aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
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
                                ProgressWatcher.this.toDo = DmPermissionsPatch.this.aclDaoComponent
                                        .getDmNodeCount();
                                ProgressWatcher.this.max = DmPermissionsPatch.this.aclDaoComponent.getMaxAclId();
                            }
                            return DmPermissionsPatch.this.aclDaoComponent
                                    .getDmNodeCountWithNewACLS(ProgressWatcher.this.max);
                        }
                    }, true, true);

                    reportProgress(this.toDo, done);
                }
            }
        }

    }

}
