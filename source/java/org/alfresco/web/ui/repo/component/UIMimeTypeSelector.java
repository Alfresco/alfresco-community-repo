package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;

/**
 * Component that holds a list of MIME types configured in the repository.
 * 
 * @author gavinc
 */
public class UIMimeTypeSelector extends UISelectOne
{
   public static final String COMPONENT_TYPE = "org.alfresco.faces.MimeTypeSelector";
   public static final String COMPONENT_FAMILY = "javax.faces.SelectOne";
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      // if the component does not have any children yet create the
      // list of MIME types the user can choose from as a child 
      // SelectItems component.
      if (getChildren().size() == 0)
      {
         UISelectItems items = (UISelectItems)context.getApplication().
               createComponent("javax.faces.SelectItems");
         items.setValue(createList());
         
         // add the child component
         getChildren().add(items);
      }
      
      // do the default processing
      super.encodeBegin(context);
   }

   /**
    * Creates the list of SelectItem components to represent the list
    * of MIME types the user can select from
    * 
    * @return List of SelectItem components
    */
   protected List<SelectItem> createList()
   {
      List<SelectItem> items = new ArrayList<SelectItem>(80);
      ServiceRegistry registry = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      MimetypeService mimetypeService = registry.getMimetypeService();
      
      // get the mime type display names
      Map<String, String> mimeTypes = mimetypeService.getDisplaysByMimetype();
      for (String mimeType : mimeTypes.keySet())
      {
         items.add(new SelectItem(mimeType, mimeTypes.get(mimeType)));
      }
      
      // make sure the list is sorted by the values
      QuickSort sorter = new QuickSort(items, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      
      return items;
   }
}
