 
package org.alfresco.module.org_alfresco_module_rm.action.evaluator;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionConditionEvaluatorAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;



/**
 * Records management IsKind evaluator that evaluates according to the file plan
 * component kind passed in.
 *
 * @author Craig Tan
 * @since 2.1
 */
/**
 * @author ctan
 */
public class IsKindEvaluator extends RecordsManagementActionConditionEvaluatorAbstractBase
{
    /**
     * Evaluator constants
     */
    public static final String NAME = "isKind";
    public static final String PARAM_KIND = "kind";

    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition, NodeRef actionedUponNodeRef)
    {
        boolean result = false;
        String kind = ((QName) actionCondition.getParameterValue(PARAM_KIND)).getLocalName();

        FilePlanComponentKind filePlanComponentKind = getFilePlanService().getFilePlanComponentKind(actionedUponNodeRef);

        if (filePlanComponentKind != null &&
            filePlanComponentKind.toString().equals(kind))
        {
                result = true;
        }
        return result;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_KIND, DataTypeDefinition.QNAME, true, getParamDisplayLabel(PARAM_KIND), false, "rm-ac-is-kind-kinds"));
    }

}