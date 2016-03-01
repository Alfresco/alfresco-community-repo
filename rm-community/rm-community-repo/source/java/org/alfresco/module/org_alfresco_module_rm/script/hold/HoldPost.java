 
package org.alfresco.module.org_alfresco_module_rm.script.hold;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Implementation for Java backed webscript to add an item to the given hold(s) in the hold container.
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class HoldPost extends BaseHold
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.script.hold.BaseHold#doAction(java.util.List, java.util.List)
     */
    @Override
    void doAction(List<NodeRef> holds, List<NodeRef> nodeRefs)
    {
        getHoldService().addToHolds(holds, nodeRefs);
    }
}
