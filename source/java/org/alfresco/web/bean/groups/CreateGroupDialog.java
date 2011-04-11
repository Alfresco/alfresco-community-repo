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
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;

public class CreateGroupDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -8074475974375860695L;
   
   protected String parentGroup;
   protected String parentGroupName;
   protected String name;
   
   /** The AuthorityService to be used by the bean */
   transient private AuthorityService authService;
   
   private static final String MSG_ERR_EXISTS = "groups_err_exists";
   private static final String MSG_GROUPNAME_LENGTH = "groups_err_group_name_length";
   private static final String MSG_ERR_NAME = "groups_err_group_name";
   private static final String MSG_ROOT_GROUPS = "root_groups";
   private static final String MSG_BUTTON_NEW_GROUP = "new_group";

   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);

      // retrieve parameters
      this.parentGroup = parameters.get(GroupsDialog.PARAM_GROUP);
      this.parentGroupName = parameters.get(GroupsDialog.PARAM_GROUP_NAME);
      
      // reset variables
      this.name = null;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // create new Group using Authentication Service
      String groupName = this.getAuthService().getName(AuthorityType.GROUP, this.name);
      if (this.getAuthService().authorityExists(groupName) == false)
      {
         this.getAuthService().createAuthority(AuthorityType.GROUP, this.name);
         if (this.parentGroup != null)
         {
             this.getAuthService().addAuthority(this.parentGroup, groupName);
         }
      }
      else
      {
         Utils.addErrorMessage(Application.getMessage(context, MSG_ERR_EXISTS));
         outcome = null;
         this.isFinished = false; 
      }
      
      return outcome;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_BUTTON_NEW_GROUP);
   }

   @Override
   public String getContainerSubTitle()
   {
      String subtitle = null;

      if (this.parentGroupName != null)
      {
         subtitle = this.parentGroupName;
      }
      else
      {
         subtitle = Application.getMessage(FacesContext.getCurrentInstance(), MSG_ROOT_GROUPS);
      }

      return subtitle;
   }
   
   
   // ------------------------------------------------------------------------------
   // Bean property getters and setters
   
   public String getName()
   {
      return this.name;
   }

   public void setName(String name)
   {
      this.name = name;
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
      if (authService == null)
      {
         authService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAuthorityService();
      }
      return authService;
   }
   
   
   // ------------------------------------------------------------------------------
   // Helpers

   public void validateGroupName(FacesContext context, UIComponent component, Object value) throws ValidatorException
   {
      int minGroupNameLength = Application.getClientConfig(context).getMinGroupNameLength();

      String name = ((String)value).trim();

      if (name.length() < minGroupNameLength || name.length() > 100)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_GROUPNAME_LENGTH),
               new Object[]{minGroupNameLength, 100});
         throw new ValidatorException(new FacesMessage(err));
      }

      if (name.indexOf('"') != -1 || name.indexOf('\\') != -1)
      {
         String err = MessageFormat.format(Application.getMessage(context, MSG_ERR_NAME), 
                  new Object[] { "\", \\" });
         throw new ValidatorException(new FacesMessage(err));
      }
   }
}
