 
package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Transfered indicator
 * 
 * @author Roy Wetherall
 */
public class HasAspectEvaluator extends BaseEvaluator
{       
    private String prefixAspectQNameString;
    
    public void setAspect(String aspect)
    {
        prefixAspectQNameString = aspect;
    }
    
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        QName aspect = QName.createQName(prefixAspectQNameString, namespaceService);
        return nodeService.hasAspect(nodeRef, aspect);        
    }
}
