package org.alfresco.web.ui.repo.tag.evaluator;

import javax.faces.component.UIComponent;

import org.alfresco.web.ui.common.tag.evaluator.GenericEvaluatorTag;

/**
 * @author Kevin Roast
 */
public class PermissionEvaluatorTag extends GenericEvaluatorTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.PermissionEvaluator";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "allow", this.allow);
      setStringProperty(component, "deny", this.deny);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.allow = null;
      this.deny = null;
   }
   
   /**
    * Set the allow permissions
    *
    * @param allow     the allow permissions
    */
   public void setAllow(String allow)
   {
      this.allow = allow;
   }

   /**
    * Set the deny permissions
    *
    * @param deny     the deny permissions
    */
   public void setDeny(String deny)
   {
      this.deny = deny;
   }


   /** the allow permissions */
   private String allow;

   /** the deny permissions */
   private String deny;
}
