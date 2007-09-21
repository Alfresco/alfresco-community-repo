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
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.service.cmr.security.PermissionService;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.GroupsDialog;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIGenericPicker;

/**
 * Implementation of the add user dialog.
 * 
 * @author YanO
 * @author gavinc
 */
public class AddUsersDialog extends GroupsDialog
{
   private static final String BUTTON_FINISH = "finish_button";

   /** selected users to be added to a group */
   private List<UserAuthorityDetails> usersForGroup;

   /** datamodel for table of users added to group */
   private DataModel usersDataModel = null;

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      usersForGroup = new ArrayList<UserAuthorityDetails>();
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      try
      {
         // add each selected user to the current group in turn
         for (UserAuthorityDetails wrapper : this.usersForGroup)
         {
            properties.getAuthService().addAuthority(properties.getActionGroup(), wrapper.getAuthority());
         }
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                  FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         outcome = null;
      }
      setActionGroup(null);
      return outcome;
   }

   @Override
   public String getContainerSubTitle()
   {
      return properties.getActionGroupName();
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), BUTTON_FINISH);
   }

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
               SelectItem[] items;

               // build xpath to match available User/Person objects
               ServiceRegistry services = Repository.getServiceRegistry(context);
               NodeRef peopleRef = properties.getPersonService().getPeopleContainer();
               String xpath = "*[like(@" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + "firstName, '%" + contains + 
                              "%', false)" + " or " + "like(@" + NamespaceService.CONTENT_MODEL_PREFIX + ":" + 
                              "lastName, '%" + contains + "%', false)]";

               List<NodeRef> nodes = services.getSearchService().selectNodes(peopleRef, xpath, null, 
                        services.getNamespaceService(), false);

               ArrayList<SelectItem> itemList = new ArrayList<SelectItem>(nodes.size());
               for (NodeRef personRef : nodes)
               {
                  String username = (String) nodeService.getProperty(personRef, ContentModel.PROP_USERNAME);
                  if (PermissionService.GUEST_AUTHORITY.equals(username) == false)
                  {
                     String firstName = (String) nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
                     String lastName = (String) nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);

                     SelectItem item = new SortableSelectItem(username, firstName + " " + lastName, lastName);
                     itemList.add(item);
                  }
               }
               items = new SelectItem[itemList.size()];
               itemList.toArray(items);
               return items;
            }
         });
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
               if (properties.getPersonService().personExists(authority) == true)
               {
                  // found a Person with a User authority
                  NodeRef ref = properties.getPersonService().getPerson(authority);
                  String firstName = (String) this.nodeService.getProperty(ref, ContentModel.PROP_FIRSTNAME);
                  String lastName = (String) this.nodeService.getProperty(ref, ContentModel.PROP_LASTNAME);

                  // build a sensible label for display
                  label.append(firstName).append(' ').append(lastName);

                  // add a wrapper object with the details to the results list
                  // for display
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
}
