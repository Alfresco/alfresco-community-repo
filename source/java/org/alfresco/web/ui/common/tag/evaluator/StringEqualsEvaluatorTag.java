package org.alfresco.web.ui.common.tag.evaluator;

import javax.faces.component.UIComponent;

/**
 * @author kevinr
 */
public class StringEqualsEvaluatorTag extends GenericEvaluatorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.StringEqualsEvaluator";
   }
   
   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "condition", this.condition);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.condition = null;
   }
   
   /**
    * Set the condition string to test value against
    *
    * @param condition     the condition string to test value against
    */
   public void setCondition(String condition)
   {
      this.condition = condition;
   }


   /** the condition string to test value against */
   private String condition;
}
