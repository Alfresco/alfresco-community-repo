package org.alfresco.web.ui.repo.tag.shelf;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;

import org.alfresco.web.ui.repo.component.shelf.UIClipboardShelfItem;
import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

/**
 * @author Kevin Roast
 */
public class ClipboardShelfItemTag extends BaseComponentTag
{
   /**
    * @see javax.faces.webapp.UIComponentTag#getComponentType()
    */
   public String getComponentType()
   {
      return "org.alfresco.faces.ClipboardShelfItem";
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
      
      setStringBindingProperty(component, "collections", this.collections);
      if (isValueReference(this.pasteActionListener))
      {
         MethodBinding vb = getFacesContext().getApplication().createMethodBinding(this.pasteActionListener, ACTION_CLASS_ARGS);
         ((UIClipboardShelfItem)component).setPasteActionListener(vb);
      }
      else
      {
         throw new FacesException("Paste Action listener method binding incorrectly specified: " + this.pasteActionListener);
      }
   }
   
   /**
    * @see javax.servlet.jsp.tagext.Tag#release()
    */
   public void release()
   {
      super.release();
      
      this.collections = null;
      this.pasteActionListener = null;
   }
   
   /**
    * Set the clipboard collections to show
    *
    * @param collections     the clipboard collections to show
    */
   public void setCollections(String collections)
   {
      this.collections = collections;
   }
   
   /**
    * Set the pasteActionListener
    *
    * @param pasteActionListener     the pasteActionListener
    */
   public void setPasteActionListener(String pasteActionListener)
   {
      this.pasteActionListener = pasteActionListener;
   }


   /** the pasteActionListener */
   private String pasteActionListener;

   /** the clipboard collections reference */
   private String collections;
}
