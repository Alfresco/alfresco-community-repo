package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

/**
 * Component that holds a list of characterset encodings supported by the repository.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class UICharsetSelector extends UISelectOne
{
   public static final String COMPONENT_TYPE = "org.alfresco.faces.CharsetSelector";
   public static final String COMPONENT_FAMILY = "javax.faces.SelectOne";
   
   private static List<SelectItem> charsetEncodings = null;
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      // if the component does not have any children yet create the
      // list of Charsets the user can choose from as a child 
      // SelectItems component.
      if (getChildren().size() == 0)
      {
         UISelectItems items = (UISelectItems)context.getApplication().createComponent("javax.faces.SelectItems");
         items.setId(this.getId() + "_items");
         items.setValue(createList());
         
         // add the child component
         getChildren().add(items);
      }
      
      // do the default processing
      super.encodeBegin(context);
   }

   /**
    * Creates the list of SelectItem components to represent the list
    * of Charsets the user can select from
    * 
    * @return List of SelectItem components
    */
   protected List<SelectItem> createList()
   {
      return getCharsetEncodingList();
   }
   
   /**
    * @return the List of available system character set encodings as a List of SelectItem objects
    */
   public static List<SelectItem> getCharsetEncodingList()
   {
      if (charsetEncodings == null)
      {
         Map<String, Charset> availableCharsets = Charset.availableCharsets();
         List<SelectItem> items = new ArrayList<SelectItem>(availableCharsets.size());
         for (Charset charset : availableCharsets.values())
         {
            SelectItem item = new SelectItem(charset.name(), charset.displayName());
            items.add(item);
         }
         charsetEncodings = items;
      }
      return charsetEncodings;
   }
}
