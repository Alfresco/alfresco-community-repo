package org.alfresco.web.ui.common.tag.debug;


/**
 * Tag implementation used to place the HTTP request state component
 * on a page.
 * 
 * @author gavinc
 */
public class HttpRequestStateTag extends BaseDebugTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.debug.HttpRequestState";
   }
}
