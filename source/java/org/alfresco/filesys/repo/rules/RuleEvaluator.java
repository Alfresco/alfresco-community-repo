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
