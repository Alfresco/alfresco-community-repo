/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Action to add types to a record
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class AddRecordTypeAction extends RMActionExecuterAbstractBase
{
    /** Logger */
    private static Log logger = LogFactory.getLog(AddRecordTypeAction.class);

    /** I18N */
    private static final String MSG_ACTIONED_UPON_NOT_RECORD = "rm.action.actioned-upon-not-record";
    private static final String MSG_ACTIONED_UPON_HAS_ASPECT = "rm.action.actioned-upon-has-aspect";

    /** Parameter names */
    public static final String PARAM_ADD_RECORD_TYPES = "recordTypes";

    /** Action name */
    public static final String NAME = "addRecordTypes";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == true &&
            freezeService.isFrozen(actionedUponNodeRef) == false &&
            recordService.isRecord(actionedUponNodeRef) == true &&
            recordService.isDeclared(actionedUponNodeRef) == false)
        {
            String recordTypes = (String) action.getParameterValue(PARAM_ADD_RECORD_TYPES);
            String[] types = recordTypes.split(",");
            for (String type : types)
            {
                QName aspectTypeQName = QName.createQName(type, namespaceService);
                if (nodeService.hasAspect(actionedUponNodeRef, aspectTypeQName) == false)
                {
                    nodeService.addAspect(actionedUponNodeRef, aspectTypeQName, null);
                }
                else
                {
                    logger.info(I18NUtil.getMessage(MSG_ACTIONED_UPON_HAS_ASPECT, actionedUponNodeRef.toString(), aspectTypeQName.toString()));
                }
            }
        }
        else if (logger.isWarnEnabled() == true)
        {
            logger.warn(I18NUtil.getMessage(MSG_ACTIONED_UPON_NOT_RECORD, this.getClass().getSimpleName(), actionedUponNodeRef.toString()));
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_ADD_RECORD_TYPES, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_ADD_RECORD_TYPES)));
    }
}
