package org.alfresco.web.ui.repo.tag.evaluator;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.evaluator.GenericEvaluatorTag;

/**
 * @author Kevin Roast
 */
public class ActionInstanceEvaluatorTag extends GenericEvaluatorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ActionInstanceEvaluator";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "evaluatorClassName", this.evaluatorClassName);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.evaluatorClassName = null;
   }
   
   /**
    * Set the evaluatorClassName
    *
    * @param evaluatorClassName     the evaluatorClassName
    */
   public void setEvaluatorClassName(String evaluatorClassName)
   {
      this.evaluatorClassName = evaluatorClassName;
   }


   /** the evaluatorClassName */
   private String evaluatorClassName;  
}
