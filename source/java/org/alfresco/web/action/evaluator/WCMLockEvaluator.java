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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.WebProject;

/**
 * Evaluator to return if a item is accessable due to a WCM user level lock.
 * 
 * @author Kevin Roast
 */
public class WCMLockEvaluator extends BaseActionEvaluator
{
   /**
    * @return true if the item is not locked by another user
    */
   public boolean evaluate(final Node node)
   {
      boolean result = false;
      final String path = AVMNodeConverter.ToAVMVersionPath(node.getNodeRef()).getSecond();
      if (!AVMUtil.isMainStore(AVMUtil.getStoreName(path)))
      {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final AVMLockingService avmLockService = Repository.getServiceRegistry(fc).getAVMLockingService();
         final AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(fc, AVMBrowseBean.BEAN_NAME);
         
         final String username = Application.getCurrentUser(fc).getUserName();
         
         WebProject webProject = avmBrowseBean.getWebProject();
         if (webProject == null)
         {
            // when in a workflow context, the WebProject may not be accurate on the browsebean
            // so get the web project associated with the path
            webProject = new WebProject(path);
         }
         result = avmLockService.hasAccess(webProject.getNodeRef(), path, username);
      }
      return result;
   }
}
