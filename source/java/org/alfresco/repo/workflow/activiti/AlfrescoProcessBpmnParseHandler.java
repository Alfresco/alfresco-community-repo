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
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.parse.BpmnParseHandler;
import org.activiti.bpmn.model.Process;
import org.alfresco.repo.tenant.TenantService;

/**
 * A {@link BpmnParseHandler} that adds a start listener to the process definition 
 * and makes the process definition tenant aware.
 * 
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Nick Smith
 */
public class AlfrescoProcessBpmnParseHandler extends AbstractBpmnParseHandler<Process> 
{

    private ExecutionListener processCreateListener;
    private TenantService     tenantService;
    private boolean           multiTenancyEnabled = true;
    
    protected Class<? extends BaseElement> getHandledType()
    {
        return Process.class;
    }
    
    protected void executeParse(BpmnParse bpmnParse, Process process)
    {
        ProcessDefinitionEntity processDefinition = bpmnParse.getCurrentProcessDefinition();
        processDefinition.addExecutionListener(ExecutionListener.EVENTNAME_START, processCreateListener);
        if (multiTenancyEnabled && tenantService.isEnabled())
        {
            String key = tenantService.getName(processDefinition.getKey());
            processDefinition.setKey(key);
        }
    }
    
    public void setProcessCreateListener(ExecutionListener processCreateListener)
    {
        this.processCreateListener = processCreateListener;
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