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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.web.app.AlfrescoNavigationHandler;

/**
 * Backing bean for the Edit Web Project wizard.
 * 
 * @author Kevin Roast
 */
public class EditWebsiteWizard extends CreateWebsiteWizard
{
   private static final long serialVersionUID = -4856350244207566218L;
   
   protected AVMBrowseBean avmBrowseBean;

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
      
      this.webappsList = null;
      
      loadWebProjectModel(websiteRef, true, false);
   }
   
   @Override
   public boolean getFinishButtonDisabled()
   {
      // always allow Finish as we are editing existing settings
      return false;
   }
   
   /**
    * @return List of SelectItem objects representing the webapp folders present in the project
    */
   @Override
   public List<SelectItem> getWebappsList()
   {
      if (this.webappsList == null)
      {
         // create list of webapps
         List<String> webApps = getWebProjectService().listWebApps(getWebProjectNodeRef());
         this.webappsList = new ArrayList<SelectItem>(webApps.size());
         for (String webAppName : webApps)
         {
            this.webappsList.add(new SelectItem(webAppName, webAppName));
         }
      }
      
      return this.webappsList;
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      NodeRef nodeRef = this.browseBean.getActionSpace().getNodeRef();
      
      WebProjectInfo wpInfo = getWebProjectService().getWebProject(nodeRef);

      if(!wpInfo.getName().equals(this.name))
      {
    	  getFileFolderService().rename(nodeRef, this.name); 
      }

      // apply the name, title and description props
      
      wpInfo.setName(this.name);
      wpInfo.setTitle(this.title);
      wpInfo.setDescription(this.description);
      wpInfo.setIsTemplate(this.isSource);
      wpInfo.setPreviewProviderName(this.previewProvider);
      
      getWebProjectService().updateWebProject(wpInfo);
      
      // clear the existing settings for forms, template, workflows and deployment - then 
      // the existing methods can be used to apply the modified and previous settings from scratch
      clearWebProjectModel(nodeRef);
      
      // change the root webapp name for the website
      if (this.webapp != null && this.webapp.length() != 0)
      {
         getNodeService().setProperty(nodeRef, WCMAppModel.PROP_DEFAULTWEBAPP, this.webapp);
         
         // inform the AVMBrowseBean of the potential change
         this.avmBrowseBean.setWebapp(this.webapp);
      }
      
      // TODO: allow change of dns name - via store rename functionality
      
      // persist the forms, templates, workflows, workflow defaults and deployment config
      // to the model for this web project
      saveWebProjectModel(nodeRef);

      // Make sure name and description are refreshed before display
      this.navigator.resetCurrentNodeProperties();      

      return AlfrescoNavigationHandler.CLOSE_WIZARD_OUTCOME;
   }
   
   /**
    * @param avmBrowseBean The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * Cascade delete the existing Form and Workflow defs attached to the specified Web Project node
    * 
    * @param nodeRef    Web project node
    */
   private void clearWebProjectModel(NodeRef nodeRef)
   {
      List<ChildAssociationRef> webFormRefs = getNodeService().getChildAssocs(
               nodeRef, WCMAppModel.ASSOC_WEBFORM, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : webFormRefs)
      {
         // cascade delete will take case of child-child relationships
         getNodeService().removeChild(nodeRef, ref.getChildRef());
      }
      
      List<ChildAssociationRef> wfRefs = getNodeService().getChildAssocs(
               nodeRef, WCMAppModel.ASSOC_WEBWORKFLOWDEFAULTS, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : wfRefs)
      {
         getNodeService().removeChild(nodeRef, ref.getChildRef());
      }
      
      List<ChildAssociationRef> serverRefs = getNodeService().getChildAssocs(
               nodeRef, WCMAppModel.ASSOC_DEPLOYMENTSERVER, RegexQNamePattern.MATCH_ALL);
      for (ChildAssociationRef ref : serverRefs)
      {
         getNodeService().removeChild(nodeRef, ref.getChildRef());
      }
   }
}
