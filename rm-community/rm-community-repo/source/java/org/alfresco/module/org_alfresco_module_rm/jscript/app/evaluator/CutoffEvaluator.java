 
package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Cutoff indicator
 *
 * @author Roy Wetherall
 */
public class CutoffEvaluator extends BaseEvaluator
{
    private boolean isCutoff = true;

    public void setCutoff(boolean isCutoff)
    {
        this.isCutoff = isCutoff;
    }

    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        return (dispositionService.isDisposableItemCutoff(nodeRef) == isCutoff);
    }
}
