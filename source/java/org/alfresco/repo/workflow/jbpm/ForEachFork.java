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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.workflow.WorkflowException;
import org.dom4j.Element;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.instantiation.FieldInstantiator;
import org.jbpm.jpdl.el.impl.JbpmExpressionEvaluator;


/**
 * For each "item in collection", create a fork.
 */
public class ForEachFork implements ActionHandler
{
    private static final long serialVersionUID = 4643103713602441652L;
    
    private Element foreach;
    private String var;

    
    /**
     * Create a new child token for each item in list.
     * 
     * @param executionContext
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
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

        // build "for each" collection
        List forEachColl = null;
        String forEachCollStr = foreach.getTextTrim();
        if (forEachCollStr != null)
        {
            if (forEachCollStr.startsWith("#{"))
            {
                Object eval = JbpmExpressionEvaluator.evaluate(forEachCollStr, executionContext);
                if (eval == null)
                {
                    throw new WorkflowException("forEach expression '" + forEachCollStr + "' evaluates to null");
                }
                
                // expression evaluates to string
                if (eval instanceof String)
                {
                    String[] forEachStrs = ((String)eval).trim().split(",");
                    forEachColl = new ArrayList(forEachStrs.length);
                    for (String forEachStr : forEachStrs)
                    {
                        forEachColl.add(forEachStr);
                    }
                }
                
                // expression evaluates to collection
                else if (eval instanceof Collection)
                {
                    forEachColl = (List)eval;
                }
            }
        }
        else
        {
            forEachColl = (List)FieldInstantiator.getValue(List.class, foreach);
        }
        
        if (var == null || var.length() == 0)
        {
            throw new WorkflowException("forEach variable name has not been provided");
        }
        
        //
        // create forked paths
        //
        
        Token rootToken = executionContext.getToken();
        Node node = executionContext.getNode();
        List<ForkedTransition> forkTransitions = new ArrayList<ForkedTransition>();

        // first, create a new token and execution context for each item in list
        for (int i = 0; i < node.getLeavingTransitions().size(); i++)
        {
            Transition transition = (Transition) node.getLeavingTransitions().get(i);

            for (int iVar = 0; iVar < forEachColl.size(); iVar++)
            {
                // create child token to represent new path
                String tokenName = getTokenName(rootToken, transition.getName(), iVar); 
                Token loopToken = new Token(rootToken, tokenName);
                loopToken.setTerminationImplicit(true);
                executionContext.getJbpmContext().getSession().save(loopToken);
            
                // assign variable within path
                final ExecutionContext newExecutionContext = new ExecutionContext(loopToken);
                newExecutionContext.getContextInstance().createVariable(var, forEachColl.get(iVar), loopToken);
                
                // record path & transition
                ForkedTransition forkTransition = new ForkedTransition();
                forkTransition.executionContext = newExecutionContext;
                forkTransition.transition = transition;
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

    /**
     * Create a token name
     * 
     * @param parent
     * @param transitionName
     * @return
     */
    protected String getTokenName(Token parent, String transitionName, int loopIndex)
    {
        String tokenName = null;
        if (transitionName != null)
        {
            if (!parent.hasChild(transitionName))
            {
                tokenName = transitionName;
            }
            else
            {
                int i = 2;
                tokenName = transitionName + Integer.toString(i);
                while (parent.hasChild(tokenName))
                {
                    i++;
                    tokenName = transitionName + Integer.toString(i);
                }
            }
        }
        else
        {
            // no transition name
            int size = ( parent.getChildren()!=null ? parent.getChildren().size()+1 : 1 );
            tokenName = Integer.toString(size);
        }
        return tokenName + "." + loopIndex;
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
