/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
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
