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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.IBreadcrumbHandler;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.alfresco.web.ui.common.component.UIModeList;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.apache.log4j.Logger;

/**
 * Backing Bean for the Groups Management pages.
 * 
 * @author Kevin Roast
 */
public class GroupsBean implements IContextListener
{
   private static final String FILTER_CHILDREN = "children";
   private static final String FILTER_ALL      = "all";
   
   private static final String DEFAULT_OUTCOME = "finish";
   
   private static final String MSG_ERR_EXISTS = "groups_err_exists";
   private static final String MSG_GROUPS = "root_groups";
   
   private static Logger logger = Logger.getLogger(GroupsBean.class);
   
   /** The NodeService to be used by the bean */
   private NodeService nodeService;
   
   /** The AuthorityService to be used by the bean */
   private AuthorityService authService;
   
   /** personService bean reference */
   private PersonService personService;
   
   /** Component references */
   private UIRichList groupsRichList;
   private UIRichList usersRichList;
   
   /** datamodel for table of users added to group */
   private DataModel usersDataModel = null;
   
   /** Currently visible Group Authority */
   private String group = null;
   private String groupName = null;
   
   /** Action group authority */
   private String actionGroup = null;
   private String actionGroupName = null;
   private int actionGroupItems = 0;
   
   /** selected users to be added to a group */
   private List<UserAuthorityDetails> usersForGroup = null;
   
   /** Dialog properties */
   private String name = null;
   
   /** RichList view mode */
   private String viewMode = "icons"; 
   
   /** List filter mode */
   private String filterMode = FILTER_CHILDREN;
   
   /** Groups path breadcrumb location */
   private List<IBreadcrumbHandler> location = null;
   
   
   // ------------------------------------------------------------------------------
   // Construction 
   
   /**
    * Default Constructor
    */
   public GroupsBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters 
   
   /**
    * @param nodeService      The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param authService      The AuthorityService to set.
    */
   public void setAuthorityService(AuthorityService authService)
   {
      this.authService = authService;
   }
   
   /**
    * @param personService   The PersonService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }

   /**
    * @return Returns the groups RichList to set.
    */
   public UIRichList getGroupsRichList()
   {
      return this.groupsRichList;
   }
   
   /**
    * @param list    The RichList to set.
    */
   public void setGroupsRichList(UIRichList list)
   {
      this.groupsRichList = list;
   }
   
   /**
    * @return Returns the users RichList.
    */
   public UIRichList getUsersRichList()
   {
      return this.usersRichList;
   }

   /**
    * @param usersRichList The RichList to set.
    */
   public void setUsersRichList(UIRichList usersRichList)
   {
      this.usersRichList = usersRichList;
   }
   
   /**
    * @return Returns the usersDataModel.
    */
   public DataModel getUsersDataModel()
   {
      if (this.usersDataModel == null)
      {
         this.usersDataModel = new ListDataModel();
      }
      
      this.usersDataModel.setWrappedData(this.usersForGroup);
      
      return this.usersDataModel;
   }

   /**
    * @param usersDataModel The usersDataModel to set.
    */
   public void setUsersDataModel(DataModel usersDataModel)
   {
      this.usersDataModel = usersDataModel;
   }

   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * @return Returns the viewMode.
    */
   public String getViewMode()
   {
      return this.viewMode;
   }

   /**
    * @param viewMode The viewMode to set.
    */
   public void setViewMode(String viewMode)
   {
      this.viewMode = viewMode;
   }
   
   /**
    * @return Returns the filterMode.
    */
   public String getFilterMode()
   {
      return this.filterMode;
   }

   /**
    * @param filterMode The filterMode to set.
    */
   public void setFilterMode(String filterMode)
   {
      this.filterMode = filterMode;
      
      // clear datalist cache ready to change results based on filter setting
      contextUpdated();
   }
   
   /**
    * @return Returns the Group being used for the current action screen.
    */
   public String getActionGroup()
   {
      return this.actionGroup;
   }
   
   /**
    * @param group     Set the Group to be used for the current action screen.
    */
   public void setActionGroup(String group)
   {
      this.actionGroup = group;
      
      if (group != null)
      {
         // calculate action group metadata
         setActionGroupName(this.authService.getShortName(group));
         int count = this.authService.getContainedAuthorities(AuthorityType.GROUP, group, false).size();
         count += this.authService.getContainedAuthorities(AuthorityType.USER, group, false).size();
         setActionGroupItems(count);
      }
      else
      {
         setActionGroupName(null);
         setActionGroupItems(0);
      }
      
      // clear value used by Create Group form
      this.name = null;
      
      // clear list for Add Users to Group screen
      this.usersForGroup = new ArrayList<UserAuthorityDetails>();
   }
   
   /**
    * @return Returns the actionGroupName.
    */
   public String getActionGroupName()
   {
      return this.actionGroupName;
   }

   /**
    * @param actionGroupName The actionGroupName to set.
    */
   public void setActionGroupName(String actionGroupName)
   {
      this.actionGroupName = actionGroupName;
   }
   
   /**
    * @return Returns the action Group Items count.
    */
   public int getActionGroupItems()
   {
      return this.actionGroupItems;
   }

   /**
    * @param actionGroupItems The action Group Items count to set.
    */
   public void setActionGroupItems(int actionGroupItems)
   {
      this.actionGroupItems = actionGroupItems;
   }
   
   /**
    * @return The currently displayed group or null if at the root.
    */
   public String getCurrentGroup()
   {
      return this.group;
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
      this.group = group;
      this.groupName = groupName;
      
      // inform that the UI needs updating after this change 
      contextUpdated();
   }
   
   /**
    * @return Returns the groupName.
    */
   public String getGroupName()
   {
      return this.groupName;
   }

   /**
    * @param groupName The groupName to set.
    */
   public void setGroupName(String groupName)
   {
      this.groupName = groupName;
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
               Application.getMessage(FacesContext.getCurrentInstance(), MSG_GROUPS)));
         
         this.location = loc;
      }
      return this.location;
   }
   
   /**
    * @param location Breadcrumb location list
    */
   public void setLocation(List<IBreadcrumbHandler> location)
   {
      this.location = location;
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
               authorities = this.authService.getAllRootAuthorities(AuthorityType.GROUP);
            }
            else
            {
               authorities = this.authService.getAllAuthorities(AuthorityType.GROUP);
            }
         }
         else
         {
            // sub-group of an existing group
            authorities = this.authService.getContainedAuthorities(AuthorityType.GROUP, group, immediate);
         }
         groups = new ArrayList<Map>(authorities.size());
         for (String authority : authorities)
         {
            Map authMap = new HashMap(3, 1.0f);
            
            String name = this.authService.getShortName(authority);
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
         if (this.group == null)
         {
            authorities = Collections.<String>emptySet();
         }
         else
         {
            // users of an existing group
            boolean immediate = (this.filterMode.equals(FILTER_CHILDREN));
            authorities = this.authService.getContainedAuthorities(AuthorityType.USER, group, immediate);
         }
         users = new ArrayList<Map>(authorities.size());
         for (String authority : authorities)
         {
            Map authMap = new HashMap(3, 1.0f);
            
            String userName = this.authService.getShortName(authority);
            authMap.put("userName", userName);
            authMap.put("id", authority);
            
            // get Person details for this Authority
            NodeRef ref = this.personService.getPerson(authority);
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
    * Validate password field data is acceptable
    */
   public void validateGroupName(FacesContext context, UIComponent component, Object value)
         throws ValidatorException
   {
      String name = (String)value;
      if (name.indexOf('\'') != -1 || name.indexOf('"') != -1 || name.indexOf('\\') != -1)
      {
         String err = MessageFormat.format(Application.getMessage(context, "groups_err_group_name"),
               new Object[]{"', \", \\"});
         throw new ValidatorException(new FacesMessage(err));
      }
   }
   
   /**
    * Query callback method executed by the Generic Picker component.
    * This method is part of the contract to the Generic Picker, it is up to the backing bean
    * to execute whatever query is appropriate and return the results.
    * 
    * @param filterIndex        Index of the filter drop-down selection
    * @param contains           Text from the contains textbox
    * 
    * @return An array of SelectItem objects containing the results to display in the picker.
    */
   public SelectItem[] pickerCallback(int filterIndex, String contains)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      SelectItem[] items;
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // build xpath to match available User/Person objects
         ServiceRegistry services = Repository.getServiceRegistry(context);
         NodeRef peopleRef = personService.getPeopleContainer();
         String xpath = "*[like(@" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + "firstName, '%" + contains + "%', false)" +
                 " or " + "like(@" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + "lastName, '%" + contains + "%', false)]";
         
         List<NodeRef> nodes = services.getSearchService().selectNodes(
               peopleRef,
               xpath,
               null,
               services.getNamespaceService(),
               false);
         
         ArrayList<SelectItem> itemList = new ArrayList<SelectItem>(nodes.size());
         for (NodeRef personRef : nodes)
         {
            String username = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
            if (PermissionService.GUEST_AUTHORITY.equals(username) == false)
            {
               String firstName = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
               String lastName = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
               
               SelectItem item = new SortableSelectItem(username, firstName + " " + lastName, lastName);
               itemList.add(item);
            }
         }
         items = new SelectItem[itemList.size()];
         itemList.toArray(items);
         
         // commit the transaction
         tx.commit();
      }
      catch (Exception err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err );
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         
         items = new SelectItem[0];
      }
      
      return items;
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
    * Action handler called on Create Group finish button click.
    */
   public String finishCreate()
   {
      String outcome = DEFAULT_OUTCOME;
      
      FacesContext context = FacesContext.getCurrentInstance();
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // create new Group using Authentication Service
         String groupName = this.authService.getName(AuthorityType.GROUP, this.name);
         if (this.authService.authorityExists(groupName) == false)
         {
            this.authService.createAuthority(AuthorityType.GROUP, getActionGroup(), this.name);
         }
         else
         {
            Utils.addErrorMessage(Application.getMessage(context, MSG_ERR_EXISTS));
            outcome = null;
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), err.getMessage()), err);
         outcome = null;
      }
      
      return outcome;
   }
   
   /**
    * Action handler called on Delete Group finish button click.
    */
   public String finishDelete()
   {
      String outcome = DEFAULT_OUTCOME;
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // delete group using the Authentication Service
         this.authService.deleteAuthority(getActionGroup());
         
         // commit the transaction
         tx.commit();
         
         // remove this node from the breadcrumb if required
         List<IBreadcrumbHandler> location = getLocation();
         GroupBreadcrumbHandler handler = (GroupBreadcrumbHandler)location.get(location.size() - 1);
         
         // see if the current breadcrumb location is our Group
         if ( getActionGroup().equals(handler.Group) )
         {
            location.remove(location.size() - 1);
            
            // now work out which Group to set the list to refresh against
            if (location.size() != 0)
            {
               handler = (GroupBreadcrumbHandler)location.get(location.size() - 1);
               this.setCurrentGroup(handler.Group, handler.Label);
            }
         }
         
         // clear action context
         setActionGroup(null);
      }
      catch (Throwable err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         outcome = null;
      }
      
      return outcome;
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
            this.authService.removeAuthority(this.group, authority);
            
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
    * Action handler called when Finish button clicked on Add User to Group page
    */
   public String finishAddUser()
   {
      String outcome = DEFAULT_OUTCOME;
      
      UserTransaction tx = null;
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // add each selected user to the current group in turn
         for (UserAuthorityDetails wrapper : this.usersForGroup)
         {
            this.authService.addAuthority(getActionGroup(), wrapper.authority);
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         outcome = null;
      }
      
      return outcome;
   }
   
   /**
    * Add the selected User to the list for adding to a Group
    */
   public void addSelectedUsers(ActionEvent event)
   {
      UIGenericPicker picker = (UIGenericPicker)event.getComponent().findComponent("picker");
      String[] results = picker.getSelectedResults();
      if (results != null)
      {
         for (int i=0; i<results.length; i++)
         {
            String authority = results[i];
            
            // check for same authority so not added twice
            boolean foundExisting = false;
            for (int n=0; n<this.usersForGroup.size(); n++)
            {
               UserAuthorityDetails wrapper = this.usersForGroup.get(n);
               if (authority.equals(wrapper.getAuthority()))
               {
                  foundExisting = true;
                  break;
               }
            }
            
            if (foundExisting == false)
            {
               StringBuilder label = new StringBuilder(48);
               
               // build a display label showing the user person name
               if (this.personService.personExists(authority) == true)
               {
                  // found a Person with a User authority
                  NodeRef ref = this.personService.getPerson(authority);
                  String firstName = (String)this.nodeService.getProperty(ref, ContentModel.PROP_FIRSTNAME);
                  String lastName = (String)this.nodeService.getProperty(ref, ContentModel.PROP_LASTNAME);
                  
                  // build a sensible label for display
                  label.append(firstName)
                       .append(' ')
                       .append(lastName);
                  
                  // add a wrapper object with the details to the results list for display
                  UserAuthorityDetails userDetails = new UserAuthorityDetails(label.toString(), authority);
                  this.usersForGroup.add(userDetails);
               }
            }
         }
      }
   }
   
   /**
    * Action handler called when the Remove button is pressed to remove a user from the results list
    */
   public void removeUserSelection(ActionEvent event)
   {
      UserAuthorityDetails wrapper = (UserAuthorityDetails)this.usersDataModel.getRowData();
      if (wrapper != null)
      {
         this.usersForGroup.remove(wrapper);
      }
   }
   
   /**
    * Change the current view mode based on user selection
    */
   public void viewModeChanged(ActionEvent event)
   {
      UIModeList viewList = (UIModeList)event.getComponent();
      
      // update view mode from user selection
      setViewMode(viewList.getValue().toString());
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
      String groupName = this.authService.getShortName(group);
      this.location.add(new GroupBreadcrumbHandler(group, groupName));
      this.setCurrentGroup(group, groupName);
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
      this.groupsRichList.setValue(null);
      this.usersRichList.setValue(null);
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
         setLocation( (List)breadcrumb.getValue() );
         
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
