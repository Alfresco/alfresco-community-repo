package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Cutoff indicator
 * 
 * @author Roy Wetherall
 */
public class TrueEvaluator extends BaseEvaluator
{       
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        return true;
    }
}
