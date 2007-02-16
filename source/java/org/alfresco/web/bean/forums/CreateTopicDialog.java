/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.web.bean.forums;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.spaces.CreateSpaceDialog;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation of the "Create Topic Dialog".
 * 
 * @author gavinc
 */
public class CreateTopicDialog extends CreateSpaceDialog
{
   protected String message;
   protected ContentService contentService;
   
   private static final Log logger = LogFactory.getLog(CreateTopicDialog.class);
   
   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.spaceType = ForumModel.TYPE_TOPIC.toString();
      this.message = null;
   }

   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      super.finishImpl(context, outcome);
      
      // do topic specific processing
      
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
      editProps.put(ApplicationModel.PROP_EDITINLINE, true);
      this.nodeService.addAspect(postNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, editProps);
      
      if (logger.isDebugEnabled())
         logger.debug("Added inlineeditable aspect with properties: " + editProps);
      
      // get a writer for the content and put the file
      ContentWriter writer = contentService.getWriter(postNodeRef, ContentModel.PROP_CONTENT, true);
      // set the mimetype and encoding
      writer.setMimetype(Repository.getMimeTypeForFileName(context, fileName));
      writer.setEncoding("UTF-8");
      writer.putContent(Utils.replaceLineBreaks(this.message));
      
      return outcome;
   }

   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      // if the creation was successful we need to simulate a user
      // selecting the topic, the dispatching will take us to the 
      // correct view.
      this.browseBean.clickSpace(this.createdNode);

      return outcome + AlfrescoNavigationHandler.OUTCOME_SEPARATOR + "showTopic";
   }

   @Override
   public String getFinishButtonLabel()
   {
      return Application.getMessage(FacesContext.getCurrentInstance(), "create_topic");
   }
   
   // ------------------------------------------------------------------------------
   // Bean Getters and Setters
   
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
   
   // ------------------------------------------------------------------------------
   // Service Injection
   
   /**
    * @param contentService The contentService to set.
    */
   public void setContentService(ContentService contentService)
   {
      this.contentService = contentService;
   }
}
