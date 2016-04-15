/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.repo.rules;

import java.util.Map;

/**
 * The Rule Evaluator evaluates the operation and returns 
 * details of the commands to implement those operations.
 * <p>
 * It is configured with a list of scenarios.
 */
public interface RuleEvaluator
{  
    /**
     * Create a new evaluator context.   Typically for a particular folder.
     * An evaluator context groups operations together.
     * @return the new context.
     */
    public EvaluatorContext createContext(Map<String, Object>sessionContext);
    
    /**
     * Evaluate the scenarios contained within the context against the current operation
     * @param context - the context to evaluate the operation
     * @param operation - the operation to be evaluated.
     * @return Command the command to fulfil the operation
     */
    public Command evaluate(EvaluatorContext context, Operation operation);  
    
    /**
     * Tell the context of a rename
     */
    public void notifyRename(EvaluatorContext context, Operation operation, Command c);
      
    
}
