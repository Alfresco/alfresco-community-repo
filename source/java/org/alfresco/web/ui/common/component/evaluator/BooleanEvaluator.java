package org.alfresco.web.ui.common.component.evaluator;

/**
 * @author kevinr
 * 
 * Evaluates to true if the value suppied is a boolean string of "true".
 */
public class BooleanEvaluator extends BaseEvaluator
{
   /**
    * Evaluate against the component attributes. Return true to allow the inner
    * components to render, false to hide them during rendering.
    * 
    * @return true to allow rendering of child components, false otherwise
    */
   public boolean evaluate()
   {
      boolean result = false;
      
      try
      {
         if (getValue() instanceof Boolean)
         {
            result = ((Boolean)getValue()).booleanValue();
         }
         else
         {
            result = Boolean.valueOf((String)getValue()).booleanValue();
         }
      }
      catch (Exception err)
      {
         // return default value on error
         s_logger.debug("Unable to evaluate value to boolean: " + getValue());
      }
      
      return result;
   }
}
