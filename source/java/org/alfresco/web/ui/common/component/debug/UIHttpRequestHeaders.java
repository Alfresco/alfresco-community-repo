package org.alfresco.web.ui.common.component.debug;

import java.util.Map;

import javax.faces.context.FacesContext;

/**
 * Component which displays the headers for the current HTTP request
 * 
 * @author gavinc
 */
public class UIHttpRequestHeaders extends BaseDebugComponent
{
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.debug.HttpRequestHeaders";
   }

   /**
    * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
    */
   public Map getDebugData()
   {
      return FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
   }
}
