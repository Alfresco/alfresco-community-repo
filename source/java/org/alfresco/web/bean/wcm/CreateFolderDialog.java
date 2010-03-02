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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.web.bean.dialog.BaseDialogBean;
import org.alfresco.web.bean.repository.Repository;

/**
 * Bean implementation for the AVM "Create Folder" dialog.
 * 
 * @author Kevin Roast
 */
public class CreateFolderDialog extends BaseDialogBean
{
   private static final long serialVersionUID = 5501238017264037644L;

   //private static final Log logger = LogFactory.getLog(CreateFolderDialog.class);
   
   transient private AssetService assetService;
   protected AVMBrowseBean avmBrowseBean;
   
   protected String name;
   protected String title;
   protected String description;
   
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#init(java.util.Map)
    */
   @Override
   public void init(Map<String, String> parameters)
   {
      super.init(parameters);
      this.name = null;
      this.description = null;
   }

   /**
    * @param avmBrowseBean The avmBrowseBean to set.
    */
   public void setAvmBrowseBean(AVMBrowseBean avmBrowseBean)
   {
      this.avmBrowseBean = avmBrowseBean;
   }

   /**
    * @param assetService    The assetService to set.
    */
   public void setAssetService(AssetService assetService)
   {
      this.assetService = assetService;
   }
   
   protected AssetService getAssetService()
   {
      if (assetService == null)
      {
          assetService = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getAssetService();
      }
      return assetService;
   }
   
   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return this.description;
   }

   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }
   
   /**
    * @return Returns the title.
    */
   public String getTitle()
   {
      return this.title;
   }

   /**
    * @param title The title to set.
    */
   public void setTitle(String title)
   {
      this.title = title;
   }

   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      this.name = name;
   }
   
   
   // ------------------------------------------------------------------------------
   // Dialog implementation
   
   /**
    * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
    */
   @Override
   protected String finishImpl(FacesContext context, String outcome) throws Exception
   {
      String parent = this.avmBrowseBean.getCurrentPath();
      
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>(2);
      if (title != null && title.length() != 0)
      {
          properties.put(ContentModel.PROP_TITLE, title);
      }
      if (description != null && description.length() != 0)
      {
          properties.put(ContentModel.PROP_DESCRIPTION, description);
      }
      
      String[] parts = parent.split(":");
      String sbStoreId = parts[0];
      String path = parts[1];
      
      this.getAssetService().createFolder(sbStoreId, path, this.name, properties);
      
      return outcome;
   }
}
