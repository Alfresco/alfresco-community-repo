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

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;

/**
 * Base class for Permissions dialogs
 * 
 * @author Sergei Gavrusev
 */
public class BasePermissionsDialog extends BaseDialogBean
{

    private static final long serialVersionUID = -1027989430514037278L;

    /** PermissionService bean reference */
    transient protected PermissionService permissionService;
    private AVMBrowseBean avmBrowseBean;

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
     */
    @Override
    public void init(Map<String, String> parameters)
    {

        super.init(parameters);

    }

    /**
     * Getter for permissionService
     * 
     * @return permissionService
     */
    protected PermissionService getPermissionService()
    {
        if (permissionService == null)
        {
            permissionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
        }
        return permissionService;
    }

    /**
     * @param permissionService permission service
     */
    protected void setPermissionService(final PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return outcome;
    }

    /**
     * Getter for avmBrowseBean
     * 
     * @return avmBrowseBean
     */
    public AVMBrowseBean getAvmBrowseBean()
    {
        return avmBrowseBean;
    }

    /**
     * @param avmBrowseBean The avmBrowseBean to set.
     */
    public void setAvmBrowseBean(final AVMBrowseBean avmBrowseBean)
    {
        this.avmBrowseBean = avmBrowseBean;
    }

}
