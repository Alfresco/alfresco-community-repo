package org.alfresco.web.ui.repo.component.shelf;

import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

/**
 * @author Kevin Roast
 */
public class UIShelfItem extends SelfRenderingComponent
{
   protected final static String SHELF_START = "<table border=\"0\" cellspacing=\"3\" cellpadding=\"0\" width=\"100%\" valign=\"top\">";
   protected final static String SHELF_END   = "</table>";
   
   
   // ------------------------------------------------------------------------------
   // Component Impl 

   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.Shelf";
   }
}
