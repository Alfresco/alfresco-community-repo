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
package org.alfresco.web.bean.groups;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.groups.GroupsDialog.UserAuthorityDetails;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;
import org.apache.lucene.search.BooleanQuery;

/**
 * Implementation of the add user dialog.
 * 
 * @author YanO
 * @author gavinc
 */
public class AddUsersDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 4893334797091942357L;

   /** The id of the group to add users to */
   protected String group;
   
   /** Name of the group to add users to */
   protected String groupName;
   
   /** The AuthorityService to be used by the bean */
   transient private AuthorityService authService;

   /** personService bean reference */
   transient private PersonService personService;

   /** selected users to be added to a group */
   protected List<UserAuthorityDetails> usersForGroup;

   /** datamodel for table of users added to group */
   transient protected DataModel usersDataModel = null;

   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // retrieve parameters
      this.group = parameters.get(GroupsDialog.PARAM_GROUP);
      this.groupName = parameters.get(GroupsDialog.PARAM_GROUP_NAME);
      
      this.usersDataModel = null;
      usersForGroup = new ArrayList<UserAuthorityDetails>();
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // add each selected user to the current group in turn
      for (UserAuthorityDetails wrapper : this.usersForGroup)
      {
         this.getAuthService().addAuthority(this.group, wrapper.getAuthority());
      }
      
      return outcome;
   }

   @Override
   public String getContainerSubTitle()
   {
      return this.groupName;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   // ------------------------------------------------------------------------------
   // Bean property getters and setters
   
   public void setAuthService(AuthorityService authService)
   {
      this.authService = authService;
   }

   /**
    * @return the authService
    */
   protected AuthorityService getAuthService()
   {
     //check for null in cluster environment
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
   
   /**
    * @return the personService
    */
   protected PersonService getPersonService()
   {
      //check for null in cluster environment
      if (personService == null)
      {
         personService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPersonService();
      }
      return personService;
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

      // only set the wrapped data once otherwise the rowindex is reset
      if (this.usersDataModel.getWrappedData() == null)
      {
         this.usersDataModel.setWrappedData(this.usersForGroup);
      }

      return this.usersDataModel;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   /**
    * Query callback method executed by the Generic Picker component. This
    * method is part of the contract to the Generic Picker, it is up to the
    * backing bean to execute whatever query is appropriate and return the
    * results.
    * 
    * @param filterIndex Index of the filter drop-down selection
    * @param contains Text from the contains textbox
    * @return An array of SelectItem objects containing the results to display
    *         in the picker.
    */
   public SelectItem[] pickerCallback(int filterIndex, final String contains)
   {
      final FacesContext context = FacesContext.getCurrentInstance();

      UserTransaction tx = null;
      try
      {
         RetryingTransactionHelper txHelper = Repository.getRetryingTransactionHelper(context);// getUserTransaction(context);
         return txHelper.doInTransaction(new RetryingTransactionCallback<SelectItem[]>()
         {
            public SelectItem[] execute() throws Exception
            {
               SelectItem[] items = new SelectItem[0];
               
               // Use lucene search to retrieve user details
               String term = AbstractLuceneQueryParser.escape(contains.trim());
               if (term.length() != 0)
               {
                   StringBuilder query = new StringBuilder(128);
                   Utils.generatePersonSearch(query, term);
                   List<NodeRef> nodes;
                   ResultSet resultSet = Repository.getServiceRegistry(context).getSearchService().query(
                           Repository.getStoreRef(),
                           SearchService.LANGUAGE_LUCENE,
                           query.toString());
                   try
                   {
                      nodes = resultSet.getNodeRefs();
                   }
                   finally
                   {
                      resultSet.close();
                   }
                   
                   ArrayList<SelectItem> itemList = new ArrayList<SelectItem>(nodes.size());
                   for (NodeRef personRef : nodes)
                   {
                      String username = (String)getNodeService().getProperty(personRef, ContentModel.PROP_USERNAME);
                      if (AuthenticationUtil.getGuestUserName().equals(username) == false)
                      {
                         String firstName = (String)getNodeService().getProperty(personRef, ContentModel.PROP_FIRSTNAME);
                         String lastName = (String)getNodeService().getProperty(personRef, ContentModel.PROP_LASTNAME);
                         
                         // build a sensible label for display
                         String name = (firstName != null ? firstName : "") + ' ' + (lastName != null ? lastName : "");
                         SelectItem item = new SortableSelectItem(username, name + " [" + username + "]", lastName != null ? lastName : username);
                         itemList.add(item);
                      }
                   }
                   items = new SelectItem[itemList.size()];
                   itemList.toArray(items);
               }
               return items;
            }
         });
      }
      catch (BooleanQuery.TooManyClauses clauses)
      {
         Utils.addErrorMessage(Application.getMessage(
                  FacesContext.getCurrentInstance(), "too_many_users"));
         
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         
         return new SelectItem[0];
      }
      catch (Exception err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
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
         return new SelectItem[0];
      }
   }

   // ------------------------------------------------------------------------------
   // Event handlers
   
   /**
    * Add the selected User to the list for adding to a Group
    */
   public void addSelectedUsers(ActionEvent event)
   {
      UIGenericPicker picker = (UIGenericPicker) event.getComponent().findComponent("picker");
      String[] results = picker.getSelectedResults();
      if (results != null)
      {
         for (int i = 0; i < results.length; i++)
         {
            String authority = results[i];

            // check for same authority so not added twice
            boolean foundExisting = false;
            for (int n = 0; n < this.usersForGroup.size(); n++)
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
               if (this.getPersonService().personExists(authority) == true)
               {
                  // found a Person with a User authority
                  NodeRef ref = this.getPersonService().getPerson(authority);
                  String firstName = (String)getNodeService().getProperty(ref, ContentModel.PROP_FIRSTNAME);
                  String lastName = (String)getNodeService().getProperty(ref, ContentModel.PROP_LASTNAME);
                  
                  // build a sensible label for display
                  label.append(firstName != null ? firstName : "").append(' ').append(lastName != null ? lastName : "");
                  
                  // add a wrapper object with the details to the results list for display
                  UserAuthorityDetails userDetails = new UserAuthorityDetails(label.toString(), authority);
                  this.usersForGroup.add(userDetails);
               }
            }
         }
      }
   }

   /**
    * Action handler called when the Remove button is pressed to remove a user
    * from the results list
    */
   public void removeUserSelection(ActionEvent event)
   {
      UserAuthorityDetails wrapper = (UserAuthorityDetails) this.usersDataModel.getRowData();
      if (wrapper != null)
      {
         this.usersForGroup.remove(wrapper);
      }
   }
}
