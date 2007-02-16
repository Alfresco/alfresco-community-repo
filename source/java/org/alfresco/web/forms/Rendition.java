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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.web.forms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import org.xml.sax.SAXException;

/**
 * Encapsulation of a rendition.
 *
 * @author Ariel Backenroth
 */
public interface Rendition
   extends Serializable
{
   /** the name of this rendition */
   public String getName();

   /** the description of this rendition */
   public String getDescription();

   /** the path relative to the containing webapp */
   public String getWebappRelativePath();

   /** the path relative to the sandbox */
   public String getSandboxRelativePath();

   /** the primary form instance data used to generate this rendition */
   public FormInstanceData getPrimaryFormInstanceData()
      throws FileNotFoundException;

   /** the rendering engine template that generated this rendition */
   public RenderingEngineTemplate getRenderingEngineTemplate();

   /** the path to the contents of this rendition */
   public String getPath();

   /** the url to the asset */
   public String getUrl();

   /** the file type image for the rendition */
   public String getFileTypeImage();

   /** the output stream for the rendition */
   public OutputStream getOutputStream();

   /** regenerates the contents of this rendition using the primary form instance data */
   public void regenerate()
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException;

   /** regenerates the contents of this rendition using the provided form instance data*/
   public void regenerate(final FormInstanceData formInstanceData)
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException;
}
