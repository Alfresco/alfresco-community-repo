/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.workflow.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.workflow.WorkflowNotificationUtils;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
    private DictionaryService dictionaryService;
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
        dictionaryService = services.getDictionaryService();
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
                    Object eval = AlfrescoJavaScript.executeScript(executionContext, services, expression, null, null);
                    if (eval == null)
                    {
                        throw new WorkflowException("actor expression '" + actorValStr + "' evaluates to null");
                    }

                    String theActor = null;
                    if (eval instanceof String)
                    {
                        theActor = (String)eval;
                    }
                    else if (eval instanceof JBPMNode)
                    {
                        theActor = mapAuthorityToName((JBPMNode)eval, false);
                    }
                    if (theActor == null)
                    {
                        throw new WorkflowException("actor expression must evaluate to a person");
                    }
                    assignedActor = theActor;
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
                    Object eval = AlfrescoJavaScript.executeScript(executionContext, services, expression, null, null);
                    if (eval == null)
                    {
                        throw new WorkflowException("pooledactors expression '" + pooledactorValStr + "' evaluates to null");
                    }
    
                    if (eval instanceof ScriptNode[])
                    {
                        ScriptNode[] nodes = (ScriptNode[])eval;
                        assignedPooledActors = new String[nodes.length];
                        
                        int i = 0;
                        for (ScriptNode node : nodes)
                        {
                            String theActor = mapAuthorityToName(node, true);
                            if (theActor == null)
                            {
                                throw new WorkflowException("pooledactors expression does not evaluate to a collection of authorities");
                            }
                            assignedPooledActors[i++] = theActor;
                        }
                    }
                    if (eval instanceof Collection)
                    {
                        List<String> actors = new ArrayList<String>();
                        Collection<?> nodes = (Collection<?>)eval;
                        for (Object node : nodes)
                        {
                            if (node instanceof ScriptNode)
                            {
                                String theActor = mapAuthorityToName((ScriptNode)node, true);
                                if (theActor == null)
                                {
                                    throw new WorkflowException("pooledactors expression does not evaluate to a collection of authorities");
                                }
                                actors.add(theActor);
                            }
                        }
                        assignedPooledActors = new String[actors.size()];
                        actors.toArray(assignedPooledActors);
                    }
                    else if (eval instanceof ScriptNode)
                    {
                        ScriptNode node = (ScriptNode)eval;
                        String theActor = mapAuthorityToName(node, true);
                        if (theActor == null)
                        {
                            throw new WorkflowException("pooledactors expression does not evaluate to a collection of authorities");
                        }
                        assignedPooledActors = new String[] {theActor};
                    }
                    else if (eval instanceof String)
                    {
                        assignedPooledActors = new String[] {(String)eval};
                    }
                    else
                    {
                        throw new WorkflowException("pooledactors expression does not evaluate to a collection of authorities");
                    }
                }
                else
                {
                    assignedPooledActors = new String[] {pooledactorValStr};
                }
            }
        }
        
        //Determine whether we need to send email notifications of not
        Boolean sendEMailNotification = (Boolean)executionContext.getVariable(WorkflowNotificationUtils.PROP_SEND_EMAIL_NOTIFICATIONS);
               
        //
        // make the assignment
        //
        if (assignedActor != null)
        {
            assignable.setActorId(assignedActor);
            
            if (Boolean.TRUE.equals(sendEMailNotification) == true)
            {
                // Send the notification
                WorkflowNotificationUtils.sendWorkflowAssignedNotificationEMail(
                            services, 
                            JBPMEngine.ENGINE_ID + "$" + executionContext.getTaskInstance().getId(),
                            assignedActor,
                            false);
            }
            
        }
        if (assignedPooledActors != null)
        {
            assignable.setPooledActors(assignedPooledActors);
            
            if (Boolean.TRUE.equals(sendEMailNotification) == true)
            {
                // Send the notification
                WorkflowNotificationUtils.sendWorkflowAssignedNotificationEMail(
                        services, 
                        JBPMEngine.ENGINE_ID + "$" + executionContext.getTaskInstance().getId(),
                        assignedPooledActors,
                        true);
            }
        }         
    }

    
    /**
     * Convert Alfresco authority to actor id
     *  
     * @param authority
     * @return  actor id
     */
    private String mapAuthorityToName(ScriptNode authority, boolean allowGroup)
    {
        String name = null;
        QName type = authority.getQNameType();

        if (dictionaryService.isSubClass(type, ContentModel.TYPE_PERSON))
        {
            name = (String)authority.getProperties().get(ContentModel.PROP_USERNAME);
        }
        else if (allowGroup && dictionaryService.isSubClass(type, ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            name = authorityDAO.getAuthorityName(authority.getNodeRef());
        }
        else if (type.equals(ContentModel.TYPE_AUTHORITY))
        {
            name = authorityDAO.getAuthorityName(authority.getNodeRef());
        }
        return name;
    }
}
