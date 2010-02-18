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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Bean implementation for the "Release Test Server" dialog
 *
 * @author gavinc
 */
public class ReleaseTestServerDialog extends BaseDialogBean
{
   protected String store;
   
   private static final long serialVersionUID = -3702005115210010993L;
   
   private static final Log logger = LogFactory.getLog(ReleaseTestServerDialog.class);

   // ------------------------------------------------------------------------------
   // Dialog implementation

   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.store = parameters.get("store");
      
      if (this.store == null || this.store.length() == 0)
      {
         throw new IllegalArgumentException("store parameter is mandatory");
      }
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected String finishImpl(FacesContext context, String outcome)
         throws Exception
   {
       
      WebApplicationContext wac = FacesContextUtils.getRequiredWebApplicationContext(context);
      NodeService unprotectedNodeService = (NodeService)wac.getBean("nodeService");
       
      List<NodeRef> testServers = DeploymentUtil.findAllocatedTestServers(this.store);
      for(NodeRef testServer : testServers)
      {
         unprotectedNodeService.setProperty(testServer, 
                  WCMAppModel.PROP_DEPLOYSERVERALLOCATEDTO, null);
         
         if (logger.isDebugEnabled())
            logger.debug("Released test server '" + testServer + "' from store: " + this.store);
      }
         
      // close dialog and refresh website view
//      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME + 
//                AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "browseWebsite";
      return(outcome);
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
    * Returns the confirmation to display to the user before deleting the reports.
    *
    * @return The message to display
    */
   public String getConfirmMessage()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "release_server_confirm");
   }
}
