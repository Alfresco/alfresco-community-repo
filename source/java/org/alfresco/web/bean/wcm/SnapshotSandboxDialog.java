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

import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Backing bean for the Snaphost Sandbox dialog.
 * 
 * @author Kevin Roast
 */
public class SnapshotSandboxDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(SnapshotSandboxDialog.class);
   
   private static final String MSG_SNAPSHOT_FAILURE = "snapshot_failure";
   private static final String MSG_SNAPSHOT_SUCCESS = "snapshot_success";

   protected AVMService avmService;
   protected AVMBrowseBean avmBrowseBean;
   
   private String label;
   private String description;
   
   
   /**
    * @param avmBrowseBean The avmBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * @param avmService    The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @return Returns the snapshot description.
    */
   public String getDescription()
   {
      return this.description;
   }

   /**
    * @param description   The snapshot description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return Returns the snaphost label.
    */
   public String getLabel()
   {
      return this.label;
   }

   /**
    * @param label   The snapshot label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      this.label = null;
      this.description = null;
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      // find the previous version - to see if a snapshot was acutally performed
      int oldVersion = this.avmService.getLatestSnapshotID(this.avmBrowseBean.getSandbox());
      int version = this.avmService.createSnapshot(
            this.avmBrowseBean.getSandbox(), this.label, this.description)
            .get(this.avmBrowseBean.getSandbox());
      if (version > oldVersion)
      {
         // a new snapshot was created
         String msg = MessageFormat.format(Application.getMessage(
               context, MSG_SNAPSHOT_SUCCESS), this.label, this.avmBrowseBean.getSandbox());
         this.avmBrowseBean.displayStatusMessage(context, msg);
      }
      else
      {
         // no changes had occured - no snapshot was required
         String msg = Application.getMessage(context, MSG_SNAPSHOT_FAILURE);
         this.avmBrowseBean.displayStatusMessage(context, msg);
      }
      
      return outcome;
   }
}
