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

import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Class for RemovePermissions dialog
 * 
 * @author Sergei Gavrusev
 */
public class RemovePermissionsDialog extends UpdatePermissionsDialog
{
    private static final long serialVersionUID = 7804466683515156182L;

    private static final String MSG_YES = "yes";
    private static final String MSG_NO = "no";
    private static final String MSG_REMOVE_PERMS_FOR = "remove_permissions_title";
    private String personAuthority = null;

    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    @Override
    public String getFinishButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_YES);
    }

    @Override
    public String getCancelButtonLabel()
    {

        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_NO);
    }

    @Override
    public String getContainerTitle()
    {

        FacesContext fc = FacesContext.getCurrentInstance();
        String pattern = Application.getMessage(fc, MSG_REMOVE_PERMS_FOR);
        return MessageFormat.format(pattern, personAuthority);

    }


    @Override
    public void init(Map<String, String> parameters)
    {
        super.init(parameters);
        setActiveNode(getAvmBrowseBean().getAvmActionNode());
    }


    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {

        removePermissions(getActiveNode());
        createLock(getActiveNode());

        return outcome;
    }

    /**
     * Remove permission from node
     * 
     * @param node
     */
    private void removePermissions(AVMNode node)
    {
        Set<String> permsForRemove = ManagePermissionsDialog.getPermissionsForType();
        Set<AccessPermission> allSetPerms = getPermissionService().getAllSetPermissions(node.getNodeRef());
        for (AccessPermission perm : allSetPerms)
        {
            if (perm.getAuthority().equals(personAuthority) && permsForRemove.contains(perm.getPermission()))
            {
                getPermissionService().deletePermission(node.getNodeRef(), personAuthority, perm.getPermission());

            }
        }
    }

    /**
     * @param event
     */
    public void setupAction(ActionEvent event)
    {

        UIActionLink link = (UIActionLink) event.getComponent();
        Map<String, String> params = link.getParameterMap();
        personAuthority = params.get("userName");
    }

}
