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
     * @param dispositionService
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
