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

import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Delete Deployment Reports" dialog
 *
 * @author gavinc
 */
public class DeleteDeploymentReportsDialog extends BaseDialogBean
{
   protected AVMBrowseBean avmBrowseBean;
   
   private static final long serialVersionUID = -3702005115210010993L;
   
   private static final Log logger = LogFactory.getLog(DeleteDeploymentReportsDialog.class);

   // ------------------------------------------------------------------------------
   // Dialog implementation

   @SuppressWarnings("unchecked")
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
      // get the node service and the website we are deleting reports for
      NodeService nodeService = this.getNodeService();
      NodeRef websiteRef = this.avmBrowseBean.getWebsite().getNodeRef();
      
      // just in case there are any left, iterate through any old deploymentreport 
      // associations from the current web project and delete them.
      List<ChildAssociationRef> deployReportRefs = nodeService.getChildAssocs(
               websiteRef, WCMAppModel.ASSOC_DEPLOYMENTREPORT, 
               RegexQNamePattern.MATCH_ALL);
      int count = deployReportRefs.size();
      for (ChildAssociationRef ref : deployReportRefs)
      {
         NodeRef report = ref.getChildRef();
         if (report != null)
         {
            // remove the node
            nodeService.deleteNode(report);
         }
      }
      
      // iterate through all deploymentattempt associations from the current 
      // web project and delete them.
      List<ChildAssociationRef> deployAttemptRefs = nodeService.getChildAssocs(
               websiteRef, WCMAppModel.ASSOC_DEPLOYMENTATTEMPT, 
               RegexQNamePattern.MATCH_ALL);
      count += deployAttemptRefs.size();
      for (ChildAssociationRef ref : deployAttemptRefs)
      {
         NodeRef attempt = ref.getChildRef();
         if (attempt != null)
         {
            // remove the node
            nodeService.deleteNode(attempt);
         }
      }
      
      // remove the old properties in case they are still present
      nodeService.removeProperty(websiteRef, 
               WCMAppModel.PROP_DEPLOYTO);
      nodeService.removeProperty(websiteRef, 
               WCMAppModel.PROP_SELECTEDDEPLOYTO);
      nodeService.removeProperty(websiteRef, 
               WCMAppModel.PROP_SELECTEDDEPLOYVERSION);
      
      // remove the hasBeenDeployed object from the session so it gets
      // re-evaluated (and disappears)
      Map request = FacesContext.getCurrentInstance().
            getExternalContext().getRequestMap();
      request.remove(AVMBrowseBean.REQUEST_BEEN_DEPLOYED_RESULT);
      
      if (logger.isDebugEnabled())
         logger.debug("Removed " + count + " previous deployment attempts"); 
         
      // close dialog and refresh website view
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
                AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browseWebsite";
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }
   
   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "yes");
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "no");
   }

   // ------------------------------------------------------------------------------
   // Bean Getters and Setters

   /**
    * @param avmBrowseBean    The AVM BrowseBean to set
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * Returns the confirmation to display to the user before deleting the reports.
    *
    * @return The message to display
    */
   public String getConfirmMessage()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "delete_reports_confirm");
   }
}
