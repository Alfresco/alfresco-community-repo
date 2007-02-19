/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.forms;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import org.w3c.dom.Document;
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

   public static class RenderingException
      extends Exception
   {

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

   /////////////////////////////////////////////////////////////////////////////

   public interface TemplateProcessorMethod
      extends Serializable
   {
      public Object exec(final Object[] arguments)
         throws Exception;
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
    * @param formInstanceData the xml content to serialize.
    * @param ret the rendering engine template
    * @param form the form that collected the xml content.
    * @param rendition the rendition to serialize to.
    */
   public void render(final Map<QName, Object> model,
                      final RenderingEngineTemplate ret,
                      final OutputStream out)
      throws IOException, RenderingException, SAXException;
}
