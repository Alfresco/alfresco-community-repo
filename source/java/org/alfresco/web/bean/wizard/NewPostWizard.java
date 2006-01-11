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

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.bean.ForumsBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.springframework.util.StringUtils;

/**
 * Backing bean for posting forum articles.
 * 
 * @author gavinc
 */
public class NewPostWizard extends CreateContentWizard
{
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#init()
    */
   @Override
   public void init()
   {
      super.init();
      
      // set up for creating a post
      this.objectType = ForumModel.TYPE_POST.toString();
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#startWizardForEdit(javax.faces.event.ActionEvent)
    */
   @Override
   public void startWizardForEdit(ActionEvent event)
   {
      // TODO: Allow action link to have multiple action listeners
      //       then we wouldn't need to have this coupling back
      //       to the browse bean in here
      
      // we need to setup the content in the browse bean first
      this.browseBean.setupContentAction(event);
      
      super.startWizardForEdit(event);
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#populate()
    */
   @Override
   public void populate()
   {
      super.populate();
      
      // we need to remove the <br> tags and replace with carriage returns
      // and then setup the content member variable
      Node currentDocument = this.browseBean.getDocument();
      ContentReader reader = this.contentService.getReader(currentDocument.getNodeRef(), 
            ContentModel.PROP_CONTENT);
      
      if (reader != null)
      {
         String htmlContent = reader.getContentString();
         if (htmlContent != null)
         {
            this.content = StringUtils.replace(htmlContent, "<br/>", "\r\n");
         }
      }
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#finish()
    */
   @Override
   public String finish()
   {
      if (this.editMode)
      {
         // remove the line breaks before the save
         this.content = Utils.replaceLineBreaks(this.content);
      }
      else
      {
         // create appropriate values for filename and content type
         this.fileName = ForumsBean.createPostFileName();
         this.contentType = Repository.getMimeTypeForFileName(
                     FacesContext.getCurrentInstance(), this.fileName);
         
         // remove link breaks and replace with <br/>
         this.content = Utils.replaceLineBreaks(this.content);
      }
      
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
      if (this.editMode)
      {
         // update the content
         Node currentDocument = this.browseBean.getDocument();
         
         ContentWriter writer = this.contentService.getWriter(currentDocument.getNodeRef(), 
               ContentModel.PROP_CONTENT, true);
         if (writer != null)
         {
            writer.putContent(this.content);
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
