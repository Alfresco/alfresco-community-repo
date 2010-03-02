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
   
   /** the primary form instance data used to generate this rendition (include deleted AVM nodes) */
   public FormInstanceData getPrimaryFormInstanceData(boolean includeDeleted)
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

   /** the HTML label attribute for UI */
   public String getLabelAttribute();
   
   /** the HTML description attribute for UI */
   public String getDescriptionAttribute();

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
