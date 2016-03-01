 
package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCompositeCapability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Update capability implementation.
 *
 * @author andyh
 */
public class UpdateCapability extends DeclarativeCompositeCapability
{
    /**
     * Evaluate capability
     *
     * @param nodeRef       node reference
     * @param aspectQName   aspect qname
     * @param properties    property values
     * @return
     */
    public int evaluate(NodeRef nodeRef, QName aspectQName, Map<QName, Serializable> properties)
    {
        return evaluate(nodeRef);
    }
}
