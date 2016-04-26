package org.alfresco.repo.action.evaluator;

import java.util.List;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**  
 * This class is needed to provide some infrastructure, but the actual evaluation of 
 * Composite Conditions happens inside the ActionServiceImpl as a special case.
 * 
 * @author Jean Barmash
 */
public class CompositeConditionEvaluator extends ActionConditionEvaluatorAbstractBase
{

    private static Log logger = LogFactory.getLog(CompositeConditionEvaluator.class);

    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition,
             NodeRef actionedUponNodeRef) 
    {
       logger.error("Evaluating composite condition.  Should not be called.");
       return false;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
    }
}
