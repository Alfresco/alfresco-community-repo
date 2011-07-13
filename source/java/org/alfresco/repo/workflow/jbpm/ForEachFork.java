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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.dom4j.Element;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.instantiation.FieldInstantiator;
import org.springframework.beans.factory.BeanFactory;


/**
 * For each "item in collection", create a fork.
 */
public class ForEachFork extends JBPMSpringActionHandler
{
    private static final long serialVersionUID = 4643103713602441652L;

    private ServiceRegistry services;
    
    private Element foreach;
    private String var;

    /* (non-Javadoc)
     * @see org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler#initialiseHandler(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    protected void initialiseHandler(BeanFactory factory)
    {
        services = (ServiceRegistry)factory.getBean(ServiceRegistry.SERVICE_REGISTRY);
    }
    
    /**
     * Create a new child token for each item in list.
     * 
     * @param executionContext
     * @throws Exception
     */
    public void execute(final ExecutionContext executionContext)
        throws Exception
    {
        //
        // process action handler arguments
        //
        
        if (foreach == null)
        {
            throw new WorkflowException("forEach has not been provided");
        }

//        Collection<?> forEachColl = buildForEachCollection(executionContext);
        
        if (var == null || var.length() == 0)
        {
            throw new WorkflowException("forEach variable name has not been provided");
        }
        
        //
        // create forked paths
        //
        
        Node node = executionContext.getNode();
        List<ForkedTransition> forkTransitions = new ArrayList<ForkedTransition>();

        Collection<?> forEachColl = buildForEachCollection(executionContext);
        
        // Create a new token and execution context for each node transition and item in list
        List<Transition> nodeTransitions = node.getLeavingTransitions();
        for (Transition noderansition : nodeTransitions)
        {
            int iVar = 0;
            for (Object item: forEachColl)
            {
                // create child token to represent new path
                Token loopToken = buildChildToken(executionContext, noderansition, iVar);
                iVar++;
                
                // assign variable within path
                final ExecutionContext newExecutionContext = new ExecutionContext(loopToken);
                newExecutionContext.getContextInstance().createVariable(var, item, loopToken);
                
                // record path & transition
                ForkedTransition forkTransition = new ForkedTransition();
                forkTransition.executionContext = newExecutionContext;
                forkTransition.transition = noderansition;
                forkTransitions.add(forkTransition);
            }
        }

        //
        // let each new token leave the node.
        //
        for (ForkedTransition forkTransition : forkTransitions)
        {
            node.leave(forkTransition.executionContext, forkTransition.transition);
        }
    }

    private Token buildChildToken(final ExecutionContext executionContext, Transition noderansition,
                int iVar)
    {
        Token rootToken = executionContext.getToken();
        String tokenName = getTokenName(rootToken, noderansition.getName(), iVar); 
        Token loopToken = new Token(rootToken, tokenName);
        loopToken.setTerminationImplicit(true);
        executionContext.getJbpmContext().getSession().save(loopToken);
        return loopToken;
    }

    private Collection<?> buildForEachCollection(final ExecutionContext executionContext)
    {
        // build "for each" collection
        String text = foreach.getTextTrim();
        if (text != null && text.startsWith("#{"))
        {
            return evaluateForEachExpression(executionContext, text);
        }
        return (Collection<?>) FieldInstantiator.getValue(List.class, foreach);
    }

    private Collection<?> evaluateForEachExpression(final ExecutionContext executionContext, String forEachText)
    {
        String expression = forEachText.substring(2, forEachText.length() -1);
        Object result = AlfrescoJavaScript.executeScript(executionContext, services, expression, null, null);
        if (result == null)
        {
            throw new WorkflowException("forEach expression '" + forEachText + "' evaluates to null");
        }
        // expression evaluates to string
        if (result instanceof String)
        {
            return buildStrings((String)result);
        }
        // expression evaluates to Node array
        else if (result instanceof Serializable[])
        {
            return buildJbpmNodes((Serializable[]) result);
        }
        // expression evaluates to collection
        else if (result instanceof Collection<?>)
        {
            return (Collection<?>)result;
        }
        else return null;
    }

    private List<?> buildStrings(String result)
    {
        String[] results = result.trim().split(",");
        return Arrays.asList(results);
    }

    private List<?> buildJbpmNodes(Serializable[] nodes)
    {
        List<JBPMNode> jbpmNodes = new ArrayList<JBPMNode>(nodes.length);
        for (Serializable node : nodes)
        {
            if (node instanceof NodeRef)
            {
                JBPMNode jbpmNode = new JBPMNode((NodeRef)node, services);
                jbpmNodes.add(jbpmNode);
            }
        }
        return jbpmNodes;
    }

    /**
     * Create a token name
     * 
     * @param parent
     * @param transitionName
     * @return
     */
    protected String getTokenName(Token parent, String transitionName, int loopIndex)
    {
        String suffix = "." + loopIndex;
        if (transitionName == null || transitionName.isEmpty())
        {
            // No transition name
            int size = (parent.getChildren() != null) ? parent.getChildren().size() + 1 : 1;
            return buildTokenName("FOREACHFORK", suffix, size);
        }
        return findFirstAvailableTokenName(parent, transitionName, suffix);
    }

    private String findFirstAvailableTokenName(Token parent, String transitionName, String suffix)
    {
        int i = 1;
        while (true)
        {
            String tokenName = buildTokenName(transitionName, suffix, i);
            if(!parent.hasChild(tokenName))
            {
                return tokenName;
            }
            i++;
        }
    }
    
    private String buildTokenName(String prefix, String suffix, int count)
    {
        String countStr = count<2 ? "": Integer.toString(count);
        return prefix + countStr + suffix;
    }

    /**
     * Sets the list of objects to be iterated over.
     * @param foreach the list of objects to set
     */
    public void setForeach(Element foreach)
    {
        this.foreach = foreach;
    }
    
    /**
     * Set the name of the variable to which the eleements of <code>foreach</code> are assigned.
     * @param var the variable name to set
     */
    public void setVar(String var)
    {
        this.var = var;
    }

    /**
     * Fork Transition
     */
    private class ForkedTransition
    {
        private ExecutionContext executionContext;
        private Transition transition;
    }

}
