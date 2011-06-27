/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.web.bean.workflow;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.transaction.UserTransaction;

import org.alfresco.query.PagingRequest;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.SortableSelectItem;
import org.alfresco.web.ui.common.Utils;

/**
 * Base dialog bean for workflow user reassignment.
 * 
 * @author gavinc
 * @author Kevin Roast
 */
public abstract class BaseReassignDialog extends BaseDialogBean
{
   /**
     * 
     */
    private static final long serialVersionUID = 3392941403282035753L;
    
    private static final String MSG_SEARCH_MINIMUM = "picker_search_min";
   
   transient private WorkflowService workflowService;
   transient private PersonService personService;
   
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * Sets the workflow service to use
    * 
    * @param workflowService The WorkflowService instance
    */
   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }
   
   protected WorkflowService getWorkflowService()
   {
      if (workflowService == null)
      {
         workflowService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWorkflowService();
      }
      return workflowService;
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
    * Property accessed by the Generic Picker component.
    * 
    * @return the array of filter options to show in the users/groups picker
    */
   public SelectItem[] getFilters()
   {
      ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
      return new SelectItem[] {new SelectItem("0", bundle.getString("users"))};
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
      
      UserTransaction tx = null;
      ResultSet resultSet = null;
      try
      {
         tx = Repository.getUserTransaction(context, true);
         tx.begin();
         
         int maxResults = Application.getClientConfig(context).getInviteUsersMaxResults();
         if(maxResults <= 0)
         {
            maxResults = Utils.getPersonMaxResults();
         }
         
         List<PersonInfo> persons = getPersonService().getPeople(
               Utils.generatePersonFilter(contains.trim()),
               true,
               Utils.generatePersonSort(),
               new PagingRequest(maxResults, null)
         ).getPage();
         
         ArrayList<SelectItem> itemList = new ArrayList<SelectItem>(persons.size());
         for (PersonInfo person : persons)
         {
            String username = person.getUserName();
            if (AuthenticationUtil.getGuestUserName().equals(username) == false)
            {
               String firstName = person.getFirstName();
               String lastName = person.getLastName();
               String name = (firstName != null ? firstName : "") + ' ' + (lastName != null ? lastName : "");
               SelectItem item = new SortableSelectItem(username, name + " [" + username + "]", lastName != null ? lastName : username);
               itemList.add(item);
            }
         }
         items = new SelectItem[itemList.size()];
         itemList.toArray(items);
         
         Arrays.sort(items);
         
         // commit the transaction
         tx.commit();
      }
      catch (Throwable err)
      {
         Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
               FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), err.getMessage()), err);
         try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
         
         items = new SelectItem[0];
      }
      finally
      {
         if (resultSet != null)
         {
            resultSet.close();
         }
      }
      
      return items;
   }
}
