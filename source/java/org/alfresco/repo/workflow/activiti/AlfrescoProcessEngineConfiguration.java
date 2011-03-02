package org.alfresco.repo.workflow.activiti;

import java.util.List;

import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.activiti.engine.impl.variable.SerializableType;
import org.activiti.engine.impl.variable.VariableType;
import org.activiti.spring.SpringProcessEngineConfiguration;

public class AlfrescoProcessEngineConfiguration extends SpringProcessEngineConfiguration
{
    private List<VariableType> customTypes;
    
    @Override
    protected void initVariableTypes()
    {
        super.initVariableTypes();
        // Add custom types before SerializableType
        if(customTypes != null)
        {
            int serializableIndex = variableTypes.getTypeIndex(SerializableType.TYPE_NAME);
            for(VariableType type : customTypes) {
                variableTypes.addType(type, serializableIndex);
            }
        }
    }
    
    @Override
    protected void initJobExecutor() {
    	super.initJobExecutor();
    	
    	// Get the existing timer-job handler and wrap
    	// with one that is alfresco-authentication aware
    	JobHandler jobHandler = jobHandlers.get(TimerExecuteNestedActivityJobHandler.TYPE);
    	JobHandler wrappingJobHandler = new AuthenticatedTimerJobHandler(jobHandler);
    	
    	jobHandlers.put(TimerExecuteNestedActivityJobHandler.TYPE, wrappingJobHandler);
    }
    
    public void setCustomTypes(List<VariableType> customTypes)
    {
        this.customTypes = customTypes;
    }
    
}
