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
package org.alfresco.web.bean.groups;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.dialog.ChangeViewSupport;
import org.alfresco.web.bean.dialog.FilterViewSupport;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIListItem;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing Bean for the Groups Management pages.
 * 
 * @author Kevin Roast
 */
public class GroupsDialog extends BaseDialogBean 
   implements IContextListener, FilterViewSupport, ChangeViewSupport
{
   private static final long serialVersionUID = -624617545796275734L;
   
   public static final String KEY_GROUP = "group";
   public static final String PARAM_GROUP = "group";
   public static final String PARAM_GROUP_NAME = "groupName";
   
   /** The AuthorityService to be used by the bean */
   transient private AuthorityService authService;

   /** personService bean reference */
   transient private PersonService personService;

   /** Component references */
   protected UIRichList groupsRichList;
   protected UIRichList usersRichList;

   /** Currently visible Group Authority */
   protected String group = null;
   protected String groupName = null;
   
   /** RichList view mode */
   protected String viewMode = VIEW_ICONS;
   
   /** Filter mode */
   protected String filterMode = FILTER_CHILDREN;

   /** Groups path breadcrumb location */
   protected List<IBreadcrumbHandler> location = null;

   private static final String VIEW_ICONS = "icons";
   private static final String VIEW_DETAILS = "details";
   private static final String FILTER_CHILDREN = "children";
   private static final String FILTER_ALL = "all";
   
   private static final String LABEL_VIEW_ICONS = "group_icons";
   private static final String LABEL_VIEW_DETAILS = "group_details";
   private static final String LABEL_FILTER_CHILDREN = "group_filter_children";
   private static final String LABEL_FILTER_ALL = "group_filter_all";
   
   private static final String MSG_ROOT_GROUPS = "root_groups";
   private static final String MSG_CLOSE = "close";
   
   private static Log logger = LogFactory.getLog(GroupsDialog.class);
   
   // ------------------------------------------------------------------------------
   // Construction 
   
   /**
    * Default Constructor
    */
   public GroupsDialog()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
   }
   
   @Override
   public String getContainerSubTitle()
   {
      String subtitle = null;

      if (this.group != null)
      {
         subtitle = this.groupName;
      }
      else
      {
         subtitle = Application.getMessage(FacesContext.getCurrentInstance(), MSG_ROOT_GROUPS);
      }

      return subtitle;
   }
   
   @Override
   public String getCancelButtonLabel() 
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_CLOSE);
   }
   
   @Override
   public void restored()
   {
      Object groupToRemove = FacesContext.getCurrentInstance().getExternalContext().
            getRequestMap().get(KEY_GROUP);
      if (groupToRemove != null)
      {
         if (logger.isDebugEnabled())
            logger.debug("Removing group '" + groupToRemove + "' from breadcrumb");
         
         removeFromBreadcrumb((String)groupToRemove);
      }
   }
   
   @Override
   public Object getActionsContext()
   {
      return this;
   }
   
   // ------------------------------------------------------------------------------
   // FilterViewSupport implementation
   
   public List<UIListItem> getFilterItems()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      List<UIListItem> items = new ArrayList<UIListItem>(2);
      
      UIListItem item1 = new UIListItem();
      item1.setValue(FILTER_CHILDREN);
      item1.setLabel(Application.getMessage(context, LABEL_FILTER_CHILDREN));
      items.add(item1);
      
      UIListItem item2 = new UIListItem();
      item2.setValue(FILTER_ALL);
      item2.setLabel(Application.getMessage(context, LABEL_FILTER_ALL));
      items.add(item2);
      
      return items;
   }
   
   public void filterModeChanged(ActionEvent event)
   {
      UIModeList filterList = (UIModeList)event.getComponent();
      
      // update list filter mode from user selection
      setFilterMode(filterList.getValue().toString());
   }
   
   public String getFilterMode()
   {
      return this.filterMode;
   }
   
   public void setFilterMode(String filterMode)
   {
      this.filterMode = filterMode;
      
      // clear datalist cache ready to change results based on filter setting
      contextUpdated();
   }
   
   // ------------------------------------------------------------------------------
   // ChangeViewSupport implementation
   
   public List<UIListItem> getViewItems()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      List<UIListItem> items = new ArrayList<UIListItem>(2);
      
      UIListItem item1 = new UIListItem();
      item1.setValue(VIEW_ICONS);
      item1.setLabel(Application.getMessage(context, LABEL_VIEW_ICONS));
      items.add(item1);
      
      UIListItem item2 = new UIListItem();
      item2.setValue(VIEW_DETAILS);
      item2.setLabel(Application.getMessage(context, LABEL_VIEW_DETAILS));
      items.add(item2);
      
      return items;
   }
   
   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      
      // update view mode from user selection
      setViewMode(viewList.getValue().toString());
   }
   
   public String getViewMode()
   {
      return this.viewMode;
   }

   public void setViewMode(String viewMode)
   {
      this.viewMode = viewMode;
   }
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters

   public String getGroup()
   {
      return this.group;
   }

   public String getGroupName()
   {
      return this.groupName;
   }
   
   public void setAuthService(AuthorityService authService)
   {
      this.authService = authService;
   }
   
   private AuthorityService getAuthorityService()
   {
      if (authService == null)
      {
         authService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthorityService();
      }
      return authService;
   }

   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
   
   private PersonService getPersonService()
   {
      if (personService == null)
      {
         personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService();
      }
      return personService;
   }

   public UIRichList getGroupsRichList()
   {
      return groupsRichList;
   }

   public void setGroupsRichList(UIRichList groupsRichList)
   {
      this.groupsRichList = groupsRichList;
   }

   public UIRichList getUsersRichList()
   {
      return usersRichList;
   }

   public void setUsersRichList(UIRichList usersRichList)
   {
      this.usersRichList = usersRichList;
   }
   
   /**
    * @return Breadcrumb location list
    */
   public List<IBreadcrumbHandler> getLocation()
   {
      if (this.location == null)
      {
         List<IBreadcrumbHandler> loc = new ArrayList<IBreadcrumbHandler>(8);
         loc.add(new GroupBreadcrumbHandler(null,
               Application.getMessage(FacesContext.getCurrentInstance(), MSG_ROOT_GROUPS)));
         
         this.location = loc;
      }
      
      return this.location;
   }
   
   /**
    * @return The list of group objects to display. Returns the list of root groups or the
    *         list of sub-groups for the current group if set.
    */
   public List<Map> getGroups()
   {
      List<Map> groups;
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         Set<String> authorities;
         boolean immediate = (this.filterMode.equals(FILTER_CHILDREN));
         if (this.group == null)
         {
            // root groups
            if (immediate == true)
            {
               authorities = this.getAuthorityService().getAllRootAuthorities(AuthorityType.GROUP);
            }
            else
            {
               authorities = this.getAuthorityService().getAllAuthorities(AuthorityType.GROUP);
            }
         }
         else
         {
            // sub-group of an existing group
            authorities = this.getAuthorityService().getContainedAuthorities(AuthorityType.GROUP, this.group, immediate);
         }
         groups = new ArrayList<Map>(authorities.size());
         for (String authority : authorities)
         {
            Map<String, String> authMap = new HashMap<String, String>(3, 1.0f);

            String name = this.getAuthorityService().getShortName(authority);
            authMap.put("name", name);
            authMap.put("id", authority);
            authMap.put("group", authority);
            authMap.put("groupName", name);
            
            groups.add(authMap);
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         groups = Collections.<Map>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return groups;
   }
   
   /**
    * @return The list of user objects to display. Returns the list of user for the current group.
    */
   public List<Map> getUsers()
   {
      List<Map> users;
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         Set<String> authorities;
         if (this.group == null)
         {
            authorities = Collections.<String>emptySet();
         }
         else
         {
            // users of an existing group
            boolean immediate = (this.filterMode.equals(FILTER_CHILDREN));
            authorities = this.getAuthorityService().getContainedAuthorities(AuthorityType.USER, this.group, immediate);
         }
         users = new ArrayList<Map>(authorities.size());
         for (String authority : authorities)
         {
            Map<String, String> authMap = new HashMap<String, String>(3, 1.0f);
            
            String userName = this.getAuthorityService().getShortName(authority);
            authMap.put("userName", userName);
            authMap.put("id", authority);
            
            // get Person details for this Authority
            NodeRef ref = this.getPersonService().getPerson(authority);
            String firstName = (String)this.getNodeService().getProperty(ref, ContentModel.PROP_FIRSTNAME);
            String lastName = (String)this.getNodeService().getProperty(ref, ContentModel.PROP_LASTNAME);
            
            // build a sensible label for display
            StringBuilder label = new StringBuilder(48);
            label.append(firstName)
                 .append(' ')
                 .append(lastName);
            authMap.put("name", label.toString());
            
            users.add(authMap);
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         users = Collections.<Map>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return users;
   }
   
   /**
    * Set the current Group Authority.
    * <p>
    * Setting this value causes the UI to update and display the specified node as current.
    * 
    * @param group      The current group authority.
    */
   protected void setCurrentGroup(String group, String groupName)
   {
      if (logger.isDebugEnabled())
         logger.debug("Setting current group: " + group);
      
      // set the current Group Authority for our UI context operations
      this.group = group;
      this.groupName = groupName;
      
      // inform that the UI needs updating after this change 
      contextUpdated();
   }
   
   // ------------------------------------------------------------------------------
   // Action handlers 
   
   /**
    * Action called when a Group folder is clicked.
    * Navigate into the Group and show child Groups and child Users.
    */
   public void clickGroup(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String group = params.get("id");
      if (group != null && group.length() != 0)
      {
         // refresh UI based on node selection
         updateUILocation(group);
      }
   }
   
   /**
    * Remove specified user from the current group
    */
   public void removeUser(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String authority = params.get("id");
      if (authority != null && authority.length() != 0)
      {
         UserTransaction tx = null;
         try
         {
            FacesContext context = FacesContext.getCurrentInstance();
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            this.getAuthorityService().removeAuthority(this.group, authority);
            
            // commit the transaction
            tx.commit();
            
            // refresh UI after change
            contextUpdated();
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
   }
   
   // ------------------------------------------------------------------------------
   // Helpers 
   
   /**
    * Update the breadcrumb with the clicked Group location
    */
   protected void updateUILocation(String group)
   {
      String groupName = this.getAuthorityService().getShortName(group);
      this.location.add(new GroupBreadcrumbHandler(group, groupName));
      this.setCurrentGroup(group, groupName);
   }
   
   protected void removeFromBreadcrumb(String group)
   {
      // remove this node from the breadcrumb if required
      List<IBreadcrumbHandler> location = getLocation();
      GroupBreadcrumbHandler handler = (GroupBreadcrumbHandler) location.get(location.size() - 1);
      
      // see if the current breadcrumb location is our Group
      if (group.equals(handler.Group))
      {
         location.remove(location.size() - 1);
         
         // now work out which Group to set the list to refresh against
         if (location.size() != 0)
         {
            handler = (GroupBreadcrumbHandler) location.get(location.size() - 1);
            this.setCurrentGroup(handler.Group, handler.Label);
         }
      }
   }
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation 
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      if (logger.isDebugEnabled())
         logger.debug("Invalidating Group Management Components...");
      
      // force a requery of the richlist dataset
      if (this.groupsRichList != null)
      {
         this.groupsRichList.setValue(null);
      }
      
      if (this.usersRichList != null)
      {
         this.usersRichList.setValue(null);
      }
   }
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#areaChanged()
    */
   public void areaChanged()
   {
      // nothing to do
   }

   /**
    * @see org.alfresco.web.app.context.IContextListener#spaceChanged()
    */
   public void spaceChanged()
   {
      // nothing to do
   }
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class to handle breadcrumb interaction for Group pages
    */
   private class GroupBreadcrumbHandler implements IBreadcrumbHandler
   {
      private static final long serialVersionUID = 1871876653151036630L;
      
      /**
       * Constructor
       * 
       * @param group      The group for this navigation element if any
       * @param label      Element label
       */
      public GroupBreadcrumbHandler(String group, String label)
      {
         this.Group = group;
         this.Label = label;
      }
      
      /**
       * @see java.lang.Object#toString()
       */
      public String toString()
      {
         return this.Label;
      }

      /**
       * @see org.alfresco.web.ui.common.component.IBreadcrumbHandler#navigationOutcome(org.alfresco.web.ui.common.component.UIBreadcrumb)
       */
      public String navigationOutcome(UIBreadcrumb breadcrumb)
      {
         // All group breadcrumb elements relate to a Group
         // when selected we set the current Group Id and return
         setCurrentGroup(this.Group, this.Label);
         location = (List<IBreadcrumbHandler>)breadcrumb.getValue();
         
         return null;
      }
      
      public String Group;
      public String Label;
   }
   
   /**
    * Simple wrapper bean exposing user authority and person details for JSF results list
    */
   public static class UserAuthorityDetails implements Serializable
   {
      private static final long serialVersionUID = 1056255933962068348L;
      
      public UserAuthorityDetails(String name, String authority)
      {
         this.name = name;
         this.authority = authority;
      }
      
      public String getName()
      {
         return this.name;
      }
      
      public String getAuthority()
      {
         return this.authority;
      }
      
      private String name;
      private String authority;
   }
}
