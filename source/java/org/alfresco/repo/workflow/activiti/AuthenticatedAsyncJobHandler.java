
package org.alfresco.repo.workflow.activiti;

import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.jobexecutor.JobHandler;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.JobEntity;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowConstants;

/**
 * An {@link JobHandler} which executes activiti jobs authenticated against Alfresco.
 * The job is executed as the process-initiator. If initiator is not available, system user is used.
 * 
 * It wraps another JobHandler to which the actual execution is delegated to.
 *
 * @author Frederik Heremans
 * @since 4.2
 */
public class AuthenticatedAsyncJobHandler implements JobHandler 
{
    private JobHandler wrappedHandler;
    
    public AuthenticatedAsyncJobHandler(JobHandler jobHandler) 
    {
        if (jobHandler == null)
        {
            throw new IllegalArgumentException("JobHandler to delegate to is required");
        }
        this.wrappedHandler = jobHandler;
    }
    
    @Override
    public void execute(final JobEntity job, final String configuration, final ExecutionEntity execution,
                final CommandContext commandContext) 
    {
        // Get initiator
        String userName = AuthenticationUtil.runAsSystem(new RunAsWork<String>() {

			@Override
			public String doWork() throws Exception {
				ActivitiScriptNode ownerNode =  (ActivitiScriptNode) execution.getVariable(WorkflowConstants.PROP_INITIATOR);
				if(ownerNode != null && ownerNode.exists())
		        {
		          return (String) ownerNode.getProperties().get(ContentModel.PROP_USERNAME);            
		        }
				return null;
			}
		});
        
        
        // When no initiator is set, use system user to run job
        if (userName == null)
        {
            userName = AuthenticationUtil.getSystemUserName();
        }
        
        // Execute job
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @SuppressWarnings("synthetic-access")
            public Void doWork() throws Exception
            {
                wrappedHandler.execute(job, configuration, execution, commandContext);
                return null;
            }
        }, userName);
    }

    @Override
    public String getType() 
    {
        return wrappedHandler.getType();
    }
}
