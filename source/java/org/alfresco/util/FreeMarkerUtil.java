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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.util;

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