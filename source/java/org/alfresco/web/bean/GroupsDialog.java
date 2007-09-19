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
package org.alfresco.web.bean;

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
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.groups.GroupsProperties;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIModeList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing Bean for the Groups Management pages.
 * 
 * @author Kevin Roast
 */
public class GroupsDialog extends BaseDialogBean implements IContextListener
{
   protected GroupsProperties properties;
   
   private static final String FILTER_CHILDREN = "children";
   private static final String MSG_GROUPS = "root_groups";
   
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
   // Bean property getters and setters
   
   public void setProperties(GroupsProperties properties)
   {
      this.properties = properties;
   }

   /**
    * @param filterMode The filterMode to set.
    */
   public void setFilterMode(String filterMode)
   {
      properties.setFilterMode(filterMode);
      
      // clear datalist cache ready to change results based on filter setting
      contextUpdated();
   }
   
   /**
    * @param group Set the Group to be used for the current action screen.
    */
   public void setActionGroup(String group)
   {
      properties.setActionGroup(group);
      
      if (group != null)
      {
         // calculate action group metadata
         properties.setActionGroupName(properties.getAuthService().getShortName(group));
         int count = properties.getAuthService().getContainedAuthorities(AuthorityType.GROUP, group, false).size();
         count += properties.getAuthService().getContainedAuthorities(AuthorityType.USER, group, false).size();
         properties.setActionGroupItems(count);
      }
      else
      {
         properties.setActionGroupName(null);
         properties.setActionGroupItems(0);
      }
      
      // clear value used by Create Group form
      properties.setName(null);
   }
   
   /**
    * Set the current Group Authority.
    * <p>
    * Setting this value causes the UI to update and display the specified node as current.
    * 
    * @param group      The current group authority.
    */
   public void setCurrentGroup(String group, String groupName)
   {
      if (logger.isDebugEnabled())
         logger.debug("Setting current group: " + group);
      
      // set the current Group Authority for our UI context operations
      properties.setGroup(group);
      properties.setGroupName(groupName);
      
      // inform that the UI needs updating after this change 
      contextUpdated();
   }
   
   /**
    * @return Breadcrumb location list
    */
   public List<IBreadcrumbHandler> getLocation()
   {
      if (properties.getLocation() == null)
      {
         List<IBreadcrumbHandler> loc = new ArrayList<IBreadcrumbHandler>(8);
         loc.add(new GroupBreadcrumbHandler(null,
               Application.getMessage(FacesContext.getCurrentInstance(), MSG_GROUPS)));
         
         properties.setLocation(loc);
      }
      
      return properties.getLocation();
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
         boolean immediate = (properties.getFilterMode().equals(FILTER_CHILDREN));
         if (properties.getGroup() == null)
         {
            // root groups
            if (immediate == true)
            {
               authorities = properties.getAuthService().getAllRootAuthorities(AuthorityType.GROUP);
            }
            else
            {
               authorities = properties.getAuthService().getAllAuthorities(AuthorityType.GROUP);
            }
         }
         else
         {
            // sub-group of an existing group
            authorities = properties.getAuthService().getContainedAuthorities(AuthorityType.GROUP, properties.getGroup(), immediate);
         }
         groups = new ArrayList<Map>(authorities.size());
         for (String authority : authorities)
         {
            Map<String, String> authMap = new HashMap<String, String>(3, 1.0f);

            String name = properties.getAuthService().getShortName(authority);
            authMap.put("name", name);
            authMap.put("id", authority);
            
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
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         Set<String> authorities;
         if (properties.getGroup() == null)
         {
            authorities = Collections.<String>emptySet();
         }
         else
         {
            // users of an existing group
            boolean immediate = (properties.getFilterMode().equals(FILTER_CHILDREN));
            authorities = properties.getAuthService().getContainedAuthorities(AuthorityType.USER, properties.getGroup(), immediate);
         }
         users = new ArrayList<Map>(authorities.size());
         for (String authority : authorities)
         {
            Map<String, String> authMap = new HashMap<String, String>(3, 1.0f);
            
            String userName = properties.getAuthService().getShortName(authority);
            authMap.put("userName", userName);
            authMap.put("id", authority);
            
            // get Person details for this Authority
            NodeRef ref = properties.getPersonService().getPerson(authority);
            String firstName = (String)this.nodeService.getProperty(ref, ContentModel.PROP_FIRSTNAME);
            String lastName = (String)this.nodeService.getProperty(ref, ContentModel.PROP_LASTNAME);
            
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
    * Set the Group to be used for next action dialog
    */
   public void setupGroupAction(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String group = params.get("id");
      if (group != null && group.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Setup for action, setting current Group to: " + group);
         
         // prepare a node for the action context
         setActionGroup(group);
         
         // clear datalist cache ready from return from action dialog
         contextUpdated();
      }
   }
   
   /**
    * Clear the Group action context - e.g. ready for a Create Root Group operation
    */
   public void clearGroupAction(ActionEvent event)
   {
      setActionGroup(null);
      
      // clear datalist cache ready from return from action dialog
      contextUpdated();
   }
   
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
         try
         {
            properties.getAuthService().removeAuthority(properties.getGroup(), authority);
            
            // refresh UI after change
            contextUpdated();
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         }
      }
   }
   
   /**
    * Change the current view mode based on user selection
    */
   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      
      // update view mode from user selection
      properties.setViewMode(viewList.getValue().toString());
   }
   
   /**
    * Change the current list filter mode based on user selection
    */
   public void filterModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      
      // update list filter mode from user selection
      setFilterMode(viewList.getValue().toString());
   }
   
   /**
    * Update the breadcrumb with the clicked Group location
    */
   private void updateUILocation(String group)
   {
      String groupName = properties.getAuthService().getShortName(group);
      properties.getLocation().add(new GroupBreadcrumbHandler(group, groupName));
      this.setCurrentGroup(group, groupName);
   }
   
   public void removeFromBreadcrumb(String group)
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
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return null;
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
      properties.getGroupsRichList().setValue(null);
      properties.getUsersRichList().setValue(null);
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
         properties.setLocation( (List)breadcrumb.getValue() );
         
         return null;
      }
      
      public String Group;
      public String Label;
   }
   
   /**
    * Simple wrapper bean exposing user authority and person details for JSF results list
    */
   public static class UserAuthorityDetails
   {
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
