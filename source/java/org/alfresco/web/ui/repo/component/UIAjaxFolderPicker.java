package org.alfresco.web.ui.repo.component;

/**
 * JSF Ajax object picker for navigating through and selecting folders.
 * 
 * @author Kevin Roast
 */
public class UIAjaxFolderPicker extends BaseAjaxItemPicker
{
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.AjaxFolderPicker";
   }

   @Override
   protected String getServiceCall()
   {
      return "PickerBean.getFolderNodes";
   }

   @Override
   protected String getDefaultIcon()
   {
      // none required - we always return an icon name in the service call
      return null;
   }
}
