/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.workflow.jbpm;

import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.dom4j.Element;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.exe.Assignable;


/**
 * Assignment Handler for assigning Alfresco People and Groups to Tasks
 * and Swimlanes
 * 
 * @author davidc
 */
public class AlfrescoAssignment implements AssignmentHandler
{
    private static final long serialVersionUID = 1025667849552265719L;

    private Element actor;
    private Element pooledactors;

    
    /* (non-Javadoc)
     * @see org.jbpm.taskmgmt.def.AssignmentHandler#assign(org.jbpm.taskmgmt.exe.Assignable, org.jbpm.graph.exe.ExecutionContext)
     */
    public void assign(Assignable assignable, ExecutionContext executionContext) throws Exception
    {
        if (actor == null && pooledactors == null)
        {
            throw new WorkflowException("no actor or pooled actors has been specified");
        }

        //
        // extract actor
        //
        
        String assignedActor = null;
        if (actor != null)
        {
            String actorValStr = actor.getTextTrim();
            if (actorValStr != null && actorValStr.length() > 0)
            {
                if (actorValStr.startsWith("#{"))
                {
                    Object eval = JbpmExpressionEvaluator.evaluate(actorValStr, executionContext);
                    if (eval == null)
                    {
                        throw new WorkflowException("actor expression '" + actorValStr + "' evaluates to null");
                    }
    
                    if (eval instanceof String)
                    {
                        assignedActor = (String)eval;
                    }
                    else if (eval instanceof JBPMNode)
                    {
                        JBPMNode node = (JBPMNode)eval;
                        if (!node.getType().equals(ContentModel.TYPE_PERSON))
                        {
                            throw new WorkflowException("actor expression does not evaluate to a person");
                        }
                        assignedActor = (String)node.getProperties().get(ContentModel.PROP_USERNAME);
                    }
                    else
                    {
                        throw new WorkflowException("actor expression does not evaluate to a person");
                    }
                }
                else
                {
                    assignedActor = actorValStr;
                }
            }
        }

        //
        // extract pooled actors
        //
        
        String[] assignedPooledActors = null;
        if (pooledactors != null)
        {
            String pooledactorValStr = pooledactors.getTextTrim();
            if (pooledactorValStr != null && pooledactorValStr.length() > 0)
            {
                if (pooledactorValStr.startsWith("#{"))
                {
                    Object eval = JbpmExpressionEvaluator.evaluate(pooledactorValStr, executionContext);
                    if (eval == null)
                    {
                        throw new WorkflowException("pooledactors expression '" + pooledactorValStr + "' evaluates to null");
                    }
    
                    if (eval instanceof Collection)
                    {
                        Collection coll = (Collection)eval;
                        assignedPooledActors = new String[coll.size()];
                        
                        int i = 0;
                        for (Object obj : coll)
                        {
                            if (!(obj instanceof JBPMNode))
                            {
                                throw new WorkflowException("pooledactors does not refer to a collection of people");
                            }
                            JBPMNode node = (JBPMNode)obj;
                            if (!node.getType().equals(ContentModel.TYPE_PERSON))
                            {
                                throw new WorkflowException("pooledactors expression does not evaluate to a collection of people");
                            }
                            assignedPooledActors[i++] = (String)node.getProperties().get(ContentModel.PROP_USERNAME);
                        }
                    }
                    else if (eval instanceof JBPMNode)
                    {
                        JBPMNode node = (JBPMNode)eval;
                        if (node.getType().equals(ContentModel.TYPE_PERSON))
                        {
                            assignedPooledActors[0] = (String)node.getProperties().get(ContentModel.PROP_USERNAME);
                        }
                        // TODO: Support Group
                        else
                        {
                            throw new WorkflowException("pooledactors expression does not evaluate to a collection of people");
                        }
                    }
                    else
                    {
                        throw new WorkflowException("pooledactor expression does not evaluate to a group or collection of people");
                    }
                }
                else
                {
                    assignedPooledActors[0] = pooledactorValStr;
                }
            }
        }
        
        //
        // make the assignment
        //
        if (assignedActor != null)
        {
            assignable.setActorId(assignedActor);
        }
        if (assignedPooledActors != null)
        {
            assignable.setPooledActors(assignedPooledActors);
        }
    }

}
