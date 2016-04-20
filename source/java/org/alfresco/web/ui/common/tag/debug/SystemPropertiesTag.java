package org.alfresco.web.ui.common.tag.debug;


/**
 * Tag implementation used to place the system properties component
 * on a page.
 * 
 * @author gavinc
 */
public class SystemPropertiesTag extends BaseDebugTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.debug.SystemProperties";
   }
}
