/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.bean.users;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.TemplateMailHelperBean;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.MapNode;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.WebResources;
import org.alfresco.web.ui.repo.component.UIUserGroupPicker;
import org.alfresco.web.ui.repo.component.UIUserGroupPicker.PickerEvent;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Dialog bean managing the state for the Email Space Users page. Calculates the user/groups
 * that are invited to a space and builds the data structures needed to display and modify
 * the list in the web-client UI. Notifies the selected user/groups with a templatable email.
 * 
 * @author Kevin Roast
 */
public class EmailSpaceUsersDialog extends BaseDialogBean implements IContextListener
{
   private static final String PROP_DUPLICATE = "duplicate";
   private static final String PROP_PARENT = "parent";
   private static final String PROP_ID = "id";
   private static final String PROP_ISGROUP = "isGroup";
   private static final String PROP_ICON = "icon";
   private static final String PROP_FULLNAME = "fullName";
   private static final String PROP_ROLES = "roles";
   private static final String PROP_EXPANDED = "expanded";
   private static final String PROP_SELECTED = "selected";
   private static final String PROP_USERNAME = "userName";
   
   /** Injected Bean references */
   protected PermissionService permissionService;
   protected PersonService personService;
   protected AuthorityService authorityService;
   protected JavaMailSender mailSender;
   
   /** Helper providing template based mailing facilities */
   protected TemplateMailHelperBean mailHelper;
   
   /** List of user/group property map/node instances */
   private List<Map> usersGroups = null;
   
   /** Quick lookup table of authority to user/group instance */
   private Map<String, Map> userGroupLookup = new HashMap<String, Map>();
   
   
   /**
    * Default constructor
    */
   public EmailSpaceUsersDialog()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }
   
   /**
    * Setup the dialog
    */
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      mailHelper = new TemplateMailHelperBean();
      mailHelper.setMailSender(mailSender);
      mailHelper.setNodeService(nodeService);
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // get the space ref this mail applies to
      NodeRef spaceRef = getSpace().getNodeRef();
      
      // calculate the 'from' email address
      User user = Application.getCurrentUser(context);
      String from = (String)this.nodeService.getProperty(user.getPerson(), ContentModel.PROP_EMAIL);
      if (from == null || from.length() == 0)
      {
         // if the user does not have an email address get the default one from the config service
         from = Application.getClientConfig(context).getFromEmailAddress();
      }
      
      Set<String> mailedAuthorities = new HashSet<String>(usersGroups.size());
      
      // walk the list of users/groups to notify - handle duplicates along the way
      for (Map node : usersGroups)
      {
         String authority = (String)node.get(PROP_USERNAME);
         boolean selected = (Boolean)node.get(PROP_SELECTED);
         
         // if User, email then, else if Group get all members and email them
         AuthorityType authType = AuthorityType.getAuthorityType(authority);
         if (authType.equals(AuthorityType.USER))
         {
            if (selected == true && this.personService.personExists(authority))
            {
               if (mailedAuthorities.contains(authority) == false)
               {
                  this.mailHelper.notifyUser(
                        this.personService.getPerson(authority), spaceRef, from, (String)node.get(PROP_ROLES));
                  mailedAuthorities.add(authority);
               }
            }
         }
         else if (authType.equals(AuthorityType.GROUP))
         {
            // is the group expanded? if so we'll deal with the child authorities instead
            boolean expanded = (Boolean)node.get(PROP_EXPANDED);
            if (expanded == false && selected == true)
            {
               // notify all members of the group
               Set<String> users = this.authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
               for (String userAuth : users)
               {
                  if (this.personService.personExists(userAuth) == true)
                  {
                     if (mailedAuthorities.contains(userAuth) == false)
                     {
                        this.mailHelper.notifyUser(
                              this.personService.getPerson(userAuth), spaceRef, from, (String)node.get(PROP_ROLES));
                        mailedAuthorities.add(userAuth);
                     }
                  }
               }
            }
         }
      }
      
      return outcome;
   }
   
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation 
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      this.usersGroups = null;
      this.userGroupLookup = new HashMap<String, Map>();
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
   // Bean Getters and Setters

   /**
    * @param permissionService   The PermissionService to set
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    * @param permissionService   The PersonService to set
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
   
   /**
    * @param mailSender          The JavaMailSender to set.
    */
   public void setMailSender(JavaMailSender mailSender)
   {
      this.mailSender = mailSender;
   }
   
   /**
    * @param authorityService    The AuthorityService to set.
    */
   public void setAuthorityService(AuthorityService authorityService)
   {
      this.authorityService = authorityService;
   }
   
   /**
    * @return The space to email users for
    */
   public Node getSpace()
   {
      return this.browseBean.getActionSpace();
   }
   
   /**
    * Return the List of objects representing the Users and Groups invited to this space.
    * The picker is then responsible for rendering a view to represent those users and groups
    * which allows the users to select and deselect users and groups, also to expand groups
    * to show sub-groups and users. 
    * 
    * @return List of Map objects representing the users/groups assigned to the current space
    */
   public List<Map> getUsersGroups()
   {
      if (this.usersGroups == null)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         UserTransaction tx = null;
         try
         {
            tx = Repository.getUserTransaction(context, true);
            tx.begin();
            
            // Return all the permissions set against the current node for any authentication
            // instance (user/group), walking the parent space inheritance chain.
            // Then combine them into a single list for each authentication found.
            String currentAuthority = Application.getCurrentUser(context).getUserName();
            Map<String, List<String>> permissionMap = new HashMap<String, List<String>>(8, 1.0f);
            NodeRef spaceRef = getSpace().getNodeRef();
            while (spaceRef != null)
            {
               Set<AccessPermission> permissions = permissionService.getAllSetPermissions(spaceRef);
               for (AccessPermission permission : permissions)
               {
                  // we are only interested in Allow and not Guest/Everyone/owner
                  if (permission.getAccessStatus() == AccessStatus.ALLOWED &&
                      (permission.getAuthorityType() == AuthorityType.USER ||
                       permission.getAuthorityType() == AuthorityType.GROUP))
                  {
                     String authority = permission.getAuthority();
                     
                     if (currentAuthority.equals(authority) == false)
                     {
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
               
               // walk parent inheritance chain until root or no longer inherits
               if (permissionService.getInheritParentPermissions(spaceRef))
               {
                  spaceRef = nodeService.getPrimaryParent(spaceRef).getParentRef();
               }
               else
               {
                  spaceRef = null;
               }
            }
            
            // create the structure as a linked list for fast insert/removal of items
            this.usersGroups = new LinkedList<Map>();
            
            // for each authentication (username/group key) found we get the Person
            // node represented by it and use that for our list databinding object
            for (String authority : permissionMap.keySet())
            {
               Map node = buildAuthorityMap(authority, UserMembersBean.roleListToString(context, permissionMap.get(authority)));
               if (node != null)
               {
                  this.usersGroups.add(node);
               }
            }
            
            // commit the transaction
            tx.commit();
         }
         catch (InvalidNodeRefException refErr)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  context, Repository.ERROR_NODEREF), new Object[] {refErr.getNodeRef()}) );
            this.usersGroups = Collections.<Map>emptyList();
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
         catch (Throwable err)
         {
            Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  context, Repository.ERROR_GENERIC), err.getMessage()), err );
            this.usersGroups = Collections.<Map>emptyList();
            try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         }
      }
      return this.usersGroups;
   }
   
   /**
    * @return TemplateMailHelperBean instance for this wizard
    */
   public TemplateMailHelperBean getMailHelper()
   {
      return this.mailHelper;
   }
   
   /**
    * @return true if any authorities are selected, false otherwise
    */
   public boolean getFinishButtonDisabled()
   {
      boolean disabled = true;
      
      if (this.usersGroups != null)
      {
         for (Map userGroup : this.usersGroups)
         {
            if (((Boolean)userGroup.get(PROP_EXPANDED)) == false &&
                ((Boolean)userGroup.get(PROP_SELECTED)) == true)
            {
               disabled = false;
               break;
            }
         }
      }
      
      return disabled;
   }
   
   
   // ------------------------------------------------------------------------------
   // Action Event Listeners
   
   /**
    * Action handler for a user/group selector event
    */
   public void userGroupSelectorAction(ActionEvent event)
   {
      if (event instanceof PickerEvent)
      {
         PickerEvent pickerEvent = (PickerEvent)event;
         
         // find the user/group this event represents
         Map userGroup = null;
         int index = 0;
         for (; index<this.usersGroups.size(); index++)
         {
            if (pickerEvent.Authority.equals(this.usersGroups.get(index).get(PROP_ID)))
            {
               userGroup = this.usersGroups.get(index);
               break;
            }
         }
         
         if (userGroup != null)
         {
            switch (pickerEvent.Action)
            {
               // expand/collapse events only applicable for a Group
               case UIUserGroupPicker.ACTION_EXPANDCOLLAPSE:
                  boolean expanded = (Boolean)userGroup.get(PROP_EXPANDED);
                  userGroup.put(PROP_EXPANDED, !expanded);
                  if (expanded == false)
                  {
                     // expand the list for this group by adding the immediate child authorities
                     boolean selected = (Boolean)userGroup.get(PROP_SELECTED);
                     String currentAuthority =
                        Application.getCurrentUser(FacesContext.getCurrentInstance()).getUserName();
                     Set<String> authorities = authorityService.getContainedAuthorities(
                           null, pickerEvent.Authority, true);
                     for (String authority : authorities)
                     {
                        if (currentAuthority.equals(authority) == false)
                        {
                           if (AuthorityType.getAuthorityType(authority) == AuthorityType.USER ||
                               AuthorityType.getAuthorityType(authority) == AuthorityType.GROUP)
                           {
                              Map node = buildAuthorityMap(authority, (String)userGroup.get(PROP_ROLES));
                              if (node != null)
                              {
                                 node.put(PROP_PARENT, userGroup);
                                 node.put(PROP_SELECTED, selected);
                                 this.usersGroups.add(++index, node);
                              }
                           }
                        }
                     }
                  }
                  else
                  {
                     // remove the children for the group
                     for (index++; index<this.usersGroups.size(); /**/)
                     {
                        Map node = this.usersGroups.get(index);
                        Map parent = (Map)node.get(PROP_PARENT);
                        
                        // only remove those Groups that have this group as the parent
                        // they are added sequentially - so we know when to stop removing
                        boolean foundParent = false;
                        while (parent != null && foundParent == false)
                        {
                           // search up the parent hierarchy
                           if (parent == userGroup)
                           {
                              foundParent = true;
                           }
                           parent = (Map)parent.get(PROP_PARENT);
                        }
                        if (foundParent == true)
                        {
                           // handle duplicates - only remove the first from the lookup table
                           if (((Boolean)node.get(PROP_DUPLICATE)) == false)
                           {
                              this.userGroupLookup.remove((String)node.get(PROP_USERNAME));
                           }
                           this.usersGroups.remove(index);
                        }
                        else
                        {
                           // need to increment loop counter if did not remove a value from the list
                           index++;
                        }
                     }
                  }
                  break;
                  
               case UIUserGroupPicker.ACTION_SELECT:
                  boolean selected = (Boolean)userGroup.get(PROP_SELECTED);
                  userGroup.put(PROP_SELECTED, !selected);
                  break;
            }
         }
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private helpers
   
   /**
    * Build a Map representing a user/group with a set of useful property values required
    * by the UIUserGroupPicker UI component.
    * 
    * @param authority     User/Group authority
    * @param roles         Role text for the authority
    * 
    * @return Map
    */
   private Map buildAuthorityMap(String authority, String roles)
   {
      Map node = null;
      
      if (AuthorityType.getAuthorityType(authority) == AuthorityType.GUEST ||
          this.personService.personExists(authority))
      {
         NodeRef nodeRef = this.personService.getPerson(authority);
         if (nodeRef != null)
         {
            // create our Node representation
            node = new MapNode(nodeRef);
            
            // set data binding properties
            // this will also force initialisation of the props now during the UserTransaction
            // it is much better for performance to do this now rather than during page bind
            Map<String, Object> props = ((MapNode)node).getProperties(); 
            props.put(PROP_FULLNAME, ((String)props.get("firstName")) + ' ' + ((String)props.get("lastName")));
            props.put(PROP_ICON, WebResources.IMAGE_PERSON);
            props.put(PROP_ISGROUP, false);
         }
      }
      else if (AuthorityType.getAuthorityType(authority) == AuthorityType.GROUP)
      {
         // need a map (dummy node) to represent props for this Group Authority
         node = new HashMap<String, Object>(8, 1.0f);
         if (authority.startsWith(PermissionService.GROUP_PREFIX) == true)
         {
            node.put(PROP_FULLNAME, authority.substring(PermissionService.GROUP_PREFIX.length()));
         }
         else
         {
            node.put(PROP_FULLNAME, authority);
         }
         node.put(PROP_USERNAME, authority);
         node.put(PROP_ID, authority);
         node.put(PROP_ICON, WebResources.IMAGE_GROUP);
         node.put(PROP_ISGROUP, true);
      }
      if (node != null)
      {
         // add the common properties
         node.put(PROP_ROLES, roles);
         node.put(PROP_PARENT, null);
         node.put(PROP_EXPANDED, false);
         
         if (this.userGroupLookup.get(authority) != null)
         {
            // this authority already exists in the list somewhere else - mark as duplicate
            node.put(PROP_DUPLICATE, true);
            node.put(PROP_SELECTED, false);
         }
         else
         {
            // add to table for the first time, not a duplicate
            this.userGroupLookup.put(authority, node);
            node.put(PROP_DUPLICATE, false);
            node.put(PROP_SELECTED, true);
         }
      }
      
      return node;
   }
}
