/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.web.forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.xml.sax.SAXException;

/**
 * Serializes the xml instance data collected by a form to a writer.
 *
 * @author Ariel Backenroth
 */
public interface RenderingEngine
   extends Serializable
{
   /////////////////////////////////////////////////////////////////////////////

   public static class RenderingException extends Exception
   {
      private static final long serialVersionUID = 6831222399250770060L;
      
      public RenderingException(final String msg)
      {
         super(msg);
      }
      
      public RenderingException(final Exception cause)
      {
         super(cause);
      }
      
      public RenderingException(final String msg, final Exception cause)
      {
         super(msg, cause);
      }
   }
   
   public static class TemplateNotFoundException extends AlfrescoRuntimeException
   {
      private static final long serialVersionUID = 3232973289475043471L;
      
      public TemplateNotFoundException(final String msg)
      {
         super(msg);
      }
      
      public TemplateNotFoundException(final String msg, final Exception cause)
      {
         super(msg, cause);
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   public interface TemplateProcessorMethod
      extends Serializable
   {
      public Object exec(final Object[] arguments)
         throws Exception;
   }

   /////////////////////////////////////////////////////////////////////////////

   public interface TemplateResourceResolver
      extends Serializable
   {
      public InputStream resolve(final String resourceName)
         throws IOException;
   }

   /////////////////////////////////////////////////////////////////////////////

   public final static QName ROOT_NAMESPACE =
      QName.createQName(null, "root_namespace");

   /**
    * Returns the rendering engines name.
    *
    * @return the name of the rendering engine.
    */
   public String getName();


   /**
    * Returns the default file extension for rendering engine templates for this
    * rendering engine.
    *
    * @return the default file extension for rendering engine templates for this
    * rendering engine.
    */
   public String getDefaultTemplateFileExtension();

   /**
    * Renders the xml data in to a presentation format.
    *
    * @param model The model
    * @param ret the rendering engine template
    * @param out The output stream to write to
    */
   public void render(final Map<QName, Object> model,
                      final RenderingEngineTemplate ret,
                      final OutputStream out)
      throws IOException, RenderingException, SAXException;
}
