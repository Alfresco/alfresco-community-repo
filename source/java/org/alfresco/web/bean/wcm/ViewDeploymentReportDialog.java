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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean.wcm;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Views the deployment reports created as a result of the last deployment attempt
 * 
 * @author gavinc
 */
public class ViewDeploymentReportDialog extends BaseDialogBean
{
   private static final long serialVersionUID = -8054466371051782132L;
   
   protected String store;
   
   private static final Log logger = LogFactory.getLog(ViewDeploymentReportDialog.class);
   
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

      if (logger.isDebugEnabled())
         logger.debug("Initialising dialog to view deployment report for: " + 
                  this.store);
   }
   
   @Override
   public String getCancelButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "close");
   }
   
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      return outcome;
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @return The store to show deployment reports for
    */
   public String getStore()
   {
      return this.store;
   }
}