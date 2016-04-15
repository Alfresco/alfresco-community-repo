
package org.alfresco.repo.workflow.jbpm;

import java.util.Date;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.job.ExecuteActionJob;
import org.jbpm.job.ExecuteNodeJob;

/**
 * @since 3.4
 * @author Nick Smith
 * 
 */
public class AlfrescoTaskNode extends TaskNode
{
    private static final long serialVersionUID = -5582345187516764993L;

    public AlfrescoTaskNode()
    {
        super();
    }

    public AlfrescoTaskNode(String name)
    {
        super(name);
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    protected ExecuteNodeJob createAsyncContinuationJob(Token token)
//    {
//        AlfrescoExecuteNodeJob job = new AlfrescoExecuteNodeJob(token);
//        job.setNode(this);
//        job.setDueDate(new Date());
//        job.setExclusive(isAsyncExclusive);
//        return job;
//    }

}
