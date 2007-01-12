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
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.Node;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.dom4j.Element;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;
import org.jbpm.taskmgmt.def.AssignmentHandler;
import org.jbpm.taskmgmt.exe.Assignable;
import org.springframework.beans.factory.BeanFactory;


/**
 * Assignment Handler for assigning Alfresco People and Groups to Tasks
 * and Swimlanes
 * 
 * @author davidc
 */
public class AlfrescoAssignment extends JBPMSpringAssignmentHandler
{
    private static final long serialVersionUID = 1025667849552265719L;
    private ServiceRegistry services;

    private Element actor;
    private Element pooledactors;


    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
    }

    
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
                    String expression = actorValStr.substring(2, actorValStr.length() -1);
                    Object eval = AlfrescoJavaScript.executeScript(executionContext, services, expression, null);
                    if (eval == null)
                    {
                        throw new WorkflowException("actor expression '" + actorValStr + "' evaluates to null");
                    }
    
                    if (eval instanceof String)
                    {
                        assignedActor = (String)eval;
                    }
                    else if (eval instanceof Node)
                    {
                        Node node = (Node)eval;
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
                    String expression = pooledactorValStr.substring(2, pooledactorValStr.length() -1);
                    Object eval = AlfrescoJavaScript.executeScript(executionContext, services, expression, null);
                    if (eval == null)
                    {
                        throw new WorkflowException("pooledactors expression '" + pooledactorValStr + "' evaluates to null");
                    }
    
                    if (eval instanceof Node[])
                    {
                        Node[] nodes = (Node[])eval;
                        assignedPooledActors = new String[nodes.length];
                        
                        int i = 0;
                        for (Node node : (Node[])nodes)
                        {
                            if (node.getType().equals(ContentModel.TYPE_PERSON))
                            {
                                assignedPooledActors[i++] = (String)node.getProperties().get(ContentModel.PROP_USERNAME);
                            }
                            else if (node.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
                            {
                                assignedPooledActors[i++] = (String)node.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
                            }
                            else
                            {
                                throw new WorkflowException("pooledactors expression does not evaluate to a collection of authorities");
                            }
                        }
                    }
                    else if (eval instanceof Node)
                    {
                        assignedPooledActors = new String[1];
                        Node node = (Node)eval;
                        if (node.getType().equals(ContentModel.TYPE_PERSON))
                        {
                            assignedPooledActors[0] = (String)node.getProperties().get(ContentModel.PROP_USERNAME);
                        }
                        else if (node.getType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
                        {
                            assignedPooledActors[0] = (String)node.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
                        }
                        else
                        {
                            throw new WorkflowException("pooledactors expression does not evaluate to a collection of authorities");
                        }
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
