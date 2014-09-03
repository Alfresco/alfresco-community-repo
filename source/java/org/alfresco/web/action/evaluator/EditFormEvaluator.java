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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.web.action.evaluator;

import org.alfresco.web.bean.repository.Node;

/**
 * UI Action Evaluator - Edit Form in the Forms DataDictionary folder
 * 
 * @author Ariel Backenroth
 */
public class EditFormEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -509493291648778510L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(final Node node)
   {
//       // WCM
//      final FacesContext fc = FacesContext.getCurrentInstance();
//      final ServiceRegistry services = Repository.getServiceRegistry(fc);
//      final NavigationBean navigator = (NavigationBean)FacesHelper.getManagedBean(fc, NavigationBean.BEAN_NAME);
//      
//      // get the path to the current name - compare last element with the Forms folder name
//      final Path path = navigator.getCurrentNode().getNodePath();
//      final Path.Element element = path.get(path.size() - 1);
//      final String endPath = element.getPrefixedString(services.getNamespaceService());
//      
//      return (Application.getContentFormsFolderName(fc).equals(endPath) && 
//              node.hasAspect(WCMAppModel.ASPECT_FORM));
       return false;
   }
}
