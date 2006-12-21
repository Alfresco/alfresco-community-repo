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

import javax.faces.context.FacesContext;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.repo.domain.PropertyValue;

/**
 * Bean implementation for the AVM "Create Webapp Folder" dialog.
 * 
 * @author Kevin Roast
 */
public class CreateWebappDialog extends CreateFolderDialog
{
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      final String parent = AVMConstants.buildAVMStoreRootPath(this.avmBrowseBean.getStagingStore());
      this.avmService.createDirectory(parent, this.name);
      
      final String path = AVMNodeConverter.ExtendAVMPath(parent, this.name);
      this.avmService.addAspect(path, ApplicationModel.ASPECT_UIFACETS);
      this.avmService.addAspect(path, WCMAppModel.ASPECT_WEBAPP);
      if (this.description != null && this.description.length() != 0)
      {
         this.avmService.setNodeProperty(path, 
                                         ContentModel.PROP_DESCRIPTION, 
                                         new PropertyValue(DataTypeDefinition.TEXT,
                                                           this.description));
      }
      
      return outcome;
   }
}
