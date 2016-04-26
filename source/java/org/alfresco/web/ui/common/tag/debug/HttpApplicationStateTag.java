package org.alfresco.web.ui.common.tag.debug;


/**
 * Tag implementation used to place the HTTP session state component
 * on a page.
 * 
 * @author gavinc
 */
public class HttpApplicationStateTag extends BaseDebugTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.debug.HttpApplicationState";
   }
}
