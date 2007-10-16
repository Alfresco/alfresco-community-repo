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
package org.alfresco.web.bean.wcm;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.AlfrescoNavigationHandler;

/**
 * Backing bean for the Edit Web Project wizard.
 * 
 * @author Kevin Roast
 */
public class EditWebsiteWizard extends CreateWebsiteWizard
{
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   /**
    * Initialises the wizard
    */
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      // the editMode flag is used to disabled some wizard fields
      this.editMode = true;
      
      NodeRef websiteRef = this.browseBean.getActionSpace().getNodeRef();
      if (websiteRef == null)
      {
         throw new IllegalArgumentException("Edit Web Project wizard requires action node context.");
      }
      
      loadWebProjectModel(websiteRef, true, false);
   }
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      NodeRef nodeRef = this.browseBean.getActionSpace().getNodeRef();
      
      // apply the name, title and description props
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, this.name);
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, this.title);
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, this.description);
      this.nodeService.setProperty(nodeRef, WCMAppModel.PROP_DEPLOYTO, (Serializable)this.deployTo);
      
      // clear the existing settings for forms, template and workflows - then the existing methods
      // can be used to apply the modified and previous settings from scratch
      clearWebProjectModel(nodeRef);
      
      // change/create the root webapp name for the website
      if (this.webapp != null && this.webapp.length() != 0)
      {
         String stagingStore = AVMUtil.buildStagingStoreName(this.dnsName);
         String webappPath = AVMUtil.buildStoreWebappPath(stagingStore, this.webapp);
         if (this.avmService.lookup(-1, webappPath) == null)
         {
            this.avmService.createDirectory(AVMUtil.buildSandboxRootPath(stagingStore), this.webapp);
         }
         this.nodeService.setProperty(nodeRef, WCMAppModel.PROP_DEFAULTWEBAPP, this.webapp);
      }
      
      // TODO: allow change of dns name - via store rename functionality
      
      // persist the forms, templates, workflows and workflow defaults to the model for this web project
      saveWebProjectModel(nodeRef);
      
      return AlfrescoNavigationHandler.CLOSE_WIZARD_OUTCOME;
   }
   
   /**
    * Cascade delete the existing Form and Workflow defs attached to the specified Web Project node
    * 
    * @param nodeRef    Web project node
    */
   private void clearWebProjectModel(NodeRef nodeRef)
   {
      List<ChildAssociationRef> webFormRefs = nodeService.getChildAssocs(
               nodeRef, WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : webFormRefs)
      {
         // cascade delete will take case of child-child relationships
         this.nodeService.removeChild(nodeRef, ref.getChildRef());
      }
      
      List<ChildAssociationRef> wfRefs = nodeService.getChildAssocs(
               nodeRef, WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : wfRefs)
      {
         this.nodeService.removeChild(nodeRef, ref.getChildRef());
      }
   }
}
