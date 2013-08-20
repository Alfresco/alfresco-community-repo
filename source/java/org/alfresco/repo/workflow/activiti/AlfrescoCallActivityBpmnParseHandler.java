/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.repo.workflow.activiti;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.parse.BpmnParseHandler;
import org.alfresco.repo.tenant.TenantService;

/**
 * A {@link BpmnParseHandler} that makes a {@link CallActivity} tenant aware.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Nick Smith
 */
public class AlfrescoCallActivityBpmnParseHandler extends AbstractBpmnParseHandler<CallActivity>
{
    
    private TenantService tenantService;
    private boolean multiTenancyEnabled = true;
    
    protected Class<? extends BaseElement> getHandledType()
    {
        return CallActivity.class;
    }
    
    protected void executeParse(BpmnParse bpmnParse, CallActivity callActivity)
    {
        if (multiTenancyEnabled && tenantService.isEnabled())
        {
            ActivityImpl activity = findActivity(bpmnParse, callActivity.getId());
            ActivityBehavior activityBehavior = activity.getActivityBehavior();
            if(activityBehavior instanceof CallActivityBehavior)
            {
                CallActivityBehavior callActivityBehavior = (CallActivityBehavior) activityBehavior;
                
                // Make name of process-definition to be called aware of the current tenant
                callActivityBehavior.setProcessDefinitonKey(tenantService.getName(callActivityBehavior.getProcessDefinitonKey()));
            }
        }
    }
    
    /**
     * @param tenantService
     *            the tenantService to set
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setMultiTenancyEnabled(boolean multiTenancyEnabled)
    {
        this.multiTenancyEnabled = multiTenancyEnabled;
    }
}