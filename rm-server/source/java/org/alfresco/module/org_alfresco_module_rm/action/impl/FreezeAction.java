/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Freeze Action
 *
 * @author Roy Wetherall
 */
public class FreezeAction extends RMActionExecuterAbstractBase
{
   /** Parameter names */
   public static final String PARAM_REASON = "reason";

   /**
    * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#addParameterDefinitions(java.util.List)
    */
   @Override
   protected void addParameterDefinitions(List<ParameterDefinition> paramList)
   {
       paramList.add(new ParameterDefinitionImpl(PARAM_REASON, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_REASON)));
   }

   /**
    * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
    */
   @SuppressWarnings("deprecation")
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
   {
       // NOTE: we can only freeze records and record folders so ignore everything else
       if (nodeService.exists(actionedUponNodeRef) &&
           !nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_PENDING_DELETE) &&
           (recordService.isRecord(actionedUponNodeRef) ||
           recordFolderService.isRecordFolder(actionedUponNodeRef)) &&
           !freezeService.isFrozen(actionedUponNodeRef))
       {
           freezeService.freeze((String) action.getParameterValue(PARAM_REASON), actionedUponNodeRef);
       }
   }
}