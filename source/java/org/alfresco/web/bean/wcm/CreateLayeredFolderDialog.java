/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.bean.wcm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the AVM "Create Layered Folder" dialog.
 * 
 * @author Gavin Cornwell
 */
public class CreateLayeredFolderDialog extends CreateFolderDialog
{
   private static final long serialVersionUID = -2922225296046521490L;

   private static final Log logger = LogFactory.getLog(CreateLayeredFolderDialog.class);
   
   protected String targetStore;
   protected String targetPath;
   protected List<SelectItem> webProjects;
   
   transient protected WebProjectService wpService;
   transient protected AVMService avmService;
   
   /**
    * @param wpService The WebProjectService to set.
    */
   public void setWebProjectService(WebProjectService wpService)
   {
      this.wpService = wpService;
   }
   
   protected WebProjectService getWebProjectService()
   {
      if (wpService == null)
      {
          wpService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getWebProjectService();
      }
      return wpService;
   }
   
   /**
    * @param avmService    The avmService to set.
    */
   public void setAvmService(AVMService avmService)
   {
      this.avmService = avmService;
   }
   
   protected AVMService getAvmService()
   {
      if (avmService == null)
      {
         avmService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAVMService();
      }
      return avmService;
   }
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      
      this.targetStore = null;
      this.targetPath = null;
      this.webProjects = null;
   } 
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      String parent = this.avmBrowseBean.getCurrentPath();
      
      if (this.targetPath.startsWith("/") == false)
      {
         this.targetPath = "/" + this.targetPath;
      }
      
      String layeredPath = AVMUtil.buildSandboxRootPath(this.targetStore) + this.targetPath;
      
      if (logger.isDebugEnabled())
         logger.debug("Creating layered folder named '" + this.name + "' in '" +
                      parent + "' pointing to '" + layeredPath + "'");
      
      // Check the target path exists, display warning if not
      AVMNodeDescriptor nodeDesc = getAvmService().lookup(-1, layeredPath);
      if (nodeDesc != null)
      {
         // create the layered directory
         getAvmService().createLayeredDirectory(layeredPath, parent, this.name);
         
         // add titled aspect and set the title (if supplied) and description
         String newDirPath = parent + "/" + this.name;
         NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, newDirPath);
         getNodeService().addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, null);
         if (this.title != null && this.title.length() != 0)
         {
            this.getAvmService().setNodeProperty(newDirPath, ContentModel.PROP_TITLE, 
                     new PropertyValue(DataTypeDefinition.TEXT, this.title));
         }
         
         String desc = MessageFormat.format(
                  Application.getMessage(FacesContext.getCurrentInstance(), "shared_from"), 
                  layeredPath);
         this.getAvmService().setNodeProperty(newDirPath, ContentModel.PROP_DESCRIPTION, 
                  new PropertyValue(DataTypeDefinition.TEXT, desc));
      }
      else
      {
         String pattern = Application.getMessage(context, "target_does_not_exists");
         Utils.addErrorMessage(MessageFormat.format(pattern, this.targetPath));
      }
      
      return outcome;
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * @return List of UISelectItem objects representing the web projects to select from
    */
   public List<SelectItem> getWebProjects()
   {
      if (this.webProjects == null)
      {
         // get the current web project dns name
         String thisStoreName = this.avmBrowseBean.getWebProject().getStagingStore();
         
         List<WebProjectInfo> wpInfos = getWebProjectService().listWebProjects();
         
         this.webProjects = new ArrayList<SelectItem>(wpInfos.size());
         for (WebProjectInfo wpInfo : wpInfos)
         {
            String name = wpInfo.getName();
            String dns = wpInfo.getStoreId();
           
            // don't add ourself to the list of projects
            if (thisStoreName.equals(dns) == false)
            {
               this.webProjects.add(new SelectItem(dns, name));
            }
         }
         
         // sort the projects by their name
         QuickSort sorter = new QuickSort(this.webProjects, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
         sorter.sort();
      }
      
      return this.webProjects;
   }

   /**
    * @param targetStore The store the layered folder is in
    */
   public void setTargetStore(String targetStore)
   {
      this.targetStore = targetStore;
   }
   
   /**
    * @return The target store the layered folder is in
    */
   public String getTargetStore()
   {
      return this.targetStore;
   }

   /**
    * @return The target path for the layered folder
    */
   public String getTargetPath()
   {
      return this.targetPath;
   }

   /**
    * @param targetPath The target path of the layered folder
    */
   public void setTargetPath(String targetPath)
   {
      this.targetPath = targetPath;
   }
}
