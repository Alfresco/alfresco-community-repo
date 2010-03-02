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

import org.alfresco.wcm.webproject.WebProjectService;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * Evaluator to return true if the current user is a content manager for the current website.
 * 
 * @author Gavin Cornwell
 */
public class WCMContentManagerEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 2588681368739253602L;

   /**
    * @return true if the item is not locked by another user
    */
   public boolean evaluate(final Node node)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      WebProjectService webProjectService = Repository.getServiceRegistry(facesContext).getWebProjectService();
      return webProjectService.isContentManager(node.getNodeRef());
   }
}
