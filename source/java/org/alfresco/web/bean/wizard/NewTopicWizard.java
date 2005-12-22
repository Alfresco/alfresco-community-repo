/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.wizard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.bean.ForumsBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wizard bean used for creating and editing topic spaces
 * 
 * @author gavinc
 */
public class NewTopicWizard extends NewSpaceWizard
{
   private static final Log logger = LogFactory.getLog(NewTopicWizard.class);
   
   protected String message;
   
   protected ContentService contentService;

   /**
    * Returns the message entered by the user for the first post
    * 
    * @return The message for the first post
    */
   public String getMessage()
   {
      return this.message;
   }

   /**
    * Sets the message
    * 
    * @param message The message
    */
   public void setMessage(String message)
   {
      this.message = message;
   }
   
   /**
    * @param contentService The contentService to set.
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#init()
    */
   public void init()
   {
      super.init();

      this.spaceType = ForumModel.TYPE_TOPIC.toString();
      this.message = null;
   }

   /**
    * @see org.alfresco.web.bean.wizard.NewSpaceWizard#performCustomProcessing(javax.faces.context.FacesContext)
    */
   @Override
   protected void performCustomProcessing(FacesContext context) throws Exception
   {
      if (this.editMode == false)
      {
         // get the node ref of the node that will contain the content
         NodeRef containerNodeRef = this.createdNode;
         
         // create a unique file name for the message content
         String fileName = ForumsBean.createPostFileName();
         
         FileInfo fileInfo = this.fileFolderService.create(containerNodeRef,
               fileName, ForumModel.TYPE_POST);
         NodeRef postNodeRef = fileInfo.getNodeRef();
         
         if (logger.isDebugEnabled())
            logger.debug("Created post node with filename: " + fileName);
         
         // apply the titled aspect - title and description
         Map<QName, Serializable> titledProps = new HashMap<QName, Serializable>(3, 1.0f);
         titledProps.put(ContentModel.PROP_TITLE, fileName);
         this.nodeService.addAspect(postNodeRef, ContentModel.ASPECT_TITLED, titledProps);
         
         if (logger.isDebugEnabled())
            logger.debug("Added titled aspect with properties: " + titledProps);
         
         Map<QName, Serializable> editProps = new HashMap<QName, Serializable>(1, 1.0f);
         editProps.put(ContentModel.PROP_EDITINLINE, true);
         this.nodeService.addAspect(postNodeRef, ContentModel.ASPECT_INLINEEDITABLE, editProps);
         
         if (logger.isDebugEnabled())
            logger.debug("Added inlineeditable aspect with properties: " + editProps);
         
         // get a writer for the content and put the file
         ContentWriter writer = contentService.getWriter(postNodeRef, ContentModel.PROP_CONTENT, true);
         // set the mimetype and encoding
         writer.setMimetype(Repository.getMimeTypeForFileName(context, fileName));
         writer.setEncoding("UTF-8");
         writer.putContent(Utils.replaceLineBreaks(this.message));
      }
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#finish()
    */
   @Override
   public String finish()
   {
      super.finish();
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#cancel()
    */
   @Override
   public String cancel()
   {
      super.cancel();
      
      return AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
   }
}
