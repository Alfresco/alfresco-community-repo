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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.component.UIListItem;
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
   protected String topicType;
   protected List<UIListItem> topicIcons;
   protected List<SelectItem> topicTypes;
   
   protected ContentService contentService;
   
   /**
    * Returns a list of topic types for the user to select from
    * 
    * @return The topic types
    */
   public List<SelectItem> getTopicTypes()
   {
      if (this.topicTypes == null)
      {
         this.topicTypes = new ArrayList<SelectItem>(3);
         
         // TODO: change this to be based on categories
         this.topicTypes.add(new SelectItem("1", "Announcement"));
         this.topicTypes.add(new SelectItem("0", "Normal"));
         this.topicTypes.add(new SelectItem("2", "Sticky"));
      }
      
      return this.topicTypes;
   }
   
   /**
    * Returns the type of the topic
    * 
    * @return The type of topic
    */
   public String getTopicType()
   {
      return this.topicType;
   }
   
   /**
    * Sets the type of the topic
    *  
    * @param topicType The type of the topic
    */
   public void setTopicType(String topicType)
   {
      this.topicType = topicType;
   }

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
      this.topicType = "0";
      this.message = null;
   }

   /**
    * @see org.alfresco.web.bean.wizard.NewSpaceWizard#performCustomProcessing(javax.faces.context.FacesContext)
    */
   @Override
   protected void performCustomProcessing(FacesContext context)
   {
      if (this.editMode == false)
      {
         // *************************
         // TODO: Add or update the ForumModel.PROP_TYPE property depending on the editMode
         // *************************
         
         // get the node ref of the node that will contain the content
         NodeRef containerNodeRef = this.createdNode;
         
         // create a unique file name for the message content
         String fileName = GUID.generate() + ".txt";
         
         // create properties for content type
         Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>(5, 1.0f);
         contentProps.put(ContentModel.PROP_NAME, fileName);
         
         // create the node to represent the content
         String assocName = QName.createValidLocalName(fileName);
         ChildAssociationRef assocRef = this.nodeService.createNode(
               containerNodeRef,
               ContentModel.ASSOC_CONTAINS,
               QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, assocName),
               Repository.resolveToQName(ForumModel.TYPE_POST.toString()),
               contentProps);
         
         NodeRef postNodeRef = assocRef.getChildRef();
         
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
         writer.putContent(this.message);
      }
   }
}
