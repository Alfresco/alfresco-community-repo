package org.alfresco.web.ui.repo.component;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.URLEncoder;
import org.alfresco.web.ui.common.Utils;

/**
 * JSF Ajax object picker for navigating through folders and selecting a file.
 * 
 * @author Kevin Roast
 */
public class UIAjaxFilePicker extends BaseAjaxItemPicker
{
   /** list of mimetypes to restrict the available file list */
   private String mimetypes = null;
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.AjaxFilePicker";
   }
   
      /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      super.restoreState(context, values[0]);
      this.mimetypes = (String)values[1];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[] {
         super.saveState(context),
         this.mimetypes};
      return values;
   }

   @Override
   protected String getServiceCall()
   {
      return "PickerBean.getFileFolderNodes";
   }

   @Override
   protected String getDefaultIcon()
   {
      // none required - we always return an icon name in the service call
      return null;
   }
   
   @Override
   protected String getRequestAttributes()
   {
      String mimetypes = getMimetypes();
      if (mimetypes != null)
      {
         return "mimetypes=" + URLEncoder.encode(mimetypes);
      }
      else
      {
         return null;
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return Returns the mimetypes to restrict the file list.
    */
   public String getMimetypes()
   {
      ValueBinding vb = getValueBinding("mimetypes");
      if (vb != null)
      {
         this.mimetypes = (String)vb.getValue(getFacesContext());
      }
      
      return this.mimetypes;
   }
   
   /**
    * @param mimetypes The mimetypes restriction list to set.
    */
   public void setMimetypes(String mimetypes)
   {
      this.mimetypes = mimetypes;
   }
}
