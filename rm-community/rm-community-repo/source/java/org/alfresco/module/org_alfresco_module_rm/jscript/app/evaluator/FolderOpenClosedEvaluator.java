package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 */
public class FolderOpenClosedEvaluator extends BaseEvaluator
{
    private boolean expected = true;

    public void setExpected(boolean expected)
    {
        this.expected = expected;
    }

    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        return (recordFolderService.isRecordFolderClosed(nodeRef) == expected);
    }
}
