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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Encapsulation of form instance data.
 *
 * @author Ariel Backenroth
 */
public interface FormInstanceData
   extends Serializable
{

   /** the form generate this form instance data */
   public Form getForm();

   /** the name of this instance data */
   public String getName();

   /** the path relative to the containing webapp */
   public String getWebappRelativePath();

   /** the path relative to the sandbox */
   public String getSandboxRelativePath();

   /** the path to the contents of this form instance data */
   public String getPath();

   /** the url to the asset */
   public String getUrl();

   /** returns the parsed form instance data */
   public Document getDocument()
      throws IOException, SAXException;

   /** returns all renditions of this form instance data */
   public List<Rendition> getRenditions();
}
