/*
 * Copyright (C) 2005-2008 Alfresco Software Limited. This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA. As a special exception to the terms and conditions of version 2.0 of the GPL, you may redistribute this Program in connection with Free/Libre and
 * Open Source Software ("FLOSS") applications as described in Alfresco's FLOSS exception. You should have recieved a copy of the text describing the FLOSS exception, and it is
 * also available here: http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.wcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.security.AuthenticationService;
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
     * 
     * @param node
     */
    protected void createLock(AVMNode node)
    {
        if (getAvmLockingService().getLock(node.getWebProject().getStoreId(), node.getPath().substring(node.getPath().indexOf("/"))) == null && !node.isDirectory())
        {
            String userName = getAuthenticationService().getCurrentUserName();
            List<String> owners = new ArrayList<String>(1);
            owners.add(userName);

            String webProject = node.getWebProject().getStoreId();
            String[] storePath = node.getPath().split(":");

            AVMLock lock = new AVMLock(webProject, storePath[0], storePath[1], AVMLockingService.Type.DISCRETIONARY, owners);
            getAvmLockingService().lockPath(lock);
        }

    }

    /**
     * Getter for active node property
     * 
     * @return activeNode
     */
    public AVMNode getActiveNode()
    {
        return activeNode;
    }

    /**
     * Setter for active node property
     * 
     * @param activeNode
     */
    public void setActiveNode(final AVMNode activeNode)
    {
        this.activeNode = activeNode;
    }

}
