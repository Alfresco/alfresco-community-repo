/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.workflow.activiti;

import java.util.List;

import org.activiti.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerCatchIntermediateEventJobHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.workflow.activiti.variable.CustomStringVariableType;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class AlfrescoProcessEngineConfiguration extends SpringProcessEngineConfiguration
{
    private List<VariableType> customTypes;
    private NodeService unprotectedNodeService;
    
    public AlfrescoProcessEngineConfiguration()
    {
        // Make sure the synchornizationAdapter is run before the AlfrescoTransactionSupport (and also before the
        // myBatis synchonisation, which unbinds the neccesairy sqlSession used by the JobFailedListener)
        this.transactionSynchronizationAdapterOrder = AlfrescoTransactionSupport.SESSION_SYNCHRONIZATION_ORDER - 100;
    }
    
    @Override
    protected void initVariableTypes()
    {
        super.initVariableTypes();
        // Add custom types before SerializableType
        if (customTypes != null)
        {
            int serializableIndex = variableTypes.getTypeIndex(SerializableType.TYPE_NAME);
            for (VariableType type : customTypes) 
            {
                variableTypes.addType(type, serializableIndex);
            }
        }
        
        // WOR-171: Replace string type by custom one to handle large text-values
        int stringIndex = variableTypes.getTypeIndex("string");
        variableTypes.removeType(variableTypes.getVariableType("string"));
        variableTypes.addType(new CustomStringVariableType(), stringIndex);
    }
    
    @Override
    protected void initJobExecutor() 
    {
        super.initJobExecutor();

        // Wrap timer-job handler to handle authentication
        JobHandler timerJobHandler = jobHandlers.get(TimerExecuteNestedActivityJobHandler.TYPE);
        JobHandler wrappingTimerJobHandler = new AuthenticatedTimerJobHandler(timerJobHandler, unprotectedNodeService);
        jobHandlers.put(TimerExecuteNestedActivityJobHandler.TYPE, wrappingTimerJobHandler);
        
        // Wrap async-job handler to handle authentication
        JobHandler asyncJobHandler = jobHandlers.get(AsyncContinuationJobHandler.TYPE);
        JobHandler wrappingAsyncJobHandler = new AuthenticatedAsyncJobHandler(asyncJobHandler);
        jobHandlers.put(AsyncContinuationJobHandler.TYPE, wrappingAsyncJobHandler);
        
        // Wrap intermediate-timer-job handler to handle authentication
        JobHandler intermediateJobHandler = jobHandlers.get(TimerCatchIntermediateEventJobHandler.TYPE);
        JobHandler wrappingIntermediateJobHandler = new AuthenticatedAsyncJobHandler(intermediateJobHandler);
        jobHandlers.put(TimerCatchIntermediateEventJobHandler.TYPE, wrappingIntermediateJobHandler);
        
    }
    
    public void setCustomTypes(List<VariableType> customTypes)
    {
        this.customTypes = customTypes;
    }
    
    public void setUnprotectedNodeService(NodeService unprotectedNodeService)
    {
        this.unprotectedNodeService = unprotectedNodeService;
    }
}
