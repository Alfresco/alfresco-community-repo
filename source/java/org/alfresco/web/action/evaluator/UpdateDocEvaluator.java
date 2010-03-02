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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.web.bean.coci.EditOfflineDialog;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

/**
 * UI Action Evaluator - Update document content.
 * 
 * @author Kevin Roast
 */
public class UpdateDocEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = 6030963610213633893L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      DictionaryService dd = Repository.getServiceRegistry(
            FacesContext.getCurrentInstance()).getDictionaryService();

      boolean isOfflineEditing =
          (EditOfflineDialog.OFFLINE_EDITING.equals(node.getProperties().get(ContentModel.PROP_WORKING_COPY_MODE)));
      
      return dd.isSubClass(node.getType(), ContentModel.TYPE_CONTENT) && 
             ((node.isWorkingCopyOwner() && !isOfflineEditing) ||
              (!node.isLocked() && !node.hasAspect(ContentModel.ASPECT_WORKING_COPY)));
   }
}