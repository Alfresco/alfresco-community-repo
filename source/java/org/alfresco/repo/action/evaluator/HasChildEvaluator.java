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
package org.alfresco.repo.action.evaluator;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * This {@link ActionConditionEvaluator} determines whether an actionedUponNodeRef has a child {@link NodeRef}
 * with the specified assoc-name or assoc-type.
 * 
 * @author Neil McErlean
 * @since 3.4
 */
public class HasChildEvaluator extends ActionConditionEvaluatorAbstractBase
{
	/**
	 * Evaluator constants
	 */
	public static final String NAME = "has-child";

    public static final String PARAM_ASSOC_TYPE = "assoc-type";
    public static final String PARAM_ASSOC_NAME = "assoc-name";
	
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * Set node service
     * 
     * @param nodeService  the node service
     */
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @see org.alfresco.repo.action.evaluator.ActionConditionEvaluatorAbstractBase#evaluateImpl(org.alfresco.service.cmr.action.ActionCondition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean evaluateImpl(ActionCondition ruleCondition, NodeRef actionedUponNodeRef)
    {
        boolean result = false;
        
        if (this.nodeService.exists(actionedUponNodeRef) == true)
        {
            // Default match pattern is to match all.
            QNamePattern matchAll = RegexQNamePattern.MATCH_ALL;

            // Retrieve any specified parameter values.
            QName assocTypeParam = (QName)ruleCondition.getParameterValue(PARAM_ASSOC_TYPE);
            QName assocNameParam = (QName)ruleCondition.getParameterValue(PARAM_ASSOC_NAME);

            // Use the specified QNames if there are any, else default to match_all.
            QNamePattern assocType = assocTypeParam == null ? matchAll : assocTypeParam;
            QNamePattern assocName = assocNameParam == null ? matchAll : assocNameParam;

            // Are there any children which match these association name/type patterns?
            List<ChildAssociationRef> children = nodeService.getChildAssocs(actionedUponNodeRef, assocType, assocName);
            result = !children.isEmpty();
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
        paramList.add(new ParameterDefinitionImpl(PARAM_ASSOC_TYPE, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_ASSOC_TYPE), false));
        paramList.add(new ParameterDefinitionImpl(PARAM_ASSOC_NAME, DataTypeDefinition.QNAME, false, getParamDisplayLabel(PARAM_ASSOC_NAME), false));
	}
}
