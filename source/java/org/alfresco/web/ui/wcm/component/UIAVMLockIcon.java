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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.repo.component.UILockIcon;

/**
 * @author Ariel Backenroth
 */
public class UIAVMLockIcon extends UILockIcon
{
   public static final String ALFRESCO_FACES_AVMLOCKICON = "org.alfresco.faces.AVMLockIcon";
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return ALFRESCO_FACES_AVMLOCKICON;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      boolean locked = false;
      boolean lockedOwner = false;
      Object val = getValue();
      String lockUser = null;
      
      if (val != null) 
      {
         if (val instanceof AssetInfo)
         {
            // via UIUserSandboxes.renderUserFiles()
            
            AssetInfo asset = (AssetInfo)val;
            
            locked = asset.isLocked();
            
            String assetLockOwner = asset.getLockOwner();
            if (assetLockOwner != null)
            {
               lockUser = assetLockOwner;
               lockedOwner = lockUser.equals(Application.getCurrentUser(context).getUserName());
            }
         }
         else
         {
            // TODO eventually refactor out
            
            // via browse-sandbox.jsp -> AVMBrowseBean (getFolders/getFiles - directory listing or search)
            
            // get the value and see if the image is locked
            final AVMLockingService avmLockingService = Repository.getServiceRegistry(context).getAVMLockingService();
            
            // NodeRef or String
            final String avmPath = (val instanceof NodeRef 
                                    ? AVMNodeConverter.ToAVMVersionPath((NodeRef)val).getSecond() 
                                    : (val instanceof String
                                       ? (String)val
                                       : null));
            if (avmPath != null)
            {
               String[] pathParts = WCMUtil.splitPath(avmPath);
               String lockOwner = null;
               try
               {
                   lockOwner = avmLockingService.getLockOwner(WCMUtil.getWebProjectStoreId(pathParts[0]), pathParts[1]);
               }
               catch (AVMNotFoundException nfe)
               {
                  // ignore
               }
               if (lockOwner != null)
               {
                  locked = true;
                  lockUser = lockOwner;
                  lockedOwner = lockUser.equals(Application.getCurrentUser(context).getUserName());
               }
            }
         }
         
         this.encodeBegin(context,
                          locked,
                          lockedOwner,
                          lockUser == null ? new String[0] : new String[]{lockUser});
      }
   }
}
