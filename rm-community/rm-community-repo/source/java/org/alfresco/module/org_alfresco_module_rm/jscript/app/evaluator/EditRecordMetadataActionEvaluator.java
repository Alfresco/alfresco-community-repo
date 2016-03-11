package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Additional action evaluator, disabling action if there are no record metadata aspects 
 * available for the node.
 * 
 * @author Roy Wetherall
 */
public class EditRecordMetadataActionEvaluator extends BaseEvaluator
{   
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator#evaluateImpl(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        return !recordService.getRecordMetadataAspects(nodeRef).isEmpty();        
    }
}
