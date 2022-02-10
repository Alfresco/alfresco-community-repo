/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action.evaluator;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionConditionEvaluatorAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Records management IsRecordType evaluator that evaluates whether the record is of the specified type.
 *
 * @author Craig Tan
 * @since 2.1
 */
/**
 * @author ctan
 */
public class IsRecordTypeEvaluator extends RecordsManagementActionConditionEvaluatorAbstractBase implements DOD5015Model
{
    /**
     * Evaluator constants
     */
    public static final String NAME = "isRecordType";

    public static final String PARAM_RECORD_TYPE = "type";

    private NodeService nodeService;

    /**
     * Sets the node service
     *
     * @param nodeService The node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef)
    {
        boolean result = false;
        String type = ((QName) actionCondition.getParameterValue(PARAM_RECORD_TYPE)).getLocalName();

        if (type != null)
        {
            result = nodeService.hasAspect(actionedUponNodeRef, QName.createQName(DOD_URI, type));
        }

        return result;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_RECORD_TYPE, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_RECORD_TYPE), false, "rm-ac-record-types"));
    }

}
