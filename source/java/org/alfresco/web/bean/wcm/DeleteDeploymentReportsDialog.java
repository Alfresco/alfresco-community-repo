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
