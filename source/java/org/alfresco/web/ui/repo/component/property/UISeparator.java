package org.alfresco.web.ui.repo.component.property;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.ui.repo.RepoConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to represent a separator within a property sheet
 * 
 * @author gavinc
 */
public class UISeparator extends PropertySheetItem
{
   public static final String COMPONENT_FAMILY = "org.alfresco.faces.Separator";
   
   private static Log logger = LogFactory.getLog(UISeparator.class);

   /**
    * Default constructor
    */
   public UISeparator()
   {
      // set the default renderer
      setRendererType("org.alfresco.faces.SeparatorRenderer");
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return COMPONENT_FAMILY;
   }

   protected String getIncorrectParentMsg()
   {
      return "The separator component must be nested within a property sheet component";
   }

   protected void generateItem(FacesContext context, UIPropertySheet propSheet) throws IOException
   {
      String componentGeneratorName = this.getComponentGenerator(); 
      
      if (componentGeneratorName == null)
      {
         componentGeneratorName = RepoConstants.GENERATOR_SEPARATOR;
      }
      
      UIComponent separator = FacesHelper.getComponentGenerator(context, componentGeneratorName).
            generateAndAdd(context, propSheet, this);
      
      if (logger.isDebugEnabled())
         logger.debug("Created separator " + separator + "(" + 
                      separator.getClientId(context) + 
                      ") for '" + this.getName() +  
                      "' and added it to component " + this);
   }
}
