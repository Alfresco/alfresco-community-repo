/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UISelectItems;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.repo.dictionary.constraint.ConstraintRegistry;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.web.data.IDataContainer;
import org.alfresco.web.data.QuickSort;

/**
 * Component that holds a list of content stores configured in the repository.
 */
public class UIStoreSelector extends UISelectOne
{
   public static final String COMPONENT_TYPE = "org.alfresco.faces.StoreSelector";
   public static final String COMPONENT_FAMILY = "javax.faces.SelectOne";
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (getChildren().size() == 0)
      {
         UISelectItems items = (UISelectItems)context.getApplication().
               createComponent("javax.faces.SelectItems");
         items.setId(this.getId() + "_items");
         items.setValue(createList());
         
         // add the child component
         getChildren().add(items);
      }
      
      // do the default processing
      super.encodeBegin(context);
   }

   /**
    * @return List of SelectItem components
    */
   protected List<SelectItem> createList()
   {
      List<SelectItem> items = new ArrayList<SelectItem>(5);
      Constraint storesConstraint = ConstraintRegistry.getInstance().getConstraint("defaultStoreSelector");
      for(String store : ((ListOfValuesConstraint) storesConstraint).getAllowedValues())
      {
          items.add(new SelectItem(store, store));
      }
      
      // make sure the list is sorted by the values
      QuickSort sorter = new QuickSort(items, "label", true, IDataContainer.SORT_CASEINSENSITIVE);
      sorter.sort();
      
      return items;
   }
}
