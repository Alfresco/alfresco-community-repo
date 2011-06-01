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
package org.alfresco.web.bean.wcm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.BaseInviteUsersWizard;
import org.alfresco.web.ui.common.Utils;

/**
 * Bean providing the ability to invite users to a web project space.
 * 
 * @author kevinr
 */
public class InviteWebsiteUsersWizard extends BaseInviteUsersWizard
{
   private static final long serialVersionUID = -8128781845465773847L;

   /** the node representing the website */
   private Node website;
   
   /** root AVM store the users are invited into */
   private String avmStore;
   
   /** assume we are launching the wizard standalone */
   private boolean standalone = true;
   
   transient private WebProjectService wpService;
   

   public void setWebProjectService(WebProjectService wpService)
   {
      this.wpService = wpService;
   }

   protected WebProjectService getWebProjectService()
   {
      if (wpService == null)
      {
         wpService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWebProjectService();
      }
      return wpService;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.BaseInviteUsersWizard#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      // only allow one selection per authority
      this.allowDuplicateAuthorities = false;
      this.website = null;
      this.avmStore = null;
      this.standalone = true;
   }
   
   public void reset()
   {
      this.isFinished = false;
      this.allowDuplicateAuthorities = false;
      this.website = null;
      this.avmStore = null;
      this.standalone = true;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.BaseInviteUsersWizard#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      super.finishImpl(context, outcome);
      
      Map<String, String> selectedInvitees = new HashMap<String, String>(this.userGroupRoles.size());
      for (UserGroupRole userRole : this.userGroupRoles)
      {
          selectedInvitees.put(userRole.getAuthority(), userRole.getRole());
      }
      
      getWebProjectService().inviteWebUsersGroups(this.getNode().getNodeRef(), selectedInvitees, true);

      return outcome;
   }

   /**
    * @return summary text for the wizard
    */
   public String getSummary()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      
      // build a summary section to list the invited users and there roles
      StringBuilder buf = new StringBuilder(128);
      String currentUser = Application.getCurrentUser(fc).getUserName();
      boolean foundCurrentUser = false;
      for (UserGroupRole userRole : this.userGroupRoles)
      {
         if (currentUser.equals(userRole.getAuthority()))
         {
            foundCurrentUser = true;
         }
         buf.append(Utils.encode(userRole.getLabel()));
         buf.append("<br>");
      }
      if (isStandalone() == false && foundCurrentUser == false)
      {
         buf.append(buildLabelForUserAuthorityRole(
               currentUser, WCMUtil.ROLE_CONTENT_MANAGER));
      }
      
      return buildSummary(
            new String[] {Application.getMessage(fc, MSG_USERROLES)},
            new String[] {buf.toString()});
   }

   @Override
   protected Set<String> getPermissionsForType()
   {
      // Let the permission service do the caching to allow for dynamic model updates, etc.
      return this.permissionService.getSettablePermissions(WCMAppModel.TYPE_AVMWEBFOLDER);
   }

   protected void setNode(Node node)
   {
      this.website = node;
   }
   
   @Override
   protected Node getNode()
   {
      if (this.website != null)
      {
         return this.website;
      }
      else
      {
         return this.browseBean.getActionSpace();
      }
   }

   /**
    * @return Returns the root AVM store.
    */
   public String getAvmStore()
   {
      if (this.avmStore == null)
      {
         this.avmStore = (String)getNode().getProperties().get(WCMAppModel.PROP_AVMSTORE);
      }
      return this.avmStore;
   }

   /**
    * @param avmStore The root AVM store to set.
    */
   public void setAvmStore(String avmStore)
   {
      this.avmStore = avmStore;
   }

   /**
    * @return Returns the edit mode.
    */
   public boolean isStandalone()
   {
      return this.standalone;
   }

   /**
    * @param editMode The edit mode to set.
    */
   public void setStandalone(boolean editMode)
   {
      this.standalone = editMode;
   }

    @Override
    protected String getEmailTemplateXPath()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        String xpath = Application.getRootPath(fc) + "/" + 
              Application.getGlossaryFolderName(fc) + "/" +
              Application.getEmailTemplatesFolderName(fc) + "/" + 
              Application.getInviteEmailTemplatesFolderName(fc) + "//*";
        return xpath;
    }
}
