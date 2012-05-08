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

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * XML utility functions.
 * 
 * @author Ariel Backenroth
 */
public class XMLUtil
{   
   private static final Log LOGGER = LogFactory.getLog(XMLUtil.class);

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
         t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
         t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
         t.setOutputProperty(OutputKeys.METHOD, "xml");
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
   public static Document parse(final String source, final XMLFilter... filters)
      throws SAXException,
      IOException
   {
      return XMLUtil.parse(new CharArrayReader(source.toCharArray()), filters);
   }
   
   public static Document secureParseXSL (final String source, final XMLFilter... filters) 
      throws SAXException,
      IOException   
   {
	   return parse(new CharArrayReader(source.toCharArray()), addSecurityFilter(filters));
   }
   
   /** utility function for parsing xml */
   public static Document parse(final NodeRef nodeRef,
                                final ContentService contentService,
     							final XMLFilter... filters)
      throws SAXException,
      IOException
   {
      final ContentReader contentReader = 
         contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
      final InputStream in = contentReader.getContentInputStream();
      return XMLUtil.parse(in, filters);
   }
   
   public static Document secureParseXSL(final NodeRef nodeRef,
           							     final ContentService contentService,
           							     final XMLFilter... filters)
	  throws SAXException,
	  IOException
	{
      final ContentReader contentReader = 
	  contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
	  final InputStream in = contentReader.getContentInputStream();
      return parse(in, addSecurityFilter(filters));
	}

   /** utility function for parsing xml */
   public static Document parse(final int version, 
                                final String path,
                                final AVMService avmService,
                                final XMLFilter...filters)
      throws SAXException,
      IOException
   {
      return XMLUtil.parse(avmService.getFileInputStream(version, path), filters);
   }
   
   public static Document secureParseXSL(final int version, 
		   							     final String path,
		   							     final AVMService avmService,
		   							     final XMLFilter... filters)
	  throws SAXException,
	  IOException
   {
	   return parse(avmService.getFileInputStream(version, path), addSecurityFilter(filters));
   }
   
   /** utility function for parsing xml */
   public static Document parse(final File source, 
		   						final XMLFilter... filters)
      throws SAXException,
      IOException
   {
      return XMLUtil.parse(new FileInputStream(source), filters);
   }
   
   public static Document secureParseXSL(final File source,
		   								 final XMLFilter... filters)
	   throws SAXException,
	   IOException
   {
	   return parse(new FileInputStream(source), addSecurityFilter(filters));
   }
   
   private static Document parseWithXMLFilters(final InputSource source,
		   									   final XMLFilter... filters)
	   throws SAXException, 
	   IOException
   {
	   return parseWithXMLFilters(source, false, filters);
   }
   
   private static Document parseWithXMLFilters(final InputSource source,
											   final boolean validating,
											   final XMLFilter... filters)
	   throws SAXException, 
	   IOException 
   {
		TransformerFactory tf = TransformerFactory.newInstance();
		// Check to make sure this is a SAX TransformerFactory
		if (!tf.getFeature(SAXTransformerFactory.FEATURE)) 
		{
			throw new SAXException("SAX Transformation factory not found.");
		}
		// Cast to appropriate factory class
		SAXTransformerFactory stf = (SAXTransformerFactory) tf;
		final DocumentBuilder db = XMLUtil.getDocumentBuilder(true, validating);
	
		if (filters == null || filters.length == 0) 
		{
			// No filters. Process this as normal.
			return db.parse(source);
		} 
		else 
		{
			// Process with filters 
			try 
			{
				final Document doc = db.newDocument();
				final TransformerHandler th = stf.newTransformerHandler();
				// Specify transformation to DOMResult with empty Node container (Document)
				th.setResult(new DOMResult(doc));
				XMLReader reader = XMLReaderFactory.createXMLReader();
				
				//emulate what the document builder parser supports
				//all readers are required to support namespaces and namespace-prefixes
				reader.setFeature("http://xml.org/sax/features/namespaces", db.isNamespaceAware());
				reader.setFeature("http://xml.org/sax/features/namespace-prefixes", db.isNamespaceAware() ? true : false);
				
				// Chain multiple filters together
				int i = 0;
				XMLFilter filter = null;
				for (XMLFilter f : filters) 
				{
					// there can be no null in the filter list
					if (f == null)
						throw new SAXException("Nulls are not allowed in XML filter list.");
					// if first item then set new reader
					if (i == 0)
						f.setParent(reader);
					else
						// set parent filter to previous element in the array
						f.setParent(filters[i - 1]);
					
					filter = f;
					i++;
				}
				//not sure how filter could be null
				if (filter != null) 
				{
					filter.setContentHandler(th);
					filter.parse(source);
					try 
					{		
						//try to activate/deactivate validation
						filter.setFeature("http://xml.org/sax/features/validation", db.isValidating());
					} 
					catch (SAXException se) 
					{
						LOGGER.warn("XML reader does not support validation feature.", se);
					}
				} 
				else 
				{
					//not sure how we could get here
					throw new SAXException("No XML filters available to process this request.");
				}
				if (LOGGER.isDebugEnabled()) {
					StringWriter writer = new StringWriter();
					XMLUtil.print(doc, writer);
					LOGGER.debug(writer);
				}
				return doc;
			} 
			catch (TransformerException tce) 
			{
				throw new SAXException(tce);
			}
		}
   }
   
   /** utility function for parsing xml */
   public static Document parse(final InputStream source, final XMLFilter... filters)
      throws SAXException,
      IOException
   {
      try
      {
         return parseWithXMLFilters(new InputSource(source), filters);
      }
      finally
      {
         source.close();
      }
   }
   
   /** secure parse for InputStream source */
   public static Document secureParseXSL(final InputStream source,
		   							     final XMLFilter... filters)
      throws SAXException,
      IOException
   {
	   return parse(source, addSecurityFilter(filters));
   }

   /** utility function for parsing xml */
   public static Document parse(final Reader source, 
		                        final XMLFilter... filters)
      throws SAXException,
      IOException
   {
      try
      {
         return parseWithXMLFilters(new InputSource(source), filters);
      }
      finally
      {
         source.close();
      }
   }
   
   /** secure parse for Reader source **/
   public static Document secureParseXSL(final Reader source,
		   							     final XMLFilter... filters)
	   throws SAXException,
	      IOException
   {
	   return parse(source, addSecurityFilter(filters));
   }
   
   /** provides a document builder that is namespace aware but not validating by default */
   public static DocumentBuilder getDocumentBuilder()
   {
      return XMLUtil.getDocumentBuilder(true, false);
   }

   /**
    * FOR DIAGNOSTIC PURPOSES ONLY - incomplete<br/>
    * Builds a path to the node relative to the to node provided.
    * @param from the node from which to build the xpath
    * @param to an ancestor of <tt>from</tt> which will be the root of the path
    * @return an xpath to <tt>to</tt> rooted at <tt>from</tt>.
    */
   public static String buildXPath(final Node from, final Element to)
   {
      String result = "";
      Node tmp = from;
      do
      {
         if (tmp instanceof Attr)
         {
            assert result.length() == 0;
            result = "@" + tmp.getNodeName();
         }
         else if (tmp instanceof Element)
         {
            Node tmp2 = tmp;
            int position = 1;
            while (tmp2.getPreviousSibling() != null)
            {
               if (tmp2.getNodeName().equals(tmp.getNodeName()))
               {
                  position++;
               }
               tmp2 = tmp2.getPreviousSibling();
            }
            String part = tmp.getNodeName() + "[" + position + "]";
            result = "/" + part + result;
         }
         else if (tmp instanceof Text)
         {
            assert result.length() == 0;
            result = "/text()";
         }
         else
         {
            if (LOGGER.isDebugEnabled())
            {
               throw new IllegalArgumentException("unsupported node type " + tmp);
            }
         }
         tmp = tmp.getParentNode();
      }
      while (tmp != to.getParentNode() && tmp != null);
      return result;
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
   
   /**
    * returns a new array of filters with the security filter at the head of the array
    */
   private static XMLFilter[] addSecurityFilter(XMLFilter...filters) {
	   if (filters == null || filters.length == 0) {
		   return new XMLFilter[] {new FastFailSecureXMLFilter()};
	   } else {
		   XMLFilter[] xmlfilters = new XMLFilter[filters.length + 1];
		   xmlfilters[0] = new FastFailSecureXMLFilter();
		   System.arraycopy(filters, 0, xmlfilters, 1, filters.length);
		   return xmlfilters;
	   }
   }
   
   /**
    * XMLFilter that throws an exception when it comes across any insecure namespaces
    */
   private static class FastFailSecureXMLFilter extends XMLFilterImpl 
   {
	   
	   private static final List<String> insecureURIs = new LinkedList<String>()
	   {
		   private static final long serialVersionUID = 1L;

		   {
			   add("xalan://");
			   add("http://exslt.org/");
			   add("http://xml.apache.org/xalan/PipeDocument");
			   add("http://xml.apache.org/xalan/sql");
			   add("http://xml.apache.org/xalan/redirect");
			   add("http://xml.apache.org/xalan/xsltc/java");
			   add("http://xml.apache.org/xalan/java");
			   add("http://xml.apache.org/xslt");
			   add("http://xml.apache.org/java");
		   }
	   };
	   
	   public FastFailSecureXMLFilter()
	   {
	   };
	   
	   public void startPrefixMapping(String prefix, String uri)
	   throws SAXException
	   {
		   if (isInsecureURI(uri)) 
		   {
			   throw new SAXException("Insecure namespace: " + uri);
		   }
		   super.startPrefixMapping(prefix, uri);
	   }

	   
	   public void startElement (String uri, 
			   					 String localName, 
			   					 String qName,
			   					 final Attributes atts)
	      throws SAXException
	   {
		 
	     if (isInsecureURI(uri)) 
	     {
	    	 throw new SAXException("Insecure namespace: " + uri);
	     }
	     super.startElement(uri, localName, qName, atts);
	   }	
	   
	   private boolean isInsecureURI(String uri) 
	   {
		   for (String insecureURI : insecureURIs) 
		   {
			   if (StringUtils.startsWithIgnoreCase(uri, insecureURI)) return true;
		   }
		   return false;
	   }
	   
   }
}
