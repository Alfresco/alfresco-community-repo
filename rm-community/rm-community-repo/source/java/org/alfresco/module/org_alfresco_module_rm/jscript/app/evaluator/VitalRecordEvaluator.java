 
package org.alfresco.module.org_alfresco_module_rm.jscript.app.evaluator;

import org.alfresco.module.org_alfresco_module_rm.jscript.app.BaseEvaluator;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 */
public class VitalRecordEvaluator extends BaseEvaluator
{
    private VitalRecordService vitalRecordService;
    
    public void setVitalRecordService(VitalRecordService vitalRecordService)
    {
        this.vitalRecordService = vitalRecordService;
    }
    
    @Override
    protected boolean evaluateImpl(NodeRef nodeRef)
    {
        return vitalRecordService.isVitalRecord(nodeRef);
    }
}
