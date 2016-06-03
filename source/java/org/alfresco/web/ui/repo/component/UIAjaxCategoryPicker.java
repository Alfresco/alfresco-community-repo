package org.alfresco.web.ui.repo.component;

/**
 * JSF Ajax object picker for navigating through and selecting categories.
 * 
 * @author Kevin Roast
 */
public class UIAjaxCategoryPicker extends BaseAjaxItemPicker
{
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.AjaxCategoryPicker";
   }

   @Override
   protected String getServiceCall()
   {
      return "PickerBean.getCategoryNodes";
   }

   @Override
   protected String getDefaultIcon()
   {
      return "/images/icons/category_small.gif";
   }
}
