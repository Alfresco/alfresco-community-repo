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

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.bean.content.AddContentDialog;

/**
 * Add/upload content dialog for AVM browse screens.
 * 
 * @author Kevin Roast
 */
public class AddAvmContentDialog extends AddContentDialog
{
   /** The AVMService bean reference */
   protected AVMService avmService;
   
   /** AVM Browse Bean reference */
   protected AVMBrowseBean avmBrowseBean;
   
   
   /**
    * @param avmService The AVMService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   /**
    * @param avmBrowseBean    The AVMBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }
   
   /**
    * Save the specified content using the currently set wizard attributes
    * 
    * @param fileContent      File content to save
    * @param strContent       String content to save
    */
   protected void saveContent(File fileContent, String strContent) throws Exception
   {
      // get the AVM path that will contain the content
      String parent = this.avmBrowseBean.getCurrentPath();
      
      // create the file
      this.avmService.createFile(parent, this.fileName);
      String path = parent + '/' + this.fileName;
      NodeRef fileNodeRef = AVMNodeConverter.ToNodeRef(-1, path);
      
      if (logger.isDebugEnabled())
         logger.debug("Created AVM file: " + path);
      
      // apply the titled aspect - title and description
      Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(2, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, this.title);
      titledProps.put(ContentModel.PROP_DESCRIPTION, this.description);
      this.nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_TITLED, titledProps);
      
      // get a writer for the content and put the file
      ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
      writer.setMimetype(this.mimeType);
      writer.setEncoding("UTF-8");
      if (fileContent != null)
      {
         writer.putContent(fileContent);
      }
      else 
      {
         writer.putContent(strContent == null ? "" : strContent);
      }
      
      // reload the virtualisation server as required
      if (logger.isDebugEnabled())
         logger.debug("Reloading virtualisation server on path: " + path);
      AVMConstants.updateVServerWebapp(path, false);
      
      // remember the created node now
      this.createdNode = fileNodeRef;
   }

   /**
    * @see org.alfresco.web.bean.content.AddContentDialog#doPostCommitProcessing(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      clearUpload();
      
      return outcome;
   }

   /**
    * @see org.alfresco.web.bean.content.AddContentDialog#getDefaultFinishOutcome()
    */
   @Override
   protected String getDefaultFinishOutcome()
   {
      return "cancel";
   }
}
