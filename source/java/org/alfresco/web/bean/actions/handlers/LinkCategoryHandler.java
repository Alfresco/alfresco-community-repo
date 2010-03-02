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
package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.LinkCategoryActionExecuter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler implementation for the "link-category" action.
 * 
 * @author gavinc
 */
public class LinkCategoryHandler extends BaseActionHandler
{
   private static final long serialVersionUID = -5956987738674149920L;
   
   protected static final String PROP_CATEGORY = "category";
   
   public String getJSPPath()
   {
      return getJSPPath(LinkCategoryActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // add the classifiable aspect
      repoProps.put(LinkCategoryActionExecuter.PARAM_CATEGORY_ASPECT,
            ContentModel.ASPECT_GEN_CLASSIFIABLE);
      
      // put the selected category in the action params
      NodeRef catNodeRef = (NodeRef)actionProps.get(PROP_CATEGORY);
      repoProps.put(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE, 
            catNodeRef);
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      NodeRef catNodeRef = (NodeRef)repoProps.get(LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE);
      actionProps.put(PROP_CATEGORY, catNodeRef);
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      NodeRef cat = (NodeRef)actionProps.get(PROP_CATEGORY);
      String name = Repository.getNameForNode(
            Repository.getServiceRegistry(context).getNodeService(), cat);
      
      return MessageFormat.format(Application.getMessage(context, "action_link_category"),
            new Object[] {name});
   }
}
