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

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML utility functions.
 * 
 * @author Ariel Backenroth
 */
public class XMLUtil
{   

   private static final Log LOGGER = LogFactory.getLog(XMLUtil.class);

   private static DocumentBuilder documentBuilder;

   /** utility function for creating a document */
   public static Document newDocument()
   {
      return XMLUtil.getDocumentBuilder().newDocument();
   }

   /** utility function for serializing a node */
   public static void print(final Node n, final Writer output)
   {
      XMLUtil.print(n, output, true);
   }
   
   /** utility function for serializing a node */
   public static void print(final Node n, final Writer output, final boolean indent)
   {
      try 
      {
         final TransformerFactory tf = TransformerFactory.newInstance();
         final Transformer t = tf.newTransformer();
         t.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
         
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("writing out a document for " + 
      			 (n instanceof Document
      			  ? ((Document)n).getDocumentElement()
      			  : n).getNodeName() + 
   			     " to " + (output instanceof StringWriter
                                       ? "string"
                                       : output));
         }
         t.transform(new DOMSource(n), new StreamResult(output));
      }
      catch (TransformerException te)
      {
         te.printStackTrace();
         assert false : te.getMessage();
      }
   }
   
   /** utility function for serializing a node */
   public static void print(final Node n, final File output)
      throws IOException
   {
      XMLUtil.print(n, new FileWriter(output));
   }
   
   /** utility function for serializing a node */
   public static String toString(final Node n)
   {
      return XMLUtil.toString(n, true);
   }

   /** utility function for serializing a node */
   public static String toString(final Node n, final boolean indent)
   {
      final StringWriter result = new StringWriter();
      XMLUtil.print(n, result, indent);
      return result.toString();
   }
   
   /** utility function for parsing xml */
   public static Document parse(final String source)
      throws SAXException,
      IOException
   {
      return XMLUtil.parse(new ByteArrayInputStream(source.getBytes("UTF-8")));
   }
   
   /** utility function for parsing xml */
   public static Document parse(final NodeRef nodeRef,
                                final ContentService contentService)
      throws SAXException,
      IOException
   {
      final ContentReader contentReader = 
         contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
      final InputStream in = contentReader.getContentInputStream();
      return XMLUtil.parse(in);
   }
   
   /** utility function for parsing xml */
   public static Document parse(final File source)
      throws SAXException,
      IOException
   {
      return XMLUtil.parse(new FileInputStream(source));
   }
   
   /** utility function for parsing xml */
   public static Document parse(final InputStream source)
      throws SAXException,
      IOException
   {
      final DocumentBuilder db = XMLUtil.getDocumentBuilder();
      
      final Document result = db.parse(source);
      source.close();
      return result;
   }

   /** provides a document builder that is namespace aware but not validating by default */
   public static DocumentBuilder getDocumentBuilder()
   {
      if (XMLUtil.documentBuilder == null)
      {
         XMLUtil.documentBuilder = XMLUtil.getDocumentBuilder(true, false);
      }
      return XMLUtil.documentBuilder;
   }

   public static DocumentBuilder getDocumentBuilder(final boolean namespaceAware,
                                                    final boolean validating)
   { 
      try
      {
         final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware(namespaceAware);
         dbf.setValidating(validating);
         return dbf.newDocumentBuilder();
      }
      catch (ParserConfigurationException pce)
      {
         LOGGER.error(pce);
         return null;
      }
   }

   /**
    * Provides a NodeList of multiple nodelists
    */
   public static NodeList combine(final NodeList... nls)
   {

      return new NodeList()
      {
         public Node item(final int index)
         {
            int offset = 0;
            for (int i = 0; i < nls.length; i++)
            {
               if (index - offset < nls[i].getLength())
               {
                  return nls[i].item(index - offset);
               }
               else
               {
                  offset += nls[i].getLength();
               }
            }
            return null;
         }

         public int getLength()
         {
            int result = 0;
            for (int i = 0; i < nls.length; i++)
            {
               result += nls[i].getLength();
            }
            return result;
         }
      };
   }
}
