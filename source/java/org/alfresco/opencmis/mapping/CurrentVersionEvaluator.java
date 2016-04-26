package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.enums.Action;

public class CurrentVersionEvaluator extends AbstractActionEvaluator
{
    private CMISActionEvaluator currentVersionEvaluator;
    private boolean currentVersionValue;
    private boolean nonCurrentVersionValue;

    /**
     * Construct
     *
     * @param serviceRegistry ServiceRegistry
     * @param action Action
     * @param currentVersionValue boolean
     * @param nonCurrentVersionValue boolean
     */
    protected CurrentVersionEvaluator(ServiceRegistry serviceRegistry, Action action, boolean currentVersionValue,
            boolean nonCurrentVersionValue)
    {
        super(serviceRegistry, action);
        this.currentVersionValue = currentVersionValue;
        this.nonCurrentVersionValue = nonCurrentVersionValue;
    }

    /**
     * Construct
     * 
     * @param serviceRegistry
     */
    protected CurrentVersionEvaluator(ServiceRegistry serviceRegistry, CMISActionEvaluator currentVersionEvaluator,
            boolean nonCurrentVersionValue)
    {
        super(serviceRegistry, currentVersionEvaluator.getAction());
        this.currentVersionEvaluator = currentVersionEvaluator;
        this.nonCurrentVersionValue = nonCurrentVersionValue;
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        if(nodeInfo.hasPWC())
        {
            if(!nodeInfo.isPWC())
            {
                return nonCurrentVersionValue;
            }
        }
        else
        {
            if (!nodeInfo.isCurrentVersion())
            {
                return nonCurrentVersionValue;
            }
        }
        

        return currentVersionEvaluator == null ? currentVersionValue : currentVersionEvaluator.isAllowed(nodeInfo);
    }
}
