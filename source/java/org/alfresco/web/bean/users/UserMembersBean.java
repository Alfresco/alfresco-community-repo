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
package org.alfresco.web.bean.users;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.repo.WebResources;

/**
 * @author Kevin Roast
 */
public abstract class UserMembersBean implements IContextListener
{
   private static final String MSG_SUCCESS_INHERIT_NOT = "success_not_inherit_permissions";
   private static final String MSG_SUCCESS_INHERIT = "success_inherit_permissions";
   
   private static final String ERROR_DELETE = "error_remove_user";

   private static final String OUTCOME_FINISH = "finish";

   /** NodeService bean reference */
   protected NodeService nodeService;

   /** SearchService bean reference */
   protected SearchService searchService;
   
   /** PermissionService bean reference */
   protected PermissionService permissionService;
   
   /** PersonService bean reference */
   protected PersonService personService;
   
   /** BrowseBean bean refernce */
   protected BrowseBean browseBean;
   
   /** OwnableService bean reference */
   protected OwnableService ownableService;
   
   /** Component reference for Users RichList control */
   private UIRichList usersRichList;
   
   /** action context */
   private String personAuthority = null;
   
   /** action context */
   private String personName = null;
   
   /** datamodel for table of roles for current person */
   private DataModel personRolesDataModel = null;
   
   /** roles for current person */
   private List<PermissionWrapper> personRoles = null;
   
   
   /**
    * Default constructor
    */
   public UserMembersBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }
   
   
   // ------------------------------------------------------------------------------
   // Abstract methods
   
   /**
    * Returns the node that is being acted upon
    * 
    * @return The node to manage permissions for
    */
   public abstract Node getNode();
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters

   /**
    * @param nodeService        The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }

   /**
    * @param searchService      The search service
    */
   public void setSearchService(SearchService searchService)
   {
      this.searchService = searchService;
   }
   
   /**
    * @param permissionService  The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    * @param ownableService         The ownableService to set.
    */
   public void setOwnableService(OwnableService ownableService)
   {
      this.ownableService = ownableService;
   }
   
   /**
    * @param personService             The personService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
   
   /**
    * @param browseBean The BrowseBean to set.
    */
   public void setBrowseBean(BrowseBean browseBean)
   {
      this.browseBean = browseBean;
   }
   
   /**
    * @return Returns the usersRichList.
    */
   public UIRichList getUsersRichList()
   {
      return this.usersRichList;
   }

   /**
    * @param usersRichList  The usersRichList to set.
    */
   public void setUsersRichList(UIRichList usersRichList)
   {
      this.usersRichList = usersRichList;
   }
   
   /**
    * Returns the properties for current Person roles JSF DataModel
    * 
    * @return JSF DataModel representing the current Person roles
    */
   public DataModel getPersonRolesDataModel()
   {
      if (this.personRolesDataModel == null)
      {
         this.personRolesDataModel = new ListDataModel();
      }
      
      this.personRolesDataModel.setWrappedData(this.personRoles);
      
      return this.personRolesDataModel;
   }
   
   /**
    * @return Returns the current person authority.
    */
   public String getPersonAuthority()
   {
      return this.personAuthority;
   }

   /**
    * @param person     The person person authority to set.
    */
   public void setPersonAuthority(String person)
   {
      this.personAuthority = person;
   }
   
   /**
    * @return Returns the personName.
    */
   public String getPersonName()
   {
      return this.personName;
   }

   /**
    * @param personName The personName to set.
    */
   public void setPersonName(String personName)
   {
      this.personName = personName;
   }
   
   /**
    * @return true if the current user can change permissions on this Space
    */
   public boolean getHasChangePermissions()
   {
      return getNode().hasPermission(PermissionService.CHANGE_PERMISSIONS);
   }
   
   /**
    * @return Returns the inherit parent permissions flag set for the current space.
    */
   public boolean isInheritPermissions()
   {
      return this.permissionService.getInheritParentPermissions(getNode().getNodeRef());
   }

   /**
    * @param inheritPermissions The inheritPermissions to set.
    */
   public void setInheritPermissions(boolean inheritPermissions)
   {
      // stub - no impl as changes are made immediately using a ValueChanged listener
   }
   
   /**
    * Return the owner username
    */
   public String getOwner()
   {
      return this.ownableService.getOwner(getNode().getNodeRef());
   }
   
   /**
    * @return the list of user nodes for list data binding
    */
   public List<Map> getUsers()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      List<Map> personNodes = null;
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         // Return all the permissions set against the current node
         // for any authentication instance (user/group).
         // Then combine them into a single list for each authentication found. 
         Map<String, List<String>> permissionMap = new HashMap<String, List<String>>(8, 1.0f);
         Set<AccessPermission> permissions = permissionService.getAllSetPermissions(getNode().getNodeRef());
         for (AccessPermission permission : permissions)
         {
            // we are only interested in Allow and not groups/owner etc.
            if (permission.getAccessStatus() == AccessStatus.ALLOWED &&
                (permission.getAuthorityType() == AuthorityType.USER ||
                 permission.getAuthorityType() == AuthorityType.GROUP ||
                 permission.getAuthorityType() == AuthorityType.GUEST ||
                 permission.getAuthorityType() == AuthorityType.EVERYONE))
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
         
         // for each authentication (username/group key) found we get the Person
         // node represented by it and use that for our list databinding object
         personNodes = new ArrayList<Map>(permissionMap.size());
         for (String authority : permissionMap.keySet())
         {
            // check if we are dealing with a person (User Authority)
            if (AuthorityType.getAuthorityType(authority) == AuthorityType.GUEST ||
                personService.personExists(authority))
            {
               NodeRef nodeRef = personService.getPerson(authority);
               if (nodeRef != null)
               {
                  // create our Node representation
                  MapNode node = new MapNode(nodeRef);
                  
                  // set data binding properties
                  // this will also force initialisation of the props now during the UserTransaction
                  // it is much better for performance to do this now rather than during page bind
                  Map<String, Object> props = node.getProperties(); 
                  props.put("fullName", ((String)props.get("firstName")) + ' ' + ((String)props.get("lastName")));
                  props.put("roles", roleListToString(context, permissionMap.get(authority)));
                  props.put("icon", WebResources.IMAGE_PERSON);
                  
                  personNodes.add(node);
               }
            }
            else
            {
               // need a map (dummy node) to represent props for this Group Authority
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
               node.put("roles", roleListToString(context, permissionMap.get(authority)));
               node.put("icon", WebResources.IMAGE_GROUP);
               personNodes.add(node);
            }
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (InvalidNodeRefException refErr)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_NODEREF), new Object[] {refErr.getNodeRef()}) );
         personNodes = Collections.<Map>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), err.getMessage()), err );
         personNodes = Collections.<Map>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return personNodes;
   }
   
   /**
    * Convert a list of user Roles to a comma separated string list. Each individual role
    * will be looked up in message bundle to convert to a human readable string value.
    * 
    * @param context    FacesContext
    * @param list       List of Role names
    * 
    * @return Comma separated string of human readable roles
    */
   public static String roleListToString(FacesContext context, List<String> list)
   {
      StringBuilder buf = new StringBuilder();
      
      if (list != null)
      {
         for (int i=0; i<list.size(); i++)
         {
            if (buf.length() != 0)
            {
               buf.append(", ");
            }
            buf.append(Application.getMessage(context, list.get(i)));
         }
      }
      
      return buf.toString();
   }
   
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation

   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
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
   // Action event handlers
   
   /**
    * Action called to Close the dialog
    */
   public void close(ActionEvent event)
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).notifyBeans();
   }
   
   /**
    * Action event called by all actions that need to setup a Person context on
    * the UserMembers bean before an action page is called. The context will be a
    * Authority in setPersonAuthority() which can be retrieved on the action page from
    * UserMembersBean.setPersonAuthority().
    */
   public void setupUserAction(ActionEvent event)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      UIActionLink link = (UIActionLink) event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String authority = params.get("userName");
      if (authority != null && authority.length() != 0)
      {
         try
         {
            if (this.personService.personExists(authority))
            {
               // create the node ref, then our node representation
               NodeRef ref = personService.getPerson(authority);
               Node node = new Node(ref);
               
               // setup convience function for current user full name
               setPersonName((String)node.getProperties().get(ContentModel.PROP_FIRSTNAME) + ' ' +
                             (String)node.getProperties().get(ContentModel.PROP_LASTNAME));
            }
            else
            {
               setPersonName(authority);
            }
            
            // setup roles for this Authority
            List<PermissionWrapper> userPermissions = new ArrayList<PermissionWrapper>(4);
            Set<AccessPermission> permissions = permissionService.getAllSetPermissions(getNode().getNodeRef());
            if (permissions != null)
            {
               for (AccessPermission permission : permissions)
               {
                  // we are only interested in Allow permissions
                  if (permission.getAccessStatus() == AccessStatus.ALLOWED)
                  {
                     if (authority.equals(permission.getAuthority()))
                     {
                        // found a permission for this user authentiaction
                        PermissionWrapper wrapper = new PermissionWrapper(
                              permission.getPermission(),
                              Application.getMessage(context, permission.getPermission()));
                        userPermissions.add(wrapper);
                     }
                  }
               }
            }
            // action context setup
            this.personRoles = userPermissions;
            setPersonAuthority(authority);
         }
         catch (Exception err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext
                  .getCurrentInstance(), Repository.ERROR_GENERIC), new Object[] { err.getMessage() }));
         }
      }
      else
      {
         setPersonAuthority(null);
      }
      
      // force refresh on return to this page
      contextUpdated();
   }
      
   /**
    * Inherit parent Space permissions value changed by the user
    */
   public void inheritPermissionsValueChanged(ValueChangeEvent event)
   {
      try
      {
         // change the value to the new selected value
         boolean inheritPermissions = (Boolean)event.getNewValue();
         this.permissionService.setInheritParentPermissions(getNode().getNodeRef(), inheritPermissions);
         
         // inform the user that the change occured
         FacesContext context = FacesContext.getCurrentInstance();
         String msg;
         if (inheritPermissions)
         {
            msg = Application.getMessage(context, MSG_SUCCESS_INHERIT);
         }
         else
         {
            msg = Application.getMessage(context, MSG_SUCCESS_INHERIT_NOT);
         }
         
         // see if the user still has permissions to the node, if not, we need
         // to go back to the root of the current "area" by simulating the user
         // pressing the top level navigation button i.e. My Home
         if (this.permissionService.hasPermission(getNode().getNodeRef(), 
             PermissionService.CHANGE_PERMISSIONS) == AccessStatus.ALLOWED)
         {
            FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
            context.addMessage(event.getComponent().getClientId(context), facesMsg);
         }
         else
         {
            NavigationBean nb = (NavigationBean)FacesHelper.getManagedBean(
                  context, NavigationBean.BEAN_NAME);
            if (nb != null)
            {
               try
               {
                  nb.processToolbarLocation(nb.getToolbarLocation(), true);
               }
               catch (InvalidNodeRefException refErr)
               {
                  Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                        FacesContext.getCurrentInstance(), Repository.ERROR_NOHOME), 
                        Application.getCurrentUser(context).getHomeSpaceId()), refErr );
               }
               catch (Exception err)
               {
                  Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                        FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), 
                        err.getMessage()), err);
               }
            }
         }
      }
      catch (Throwable e)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
      }
   }
   
   /**
    * Action handler called when the Add Role button is pressed to process the current selection
    */
   public void addRole(ActionEvent event)
   {
      UISelectOne rolePicker = (UISelectOne)event.getComponent().findComponent("roles");
      
      String role = (String)rolePicker.getValue();
      if (role != null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         PermissionWrapper wrapper = new PermissionWrapper(role, Application.getMessage(context, role));
         this.personRoles.add(wrapper);
      }
      
      // force refresh on return to this page
      contextUpdated();
   }
   
   /**
    * Action handler called when the Remove button is pressed to remove a role from current user
    */
   public void removeRole(ActionEvent event)
   {
      PermissionWrapper wrapper = (PermissionWrapper)this.personRolesDataModel.getRowData();
      if (wrapper != null)
      {
         this.personRoles.remove(wrapper);
      }
      
      // force refresh on return to this page
      contextUpdated();
   }
   
   /**
    * Action handler called when the Finish button is clicked on the Edit User Roles page
    */
   public String finishOK()
   {
      String outcome = OUTCOME_FINISH;
      
      FacesContext context = FacesContext.getCurrentInstance();
      
      // persist new user permissions
      if (this.personRoles != null && getPersonAuthority() != null)
      {
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(context);
            tx.begin();
            
            // clear the currently set permissions for this user
            // and add each of the new permissions in turn
            NodeRef nodeRef = getNode().getNodeRef();
            this.permissionService.clearPermission(nodeRef, getPersonAuthority());
            for (PermissionWrapper wrapper : personRoles)
            {
               this.permissionService.setPermission(
                     nodeRef,
                     getPersonAuthority(),
                     wrapper.getPermission(),
                     true);
            }
            
            tx.commit();
         }
         catch (Exception err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  context, Repository.ERROR_GENERIC), err.getMessage()), err );
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
            
            outcome = null;
         }
      }
      
      return outcome;
   }
   
   /**
    * Action handler called when the OK button is clicked on the Remove User page
    */
   public String removeOK()
   {
      String outcome = OUTCOME_FINISH;
      UserTransaction tx = null;
      FacesContext context = FacesContext.getCurrentInstance();

      try
      {
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         // remove the invited User
         if (getPersonAuthority() != null)
         {
            // clear permissions for the specified Authority
            this.permissionService.clearPermission(getNode().getNodeRef(), getPersonAuthority());
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (Exception e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(FacesContext
               .getCurrentInstance(), ERROR_DELETE), e.getMessage()), e);
      }

      // see if the user still has permissions to the node, if not, we need
      // to go back to the root of the current "area" by simulating the user
      // pressing the top level navigation button i.e. My Home
      if (this.permissionService.hasPermission(getNode().getNodeRef(), 
          PermissionService.CHANGE_PERMISSIONS) == AccessStatus.DENIED)
      {
         NavigationBean nb = (NavigationBean)FacesHelper.getManagedBean(
               context, NavigationBean.BEAN_NAME);
         if (nb != null)
         {
            try
            {
               nb.processToolbarLocation(nb.getToolbarLocation(), true);
               outcome = "browse";
            }
            catch (InvalidNodeRefException refErr)
            {
               Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                     FacesContext.getCurrentInstance(), Repository.ERROR_NOHOME), 
                     Application.getCurrentUser(context).getHomeSpaceId()), refErr );
            }
            catch (Exception err)
            {
               Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                     FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), 
                     err.getMessage()), err);
            }
         }
      }
      
      return outcome;
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Wrapper class for list data model to display current roles for user
    */
   public static class PermissionWrapper
   {
      public PermissionWrapper(String permission, String label)
      {
         this.permission = permission;
         this.label = label;
      }
      
      public String getRole()
      {
         return this.label;
      }
      
      public String getPermission()
      {
         return this.permission;
      }
      
      private String label;
      private String permission;
   }
}
