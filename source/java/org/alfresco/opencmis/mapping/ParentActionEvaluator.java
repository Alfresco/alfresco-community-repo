package org.alfresco.opencmis.mapping;

import java.util.List;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;

/**
 * Action Evaluator whose evaluation takes place on parent
 * 
 * @author florian.mueller
 */
public class ParentActionEvaluator extends AbstractActionEvaluator
{
    private AbstractActionEvaluator evaluator;

    /**
     * Construct
     * 
     * @param evaluator AbstractActionEvaluator
     */
    protected ParentActionEvaluator(AbstractActionEvaluator evaluator)
    {
        super(evaluator.getServiceRegistry(), evaluator.getAction());
        this.evaluator = evaluator;
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isRootFolder())
        {
            return false;
        }

        List<CMISNodeInfo> parents = nodeInfo.getParents();
        if (!parents.isEmpty())
        {
            return evaluator.isAllowed(parents.get(0));
        }

        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ParentActionEvaluator[evaluator=").append(evaluator).append("]");
        return builder.toString();
    }
}
