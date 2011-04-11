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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.transaction.UserTransaction;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.bean.dialog.FilterViewSupport;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.QNameNodeMap;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.users.UserMembersBean;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.repo.WebResources;

/**
 * Class for ManagePermissions dialog
 * 
 * @author Sergei Gavrusev
 */
public class ManagePermissionsDialog extends BasePermissionsDialog implements IContextListener, FilterViewSupport
{
    private static final long serialVersionUID = -6980134441634707541L;

    private static final String MSG_MANAGE_PERMS_FOR = "manage_permissions_title";
    private static final String MSG_VIEW_PERMS_FOR = "view_permissions_title";

    private static final String LOCAL = "local";
    private static final String INHERITED = "inherited";

    private final static String MSG_CLOSE = "close";

    private String filterMode = INHERITED;


    /** PersonService bean reference */
    transient private PersonService personService;

    private UIRichList usersRichList = null;

    private boolean inheritParenSpacePermissions;


    /**
     * @param personService The personService to set.
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
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
     */
    public void init(Map<String, String> parameters)
    {
        super.init(parameters);
        contextUpdated();
        inheritParenSpacePermissions = getPermissionService().getInheritParentPermissions(getAvmBrowseBean().getAvmActionNode().getNodeRef());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
     */
    protected String finishImpl(FacesContext context, String outcome) throws Exception
    {
        return null;
    }

    /**
     * @return the list of user nodes for list data binding
     */
    public List<Map> getUsers()
    {
        boolean includeInherited = true;

        if (this.filterMode.equals(LOCAL))
        {
            includeInherited = false;
        }

        FacesContext context = FacesContext.getCurrentInstance();

        List<Map> personNodes = null;

        UserTransaction tx = null;
        try
        {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();

            // Return all the permissions set against the current node
            // for any authentication instance (user/group).
            // Then combine them into a single list for each authentication
            // found.

            NodeRef actionNode = getAvmBrowseBean().getAvmActionNode().getNodeRef();

            Map<String, List<String>> permissionMap;
            Map<String, List<String>> parentPermissionMap;
            Set<AccessPermission> permissions = getPermissionService().getAllSetPermissions(actionNode);
            Set<String> permsToDisplay = getPermissionsForType();

            permissionMap = getPerson(permissions, permsToDisplay);

            NodeRef parentRef = getNodeService().getPrimaryParent(actionNode).getParentRef();
            parentPermissionMap = getPerson(getPermissionService().getAllSetPermissions(parentRef), permsToDisplay);

            // for each authentication (username/group key) found we get the Person
            // node represented by it and use that for our list databinding object
            personNodes = new ArrayList<Map>(permissionMap.size());
            List<String> local = new ArrayList<String>();
            List<String> inh = new ArrayList<String>();
            for (String authority : permissionMap.keySet())
            {
                local.clear();
                inh.clear();

                divisionPermissions(authority, permissionMap, parentPermissionMap, inh, local);
                // check if we are dealing with a person (User Authority)
                if (AuthorityType.getAuthorityType(authority) == AuthorityType.GUEST || getPersonService().personExists(authority))
                {
                    NodeRef nodeRef = getPersonService().getPerson(authority);
                    if (nodeRef != null)
                    {
                        // create our Node representation
                        MapNode node = new MapNode(nodeRef);

                        // set data binding properties
                        // this will also force initialisation of the props now
                        // during the UserTransaction
                        // it is much better for performance to do this now
                        // rather than during page bind

                        if (includeInherited)
                        {
                            addUserPermissions(node, context, inh, personNodes, nodeRef, true);
                        }

                        addUserPermissions(node, context, local, personNodes, nodeRef, false);
                    }
                }
                else
                {
                    // need a map (dummy node) to represent props for this Group
                    // Authority

                    if (includeInherited)
                    {
                        addGroupPermissions(authority, context, personNodes, inh, true);
                    }
                    addGroupPermissions(authority, context, personNodes, local, false);
                }
            }

            // commit the transaction
            tx.commit();
        }
        catch (InvalidNodeRefException refErr)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, Repository.ERROR_NODEREF), new Object[] { refErr.getNodeRef() }));
            personNodes = Collections.<Map> emptyList();
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
        }
        catch (Throwable err)
        {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, Repository.ERROR_GENERIC), err.getMessage()), err);
            personNodes = Collections.<Map> emptyList();
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
        }

        return personNodes;
    }

    /**
     * Add group permissions
     * 
     * @param authority
     * @param context
     * @param personNodes
     * @param perms
     * @param inherited
     */
    private void addGroupPermissions(final String authority, FacesContext context, List<Map> personNodes, List<String> perms, final boolean inherited)
    {
        if (perms == null || perms.size() == 0)
            return;
        Map<String, Object> node = new HashMap<String, Object>(5, 1.0f);
        if (authority.startsWith(PermissionService.GROUP_PREFIX) == true)
        {
            node.put("fullName", authority.substring(PermissionService.GROUP_PREFIX.length()));
        }
        else
        {
            node.put("fullName", authority);
        }
        node.put("userName", authority);
        node.put("id", authority);
        node.put("perms", UserMembersBean.roleListToString(context, perms));
        node.put("icon", WebResources.IMAGE_GROUP);
        node.put("inherited", inherited);

        personNodes.add(node);
    }

    /**
     * Add users permissions
     * 
     * @param node
     * @param context
     * @param perms
     * @param personNodes
     * @param nodeRef
     * @param inherited
     */
    private void addUserPermissions(MapNode node, FacesContext context, List<String> perms, List<Map> personNodes, NodeRef nodeRef, boolean inherited)
    {
        if (perms == null || perms.size() == 0)
            return;
        Map<String, Object> props = (Map<String, Object>) ((QNameNodeMap) node.getProperties()).clone();
        String firstName = (String)node.get("firstName");
        String lastName = (String)node.get("lastName");
        props.put("fullName", (firstName != null ? firstName : "") + ' ' + (lastName != null ? lastName : ""));
        props.put("perms", UserMembersBean.roleListToString(context, perms));
        props.put("icon", WebResources.IMAGE_PERSON);
        props.put("inherited", inherited);
        
        personNodes.add(props);
    }

    /**
     * Separate permissions on inherited and local
     * 
     * @param authority
     * @param person
     * @param parentPerson
     * @param inherited
     * @param local
     */
    private void divisionPermissions(String authority, Map<String, List<String>> person, Map<String, List<String>> parentPerson, List<String> inherited, List<String> local)
    {
        List<String> parentPerms = parentPerson.get(authority);
        List<String> perms = person.get(authority);
        if (parentPerms == null)
        {
            local.addAll(perms);
            return;
        }
        if (perms.equals(parentPerms))
        {
            inherited.addAll(perms);
            return;
        }
        for (String perm : perms)
        {
            if (parentPerms.contains(perm))
            {
                inherited.add(perm);
            }
            else
            {
                local.add(perm);
            }
        }
    }

    /**
     * @param permissions
     * @param permsToDisplay
     * @return
     */
    private Map<String, List<String>> getPerson(Set<AccessPermission> permissions, Set<String> permsToDisplay)
    {
        Map<String, List<String>> permissionMap = new HashMap<String, List<String>>(8, 1.0f);
        for (AccessPermission permission : permissions)
        {
            if (permsToDisplay.contains(permission.getPermission()))
            {
                // we are only interested in Allow and not groups/owner etc.
                if (permission.getAccessStatus() == AccessStatus.ALLOWED
                        && (permission.getAuthorityType() == AuthorityType.USER || permission.getAuthorityType() == AuthorityType.GROUP
                                || permission.getAuthorityType() == AuthorityType.GUEST || permission.getAuthorityType() == AuthorityType.EVERYONE))
                {
                    String authority = permission.getAuthority();

                    List<String> userPermissions = permissionMap.get(authority);
                    if (userPermissions == null)
                    {
                        // create for first time
                        userPermissions = new ArrayList<String>(4);
                        permissionMap.put(authority, userPermissions);
                    }
                    // add the permission name for this authority
                    userPermissions.add(permission.getPermission());
                }
            }
        }
        return permissionMap;
    }

    /**
     * Get available permissions
     * 
     * @return Set of permissions
     */
    public static Set<String> getPermissionsForType()
    {
        Set<String> permissions = new LinkedHashSet<String>(3);
        permissions.add(PermissionService.READ);
        permissions.add(PermissionService.WRITE);
        permissions.add(PermissionService.DELETE);

        return permissions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.app.context.IContextListener#areaChanged()
     */
    public void areaChanged()
    {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
     */
    public void contextUpdated()
    {
        if (this.usersRichList != null)
        {
            this.usersRichList.setValue(null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.app.context.IContextListener#spaceChanged()
     */
    public void spaceChanged()
    {
        // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.FilterViewSupport#filterModeChanged(javax.faces.event.ActionEvent)
     */
    public void filterModeChanged(ActionEvent event)
    {
        UIModeList viewList = (UIModeList) event.getComponent();
        setFilterMode(viewList.getValue().toString());
        // force the list to be re-queried when the page is refreshed
        if (this.usersRichList != null)
        {
            this.usersRichList.setValue(null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.FilterViewSupport#getFilterItems()
     */
    public List<UIListItem> getFilterItems()
    {
        FacesContext context = FacesContext.getCurrentInstance();
        List<UIListItem> items = new ArrayList<UIListItem>(2);

        UIListItem item1 = new UIListItem();
        item1.setValue(INHERITED);
        item1.setLabel(Application.getMessage(context, INHERITED));
        items.add(item1);

        UIListItem item2 = new UIListItem();
        item2.setValue(LOCAL);
        item2.setLabel(Application.getMessage(context, LOCAL));
        items.add(item2);

        return items;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.FilterViewSupport#getFilterMode()
     */
    public String getFilterMode()
    {
        return filterMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.FilterViewSupport#setFilterMode(java.lang.String)
     */
    public void setFilterMode(final String filterMode)
    {
        this.filterMode = filterMode;

        // clear datalist cache ready to change results based on filter setting
        contextUpdated();

    }

    /**
     * Getter for usersRichList
     * 
     * @return usersRichList
     */
    public UIRichList getUsersRichList()
    {
        return usersRichList;
    }

    /**
     * Setter for usersRichList
     * 
     * @param usersRichList
     */
    public void setUsersRichList(final UIRichList usersRichList)
    {
        this.usersRichList = usersRichList;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#restored()
     */
    @Override
    public void restored()
    {
        super.restored();
        contextUpdated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getCancelButtonLabel()
     */
    @Override
    public String getCancelButtonLabel()
    {
        return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
     */
    @Override
    public boolean getFinishButtonDisabled()
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.web.bean.dialog.BaseDialogBean#getActionsContext()
     */
    @Override
    public Object getActionsContext()
    {
        return this;
    }

    @Override
    public String getContainerTitle()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        String pattern = Application.getMessage(fc, isRendered() ? MSG_MANAGE_PERMS_FOR : MSG_VIEW_PERMS_FOR);

        return MessageFormat.format(pattern, getAvmBrowseBean().getAvmActionNode().getName());
    }

    /**
     * @return true if node in Staging Sandbox
     */
    public boolean isRendered()
    {
        boolean result = false;
        final String path = AVMNodeConverter.ToAVMVersionPath(getAvmBrowseBean().getAvmActionNode().getNodeRef()).getSecond();
        if (!AVMUtil.isMainStore(AVMUtil.getStoreName(path)))
        {
            result = true;
        }
        return result;

    }

    /**
     * Getter for inheritParenSpacePermissions
     * 
     * @return inheritParenSpacePermissions
     */
    public boolean isInheritParenSpacePermissions()
    {
        return inheritParenSpacePermissions;
    }

    /**
     * Setter for inheritParenSpacePermissions Set the global inheritance behaviour for permissions on a node.
     * 
     * @param inheritParenSpacePermissions
     */
    public void setInheritParenSpacePermissions(final boolean inheritParenSpacePermissions)
    {
        this.inheritParenSpacePermissions = inheritParenSpacePermissions;
        getPermissionService().setInheritParentPermissions(getAvmBrowseBean().getAvmActionNode().getNodeRef(), inheritParenSpacePermissions);
        contextUpdated();
    }

    public void inheritPermissionsValueChanged(ValueChangeEvent event)
    {
        boolean inheritPermissions = (Boolean)event.getNewValue();
        setInheritParenSpacePermissions(inheritPermissions);
    }
}
