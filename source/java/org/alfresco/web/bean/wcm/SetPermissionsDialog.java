/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class for SetPermissions dialog
 * 
 * @author Sergei Gavrusev
 */
public class SetPermissionsDialog extends UpdatePermissionsDialog
{
    private static final long serialVersionUID = -8139619811033232880L;

    /** logger */
    private static Log logger = LogFactory.getLog(SetPermissionsDialog.class);

    private static final String MSG_USERS = "users";
    private static final String MSG_GROUPS = "groups";

    private static final String MSG_SET_PERMS_FOR = "set_permissions_title";

    transient private AuthorityService authorityService;
    transient private PersonService personService;

    transient private DataModel userPermsDataModel = null;

    private List<AVMNodeDescriptor> childList = null;
    private List<UserGroupPerm> userGroupPerms = null;

    /**
     * @param authorityService The authorityService to set.
     */
    public void setAuthorityService(final AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Getter for authorityService
     * 
     * @return authorityService
     */
    protected AuthorityService getAuthorityService()
    {
        if (authorityService == null)
        {
            authorityService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthorityService();
        }
        return authorityService;
    }

    /**
     * @param personService The PersonService to set.
     */
    public void setPersonService(final PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Getter for personService
     * 
     * @return personService
     */

    protected PersonService getPersonService()
    {
        if (personService == null)
        {
            personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService();
        }
        return personService;
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
        userGroupPerms = new ArrayList<UserGroupPerm>(8);
        userPermsDataModel = null;
        childList = new ArrayList<AVMNodeDescriptor>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
     */
    @Override
    public boolean getFinishButtonDisabled()
    {
        if (userGroupPerms.size() == 0)
            return true;
        else
            return false;
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
        String pattern = Application.getMessage(fc, MSG_SET_PERMS_FOR);
        return MessageFormat.format(pattern, getActiveNode().getName());

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.wcm.BasePermissionsDialog#finishImpl(javax.faces.context.FacesContext, java.lang.String)
     */
    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        // check if any permissions was selected
        if (userGroupPerms.size() > 0)
        {
            childList.add(getActiveNode().getDescriptor());
            setPermissions(getActiveNode().getDescriptor());
            createLock(getActiveNode());
        }

        return outcome;
    }

    /**
     * Set permissions for current node
     * 
     * @param node
     */
    private void setPermissions(final AVMNodeDescriptor node)
    {

        for (int i = 0; i < userGroupPerms.size(); i++)
        {
            UserGroupPerm userGroupPerm = userGroupPerms.get(i);
            final String authority = userGroupPerm.getAuthority();
            // find the selected permission ref from it's name and apply for the specified user
            Set<String> perms = ManagePermissionsDialog.getPermissionsForType();
            for (final String permission : perms)
            {
                if (userGroupPerm.getPermission().equals(permission))
                {
                    getPermissionService().setPermission(AVMNodeConverter.ToNodeRef(-1, node.getPath()), authority, permission, true);

                    if (logger.isDebugEnabled())
                        logger.debug("permission setted:" + permission);
                    break;
                }
            }
        }

    }

    /**
     * Property accessed by the Generic Picker component.
     * 
     * @return The array of filter options to show in the users/groups picker
     */
    public SelectItem[] getFilters()
    {
        ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());

        return new SelectItem[] { new SelectItem("0", bundle.getString(MSG_USERS)), new SelectItem("1", bundle.getString(MSG_GROUPS)) };
    }

    /**
     * Query callback method executed by the Generic Picker component. This method is part of the contract to the Generic Picker, it is up to the backing bean to execute whatever
     * query is appropriate and return the results.
     * 
     * @param filterIndex Index of the filter drop-down selection
     * @param contains Text from the contains textbox
     * @return An array of SelectItem objects containing the results to display in the picker.
     */
    public SelectItem[] pickerCallback(int filterIndex, String contains)
    {
        FacesContext context = FacesContext.getCurrentInstance();

        SelectItem[] items;

        UserTransaction tx = null;
        try
        {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();

            List<SelectItem> results = new ArrayList<SelectItem>();

            if (filterIndex == 0)
            {
                List<PersonInfo> persons = getPersonService().getPeople(
                      Utils.generatePersonFilter(contains.trim()),
                      true,
                      Utils.generatePersonSort(),
                      new PagingRequest(Utils.getPersonMaxResults(), null)
                ).getPage();
                
                for (int index = 0; index < persons.size(); index++)
                {
                    PersonInfo person = persons.get(index);
                    String firstName = person.getFirstName();
                    String lastName = person.getLastName();
                    String username = person.getUserName();
                    if (username != null)
                    {
                        SelectItem item = new SortableSelectItem(username, firstName + " " + lastName + " [" + username + "]", lastName);
                        results.add(item);
                    }
                }
            }
            else
            {
                Set<String> groups;
                
                if (contains != null && contains.startsWith("*"))
                {
                   // if the search term starts with a wildcard use Lucene based search to find groups (results will be inconsistent)
                   String term = contains.trim() + "*";
                   groups = getAuthorityService().findAuthorities(AuthorityType.GROUP, null, false, term,
                               AuthorityService.ZONE_APP_DEFAULT);
                }
                else
                {
                   // all other searches use the canned query so search results are consistent
                   PagingResults<String> pagedResults = getAuthorityService().getAuthorities(AuthorityType.GROUP, 
                               AuthorityService.ZONE_APP_DEFAULT, contains, true, true, new PagingRequest(10000));
                   groups = new LinkedHashSet<String>(pagedResults.getPage());
                }
                
                // add the EVERYONE group to the results
                groups.addAll(getAuthorityService().getAllAuthorities(AuthorityType.EVERYONE));

                String groupDisplayName;
                for (String group : groups)
                {
                    // get display name, if not present strip prefix from group id
                    groupDisplayName = getAuthorityService().getAuthorityDisplayName(group);
                    if (groupDisplayName == null || groupDisplayName.length() == 0)
                    {
                        groupDisplayName = group.substring(PermissionService.GROUP_PREFIX.length());
                    }

                    results.add(new SortableSelectItem(group, groupDisplayName, groupDisplayName));
                }
            }

            items = new SelectItem[results.size()];
            results.toArray(items);
            Arrays.sort(items);

            // commit the transaction
            tx.commit();
        }
        catch (Throwable err)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            try
            {
                if (tx != null)
                {
                    tx.rollback();
                }
            }
            catch (Exception tex)
            {
            }

            items = new SelectItem[0];
        }

        return items;
    }

    /**
     * Action handler called when the Add button is pressed to process the current selection
     */
    public void addSelection(ActionEvent event)
    {
        UIGenericPicker picker = (UIGenericPicker) event.getComponent().findComponent("picker");
        UISelectOne permissionPicker = (UISelectOne) event.getComponent().findComponent("permissions");

        String[] results = picker.getSelectedResults();
        if (results != null)
        {
            String permission = (String) permissionPicker.getValue();
            if (permission != null)
            {
                for (int i = 0; i < results.length; i++)
                {
                    addAuthorityWithPerm(results[i], permission);
                }
            }
        }
    }

    /**
     * Add an authority with the specified permission to the list .
     * 
     * @param authority Authority to add (cannot be null)
     * @param permsission Permission for the authorities (cannot be null)
     */
    public void addAuthorityWithPerm(String authority, String permsission)
    {
        // only add if authority not already present in the list with same role
        boolean foundExisting = false;
        for (int n = 0; n < userGroupPerms.size(); n++)
        {
            UserGroupPerm wrapper = userGroupPerms.get(n);
            if (authority.equals(wrapper.getAuthority()) && (permsission.equals(wrapper.getPermission())))
            {
                foundExisting = true;
                break;
            }
        }

        if (foundExisting == false)
        {
            StringBuilder label = new StringBuilder(64);

            // build a display label showing the user and their role for the space
            AuthorityType authType = AuthorityType.getAuthorityType(authority);
            if (authType == AuthorityType.GUEST || authType == AuthorityType.USER)
            {
                if (authType == AuthorityType.GUEST || getPersonService().personExists(authority) == true)
                {
                    // found a User authority
                    label.append(buildLabelForUserAuthorityPerm(authority, permsission));
                }
            }
            else
            {
                // found a group authority
                label.append(buildLabelForGroupAuthorityPerm(authority, permsission));
            }

            userGroupPerms.add(new UserGroupPerm(authority, permsission, label.toString()));
        }
    }

    /**
     * Action handler called when the Remove button is pressed to remove a user+permission
     */
    public void removeSelection(ActionEvent event)
    {
        UserGroupPerm wrapper = (UserGroupPerm) getUserPermsDataModel().getRowData();
        if (wrapper != null)
        {
            userGroupPerms.remove(wrapper);
        }
    }

    /**
     * Returns the properties for current user-roles JSF DataModel
     * 
     * @return JSF DataModel representing the current user-permission
     */
    public DataModel getUserPermsDataModel()
    {
        if (userPermsDataModel == null)
        {
            userPermsDataModel = new ListDataModel();
        }

        // only set the wrapped data once otherwise the rowindex is reset
        if (userPermsDataModel.getWrappedData() == null)
        {
            userPermsDataModel.setWrappedData(userGroupPerms);
        }

        return userPermsDataModel;
    }

    /**
     * Helper to build a label of the form: Firstname Lastname (permission)
     */
    public String buildLabelForUserAuthorityPerm(String authority, String role)
    {
        // found a User authority
        NodeRef ref = getPersonService().getPerson(authority);
        String firstName = (String) getNodeService().getProperty(ref, ContentModel.PROP_FIRSTNAME);
        String lastName = (String) getNodeService().getProperty(ref, ContentModel.PROP_LASTNAME);

        StringBuilder buf = new StringBuilder(100);
        buf.append(firstName).append(" ").append(lastName != null ? lastName : "").append(" (").append(Application.getMessage(FacesContext.getCurrentInstance(), role)).append(")");

        return buf.toString();
    }

    /**
     * Helper to build a label for a Group authority of the form: Groupname (permission)
     */
    public String buildLabelForGroupAuthorityPerm(String authority, String role)
    {
        StringBuilder buf = new StringBuilder(100);
        buf.append(authority.substring(PermissionService.GROUP_PREFIX.length())).append(" (").append(Application.getMessage(FacesContext.getCurrentInstance(), role)).append(")");

        return buf.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        userPermsDataModel = new ListDataModel();
        userPermsDataModel.setWrappedData(userGroupPerms);

    }

    /**
     * @return The list of available permissions for the users/groups
     */
    public SelectItem[] getPermissions()
    {

        return WCMPermissionsUtils.getPermissions();
    }

    /**
     * Simple wrapper class to represent a user/group and a permission combination
     */
    public static class UserGroupPerm implements Serializable
    {
        private static final long serialVersionUID = -1546705703700113861L;

        public UserGroupPerm(String authority, String permission, String label)
        {
            this.authority = authority;
            this.permission = permission;
            this.label = label;
        }

        public String getAuthority()
        {
            return authority;
        }

        public String getPermission()
        {
            return permission;
        }

        public String getLabel()
        {
            return label;
        }

        private String authority;
        private String permission;
        private String label;
    }
}
