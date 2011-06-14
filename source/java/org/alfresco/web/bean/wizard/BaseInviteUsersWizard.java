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
package org.alfresco.web.bean.wizard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
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
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PagingPersonResults;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.TemplateMailHelperBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Backing bean for the Invite Users wizard(s)
 * 
 * @author Kevin Roast
 */
public abstract class BaseInviteUsersWizard extends BaseWizardBean
{
   private static final long serialVersionUID = -5145813383038390250L;
   
   private final static Log logger = LogFactory.getLog(BaseInviteUsersWizard.class);
   
   /** I18N message strings */
   protected static final String MSG_USERROLES = "invite_users_summary";
   private static final String MSG_USERS  = "users";
   private static final String MSG_GROUPS = "groups";
   private static final String MSG_INVITED_TO = "invited_to";
   private static final String MSG_INVITED_ROLE  = "invite_role";
   private static final String MSG_MAX_USERS  = "max_users_returned";
   private static final String MSG_SEARCH_MINIMUM = "picker_search_min";
   
   protected static final String STEP_NOTIFY = "notify";
   
   private static final String NOTIFY_YES = "yes";
   private static final String NOTIFY_NO = "no";
   
   /** NamespaceService bean reference */
   transient private NamespaceService namespaceService;
   
   /** JavaMailSender bean reference */
   transient private JavaMailSender mailSender;
   
   /** AuthorityService bean reference */
   transient private AuthorityService authorityService;
   
   /** PermissionService bean reference */
   transient protected PermissionService permissionService;
   
   /** personService bean reference */
   transient private PersonService personService;
   
   /** Helper providing template based mailing facilities */
   protected TemplateMailHelperBean mailHelper;
   
   /** datamodel for table of roles for users */
   transient private DataModel userRolesDataModel = null;
   
   /** list of user/group role wrapper objects */
   protected List<UserGroupRole> userGroupRoles = null;
   
   /** True to allow duplicate authorities (with a different role) */
   protected boolean allowDuplicateAuthorities = true;
   
   /** Flag to determine if the maximum number of users have been returned */
   protected boolean maxUsersReturned = false;
   
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
     * @return Returns the list of email templates for user notification
     */
    public List<SelectItem> getEmailTemplates()
    {
        List<SelectItem> wrappers = null;

        try
        {
            FacesContext fc = FacesContext.getCurrentInstance();
            NodeRef rootNodeRef = this.getNodeService().getRootNode(Repository.getStoreRef());
            NamespaceService resolver = Repository.getServiceRegistry(fc).getNamespaceService();
            List<NodeRef> results = this.getSearchService().selectNodes(rootNodeRef, getEmailTemplateXPath(), null, resolver, false);

            wrappers = new ArrayList<SelectItem>(results.size() + 1);
            if (results.size() != 0)
            {
                DictionaryService dd = Repository.getServiceRegistry(fc).getDictionaryService();
                for (NodeRef ref : results)
                {
                    if (this.getNodeService().exists(ref) == true)
                    {
                        Node childNode = new Node(ref);
                        if (dd.isSubClass(childNode.getType(), ContentModel.TYPE_CONTENT))
                        {
                            wrappers.add(new SelectItem(childNode.getId(), childNode.getName()));
                        }
                    }
                }

                // make sure the list is sorted by the label
                QuickSort sorter = new QuickSort(wrappers, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
                sorter.sort();
            }
        }
        catch (AccessDeniedException accessErr)
        {
            // ignore the result if we cannot access the root
        }

        // add an entry (at the start) to instruct the user to select an item
        if (wrappers == null)
        {
            wrappers = new ArrayList<SelectItem>(1);
        }
        wrappers.add(0, new SelectItem("none", Application.getMessage(FacesContext.getCurrentInstance(), "select_a_template")));

        return wrappers;
    }

    protected abstract String getEmailTemplateXPath();

    /**
    * @param namespaceService   The NamespaceService to set.
    */
   public void setNamespaceService(NamespaceService namespaceService)
   {
      this.namespaceService = namespaceService;
   }
   
   protected NamespaceService getNamespaceService()
   {
      if (namespaceService == null)
      {
         namespaceService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
      }
      return namespaceService;
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
   
   protected PermissionService getPermissionService()
   {
      if (permissionService == null)
      {
         permissionService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
      }
      return permissionService;
   }
   
   /**
    * @param personService   The PersonService to set.
    */
   public void setPersonService(PersonService personService)
   {
      this.personService = personService;
   }
   
   protected PersonService getPersonService()
   {
      if (personService == null)
      {
         personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService();
      }
      return personService;
   }
   
   /**
    * @param authorityService    The authorityService to set.
    */
   public void setAuthorityService(AuthorityService authorityService)
   {
      this.authorityService = authorityService;
   }
   
   protected AuthorityService getAuthorityService()
   {
      if (authorityService == null)
      {
         authorityService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthorityService();
      }
      return authorityService;
   }

   /**
    * Initialises the wizard
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      maxUsersReturned = false;
      notify = NOTIFY_NO;
      userRolesDataModel = null;
      userGroupRoles = new ArrayList<UserGroupRole>(8);
      mailHelper = new TemplateMailHelperBean();
      mailHelper.setMailSender(mailSender);
      mailHelper.setNodeService(getNodeService());
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      User user = Application.getCurrentUser(context);
      String from = (String)this.getNodeService().getProperty(user.getPerson(), ContentModel.PROP_EMAIL);
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
               this.getPermissionService().setPermission(
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
               if (this.getPersonService().personExists(authority) == true)
               {
                  this.mailHelper.notifyUser(
                        this.getPersonService().getPerson(authority), nodeRef, from, userGroupRole.getRole());
               }
            }
            else if (authType.equals(AuthorityType.GROUP))
            {
               // else notify all members of the group
               Set<String> users = this.getAuthorityService().getContainedAuthorities(AuthorityType.USER, authority, false);
               for (String userAuth : users)
               {
                  if (this.getPersonService().personExists(userAuth) == true)
                  {
                     this.mailHelper.notifyUser(
                           this.getPersonService().getPerson(userAuth), nodeRef, from, userGroupRole.getRole());
                  }
               }
            }
         }
      }
      
      // reset max users flag
      this.maxUsersReturned = false;
      
      return outcome;
   }
   
   /* (non-Javadoc)
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#cancel()
    */
   @Override
   public String cancel()
   {
      // reset max users flag
      this.maxUsersReturned = false;
      
      return super.cancel();
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
      
      // only set the wrapped data once otherwise the rowindex is reset
      if (this.userRolesDataModel.getWrappedData() == null)
      {
         this.userRolesDataModel.setWrappedData(this.userGroupRoles);
      }
      
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
      
      // quick exit if not enough characters entered for a search
      String search = contains.trim();
      int searchMin = Application.getClientConfig(context).getPickerSearchMinimum();
      if (search.length() < searchMin)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(context, MSG_SEARCH_MINIMUM), searchMin));
         return new SelectItem[0];
      }
      
      SelectItem[] items;
      this.maxUsersReturned = false;
      
      UserTransaction tx = null;
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         int maxResults = Application.getClientConfig(context).getInviteUsersMaxResults();
         if(maxResults <=0)
         {
            maxResults = Utils.getPersonMaxResults();
         }
         
         List<SelectItem> results;
         
         if (filterIndex == 0)
         {
            // Use lucene search to retrieve user details
            List<Pair<QName,String>> filter = null;
            if (search == null || search.length() == 0)
            {
               // if there is no search term, search for all people
            }
            else
            {
               filter = Utils.generatePersonFilter(search);
            }
               
            if (logger.isDebugEnabled())
            {
               logger.debug("Maximum invite users results size: " + maxResults);
               logger.debug("Using query filter to find users: " + filter);
            }
            
            PagingPersonResults people = getPersonService().getPeople(
                  filter,
                  true,
                  Utils.generatePersonSort(),
                  new PagingRequest(maxResults, null)
            );
            List<NodeRef> nodes = people.getPage();
            
            results = new ArrayList<SelectItem>(nodes.size());
            for (int index=0; index<nodes.size(); index++)
            {
               NodeRef personRef = nodes.get(index);
               
               String firstName = (String)this.getNodeService().getProperty(personRef, ContentModel.PROP_FIRSTNAME);
               String lastName = (String)this.getNodeService().getProperty(personRef, ContentModel.PROP_LASTNAME);
               String username = (String)this.getNodeService().getProperty(personRef, ContentModel.PROP_USERNAME);
               if (username != null)
               {
                  String name = (firstName != null ? firstName : "") + ' ' + (lastName != null ? lastName : "");
                  SelectItem item = new SortableSelectItem(username, name + " [" + username + "]", lastName != null ? lastName : username);
                  results.add(item);
               }
            }
         }
         else
         {
            results = addGroupItems(search, maxResults);
         }
         
         items = new SelectItem[results.size()];
         results.toArray(items);
         Arrays.sort(items);
         
         // set the maximum users returned flag if appropriate
         if (results.size() == maxResults)
         {
            this.maxUsersReturned = true;
         }
         
         // commit the transaction
         tx.commit();
      }
      catch (BooleanQuery.TooManyClauses clauses)
      {
         Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), "too_many_users"));
         
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         
         items = new SelectItem[0];
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
   
   private List<SelectItem> addGroupItems(String search, int maxResults)
   {
       Set<String> groups = getGroups(search);
       
       List<SelectItem> results = new ArrayList<SelectItem>(groups.size());
       
       int count = 0;
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
          
          if (++count == maxResults) break;
       }
       return results;
   }
   
   protected Set<String> getGroups(String search)
   {
       // groups - text search match on supplied name
       String term = "*" + search + "*";
       Set<String> groups;
       groups = getAuthorityService().findAuthorities(AuthorityType.GROUP, null, false, term,
                AuthorityService.ZONE_APP_DEFAULT);
       groups.addAll(getAuthorityService().getAllAuthorities(AuthorityType.EVERYONE));
       return groups;
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
               addAuthorityWithRole(results[i], role);
            }
         }
      }
   }

   /**
    * Add an authority with the specified role to the list managed by this wizard.
    * 
    * @param authority        Authority to add (cannot be null)
    * @param role             Role for the authorities (cannot be null)
    */
   public void addAuthorityWithRole(String authority, String role)
   {
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
            if (authType == AuthorityType.GUEST || getPersonService().personExists(authority) == true)
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
   
   /**
    * Action handler called when the Remove button is pressed to remove a user+role
    */
   public void removeSelection(ActionEvent event)
   {
      UserGroupRole wrapper = (UserGroupRole)getUserRolesDataModel().getRowData();
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
         if (displayLabel.startsWith("$$") == true)
         {
             displayLabel = permission;
         }
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
         
         String personName = Application.getCurrentUser(context).getFullName(this.getNodeService());
         String msgInvitedTo = Application.getMessage(context, MSG_INVITED_TO);
         Node node = this.getNode();
         String path = this.getNodeService().getPath(node.getNodeRef()).toDisplayPath(
                 this.getNodeService(), getPermissionService());
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
      NodeRef ref = this.getPersonService().getPerson(authority);
      String firstName = (String)this.getNodeService().getProperty(ref, ContentModel.PROP_FIRSTNAME);
      String lastName = (String)this.getNodeService().getProperty(ref, ContentModel.PROP_LASTNAME);
      
      StringBuilder buf = new StringBuilder(100);
      buf.append(firstName != null ? firstName : "")
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
      String groupDisplayName = this.authorityService.getAuthorityDisplayName(authority);
      if (groupDisplayName == null || groupDisplayName.length() == 0)
      {
         groupDisplayName = authority.substring(PermissionService.GROUP_PREFIX.length());
      }
      
      StringBuilder buf = new StringBuilder(100);
      buf.append(groupDisplayName)
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
         buf.append(Utils.encode(userRole.getLabel()));
         buf.append("<br>");
      }
      
      return buildSummary(
            new String[] {Application.getMessage(fc, MSG_USERROLES)},
            new String[] {buf.toString()});
   }
   
   /**
    * @return flag to indicate whether maximum users have been returned
    */
   public boolean getHaveMaximumUsersBeenReturned()
   {
      return this.maxUsersReturned;
   }
   
   /**
    * @return Message to display when the maximum number of users have been returned
    */
   public String getMaximumUsersMsg()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      
      String pattern = Application.getMessage(context, MSG_MAX_USERS);
      String msg = MessageFormat.format(pattern, 
               Application.getClientConfig(context).getInviteUsersMaxResults());
      
      return Utils.encode(msg);
   }
   
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();

      this.userRolesDataModel = new ListDataModel();
      this.userRolesDataModel.setWrappedData(this.userGroupRoles);

   }
   
   /**
    * Simple wrapper class to represent a user/group and a role combination
    */
   public static class UserGroupRole implements Serializable
   {
      private static final long serialVersionUID = -3200146057437311225L;
      
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
