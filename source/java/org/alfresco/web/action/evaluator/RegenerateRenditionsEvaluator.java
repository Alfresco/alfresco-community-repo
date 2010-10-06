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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.WebProject;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;

/**
 * UI Action Evaluator for Regenerate Renditions in the Web Forms DataDictionary folder
 * 
 * @author Ariel Backenroth
 */
public class RegenerateRenditionsEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -3479861093052578775L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(final Node node)
   {
      // is the authenticated user permitted to execute the regenerate renditions action
      // against at least one web project
      boolean isUserAllowed = false;

      final FacesContext fc = FacesContext.getCurrentInstance();
      final ServiceRegistry services = Repository.getServiceRegistry(fc);
      final PermissionService permissionService = services.getPermissionService();
      final WebProjectService webProjectService = services.getWebProjectService();
      final NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);

      // before looking for web projects ensure the root folder is present i.e. WCM is enabled!
      if (!webProjectService.hasWebProjectsRoot())
      {
         return false; 
      }
      
      // check that the authenticated user has CONTENT MANAGER permissions for at least one web project
      // this will ensure that the action appears only if the user is able to regenerate renditions
      // for at least one web project
      List<WebProjectInfo> wpInfos = webProjectService.listWebProjects();
      for (WebProjectInfo wpInfo : wpInfos)
      {
         if(permissionService.hasPermission(wpInfo.getNodeRef(), PermissionService.WCM_CONTENT_MANAGER) == AccessStatus.ALLOWED)
         {
            isUserAllowed = true;
            break;
         }
      }

      // TODO improve how we determine whether the form supports the ability to regenerate renditions or not

      // get the path to the current name - compare each path element with the Web Forms folder name
      final Path path = navigator.getCurrentNode().getNodePath();
      
      boolean isWebFormsPath = false;
      Iterator<Path.Element> itr = path.iterator();
      while (itr.hasNext())
      {
         Path.Element element = (Path.Element)itr.next();
         String pathElement = element.getPrefixedString(services.getNamespaceService());
         if (Application.getWebContentFormsFolderName(fc).equals(pathElement))
         {
            isWebFormsPath = true;
            break;
         }
      }

      return (node.hasAspect(WCMAppModel.ASPECT_RENDERING_ENGINE_TEMPLATE) || isWebFormsPath) && isUserAllowed;
   }
}
