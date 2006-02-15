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
package org.alfresco.web.bean.wizard;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author Kevin Roast
 */
public abstract class InviteUsersWizard extends AbstractWizardBean
{
   private static Log logger = LogFactory.getLog(InviteUsersWizard.class);
   
   /** I18N message strings */
   private static final String MSG_USERS  = "users";
   private static final String MSG_GROUPS = "groups";
   private static final String MSG_INVITED_TO = "invited_to";
   private static final String MSG_INVITED_ROLE  = "invite_role";
   private static final String STEP1_TITLE_ID = "invite_step1_title";
   private static final String STEP2_TITLE_ID = "invite_step2_title";
   private static final String STEP2_DESCRIPTION_ID = "invite_step2_desc";
   private static final String FINISH_INSTRUCTION_ID = "invite_finish_instruction";
   
   private static final String NOTIFY_YES = "yes";
   
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
   
   /** datamodel for table of roles for users */
   private DataModel userRolesDataModel = null;
   
   /** list of user/group role wrapper objects */
   private List<UserGroupRole> userGroupRoles = null;
   
   /** dialog state */
   private String notify = NOTIFY_YES;
   private String subject = null;
   private String body = null;
   private String internalSubject = null;
   private String automaticText = null;
   
   /**
    * @return a cached list of available permissions for the type being dealt with
    */
   protected abstract Set<String> getPermissionsForType();
   
   /**
    * @return Returns the node that the permissions are being applied to
    */
   protected abstract Node getNode();
   
   /**
    * @return The text to use for the description of step 1 (depends on the type being dealt with)
    */
   protected abstract String getStep1DescriptionText();
   
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
    * @param permissionService   The PermissionService to set.
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
   public void init()
   {
      super.init();
      
      notify = NOTIFY_YES;
      userGroupRoles = new ArrayList<UserGroupRole>(8);
      subject = "";
      body = "";
      automaticText = "";
      internalSubject = null;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#finish()
    */
   public String finish()
   {
      String outcome = FINISH_OUTCOME;
      
      UserTransaction tx = null;
      
      try
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         tx = Repository.getUserTransaction(context);
         tx.begin();
         
         String subject = this.subject;
         if (subject == null || subject.length() == 0)
         {
            subject = this.internalSubject;
         }
         
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
                     notifyUser(this.personService.getPerson(authority), nodeRef, from, userGroupRole.getRole());
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
                        notifyUser(this.personService.getPerson(userAuth), nodeRef, from, userGroupRole.getRole());
                     }
                  }
               }
            }
         }
         
         // commit the transaction
         tx.commit();
         
         UIContextService.getInstance(context).notifyBeans();
      }
      catch (Throwable e)
      {
         // rollback the transaction
         try { if (tx != null) {tx.rollback();} } catch (Exception ex) {}
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), e.getMessage()), e);
         outcome = null;
      }
      
      return outcome;
   }
   
   /**
    * Send an email notification to the specified User authority
    * 
    * @param person     Person node representing the user
    * @param node       Node they are invited too
    * @param from       From text message
    * @param roleText   The role display label for the user invite notification
    */
   private void notifyUser(NodeRef person, NodeRef node, String from, String roleText)
   {
      String to = (String)this.nodeService.getProperty(person, ContentModel.PROP_EMAIL);
      
      if (to != null && to.length() != 0)
      {
         String msgRole = Application.getMessage(FacesContext.getCurrentInstance(), MSG_INVITED_ROLE);
         String roleMessage = MessageFormat.format(msgRole, new Object[] {roleText});
         
         // TODO: include External Authentication link to the invited node
         //String args = node.getStoreRef().getProtocol() + '/' +
         //   node.getStoreRef().getIdentifier() + '/' +
         //   node.getId();
         //String url = ExternalAccessServlet.generateExternalURL(LoginBean.OUTCOME_SPACEDETAILS, args);
         
         String body = this.internalSubject + "\r\n\r\n" + roleMessage + "\r\n\r\n";// + url + "\r\n\r\n";
         if (this.body != null && this.body.length() != 0)
         {
            body += this.body;
         }
         
         SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
         simpleMailMessage.setTo(to);
         simpleMailMessage.setSubject(subject);
         simpleMailMessage.setText(body);
         simpleMailMessage.setFrom(from);
         
         if (logger.isDebugEnabled())
            logger.debug("Sending notification email to: " + to + "\n...with subject:\n" + subject + "\n...with body:\n" + body);
         
         try
         {
            // Send the message
            this.mailSender.send(simpleMailMessage);
         }
         catch (Throwable e)
         {
            // don't stop the action but let admins know email is not getting sent
            logger.error("Failed to send email to " + to, e);
         }
      }
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
               if (group.toLowerCase().indexOf(containsLower) != -1)
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
                      role.equals(wrapper.getRole()))
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
                        NodeRef ref = this.personService.getPerson(authority);
                        String firstName = (String)this.nodeService.getProperty(ref, ContentModel.PROP_FIRSTNAME);
                        String lastName = (String)this.nodeService.getProperty(ref, ContentModel.PROP_LASTNAME);
                        
                        label.append(firstName)
                             .append(" ")
                             .append(lastName != null ? lastName : "")
                             .append(" (")
                             .append(Application.getMessage(FacesContext.getCurrentInstance(), role))
                             .append(")");
                     }
                  }
                  else
                  {
                     // found a group authority
                     label.append(authority.substring(PermissionService.GROUP_PREFIX.length()))
                          .append(" (")
                          .append(Application.getMessage(FacesContext.getCurrentInstance(), role))
                          .append(")");
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

   /**
    * @return Returns the automaticText.
    */
   public String getAutomaticText()
   {
      return this.automaticText;
   }

   /**
    * @param automaticText The automaticText to set.
    */
   public void setAutomaticText(String automaticText)
   {
      this.automaticText = automaticText;
   }
   
   /**
    * @return Returns the email body text.
    */
   public String getBody()
   {
      return this.body;
   }

   /**
    * @param body The email body text to set.
    */
   public void setBody(String body)
   {
      this.body = body;
   }

   /**
    * @return Returns the email subject text.
    */
   public String getSubject()
   {
      return this.subject;
   }

   /**
    * @param subject The email subject text to set.
    */
   public void setSubject(String subject)
   {
      this.subject = subject;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepDescription()
    */
   public String getStepDescription()
   {
      String stepDesc = null;
      
      switch (this.currentStep)
      {
         case 1:
         {
            stepDesc = Application.getMessage(FacesContext.getCurrentInstance(), getStep1DescriptionText());
            break;
         }
         case 2:
         {
            stepDesc = Application.getMessage(FacesContext.getCurrentInstance(), STEP2_DESCRIPTION_ID);
            break;
         }
         default:
         {
            stepDesc = "";
         }
      }
      
      return stepDesc;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepTitle()
    */
   public String getStepTitle()
   {
      String stepTitle = null;
      
      switch (this.currentStep)
      {
         case 1:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP1_TITLE_ID);
            break;
         }
         case 2:
         {
            stepTitle = Application.getMessage(FacesContext.getCurrentInstance(), STEP2_TITLE_ID);
            break;
         }
         default:
         {
            stepTitle = "";
         }
      }
      
      return stepTitle;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#getStepInstructions()
    */
   public String getStepInstructions()
   {
      String stepInstruction = null;
      
      switch (this.currentStep)
      {
         case 2:
         {
            stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), FINISH_INSTRUCTION_ID);
            break;
         }
         default:
         {
            stepInstruction = Application.getMessage(FacesContext.getCurrentInstance(), DEFAULT_INSTRUCTION_ID);
         }
      }
      
      return stepInstruction;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#next()
    */
   public String next()
   {
      String outcome = super.next();
      
      if (outcome.equals("notify"))
      {
         FacesContext context = FacesContext.getCurrentInstance();
         
         // prepare automatic text for email and screen
         StringBuilder buf = new StringBuilder(256);
         
         String personName = Application.getCurrentUser(context).getFullName(getNodeService());
         String msgInvitedTo = Application.getMessage(context, MSG_INVITED_TO);
         Node node = this.getNode();
         String path = this.nodeService.getPath(node.getNodeRef()).toDisplayPath(this.nodeService);
         buf.append(MessageFormat.format(msgInvitedTo, new Object[] {
               path + '/' + node.getName(),
               personName}) );
         
         this.internalSubject = buf.toString();
         
         buf.append("<br>");
         
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
         
         this.automaticText = buf.toString();
      }
      
      return outcome;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#determineOutcomeForStep(int)
    */
   protected String determineOutcomeForStep(int step)
   {
      String outcome = null;
      
      switch(step)
      {
         case 1:
         {
            outcome = "invite";
            break;
         }
         case 2:
         {
            outcome = "notify";
            break;
         }
         default:
         {
            outcome = CANCEL_OUTCOME;
         }
      }
      
      return outcome;
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
