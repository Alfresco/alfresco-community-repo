package org.alfresco.web.ui.common.tag;

import javax.faces.component.UIComponent;

/**
 * Tag to place the UIOutputText component on the page
 * 
 * @author gavinc
 */
public class OutputTextTag extends HtmlComponentTag
{
   private String value;
   private String encodeForJavaScript;
   
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.OutputText";
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#getRendererType()
    */
   public String getRendererType()
   {
      // the component is self renderering
      return null;
   }

   /**
    * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
    */
   protected void setProperties(UIComponent component)
   {
      super.setProperties(component);
      setStringProperty(component, "value", this.value);
      setBooleanProperty(component, "encodeForJavaScript", this.encodeForJavaScript);
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      this.value = null;
      this.encodeForJavaScript = null;
   }
   
   /**
    * Set the value
    *
    * @param value  The text
    */
   public void setValue(String value)
   {
      this.value = value;
   }
   
   /**
    * Set the encodeForJavaScript flag
    * 
    * @param encodeForJavaScript true to encode the text for use in JavaScript
    */
   public void setEncodeForJavaScript(String encodeForJavaScript)
   {
      this.encodeForJavaScript = encodeForJavaScript;
   }
}
