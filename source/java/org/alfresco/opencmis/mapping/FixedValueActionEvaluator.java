package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Action Evaluator whose evaluation is fixed
 * 
 * @author florian.mueller
 * 
 */
public class FixedValueActionEvaluator extends AbstractActionEvaluator
{
    private boolean allowed;

    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param action Action
     * @param allowed boolean
     */
    protected FixedValueActionEvaluator(ServiceRegistry serviceRegistry, Action action, boolean allowed)
    {
        super(serviceRegistry, action);
        this.allowed = allowed;
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        return allowed;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("FixedValueActionEvaluator[action=").append(getAction());
        builder.append(", allowed=").append(allowed).append("]");
        return builder.toString();
    }
}
