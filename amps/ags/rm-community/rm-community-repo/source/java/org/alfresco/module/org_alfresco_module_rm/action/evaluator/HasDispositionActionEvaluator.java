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
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;



/**
 * Records management hasDispositionAction evaluator that evaluates whether the given node's disposition schedule has the specified disposition action.
 *
 * @author Craig Tan
 * @since 2.1
 */
public class HasDispositionActionEvaluator extends RecordsManagementActionConditionEvaluatorAbstractBase
{
    /**
     * Evaluator constants
     */
    public static final String NAME = "hasDispositionAction";

    public static final String PARAM_DISPOSITION_ACTION_RELATIVE_POSITION = "position";

    public static final String PARAM_DISPOSITION_ACTION = "action";

    private DispositionService dispositionService;

    /**
     * Sets the disposition service
     *
     * @param dispositionService The disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef)
    {
        boolean result = false;
        String position = ((QName) actionCondition.getParameterValue(PARAM_DISPOSITION_ACTION_RELATIVE_POSITION)).getLocalName();
        String action = ((QName) actionCondition.getParameterValue(PARAM_DISPOSITION_ACTION)).getLocalName();


        if (dispositionService.isDisposableItem(actionedUponNodeRef))
        {

            if (position.equals(DispositionActionRelativePositions.ANY.toString()))
            {

                DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(actionedUponNodeRef);
                if (dispositionSchedule != null)
                {
                    for (DispositionActionDefinition dispositionActionDefinition : dispositionSchedule.getDispositionActionDefinitions())
                    {
                        if (dispositionActionDefinition.getName().equals(action))
                        {
                            result = true;
                            break;
                        }
                    }
                }
            }
            else if (position.equals(DispositionActionRelativePositions.NEXT.toString()))
            {
                DispositionAction nextDispositionAction = dispositionService.getNextDispositionAction(actionedUponNodeRef);
                if (nextDispositionAction != null)
                {
                    // Get the disposition actions name
                    String actionName = nextDispositionAction.getName();
                    if (actionName.equals(action))
                    {
                        result = true;
                    }
                }
            }
            else if (position.equals(DispositionActionRelativePositions.PREVIOUS.toString()))
            {
                DispositionAction lastCompletedDispositionAction = dispositionService.getLastCompletedDispostionAction(actionedUponNodeRef);
                if (lastCompletedDispositionAction != null)
                {
                    // Get the disposition actions name
                    String actionName = lastCompletedDispositionAction.getName();
                    if (actionName.equals(action))
                    {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_DISPOSITION_ACTION_RELATIVE_POSITION, DataTypeDefinition.QNAME, true,
                getParamDisplayLabel(PARAM_DISPOSITION_ACTION_RELATIVE_POSITION), false, "rm-ac-disposition-action-relative-positions"));
        paramList.add(new ParameterDefinitionImpl(PARAM_DISPOSITION_ACTION, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_DISPOSITION_ACTION), false,
                "rm-ac-disposition-actions"));

    }
}
