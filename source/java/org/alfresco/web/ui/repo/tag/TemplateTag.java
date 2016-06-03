package org.alfresco.web.ui.repo.tag;

import javax.faces.component.UIComponent;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

/**
 * @author Kevin Roast
 */
public class TemplateTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.Template";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // self rendering component
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      
      setStringProperty(component, "template", this.template);
      setStringProperty(component, "templatePath", this.templatePath);
      setStringBindingProperty(component, "model", this.model);
   }
   
   /**
    * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
    */
   public void release()
   {
      super.release();
      
      this.template = null;
      this.templatePath = null;
      this.model = null;
   }

   /**
    * Set the template name
    *
    * @param template     the template
    */
   public void setTemplate(String template)
   {
      this.template = template;
   }
   
   /**
    * Set the template name based path
    *
    * @param templatePath     the template name based path
    */
   public void setTemplatePath(String templatePath)
   {
      this.templatePath = templatePath;
   }

   /**
    * Set the data model
    *
    * @param model     the model
    */
   public void setModel(String model)
   {
      this.model = model;
   }


   /** the template name based path */
   private String templatePath;

   /** the template */
   private String template;

   /** the data model */
   private String model;
}
