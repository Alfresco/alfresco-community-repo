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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.Node;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.jbpm.graph.exe.ExecutionContext;
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
    private AuthorityDAO authorityDAO;

    private Element actor;
    private Element pooledactors;


    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authorityDAO = (AuthorityDAO)factory.getBean("authorityDAO");
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

                    String actor = null;
                    if (eval instanceof String)
                    {
                        actor = (String)eval;
                    }
                    else if (eval instanceof Node)
                    {
                        actor = mapAuthorityToName((Node)eval, false);
                    }
                    if (actor == null)
                    {
                        throw new WorkflowException("actor expression must evaluate to a person");
                    }
                    assignedActor = actor;
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
                            String actor = mapAuthorityToName(node, true);
                            if (actor == null)
                            {
                                throw new WorkflowException("pooledactors expression does not evaluate to a collection of authorities");
                            }
                            assignedPooledActors[i++] = actor;
                        }
                    }
                    else if (eval instanceof Node)
                    {
                        Node node = (Node)eval;
                        String actor = mapAuthorityToName(node, true);
                        if (actor == null)
                        {
                            throw new WorkflowException("pooledactors expression does not evaluate to a collection of authorities");
                        }
                        assignedPooledActors = new String[] {actor};
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

    
    /**
     * Convert Alfresco authority to actor id
     *  
     * @param authority
     * @return  actor id
     */
    private String mapAuthorityToName(Node authority, boolean allowGroup)
    {
        String name = null;
        QName type = authority.getType();
        if (type.equals(ContentModel.TYPE_PERSON))
        {
            name = (String)authority.getProperties().get(ContentModel.PROP_USERNAME);
        }
        else if (type.equals(ContentModel.TYPE_AUTHORITY))
        {
            name = authorityDAO.getAuthorityName(authority.getNodeRef());
        }
        else if (allowGroup && type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            name = authorityDAO.getAuthorityName(authority.getNodeRef());
        }
        return name;
    }
    
}
