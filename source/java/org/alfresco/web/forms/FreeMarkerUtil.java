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
package org.alfresco.web.forms;

import java.io.*;
import java.util.LinkedList;
import javax.xml.XMLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

/**
 * FreeMarker utility functions.
 * 
 * @author Ariel Backenroth
 */
public class FreeMarkerUtil
{
   public static String buildNamespaceDeclaration(final Document xml)
   {
      final Element docEl = xml.getDocumentElement();
      final NamedNodeMap attributes = docEl.getAttributes();
      final StringBuilder result = new StringBuilder();
      for (int i = 0; i < attributes.getLength(); i++)
      {
         final Node a = attributes.item(i);
         if (a.getNodeName().startsWith(XMLConstants.XMLNS_ATTRIBUTE))
         {
            final String prefix = a.getNodeName().substring((XMLConstants.XMLNS_ATTRIBUTE + ":").length());
            final String uri = a.getNodeValue();
            if (result.length() != 0)
            {
               result.append(",\n");
            }
            result.append("\"").append(prefix).append("\":\"").append(uri).append("\"");
         }
      }
      return "<#ftl ns_prefixes={\n" + result.toString() + "}>\n";
   }
}