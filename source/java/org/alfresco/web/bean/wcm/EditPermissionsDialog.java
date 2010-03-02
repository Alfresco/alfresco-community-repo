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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.users.UserMembersBean.PermissionWrapper;
import org.alfresco.web.ui.common.component.UIActionLink;

/**
 * Class for EditPermissions dialog
 * 
 * @author Sergei Gavrusev
 */
public class EditPermissionsDialog extends UpdatePermissionsDialog
{
    private static final long serialVersionUID = 670465612383178325L;

    private static final String MSG_EDIT_PERMS_FOR = "edit_permissions_title";

    private boolean finishButtonDisabled = true;

    private List<PermissionWrapper> personPerms = null;
    private transient DataModel personPermsDataModel = null;
    private String personAuthority;

    /**
     * @param event
     */
    public void setupAction(ActionEvent event)
    {

        UIActionLink link = (UIActionLink) event.getComponent();
        Map<String, String> params = link.getParameterMap();
        personAuthority = params.get("userName");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.wcm.UpdatePermissionsDialog#init(java.util.Map)
     */
    @Override
    public void init(Map<String, String> parameters)
    {
        super.init(parameters);
        setActiveNode(getAvmBrowseBean().getAvmActionNode());
        personPerms = new ArrayList<PermissionWrapper>(3);
        personPermsDataModel = null;

        NodeRef actionNode = getAvmBrowseBean().getAvmActionNode().getNodeRef();
        NodeRef parentRef = getNodeService().getPrimaryParent(actionNode).getParentRef();
        Set<AccessPermission> parentPermission = getPermissionService().getAllSetPermissions(parentRef);

        Set<String> permsForRemove = ManagePermissionsDialog.getPermissionsForType();
        Set<AccessPermission> allSetPerms = getPermissionService().getAllSetPermissions(getActiveNode().getNodeRef());
        for (AccessPermission perm : allSetPerms)
        {
            if (!parentPermission.contains(perm))
            {
                if (perm.getAuthority().equals(personAuthority) && permsForRemove.contains(perm.getPermission()))
                {
                    PermissionWrapper wraper = new PermissionWrapper(perm.getPermission(), perm.getPermission());
                    personPerms.add(wraper);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
     */
    @Override
    public boolean getFinishButtonDisabled()
    {
        return finishButtonDisabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getContainerTitle()
     */
    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        String pattern = Application.getMessage(fc, MSG_EDIT_PERMS_FOR);
        return MessageFormat.format(pattern, personAuthority);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.wcm.BasePermissionsDialog#finishImpl(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        setPermissions(getActiveNode());
        createLock(getActiveNode());

        finishButtonDisabled = true; 

        return outcome;
    }

    @Override
    public String cancel()
    {
        finishButtonDisabled = true;

        return super.cancel();
    }

    /**
     * Set permissions for current node
     * 
     * @param node
     */
    private void setPermissions(AVMNode node)
    {
        NodeRef nodeRef = node.getNodeRef();
        Set<String> permsForRemove = ManagePermissionsDialog.getPermissionsForType();

        // Set only those permissions that contains in personPerms
        // Other permissions are removed
        for (String perm : permsForRemove)
        {
            boolean needToSet = false;
            for (PermissionWrapper wrapper : personPerms)
            {
                if (wrapper.getPermission().equals(perm))
                {
                    needToSet = true;
                    break;
                }
            }

            if (needToSet)
            {
                getPermissionService().setPermission(nodeRef, personAuthority, perm, true);
            }
            else
            {
                getPermissionService().deletePermission(nodeRef, personAuthority, perm);
            }
        }
    }

    /**
     * @return The list of available permissions for the users/groups
     */
    public SelectItem[] getPerms()
    {

        return WCMPermissionsUtils.getPermissions();
    }

    /**
     * Action handler called when the Add Permission button is pressed to process the current selection
     */
    public void addPermission(ActionEvent event)
    {
        UISelectOne permPicker = (UISelectOne) event.getComponent().findComponent("perms");

        String permission = (String) permPicker.getValue();
        if (permission != null)
        {
            boolean foundExisting = false;
            for (int n = 0; n < personPerms.size(); n++)
            {
                PermissionWrapper wrapper = personPerms.get(n);
                if (wrapper.getPermission().equals(permission))
                {
                    foundExisting = true;
                    break;
                }
            }

            if (!foundExisting)
            {
                FacesContext context = FacesContext.getCurrentInstance();
                PermissionWrapper wrapper = new PermissionWrapper(permission, Application.getMessage(context, permission));
                this.personPerms.add(wrapper);
                finishButtonDisabled = false;
            }
        }

    }

    /**
     * Returns the properties for current Person permissions JSF DataModel
     * 
     * @return JSF DataModel representing the current Person permissions
     */
    public DataModel getPersonPermsDataModel()
    {
        if (this.personPermsDataModel == null)
        {
            this.personPermsDataModel = new ListDataModel();
        }

        if (this.personPermsDataModel.getWrappedData() == null)
        {
            this.personPermsDataModel.setWrappedData(this.personPerms);
        }

        return this.personPermsDataModel;
    }

    /**
     * Action handler called when the Remove button is pressed to remove a permission from current user
     */
    public void removePermission(ActionEvent event)
    {
        PermissionWrapper wrapper = (PermissionWrapper) getPersonPermsDataModel().getRowData();
        if (wrapper != null)
        {
            this.personPerms.remove(wrapper);
            finishButtonDisabled = false;
        }
    }

}
