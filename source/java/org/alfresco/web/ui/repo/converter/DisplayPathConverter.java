package org.alfresco.web.ui.repo.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.web.bean.repository.Repository;

/**
 * Converter class to convert a Path or NodeRef reference value (including null) into a human readable form.
 * 
 * @author Kevin Roast
 */
public class DisplayPathConverter implements Converter
{
   /**
    * <p>The standard converter id for this converter.</p>
    */
   public static final String CONVERTER_ID = "org.alfresco.faces.DisplayPathConverter";
   
   /**
    * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
    */
   public Object getAsObject(FacesContext context, UIComponent component, String value)
         throws ConverterException
   {
      return null;
   }

   /**
    * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    */
   public String getAsString(FacesContext context, UIComponent component, Object value)
         throws ConverterException
   {
      String result = "";
      
      if (value != null)
      {
         try
         {
            NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
            Path path = null;
            if (value instanceof NodeRef)
            {
               path = nodeService.getPath((NodeRef)value);
            }
            else if (value instanceof Path)
            {
               path = (Path)value;
            }
            if (path != null)
            {
               result = Repository.getNamePath(nodeService, path, null, "/", null);
            }
         }
         catch (AccessDeniedException accessErr)
         {
            // use default if this occurs
         }
         catch (InvalidNodeRefException nodeErr)
         {
            // use default if this occurs
         }
      }
      
      return result;
   }
}
