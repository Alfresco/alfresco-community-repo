package org.alfresco.web.ui.repo.component;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Kevin Roast
 */
public class UINodePath extends UICommand
{
   // ------------------------------------------------------------------------------
   // Construction
   
   /**
    * Default constructor
    */
   public UINodePath()
   {
      setRendererType("org.alfresco.faces.NodePathLinkRenderer");
   }
   
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.NodePath";
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes
   
   /**
    * Class representing the clicking of a part of the path element.
    */
   public static class PathElementEvent extends ActionEvent
   {
      public PathElementEvent(UIComponent component, NodeRef nodeRef)
      {
         super(component);
         this.NodeReference = nodeRef;
      }
      
      public NodeRef NodeReference;
   }
}
