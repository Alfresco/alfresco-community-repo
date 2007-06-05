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

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Kevin Roast
 */
public class EditXmlInlineDialog extends BaseDialogBean
{
   private static final Log logger = LogFactory.getLog(EditXmlInlineDialog.class);
   
   /** AVM Edit Bean reference */
   private AVMEditBean avmEditBean;
   
   /** AVM service bean reference */
   protected AVMService avmService;

   /** AVM sync service bean reference */
   protected AVMSyncService avmSyncService;
   
   /** The FilePickerBean reference */
   protected FilePickerBean filePickerBean;

   
   /**
    * @param avmEditBean      The AVMEditBean to set.
    */
   public void setAvmEditBean(AVMEditBean avmEditBean)
   {
      this.avmEditBean = avmEditBean;
   }
   
   /**
    * @param avmService       The AVMService to set.
    */
   public void setAvmService(final AVMService avmService)
   {
      this.avmService = avmService;
   }

   /**
    * @param avmSyncService       The AVMSyncService to set.
    */
   public void setAvmSyncService(final AVMSyncService avmSyncService)
   {
      this.avmSyncService = avmSyncService;
   }
   
   /**
    * @param filePickerBean    The FilePickerBean to set.
    */
   public void setFilePickerBean(final FilePickerBean filePickerBean)
   {
      this.filePickerBean = filePickerBean;
   }

   @Override
   public String getContainerTitle()
   {
      return this.avmEditBean.getAvmNode().getName();
   }

   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      AVMNode avmNode = this.avmEditBean.getAvmNode();
      if (avmNode == null)
      {
         return null;
      }
      
      String avmPath = avmNode.getPath();

      if (logger.isDebugEnabled())
          logger.debug("saving " + avmPath);

      // get an updating writer that we can use to modify the content on the current node
      final ContentWriter writer = this.avmService.getContentWriter(avmPath);
      String editorOutput = this.avmEditBean.getEditorOutput();
      if (this.avmService.hasAspect(-1, avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
      {
         editorOutput = XMLUtil.toString(this.avmEditBean.getInstanceDataDocument(), false);
         this.avmEditBean.setEditorOutput(editorOutput);
      }
      writer.putContent(editorOutput);
      
      return outcome;
   }

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      AVMNode avmNode = this.avmEditBean.getAvmNode();
      String avmPath = avmNode.getPath();
      
      // regenerate form content
      if (this.avmService.hasAspect(-1, avmPath, WCMAppModel.ASPECT_FORM_INSTANCE_DATA))
      {
         this.avmEditBean.regenerateRenditions();
      }
      final NodeRef[] uploadedFiles = this.filePickerBean.getUploadedFiles();

      if (logger.isDebugEnabled())
         logger.debug("updating " + uploadedFiles.length + " uploaded files");
      
      final List<AVMDifference> diffList = new ArrayList<AVMDifference>(uploadedFiles.length);
      for (NodeRef uploadedFile : uploadedFiles)
      {
         final String path = AVMNodeConverter.ToAVMVersionPath(uploadedFile).getSecond();
         diffList.add(new AVMDifference(-1, path,
                                        -1, AVMUtil.getCorrespondingPathInMainStore(path),
                                        AVMDifference.NEWER));
      }
      this.avmSyncService.update(diffList, null, true, true, true, true, null, null);
         
      // Possibly notify virt server
      AVMUtil.updateVServerWebapp(avmNode.getPath(), false);
      
      this.avmEditBean.resetState();
      
      return outcome;
   }

   @Override
   public String cancel()
   {
      // reset the state
      this.avmEditBean.resetState();
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }

   @Override
   public boolean getFinishButtonDisabled()
   {
      return false;
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "save");
   }
}
