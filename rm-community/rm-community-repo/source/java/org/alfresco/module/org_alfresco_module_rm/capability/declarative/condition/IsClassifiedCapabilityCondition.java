 
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Indicates whether the given node has an associated disposition schedule
 * 
 * @author Craig Tan
 * @since 2.1
 */
public class IsClassifiedCapabilityCondition extends AbstractCapabilityCondition
{
    /** Disposition service */
    private DispositionService dispositionService;

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;        
        
        DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(nodeRef);
        if (dispositionSchedule != null)
        {        
            result = true;
        }
        return result;
    }
}
