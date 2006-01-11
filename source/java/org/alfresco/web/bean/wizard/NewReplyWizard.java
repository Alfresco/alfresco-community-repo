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

import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Backing bean for posting replies to forum articles.
 * 
 * @author gavinc
 */
public class NewReplyWizard extends NewPostWizard
{
   private static Log logger = LogFactory.getLog(NewReplyWizard.class);
   
   private String replyContent = null;
   
   /**
    * Returns the content of the post we are replying to
    * 
    * @return The content
    */
   public String getReplyContent()
   {
      if (this.replyContent == null)
      {
         // get the content reader of the node we are replying to
         NodeRef replyNode = this.browseBean.getDocument().getNodeRef();
         if (replyNode != null)
         {
            ContentReader reader = this.contentService.getReader(replyNode, ContentModel.PROP_CONTENT);
            
            if (reader != null)
            {
               this.replyContent = reader.getContentString();
            }
         }
      }
      
      return this.replyContent;
   }
   
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#init()
    */
   @Override
   public void init()
   {
      super.init();
      
      this.replyContent = null;
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#startWizard(javax.faces.event.ActionEvent)
    */
   @Override
   public void startWizard(ActionEvent event)
   {
      super.startWizard(event);
      
      // also setup the content in the browse bean
      this.browseBean.setupContentAction(event);
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#finish()
    */
   @Override
   public String finish()
   {
      // remove link breaks and replace with <br/>
      this.content = Utils.replaceLineBreaks(this.content);
      
      String outcome = super.finish();
      
      // if we had a successful outcome from the creation close the dialog
      if (outcome != null);
      {
         outcome = AlfrescoNavigationHandler.CLOSE_DIALOG_OUTCOME;
      }
      
      return outcome;
   }

   /**
    * @see org.alfresco.web.bean.wizard.BaseContentWizard#performCustomProcessing()
    */
   @Override
   protected void performCustomProcessing()
   {
      if (this.editMode == false)
      {
         // setup the referencing aspect with the references association
         // between the new post and the one being replied to
         this.nodeService.addAspect(this.createdNode, ContentModel.ASPECT_REFERENCING, null);
         this.nodeService.createAssociation(this.createdNode, this.browseBean.getDocument().getNodeRef(), 
               ContentModel.ASSOC_REFERENCES);
         
         if (logger.isDebugEnabled())
         {
            logger.debug("created new node: " + this.createdNode);
            logger.debug("existing node: " + this.browseBean.getDocument().getNodeRef());
         }
      }
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
