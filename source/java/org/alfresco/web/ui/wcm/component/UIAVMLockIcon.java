/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLock;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.bean.wcm.WebProject;
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
      
      // get the value and see if the image is locked
      final AVMService avmService = Repository.getServiceRegistry(context).getAVMService();
      final AVMLockingService avmLockingService = Repository.getServiceRegistry(context).getAVMLockingService();

      boolean locked = false;
      boolean lockedOwner = false;
      Object val = getValue();
      List<String> lockUsers = null;
      final String avmPath = (val instanceof NodeRef 
                              ? AVMNodeConverter.ToAVMVersionPath((NodeRef)val).getSecond() 
                              : (val instanceof String
                                 ? (String)val
                                 : null));
      if (avmPath != null)
      {
         if (avmService.lookup(-1, avmPath) != null)
         {
            final WebProject webProject = new WebProject(avmPath);
            final AVMLock lock = avmLockingService.getLock(webProject.getStoreId(), avmPath.substring(avmPath.indexOf("/")));
            if (lock != null)
            {
               locked = true;
               final User currentUser = Application.getCurrentUser(context);
               lockUsers = lock.getOwners();
               lockedOwner = (lockUsers.contains(currentUser.getUserName()));
            }
         }
      }
      this.encodeBegin(context, 
                       locked, 
                       lockedOwner, 
                       lockUsers == null ? new String[0] : (String[])lockUsers.toArray(new String[lockUsers.size()]));
   }
}
