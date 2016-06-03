package org.alfresco.web.ui.common.component.debug;

import java.util.Map;

import javax.faces.context.FacesContext;

/**
 * Component which displays the current state of the HTTP session
 * 
 * @author gavinc
 */
public class UIHttpApplicationState extends BaseDebugComponent
{
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.debug.HttpApplicationState";
   }

   /**
    * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
    */
   public Map getDebugData()
   {
      return FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
   }
}
