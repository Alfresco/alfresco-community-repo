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

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;

public class DeleteGroupDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -8743700617152460027L;
   
   /** The group to be deleted */
   protected String group = null;
   protected String groupName = null;
   
   /** The number of items the group contains */
   protected int numItemsInGroup = 0;
   
   /** The AuthorityService to be used by the bean */
   transient private AuthorityService authService;
   
   private static final String MSG_DELETE = "delete";
   private static final String MSG_DELETE_GROUP = "delete_group";
   private final static String MSG_LEFT_QUOTE = "left_qoute";
   private final static String MSG_RIGHT_QUOTE = "right_quote";

   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      // retrieve parameters
      this.group = parameters.get(GroupsDialog.PARAM_GROUP);
      this.groupName = parameters.get(GroupsDialog.PARAM_GROUP_NAME);
      
      // calculate the number of items the givev group has
      if (this.group != null)
      {
         numItemsInGroup = this.getAuthService().getContainedAuthorities(
                     AuthorityType.GROUP, this.group, false).size();
         numItemsInGroup += this.getAuthService().getContainedAuthorities(
                     AuthorityType.USER, this.group, false).size();
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // delete group using the Authentication Service
      this.getAuthService().deleteAuthority(this.group);
      
      return outcome;
   }

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // add the group to the request object so it gets picked up by
      // groups dialog, this will allow it to be removed from the breadcrumb
      context.getExternalContext().getRequestMap().put(
               GroupsDialog.KEY_GROUP, this.group);
      
      return outcome;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DELETE);
   }

   @Override
   public String getContainerTitle()
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return Application.getMessage(fc, MSG_DELETE_GROUP) + " " + Application.getMessage(fc, MSG_LEFT_QUOTE) + 
             this.groupName + Application.getMessage(fc, MSG_RIGHT_QUOTE);
   }
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters
   
   public String getGroupName()
   {
      return this.groupName;
   }
   
   public int getNumItemsInGroup()
   {
      return this.numItemsInGroup;
   }
   
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
}
