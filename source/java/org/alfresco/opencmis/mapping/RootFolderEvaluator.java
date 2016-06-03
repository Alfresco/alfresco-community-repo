package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;

public class RootFolderEvaluator extends AbstractActionEvaluator
{
    private CMISActionEvaluator folderEvaluator;
    private boolean rootFolderValue;

    protected RootFolderEvaluator(ServiceRegistry serviceRegistry, CMISActionEvaluator folderEvaluator,
            boolean rootFolderValue)
    {
        super(serviceRegistry, folderEvaluator.getAction());
        this.folderEvaluator = folderEvaluator;
        this.rootFolderValue = rootFolderValue;
    }

    public boolean isAllowed(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isRootFolder())
        {
            return rootFolderValue;
        }

        return folderEvaluator.isAllowed(nodeInfo);
    }
}
