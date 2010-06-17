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
package org.alfresco.web.bean.wcm;

import java.util.Collections;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.web.bean.repository.Repository;

/**
 * Base class for Remove,Set,Edit Permissions dialogs
 * 
 * @author Sergei Gavrusev
 */
public class UpdatePermissionsDialog extends BasePermissionsDialog
{
    private static final long serialVersionUID = 7189321059584956816L;
    transient private AVMLockingService avmLockingService;
    transient private AuthenticationService authenticationService;
    
    private AVMNode activeNode;

    @Override
    public void init(Map<String, String> parameters)
    {
        
        super.init(parameters);
    }

    /**
     * @param avmLockingService The AVMLockingService to set.
     */
    public void setAvmLockingService(final AVMLockingService avmLockingService)
    {
        this.avmLockingService = avmLockingService;
    }

    protected AVMLockingService getAvmLockingService()
    {
        if (avmLockingService == null)
        {
            avmLockingService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMLockingService();
        }
        return avmLockingService;
    }

    /**
     * @param authenticationService The AuthenticationService to set.
     */
    protected void setAuthenticationService(final AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    protected AuthenticationService getAuthenticationService()
    {
        if (authenticationService == null)
        {
            authenticationService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthenticationService();
        }

        return authenticationService;
    }

    /**
     * Create lock for node if it is necessary. Also create lock for children, if they inherit parent permissions.
     */
    protected void createLock(AVMNode node)
    {
        String avmPath = node.getPath();
        String webProject = WCMUtil.getWebapp(avmPath);
        String avmStore = WCMUtil.getStoreName(avmPath);
        String relativePath = WCMUtil.getStoreRelativePath(avmPath);

        /*
         * The logic doesn't look correct here.  If the lock is held by another user, then the
         * action is to DO NOTHING!
         * TODO: Examine and fix - or remove this class completely
         */
        if (getAvmLockingService().getLockOwner(webProject, relativePath) == null && !node.isDirectory())
        {
            String userName = getAuthenticationService().getCurrentUserName();
            Map<String, String> lockAttributes = Collections.singletonMap(WCMUtil.LOCK_KEY_STORE_NAME, avmStore);
            getAvmLockingService().lock(webProject, relativePath, userName, lockAttributes);
        }
    }

    /**
     * Getter for active node property
     */
    public AVMNode getActiveNode()
    {
        return activeNode;
    }

    /**
     * Setter for active node property
     */
    public void setActiveNode(final AVMNode activeNode)
    {
        this.activeNode = activeNode;
    }
}
