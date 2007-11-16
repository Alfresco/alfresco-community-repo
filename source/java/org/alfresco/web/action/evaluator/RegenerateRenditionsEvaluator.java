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
package org.alfresco.web.action.evaluator;

import java.util.Iterator;

import javax.faces.context.FacesContext;

import org.alfresco.model.WCMAppModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator for Regenerate Renditions in the Web Forms DataDictionary folder
 * 
 * @author Ariel Backenroth
 */
public class RegenerateRenditionsEvaluator extends BaseActionEvaluator
{
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(final Node node)
   {
      final FacesContext fc = FacesContext.getCurrentInstance();
      final ServiceRegistry services = Repository.getServiceRegistry(fc);
      final NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
      
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

      return (node.hasAspect(WCMAppModel.ASPECT_RENDERING_ENGINE_TEMPLATE) || isWebFormsPath);
   }
}
