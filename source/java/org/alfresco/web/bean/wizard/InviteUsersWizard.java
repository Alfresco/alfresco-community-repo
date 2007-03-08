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
package org.alfresco.web.bean.wizard;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.TemplateMailHelperBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Backing bean for the Invite Users wizard(s)
 * 
 * @author Kevin Roast
 */
public abstract class InviteUsersWizard extends BaseWizardBean
{
   /** I18N message strings */
   protected static final String MSG_USERROLES = "invite_users_summary";
   private static final String MSG_USERS  = "users";
   private static final String MSG_GROUPS = "groups";
   private static final String MSG_INVITED_TO = "invited_to";
   private static final String MSG_INVITED_ROLE  = "invite_role";
   
   protected static final String STEP_NOTIFY = "notify";
   
   private static final String NOTIFY_YES = "yes";
   private static final String NOTIFY_NO = "no";
   
   /** NamespaceService bean reference */
   protected NamespaceService namespaceService;
   
   /** JavaMailSender bean reference */
   protected JavaMailSender mailSender;
   
   /** AuthorityService bean reference */
   protected AuthorityService authorityService;
   
   /** PermissionService bean reference */
   protected PermissionService permissionService;
   
   /** personService bean reference */
   protected PersonService personService;
   
   /** Helper providing template based mailing facilities */
   protected TemplateMailHelperBean mailHelper;
   
   /** datamodel for table of roles for users */
   protected DataModel userRolesDataModel = null;
   
   /** list of user/group role wrapper objects */
   protected List<UserGroupRole> userGroupRoles = null;
   
   /** True to allow duplicate authorities (with a different role) */
   protected boolean allowDuplicateAuthorities = true;
   
   /** dialog state */
   private String notify = NOTIFY_NO;
   
   /**
    * @return a cached list of available permissions for the type being dealt with
    */
   protected abstract Set<String> getPermissionsForType();
   
   /**
    * @return Returns the node that the permissions are being applied to
    */
   protected abstract Node getNode();
   
   /**
    * @param namespaceService   The NamespaceService to set.
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }
   
   /**
    * @param mailSender          The JavaMailSender to set.
    */
   public void setMailSender(JavaMailSender mailSender)
   {
      this.mailSender = mailSender;
   }
   
   /**
    * @param permissionService   The PermissionService to set.
    */
   public void setPermissionService(PermissionService permissionService)
   {
      this.permissionService = permissionService;
   }
   
   /**
    * @param personService   The PersonService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
   
   /**
    * @param authorityService    The authorityService to set.
    */
   public void setAuthorityService(AuthorityService authorityService)
   {
      this.authorityService = authorityService;
   }

   /**
    * Initialises the wizard
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      notify = NOTIFY_NO;
      userGroupRoles = new ArrayList<UserGroupRole>(8);
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
      User user = Application.getCurrentUser(context);
      String from = (String)this.nodeService.getProperty(user.getPerson(), ContentModel.PROP_EMAIL);
      if (from == null || from.length() == 0)
      {
         // if the user does not have an email address get the default one from the config service
         from = Application.getClientConfig(context).getFromEmailAddress();
      }
      
      // get the Space to apply changes too
      NodeRef nodeRef = this.getNode().getNodeRef();
      
      // set permissions for each user and send them a mail
      for (int i=0; i<this.userGroupRoles.size(); i++)
      {
         UserGroupRole userGroupRole = this.userGroupRoles.get(i);
         String authority = userGroupRole.getAuthority();
         
         // find the selected permission ref from it's name and apply for the specified user
         Set<String> perms = getPermissionsForType();
         for (String permission : perms)
         {
            if (userGroupRole.getRole().equals(permission))
            {
               this.permissionService.setPermission(
                     nodeRef,
                     authority,
                     permission,
                     true);
               break;
            }
         }
         
         // Create the mail message for sending to each User
         if (NOTIFY_YES.equals(this.notify))
         {
            // if User, email then, else if Group get all members and email them
            AuthorityType authType = AuthorityType.getAuthorityType(authority);
            if (authType.equals(AuthorityType.USER))
            {
               if (this.personService.personExists(authority) == true)
               {
                  this.mailHelper.notifyUser(
                        this.personService.getPerson(authority), nodeRef, from, userGroupRole.getRole());
               }
            }
            else if (authType.equals(AuthorityType.GROUP))
            {
               // else notify all members of the group
               Set<String> users = this.authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
               for (String userAuth : users)
               {
                  if (this.personService.personExists(userAuth) == true)
                  {
                     this.mailHelper.notifyUser(
                           this.personService.getPerson(userAuth), nodeRef, from, userGroupRole.getRole());
                  }
               }
            }
         }
      }
      
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#getFinishButtonDisabled()
    */
   @Override
   public boolean getFinishButtonDisabled()
   {
      boolean disabled = true;
      
      String stepName = Application.getWizardManager().getCurrentStepName();
      if (STEP_NOTIFY.equals(stepName))
      {
         disabled = false;
      }
      
      return disabled;
   }

   /**
    * Returns the properties for current user-roles JSF DataModel
    * 
    * @return JSF DataModel representing the current user-roles
    */
   public DataModel getUserRolesDataModel()
   {
      if (this.userRolesDataModel == null)
      {
         this.userRolesDataModel = new ListDataModel();
      }
      
      this.userRolesDataModel.setWrappedData(this.userGroupRoles);
      
      return this.userRolesDataModel;
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
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         if (filterIndex == 0)
         {
            // build xpath to match available User/Person objects
            NodeRef peopleRef = personService.getPeopleContainer();
            // NOTE: see SearcherComponentTest
            String xpath = "*[like(@" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + "firstName, '%" + contains + "%', false)" +
                    " or " + "like(@" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + "lastName, '%" + contains + "%', false)]";
            
            List<NodeRef> nodes = searchService.selectNodes(
                  peopleRef,
                  xpath,
                  null,
                  this.namespaceService,
                  false);
            
            items = new SelectItem[nodes.size()];
            for (int index=0; index<nodes.size(); index++)
            {
               NodeRef personRef = nodes.get(index);
               String firstName = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
               String lastName = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
               String username = (String)this.nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
               SelectItem item = new SortableSelectItem(username, firstName + " " + lastName, lastName);
               items[index] = item;
            }
         }
         else
         {
            // groups - simple text based match on name
            Set<String> groups = authorityService.getAllAuthorities(AuthorityType.GROUP);
            groups.addAll(authorityService.getAllAuthorities(AuthorityType.EVERYONE));
            
            List<SelectItem> results = new ArrayList<SelectItem>(groups.size());
            String containsLower = contains.toLowerCase();
            int offset = PermissionService.GROUP_PREFIX.length();
            for (String group : groups)
            {
               if (group.toLowerCase().indexOf(containsLower, offset) != -1)
               {
                  results.add(new SortableSelectItem(group, group.substring(offset), group));
               }
            }
            items = new SelectItem[results.size()];
            results.toArray(items);
         }
         
         Arrays.sort(items);
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err );
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         
         items = new SelectItem[0];
      }
      
      return items;
   }
   
   /**
    * Action handler called when the Add button is pressed to process the current selection
    */
   public void addSelection(ActionEvent event)
   {
      UIGenericPicker picker = (UIGenericPicker)event.getComponent().findComponent("picker");
      UISelectOne rolePicker = (UISelectOne)event.getComponent().findComponent("roles");
      
      String[] results = picker.getSelectedResults();
      if (results != null)
      {
         String role = (String)rolePicker.getValue();
         
         if (role != null)
         {
            for (int i=0; i<results.length; i++)
            {
               String authority = results[i];
               
               // only add if authority not already present in the list with same role
               boolean foundExisting = false;
               for (int n=0; n<this.userGroupRoles.size(); n++)
               {
                  UserGroupRole wrapper = this.userGroupRoles.get(n);
                  if (authority.equals(wrapper.getAuthority()) &&
                      (!this.allowDuplicateAuthorities || role.equals(wrapper.getRole())))
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
                     if (authType == AuthorityType.GUEST || this.personService.personExists(authority) == true)
                     {
                        // found a User authority
                        label.append(buildLabelForUserAuthorityRole(authority, role));
                     }
                  }
                  else
                  {
                     // found a group authority
                     label.append(buildLabelForGroupAuthorityRole(authority, role));
                  }
                  
                  this.userGroupRoles.add(new UserGroupRole(authority, role, label.toString()));
               }
            }
         }
      }
   }
   
   /**
    * Action handler called when the Remove button is pressed to remove a user+role
    */
   public void removeSelection(ActionEvent event)
   {
      UserGroupRole wrapper = (UserGroupRole)this.userRolesDataModel.getRowData();
      if (wrapper != null)
      {
         this.userGroupRoles.remove(wrapper);
      }
   }
   
   /**
    * Property accessed by the Generic Picker component.
    * 
    * @return the array of filter options to show in the users/groups picker
    */
   public SelectItem[] getFilters()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      return new SelectItem[] {
            new SelectItem("0", bundle.getString(MSG_USERS)),
            new SelectItem("1", bundle.getString(MSG_GROUPS)) };
   }
   
   /**
    * @return The list of available roles for the users/groups
    */
   public SelectItem[] getRoles()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      
      // get available roles (grouped permissions) from the permission service
      Set<String> perms = getPermissionsForType();
      SelectItem[] roles = new SelectItem[perms.size()];
      int index = 0;
      for (String permission : perms)
      {
         String displayLabel = bundle.getString(permission);
         roles[index++] = new SelectItem(permission, displayLabel);
      }
      
      return roles;
   }
   
   /**
    * @return Returns the notify listbox selection.
    */
   public String getNotify()
   {
      return this.notify;
   }

   /**
    * @param notify The notify listbox selection to set.
    */
   public void setNotify(String notify)
   {
      this.notify = notify;
   }
   
   @Override
   public String next()
   {
      String stepName = Application.getWizardManager().getCurrentStepName();
      
      if (STEP_NOTIFY.equals(stepName))
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // prepare automatic text for email and display
         StringBuilder buf = new StringBuilder(256);
         
         String personName = Application.getCurrentUser(context).getFullName(this.nodeService);
         String msgInvitedTo = Application.getMessage(context, MSG_INVITED_TO);
         Node node = this.getNode();
         String path = this.nodeService.getPath(node.getNodeRef()).toDisplayPath(this.nodeService);
         buf.append(MessageFormat.format(msgInvitedTo, new Object[] {
               path + '/' + node.getName(),
               personName}) );
         
         // default the subject line to an informative message
         this.mailHelper.setSubject(buf.toString());
         
         // add the rest of the automatic body text
         buf.append("\r\n\r\n");
         
         String msgRole = Application.getMessage(context, MSG_INVITED_ROLE);
         String roleText;
         if (this.userGroupRoles.size() != 0)
         {
            String roleMsg = Application.getMessage(context, userGroupRoles.get(0).getRole());
            roleText = MessageFormat.format(msgRole, roleMsg);
         }
         else
         {
            roleText = MessageFormat.format(msgRole, "[role]");
         }
         
         buf.append(roleText);
         
         // set the body content and default text to this text
         this.mailHelper.setAutomaticText(buf.toString());
         this.mailHelper.setBody(this.mailHelper.getAutomaticText());
      }
      
      return null;
   }
   
   /**
    * @return TemplateMailHelperBean instance for this wizard
    */
   public TemplateMailHelperBean getMailHelper()
   {
      return this.mailHelper;
   }
   
   /**
    * Helper to build a label of the form:
    *    Firstname Lastname (Role)
    */
   public String buildLabelForUserAuthorityRole(String authority, String role)
   {
      // found a User authority
      NodeRef ref = this.personService.getPerson(authority);
      String firstName = (String)this.nodeService.getProperty(ref, ContentModel.PROP_FIRSTNAME);
      String lastName = (String)this.nodeService.getProperty(ref, ContentModel.PROP_LASTNAME);
      
      StringBuilder buf = new StringBuilder(100);
      buf.append(firstName)
         .append(" ")
         .append(lastName != null ? lastName : "")
         .append(" (")
         .append(Application.getMessage(FacesContext.getCurrentInstance(), role))
         .append(")");
      
      return buf.toString();
   }
   
   /**
    * Helper to build a label for a Group authority of the form:
    *    Groupname (role)
    */
   public String buildLabelForGroupAuthorityRole(String authority, String role)
   {
      StringBuilder buf = new StringBuilder(100);
      buf.append(authority.substring(PermissionService.GROUP_PREFIX.length()))
         .append(" (")
         .append(Application.getMessage(FacesContext.getCurrentInstance(), role))
         .append(")");
      
      return buf.toString();
   }
   
   /**
    * @return summary text for the wizard
    */
   public String getSummary()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      // build a summary section to list the invited users and there roles
      StringBuilder buf = new StringBuilder(128);
      for (UserGroupRole userRole : this.userGroupRoles)
      {
         buf.append(userRole.getLabel());
         buf.append("<br>");
      }
      
      return buildSummary(
            new String[] {Application.getMessage(fc, MSG_USERROLES)},
            new String[] {buf.toString()});
   }
   
   
   /**
    * Simple wrapper class to represent a user/group and a role combination
    */
   public static class UserGroupRole
   {
      public UserGroupRole(String authority, String role, String label)
      {
         this.authority = authority;
         this.role = role;
         this.label = label;
      }
      
      public String getAuthority()
      {
         return this.authority;
      }
      
      public String getRole()
      {
         return this.role;
      }
      
      public String getLabel()
      {
         return this.label;
      }
      
      private String authority;
      private String role;
      private String label;
   }
}
