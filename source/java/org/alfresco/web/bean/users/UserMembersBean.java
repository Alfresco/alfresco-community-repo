/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
import org.alfresco.web.bean.BrowseBean;
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
public abstract class UserMembersBean
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
      
      // force refresh on exit of the page (as this property is set by JSF on view restore) 
      this.usersRichList.setValue(null);
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
         // for any authentication instance (user).
         // Then combine them into a single list for each authentication found. 
         Map<String, List<String>> permissionMap = new HashMap<String, List<String>>(13, 1.0f);
         Set<AccessPermission> permissions = permissionService.getAllSetPermissions(getNode().getNodeRef());
         if (permissions != null)
         {
            for (AccessPermission permission : permissions)
            {
               // we are only interested in Allow and not groups/owner etc.
               if (permission.getAccessStatus() == AccessStatus.ALLOWED &&
                   (permission.getAuthorityType() == AuthorityType.USER ||
                    permission.getAuthorityType() == AuthorityType.GROUP ||
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
         }
         
         // for each authentication (username key) found we get the Person
         // node represented by it and use that for our list databinding object
         personNodes = new ArrayList<Map>(permissionMap.size());
         for (String authority : permissionMap.keySet())
         {
            // check if we are dealing with a person (User Authority)
            if (personService.personExists(authority))
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

                  props.put("roles", listToString(context, permissionMap.get(authority)));
                  
                  props.put("icon", WebResources.IMAGE_PERSON);
                  
                  personNodes.add(node);
               }
            }
            else
            {
               // need a map (dummy node) to represent props for this Group Authority
               Map<String, Object> node = new HashMap<String, Object>(5, 1.0f);
               node.put("fullName", authority.substring(PermissionService.GROUP_PREFIX.length()));
               node.put("userName", authority);
               node.put("id", authority);
               node.put("roles", listToString(context, permissionMap.get(authority)));
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
               context, Repository.ERROR_NODEREF), new Object[] {"root"}) );
         personNodes = Collections.<Map>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      catch (Exception err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               context, Repository.ERROR_GENERIC), err.getMessage()), err );
         personNodes = Collections.<Map>emptyList();
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
      }
      
      return personNodes;
   }
   
   private static String listToString(FacesContext context, List<String> list)
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
   // Action event handlers
   
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
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
         context.addMessage(event.getComponent().getClientId(context), facesMsg);
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
      UserTransaction tx = null;

      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
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
      
      return OUTCOME_FINISH;
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
