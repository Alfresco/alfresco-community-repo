package org.alfresco.web.action;

import java.io.Serializable;

import org.alfresco.web.bean.repository.Node;

/**
 * Contract supported by all classes that provide dynamic evaluation for a UI action.
 * <p>
 * Evaluators are supplied with a Node instance context object.
 * <p>
 * The evaluator should decide if the action precondition is valid based on the appropriate
 * logic and the properties etc. of the Node context and return the result.
 * 
 * @author Kevin Roast
 */
public interface ActionEvaluator extends Serializable
{
   /**
    * The evaluator should decide if the action precondition is valid based on the appropriate
    * logic and the properties etc. of the Node context and return the result.
    * 
    * @param node    Node context for the action
    * 
    * @return result of whether the action can proceed.
    */
   public boolean evaluate(Node node);
   
   /**
    * The evaluator should decide if the action precondition is valid based on the appropriate
    * logic and the state etc. of the given object and return the result.
    * 
    * @param obj     The object the action is for
    * 
    * @return result of whether the action can proceed.
    */
   public boolean evaluate(Object obj);
}
