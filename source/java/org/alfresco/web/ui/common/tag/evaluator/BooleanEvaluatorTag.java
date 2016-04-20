package org.alfresco.web.ui.common.tag.evaluator;

/**
 * @author kevinr
 */
public class BooleanEvaluatorTag extends GenericEvaluatorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.BooleanEvaluator";
   }
}
