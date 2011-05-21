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

import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler for the "move" action.
 * 
 * @author gavinc
 */
public class MoveHandler extends BaseActionHandler
{
   private static final long serialVersionUID = 2778547560606246126L;

   public String getJSPPath()
   {
      return getJSPPath(MoveActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // add the destination space id to the action properties
      NodeRef destNodeRef = (NodeRef)actionProps.get(PROP_DESTINATION);
      repoProps.put(MoveActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      NodeRef destNodeRef = (NodeRef)repoProps.get(MoveActionExecuter.PARAM_DESTINATION_FOLDER);
      actionProps.put(PROP_DESTINATION, destNodeRef);
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      NodeRef space = (NodeRef)actionProps.get(PROP_DESTINATION);
      String spaceName = Repository.getNameForNode(
            Repository.getServiceRegistry(context).getNodeService(), space);
      
      return MessageFormat.format(Application.getMessage(context, "action_move"),
            new Object[] {spaceName});
   }
   
   /*
    * @see org.alfresco.web.bean.actions.IHandler#isAllowMultiple()
    */
   public boolean isAllowMultiple()
   {
      return false;
   }
}
