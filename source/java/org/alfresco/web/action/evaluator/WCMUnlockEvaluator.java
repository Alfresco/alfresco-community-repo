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
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.locking.AVMLockingService;
import org.alfresco.util.Pair;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.WebProject;

/**
 * UI Action Evaluator - return true if the node is not part of an in-progress 
 * WCM workflow and is locked
 * 
 * @author Gavin Cornwell
 */
public class WCMUnlockEvaluator extends BaseActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(final Node node)
   {
      boolean proceed = false;
      
      FacesContext context = FacesContext.getCurrentInstance();
      AVMService avmService = Repository.getServiceRegistry(context).getAVMService();
      AVMLockingService avmLockingService = Repository.getServiceRegistry(context).getAVMLockingService();
      AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(context, AVMBrowseBean.BEAN_NAME);
 
      Pair<Integer, String> p = AVMNodeConverter.ToAVMVersionPath(node.getNodeRef());
      String path = p.getSecond();
      
      if (avmService.lookup(p.getFirst(), path) != null)
      {
          WebProject webProject = avmBrowseBean.getWebProject();
          if (webProject == null)
          {
             // when in a workflow context, the WebProject may not be accurate on the browsebean
             // so get the web project associated with the path
             webProject = new WebProject(path);
          }
          
          // determine if the item is locked
          String lockOwner = avmLockingService.getLockOwner(webProject.getStoreId(), AVMUtil.getStoreRelativePath(path));
          proceed = (lockOwner != null);
      }
      
      return proceed;
   }
}
