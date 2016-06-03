package org.alfresco.web.ui.common.component.debug;

import java.util.Map;

import javax.faces.context.FacesContext;

/**
 * Component which displays the parameters for the current HTTP request
 * 
 * @author gavinc
 */
public class UIHttpRequestParams extends BaseDebugComponent
{
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.debug.HttpRequestParams";
   }

   /**
    * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
    */
   public Map getDebugData()
   {
      return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
   }
}
