package org.alfresco.web.bean.generator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.common.ComponentConstants;

/**
 * Generates a text field component.
 * 
 * @author gavinc
 */
public class TextAreaGenerator extends TextFieldGenerator
{
   private int rows = 3;
   private int columns = 32;
   
   /**
    * @return Returns the number of columns
    */
   public int getColumns()
   {
      return columns;
   }

   /**
    * @param columns Sets the number of columns
    */
   public void setColumns(int columns)
   {
      this.columns = columns;
   }

   /**
    * @return Returns the number of rows
    */
   public int getRows()
   {
      return rows;
   }

   /**
    * @param rows Sets the number of rows
    */
   public void setRows(int rows)
   {
      this.rows = rows;
   }

   @SuppressWarnings("unchecked")
   public UIComponent generate(FacesContext context, String id)
   {
      UIComponent component = context.getApplication().
            createComponent(ComponentConstants.JAVAX_FACES_INPUT);
      component.setRendererType(ComponentConstants.JAVAX_FACES_TEXTAREA);
      FacesHelper.setupComponentId(context, component, id);

      component.getAttributes().put("rows", this.rows);
      component.getAttributes().put("cols", this.columns);
      
      // MNT-10171 Exception thrown if Share metadata is longer than 1024 characters
      if (ContentModel.PROP_DESCRIPTION.getLocalName().equals(id))
      {
          // add 'onfocus' event for adding 'maxlength' attribute
          component.getAttributes().put("onfocus", "addMaxLengthForDescriptionTextArea(this)");
      }
      
      return component;
   }
}
