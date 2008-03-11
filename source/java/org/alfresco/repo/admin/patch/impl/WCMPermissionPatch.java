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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.AVMRepository;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.hibernate.AclDaoComponentImpl;
import org.alfresco.repo.search.AVMSnapShotTriggeredIndexingMethodInterceptor;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;

/**
 * Remove ACLs on all but staging area stores On staging area stores, set ACls according to the users and roles as set
 * on the web site Note: runs as the system user
 * 
 * @author andyh
 */
public class WCMPermissionPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.wcmPermissionPatch.result";

    AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor;

    AVMService avmService;

    PermissionService permissionService;

    AclDaoComponentImpl aclDaoComponent;

    public void setAvmService(AVMService avmService)
    {
        this.avmService = avmService;
    }

    public void setAvmSnapShotTriggeredIndexingMethodInterceptor(AVMSnapShotTriggeredIndexingMethodInterceptor avmSnapShotTriggeredIndexingMethodInterceptor)
    {
        this.avmSnapShotTriggeredIndexingMethodInterceptor = avmSnapShotTriggeredIndexingMethodInterceptor;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setAclDaoComponent(AclDaoComponentImpl aclDaoComponent)
    {
        this.aclDaoComponent = aclDaoComponent;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        Long toDo = aclDaoComponent.getAVMHeadNodeCount();
        Long maxId = aclDaoComponent.getMaxAclId();

        Thread progressThread = new Thread(new ProgressWatcher(toDo, maxId), "WCMPactchProgressWatcher");
        progressThread.start();

        List<AVMStoreDescriptor> stores = avmService.getStores();
        for (AVMStoreDescriptor store : stores)
        {
            switch (avmSnapShotTriggeredIndexingMethodInterceptor.getStoreType(store.getName()))
            {
            /* Set permissions in staging */
            case STAGING:
                setStagingAreaPermissions(store);
                setStagingAreaMasks(store);
                // TODO: mark read only
                break;
            /* Clear permissions */
            case AUTHOR:
            case AUTHOR_PREVIEW:
            case AUTHOR_WORKFLOW:
            case AUTHOR_WORKFLOW_PREVIEW:
                // TODO: add app access control
                clearPermissions(store);
                setSandBoxMasks(store);
                break;
            case STAGING_PREVIEW:
                clearPermissions(store);
                setStagingAreaMasks(store);
                // TODO: mark read only
                break;
            case WORKFLOW:
            case WORKFLOW_PREVIEW:
                clearPermissions(store);
                break;
            /* non WCM stores - nothing to do */
            case UNKNOWN:
            default:
            }
        }

        progressThread.interrupt();
        progressThread.join();
        
        // build the result message
        String msg = I18NUtil.getMessage(MSG_SUCCESS);
        // done
        return msg;
    }

    private void clearPermissions(AVMStoreDescriptor store)
    {
        AVMNodeDescriptor www = avmService.lookup(-1, store.getName() + ":/www");
        if (www.isLayeredDirectory() && www.isPrimary())
        {
            // throw away any acl
            AVMRepository.GetInstance().setACL(store.getName() + ":/www", null);
            // build the default layer acl
            avmService.retargetLayeredDirectory(store.getName() + ":/www", www.getIndirection());
        }
    }

    private void setStagingAreaPermissions(AVMStoreDescriptor store)
    {
        QName propQName = QName.createQName(null, ".web_project.noderef");

        NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, store.getName() + ":/www");
        permissionService.setPermission(dirRef, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);

        PropertyValue pValue = avmService.getStoreProperty(store.getName(), propQName);

        if (pValue != null)
        {
            NodeRef webProjectNodeRef = (NodeRef) pValue.getValue(DataTypeDefinition.NODE_REF);

            // Apply sepcific user permissions as set on the web project
            List<ChildAssociationRef> userInfoRefs = nodeService.getChildAssocs(webProjectNodeRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : userInfoRefs)
            {
                NodeRef userInfoRef = ref.getChildRef();
                String username = (String) nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
                String userrole = (String) nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);

                permissionService.setPermission(dirRef, username, userrole, true);
            }
        }
    }

    private void setStagingAreaMasks(AVMStoreDescriptor store)
    {
        NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, store.getName() + ":/www");
        permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);

    }

    private void setSandBoxMasks(AVMStoreDescriptor sandBoxStore)
    {
        // get the settings from the staging store ...

        String owner = extractOwner(sandBoxStore.getName());
        String stagingAreaName = extractStagingAreaName(sandBoxStore.getName());

        QName propQName = QName.createQName(null, ".web_project.noderef");

        NodeRef dirRef = AVMNodeConverter.ToNodeRef(-1, sandBoxStore.getName() + ":/www");

        PropertyValue pValue = avmService.getStoreProperty(stagingAreaName, propQName);

        permissionService.setPermission(dirRef.getStoreRef(), PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);

        if (pValue != null)
        {
            NodeRef webProjectNodeRef = (NodeRef) pValue.getValue(DataTypeDefinition.NODE_REF);

            // Apply sepcific user permissions as set on the web project
            List<ChildAssociationRef> userInfoRefs = nodeService.getChildAssocs(webProjectNodeRef, WCMAppModel.ASSOC_WEBUSER, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : userInfoRefs)
            {
                NodeRef userInfoRef = ref.getChildRef();
                String username = (String) nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERNAME);
                String userrole = (String) nodeService.getProperty(userInfoRef, WCMAppModel.PROP_WEBUSERROLE);

                if (username.equals(owner))
                {
                    permissionService.setPermission(dirRef.getStoreRef(), username, PermissionService.ALL_PERMISSIONS, true);
                }
                else if (userrole.equals("ContentManager"))
                {
                    permissionService.setPermission(dirRef.getStoreRef(), username, userrole, true);
                }
            }
        }
    }

    private String extractOwner(String name)
    {
        int start = name.indexOf("--");
        if (start == -1)
        {
            throw new UnsupportedOperationException(name);
        }
        int end = name.indexOf("--", start + 1);
        if (end == -1)
        {
            return name.substring(start + 2);
        }
        return name.substring(start + 2, end);
    }

    private String extractStagingAreaName(String name)
    {
        int index = name.indexOf("--");
        if (index == -1)
        {
            throw new UnsupportedOperationException(name);
        }
        return name.substring(0, index);
    }

    private class ProgressWatcher implements Runnable
    {
        private boolean running = true;

        Long toDo;

        Long max;

        ProgressWatcher(Long toDo, Long max)
        {
            this.toDo = toDo;
            this.max = max;
        }

        public void run()
        {
            while (running)
            {
                try
                {
                    Thread.sleep(60000);
                }
                catch (InterruptedException e)
                {
                    running = false;
                }

                if (running)
                {
                    RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
                    txHelper.setMaxRetries(1);
                    Long done = txHelper.doInTransaction(new RetryingTransactionCallback<Long>()
                    {

                        public Long execute() throws Throwable
                        {
                            return aclDaoComponent.getAVMNodeCountWithNewACLS(max);
                        }
                    }, true, true);

                    reportProgress(toDo, done);
                }
            }
        }

    }
}
