/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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