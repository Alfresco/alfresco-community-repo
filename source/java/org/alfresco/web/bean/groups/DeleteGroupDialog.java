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

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;

public class DeleteGroupDialog extends BaseDialogBean
{
   /** The group to be deleted */
   protected String group = null;
   protected String groupName = null;
   
   /** The number of items the group contains */
   protected int numItemsInGroup = 0;
   
   /** The AuthorityService to be used by the bean */
   protected AuthorityService authService;
   
   private static final String MSG_DELETE = "delete";
   private static final String MSG_DELETE_GROUP = "delete_group";

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
         numItemsInGroup = this.authService.getContainedAuthorities(
                     AuthorityType.GROUP, this.group, false).size();
         numItemsInGroup += this.authService.getContainedAuthorities(
                     AuthorityType.USER, this.group, false).size();
      }
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // delete group using the Authentication Service
      this.authService.deleteAuthority(this.group);
      
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
      return Application.getMessage(FacesContext.getCurrentInstance(), MSG_DELETE_GROUP) + " '" + 
             this.groupName + "'";
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
}
