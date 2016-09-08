/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * This class applies the aspect specified in the spring bean property customTypeAspect.
 * It is used to apply one of the 4 "custom type" aspects from the DOD 5015 model.
 * 
 * @author Neil McErlean
 */
public class ApplyCustomTypeAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_ACTIONED_UPON_NOT_RECORD = "rm.action.actioned-upon-not-record";    
    private static final String MSG_CUSTOM_ASPECT_NOT_RECOGNISED = "rm.action.custom-aspect-not-recognised";
    
    private static Log logger = LogFactory.getLog(ApplyCustomTypeAction.class);
    private QName customTypeAspect;
    private List<ParameterDefinition> parameterDefinitions;

    public void setCustomTypeAspect(String customTypeAspect)
    {
        this.customTypeAspect = QName.createQName(customTypeAspect, namespaceService);
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Executing action [" + action.getActionDefinitionName() + "] on " + actionedUponNodeRef);
        }
        
        if (recordService.isRecord(actionedUponNodeRef) == true)
        {
            // Apply the appropriate aspect and set the properties.
            Map<QName, Serializable> aspectProps = getPropertyValues(action);
            this.nodeService.addAspect(actionedUponNodeRef, customTypeAspect, aspectProps);
        }
        else if (logger.isWarnEnabled() == true)
        {
            logger.warn(I18NUtil.getMessage(MSG_ACTIONED_UPON_NOT_RECORD, this.getClass().getSimpleName(), actionedUponNodeRef.toString()));
        }
    }

    /**
     * This method extracts the properties from the custom type's aspect.
     * @see #getCustomTypeAspect()
     */
    @Override
    protected final void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        AspectDefinition aspectDef = dictionaryService.getAspect(customTypeAspect);
        for (PropertyDefinition propDef : aspectDef.getProperties().values())
        {
            QName propName = propDef.getName();
            QName propType = propDef.getDataType().getName();
            paramList.add(new ParameterDefinitionImpl(propName.toPrefixString(), propType, propDef.isMandatory(), null));
        }
    }

    /**
     * This method converts a Map of String, Serializable to a Map of QName, Serializable.
     * To do this, it assumes that each parameter name is a String representing a qname
     * of the form prefix:localName.
     */
    private Map<QName, Serializable> getPropertyValues(Action action)
    {
        Map<String, Serializable> paramValues = action.getParameterValues();
        
        Map<QName, Serializable> result = new HashMap<QName, Serializable>(paramValues.size());
        for (String paramName : paramValues.keySet())
        {
            QName propQName = QName.createQName(paramName, this.namespaceService);
            result.put(propQName, paramValues.get(paramName));
        }
    
        return result;
    }

    @Override
    protected synchronized List<ParameterDefinition> getParameterDefintions()
    {
        // We can take these parameter definitions from the properties defined in the dod model.
        if (this.parameterDefinitions == null)
        {
            AspectDefinition aspectDefinition = dictionaryService.getAspect(customTypeAspect);
            if (aspectDefinition == null)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CUSTOM_ASPECT_NOT_RECOGNISED, customTypeAspect));
            }
            
            Map<QName, PropertyDefinition> props = aspectDefinition.getProperties();
            
            this.parameterDefinitions = new ArrayList<ParameterDefinition>(props.size());
            
            for (QName qn : props.keySet())
            {
                String paramName = qn.toPrefixString(namespaceService);
                QName paramType = props.get(qn).getDataType().getName();
                boolean paramIsMandatory = props.get(qn).isMandatory();
                parameterDefinitions.add(new ParameterDefinitionImpl(paramName, paramType, paramIsMandatory, null));
            }
        }
        return parameterDefinitions;
    }
}
