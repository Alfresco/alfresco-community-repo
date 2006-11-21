/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
            this.avmBrowseBean.getSandbox(), this.label, this.description);
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
