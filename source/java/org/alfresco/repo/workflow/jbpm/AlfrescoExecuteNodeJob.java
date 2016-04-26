
package org.alfresco.repo.workflow.jbpm;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.ExecuteNodeJob;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * @since 3.4
 * @author Nick Smith
 *
 */
public class AlfrescoExecuteNodeJob extends ExecuteNodeJob
{
    private static final long serialVersionUID = 6257575556379132535L;

    public AlfrescoExecuteNodeJob()
    {
        super();
    }

    public AlfrescoExecuteNodeJob(Token token)
    {
        super(token);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final JbpmContext jbpmContext) throws Exception
    {
        // establish authentication context
        final TaskInstance taskInstance = getTaskInstance();
        String username = getActorId(taskInstance);
        
        // execute timer
        return AuthenticationUtil.runAs(new RunAsWork<Boolean>()
        {
            @Override
            public Boolean doWork() throws Exception
            {
                return AlfrescoExecuteNodeJob.super.execute(jbpmContext);
            }
        }, username);
    }
    
    private String getActorId(TaskInstance taskInstance)
    {
        if (taskInstance != null)
        {
            String actorId = taskInstance.getActorId();
            if (actorId != null && actorId.length() > 0)
            {
                return actorId;
            }
        }
        return AuthenticationUtil.getSystemUserName();
    }
}
