package org.alfresco.web.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper bean used to temporarily store the last item added in a multi
 * value editor component.
 * 
 * A Map is used so that multiple components on the same page can use the
 * same backing bean.
 * 
 * @author gavinc
 */
public class MultiValueEditorBean implements Serializable
{
   private static final long serialVersionUID = -5180578793877515158L;
   
   private Map<String, Object> lastItemsAdded = new HashMap<String, Object>(10);
   
   public Map<String, Object> getLastItemsAdded()
   {
      return lastItemsAdded;
   }
}
