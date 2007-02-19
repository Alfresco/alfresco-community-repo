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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Map;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Describes a template that is used for rendering form instance data.
 *
 * @author Ariel Backenroth
 */
public interface RenderingEngineTemplate
   extends Serializable
{
   /** the name of the rendering engine template */
   public String getName();

   /** the title of the rendering engine template */
   public String getTitle();

   /** the description of the rendering engine template */
   public String getDescription();

   /** the output path pattern for renditions */
   public String getOutputPathPattern();
   
   /**
    * Provides the rendering engine to use to process this template.
    *
    * @return the rendering engine to use to process this template.
    */
   public RenderingEngine getRenderingEngine();

   /**
    * Provides an input stream to the rendering engine template.
    * 
    * @return the input stream to the rendering engine template.
    */
   public InputStream getInputStream()
      throws IOException;

   /**
    * Returns the output path for the rendition.
    *
    * @return the output path for the rendition.
    */
   public String getOutputPathForRendition(final FormInstanceData formInstanceData);

   /**
    * Returns the mimetype to use when generating content for this
    * output method.
    *
    * @return the mimetype to use when generating content for this
    * output method, such as text/html, text/xml, application/pdf.
    */
   public String getMimetypeForRendition();

   /**
    * Produces a rendition of the provided formInstanceData.
    *
    * @param formInstanceData the form instance data for which to produce
    * the rendition.
    */
   public Rendition render(final FormInstanceData formInstanceData)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException;

   /**
    * Produces a rendition of the provided formInstanceData to an existing
    * rendition.
    *
    * @param formInstanceData the form instance data for which to produce
    * the rendition.
    * @param rendition the rendition to rerender
    */
   public void render(final FormInstanceData formInstanceData,
                      final Rendition rendition)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException;
}
