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
package org.alfresco.web.bean.search;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Holds the context required to build a search query and can return the populated query.
 * <p>
 * Builds a lucene format search string from each of the supplied attributes and terms.
 * Can be serialized to and from XML format for saving and restoring of previous searches.
 * 
 * @author Kevin Roast
 */
public class SearchContext implements Serializable
{
   private static final long serialVersionUID = 6730844584074229969L;
   
   /** XML serialization elements */
   private static final String ELEMENT_VALUE = "value";
   private static final String ELEMENT_FIXED_VALUES = "fixed-values";
   private static final String ELEMENT_INCLUSIVE = "inclusive";
   private static final String ELEMENT_UPPER = "upper";
   private static final String ELEMENT_LOWER = "lower";
   private static final String ELEMENT_RANGE = "range";
   private static final String ELEMENT_RANGES = "ranges";
   private static final String ELEMENT_NAME = "name";
   private static final String ELEMENT_ATTRIBUTE = "attribute";
   private static final String ELEMENT_ATTRIBUTES = "attributes";
   private static final String ELEMENT_MIMETYPE = "mimetype";
   private static final String ELEMENT_CONTENT_TYPE = "content-type";
   private static final String ELEMENT_FOLDER_TYPE = "folder-type";
   private static final String ELEMENT_CATEGORY = "category";
   private static final String ELEMENT_CATEGORIES = "categories";
   private static final String ELEMENT_LOCATION = "location";
   private static final String ELEMENT_MODE = "mode";
   private static final String ELEMENT_TEXT = "text";
   private static final String ELEMENT_SEARCH = "search";
   private static final String ELEMENT_QUERY = "query";
   
   /** advanced search term operators */
   private static final char OP_WILDCARD = '*';
   private static final char OP_AND = '+';
   private static final char OP_NOT = '-';
   private static final String STR_OP_WILDCARD = "" + OP_WILDCARD;
   
   /** Search mode constants */
   public final static int SEARCH_ALL = 0;
   public final static int SEARCH_FILE_NAMES_CONTENTS = 1;
   public final static int SEARCH_FILE_NAMES = 2;
   public final static int SEARCH_SPACE_NAMES = 3;
   
   /** the search text string */
   private String text = "";
   
   /** mode for the search */
   private int mode = SearchContext.SEARCH_ALL;
   
   /** folder XPath location for the search */
   private String location = null;
   
   /** categories to add to the search */
   private String[] categories = new String[0];
   
   /** folder type to restrict search against */
   private String folderType = null;
   
   /** content type to restrict search against */
   private String contentType = null;
   
   /** content mimetype to restrict search against */
   private String mimeType = null;
   
   /** any extra simple query attributes to add to the search */
   protected List<QName> simpleSearchAdditionalAttrs = new ArrayList<QName>(4);
   
   /** any extra query attributes to add to the search */
   private Map<QName, String> queryAttributes = new HashMap<QName, String>(5, 1.0f);
   
   /** any additional range attribute to add to the search */
   private Map<QName, RangeProperties> rangeAttributes = new HashMap<QName, RangeProperties>(5, 1.0f);
   
   /** any additional fixed value attributes to add to the search, such as boolean or noderef */
   private Map<QName, String> queryFixedValues = new HashMap<QName, String>(5, 1.0f);
   
   /** set true to force the use of AND between text terms */
   private boolean forceAndTerms = false;
   
   /** logger */
   private static Log logger = LogFactory.getLog(SearchContext.class);
   
   
   /**
    * Build the search query string based on the current search context members.
    * 
    * @param minimum       small possible textual string used for a match
    *                      this does not effect fixed values searches (e.g. boolean, int values) or date ranges
    * 
    * @return prepared search query string
    */
   public String buildQuery(int minimum)
   {
      String query;
      boolean validQuery = false;
      
      // the QName for the well known "name" attribute
      String nameAttr = Repository.escapeQName(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, ELEMENT_NAME));
      
      // match against content text
      String text = this.text.trim();
      
      StringBuilder fullTextBuf = new StringBuilder(64);
      StringBuilder nameAttrBuf = new StringBuilder(128);
      StringBuilder additionalAttrsBuf = new StringBuilder(128);
      
      if (text.length() != 0 && text.length() >= minimum)
      {
         if (text.indexOf(' ') == -1 && text.charAt(0) != '"')
         {
            // check for existance of a special operator
            boolean operatorAND = (text.charAt(0) == OP_AND);
            boolean operatorNOT = (text.charAt(0) == OP_NOT);
            // strip operator from term if one was found
            if (operatorAND || operatorNOT)
            {
               text = text.substring(1);
            }
            
            if (text.length() != 0)
            {
               // prepend NOT operator if supplied
               if (operatorNOT)
               {
                  fullTextBuf.append(OP_NOT);
                  nameAttrBuf.append(OP_NOT);
               }
               
               processSearchTextAttribute(nameAttr, text, nameAttrBuf, fullTextBuf);
               for (QName qname : this.simpleSearchAdditionalAttrs)
               {
                  processSearchAttribute(qname, text, additionalAttrsBuf, false, operatorNOT);
               }
            }
         }
         else
         {
            // multiple word search
            if (text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"')
            {
               // as a single quoted phrase
               String quotedSafeText = '"' + text.substring(1, text.length() - 1) + '"';
               fullTextBuf.append("TEXT:").append(quotedSafeText);
               nameAttrBuf.append("@").append(nameAttr).append(":").append(quotedSafeText);
               for (QName qname : this.simpleSearchAdditionalAttrs)
               {
                  additionalAttrsBuf.append(" @").append(
                        Repository.escapeQName(qname)).append(":").append(quotedSafeText);
               }
            }
            else
            {
               // as individual search terms
               StringTokenizer t = new StringTokenizer(text, " ");
               
               fullTextBuf.append('(');
               nameAttrBuf.append('(');
               additionalAttrsBuf.append('(');
               
               int termCount = 0;
               int tokenCount = t.countTokens();
               for (int i=0; i<tokenCount; i++)
               {
                  String term = t.nextToken();
                  
                  // check for existance of a special operator
                  boolean operatorAND = (term.charAt(0) == OP_AND);
                  boolean operatorNOT = (term.charAt(0) == OP_NOT);
                  // strip operator from term if one was found
                  if (operatorAND || operatorNOT)
                  {
                     term = term.substring(1);
                  }
                  
                  // special case for AND all terms if set (apply after operator character removed)
                  // note that we can't force AND if NOT operator has been set
                  if (operatorNOT == false)
                  {
                     operatorAND = operatorAND | this.forceAndTerms;
                  }
                  
                  if (term.length() != 0)
                  {
                     // prepend NOT operator if supplied
                     if (operatorNOT)
                     {
                        fullTextBuf.append(OP_NOT);
                        nameAttrBuf.append(OP_NOT);
                     }
                     
                     // prepend AND operator if supplied
                     if (operatorAND)
                     {
                        fullTextBuf.append(OP_AND);
                        nameAttrBuf.append(OP_AND);
                     }
                     
                     processSearchTextAttribute(nameAttr, term, nameAttrBuf, fullTextBuf);
                     for (QName qname : this.simpleSearchAdditionalAttrs)
                     {
                        processSearchAttribute(qname, term, additionalAttrsBuf, operatorAND, operatorNOT);
                     }
                     
                     fullTextBuf.append(' ');
                     nameAttrBuf.append(' ');
                     additionalAttrsBuf.append(' ');
                     
                     termCount++;
                  }
               }
               fullTextBuf.append(')');
               nameAttrBuf.append(')');
               additionalAttrsBuf.append(')');
            }
         }
         
         validQuery = true;
      }
      
      // match a specific PATH for space location or categories
      StringBuilder pathQuery = null;
      if (location != null || (categories != null && categories.length !=0))
      {
         pathQuery = new StringBuilder(128);
         if (location != null)
         {
            pathQuery.append(" PATH:\"").append(location).append("\" ");
            if (categories != null && categories.length != 0)
            {
               pathQuery.append("AND (");
            }
         }
         if (categories != null && categories.length != 0)
         {
            for (int i=0; i<categories.length; i++)
            {
               pathQuery.append(" PATH:\"").append(categories[i]).append("\" ");
            }
            if (location != null)
            {
               pathQuery.append(") ");
            }
         }
      }
      
      // match any extra query attribute values specified
      StringBuilder attributeQuery = null;
      if (queryAttributes.size() != 0)
      {
         attributeQuery = new StringBuilder(queryAttributes.size() << 6);
         for (QName qname : queryAttributes.keySet())
         {
            String value = queryAttributes.get(qname).trim();
            if (value.length() >= minimum)
            {
               processSearchAttribute(qname, value, attributeQuery);
            }
         }
         
         // handle the case where we did not add any attributes due to minimum length restrictions
         if (attributeQuery.length() == 0)
         {
            attributeQuery = null;
         }
      }
      
      // match any extra fixed value attributes specified
      if (queryFixedValues.size() != 0)
      {
         if (attributeQuery == null)
         {
            attributeQuery = new StringBuilder(queryFixedValues.size() << 6);
         }
         for (QName qname : queryFixedValues.keySet())
         {
            String escapedName = Repository.escapeQName(qname);
            String value = queryFixedValues.get(qname);
            attributeQuery.append(" +@").append(escapedName)
                          .append(":\"").append(value).append('"');
         }
      }
      
      // range attributes are a special case also
      if (rangeAttributes.size() != 0)
      {
         if (attributeQuery == null)
         {
            attributeQuery = new StringBuilder(rangeAttributes.size() << 6);
         }
         for (QName qname : rangeAttributes.keySet())
         {
            String escapedName = Repository.escapeQName(qname);
            RangeProperties rp = rangeAttributes.get(qname);
            String value1 = AbstractLuceneQueryParser.escape(rp.lower);
            String value2 = AbstractLuceneQueryParser.escape(rp.upper);
            attributeQuery.append(" +@").append(escapedName)
                          .append(":").append(rp.inclusive ? "[" : "{").append(value1)
                          .append(" TO ").append(value2).append(rp.inclusive ? "]" : "}");
         }
      }
      
      // mimetype is a special case - it is indexed as a special attribute it comes from the combined
      // ContentData attribute of cm:content - ContentData string cannot be searched directly
      if (mimeType != null && mimeType.length() != 0)
      {
         if (attributeQuery == null)
         {
            attributeQuery = new StringBuilder(64);
         }
         String escapedName = Repository.escapeQName(QName.createQName(ContentModel.PROP_CONTENT + ".mimetype"));
         attributeQuery.append(" +@").append(escapedName)
                       .append(":").append(mimeType);
      }
      
      // match against appropriate content type
      String fileTypeQuery;
      if (contentType != null)
      {
         fileTypeQuery = " TYPE:\"" + contentType + "\" ";
      }
      else
      {
         // default to cm:content
         fileTypeQuery = " TYPE:\"{" + NamespaceService.CONTENT_MODEL_1_0_URI + "}content\" ";
      }
      
      // match against appropriate folder type
      String folderTypeQuery;
      if (folderType != null)
      {
         folderTypeQuery = " TYPE:\"" + folderType + "\" ";
      }
      else
      {
         folderTypeQuery = " TYPE:\"{" + NamespaceService.CONTENT_MODEL_1_0_URI + "}folder\" ";
      }
      
      String fullTextQuery = fullTextBuf.toString();
      String nameAttrQuery = nameAttrBuf.toString();
      String additionalAttrsQuery =
         (this.simpleSearchAdditionalAttrs.size() != 0) ? additionalAttrsBuf.toString() : "";
      
      if (text.length() != 0 && text.length() >= minimum)
      {
         // text query for name and/or full text specified
         switch (mode)
         {
            case SearchContext.SEARCH_ALL:
               query = '(' + fileTypeQuery + " AND " + '(' + nameAttrQuery + ' ' + additionalAttrsQuery + ' ' + fullTextQuery + ')' + ')' +
                       ' ' +
                       '(' + folderTypeQuery + " AND " + '(' + nameAttrQuery + ' ' + additionalAttrsQuery + "))";
               break;
            
            case SearchContext.SEARCH_FILE_NAMES:
               query = fileTypeQuery + " AND " + nameAttrQuery;
               break;
            
            case SearchContext.SEARCH_FILE_NAMES_CONTENTS:
               query = fileTypeQuery + " AND " + '(' + nameAttrQuery + ' ' + fullTextQuery + ')';
               break;
            
            case SearchContext.SEARCH_SPACE_NAMES:
               query = folderTypeQuery + " AND " + nameAttrQuery;
               break;
            
            default:
               throw new IllegalStateException("Unknown search mode specified: " + mode);
         }
      }
      else
      {
         // no text query specified - must be an attribute/value query only
         switch (mode)
         {
            case SearchContext.SEARCH_ALL:
               query = '(' + fileTypeQuery + ' ' + folderTypeQuery + ')';
               break;
            
            case SearchContext.SEARCH_FILE_NAMES:
            case SearchContext.SEARCH_FILE_NAMES_CONTENTS:
               query = fileTypeQuery;
               break;
            
            case SearchContext.SEARCH_SPACE_NAMES:
               query = folderTypeQuery;
               break;
            
            default:
              throw new IllegalStateException("Unknown search mode specified: " + mode);
         }
      }
      
      // match entire query against any additional attributes specified
      if (attributeQuery != null)
      {
         query = attributeQuery + " AND (" + query + ')';
      }
      
      // match entire query against any specified paths
      if (pathQuery != null)
      {
         query = "(" + pathQuery + ") AND (" + query + ')';
      }
      
      // check that we have a query worth executing - if we have no attributes, paths or text/name search
      // then we'll only have a search against files/type TYPE which does nothing by itself!
      validQuery = validQuery | (attributeQuery != null) | (pathQuery != null);
      if (validQuery == false)
      {
         query = null;
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Query:\r\n" + query);
      
      return query;
   }
   
   /**
    * Build the lucene search terms required for the specified attribute and append to a buffer.
    * Supports text values with a wildcard '*' character as the prefix and/or the suffix. 
    * 
    * @param qname      QName of the attribute
    * @param value      Non-null value of the attribute
    * @param buf        Buffer to append lucene terms to
    */
   private static void processSearchAttribute(QName qname, String value, StringBuilder buf)
   {
      processSearchAttribute(qname, value, buf, true, false);
   }
   
   /**
    * Build the lucene search terms required for the specified attribute and append to a buffer.
    * Supports text values with a wildcard '*' character as the prefix and/or the suffix. 
    * 
    * @param qname      QName of the attribute
    * @param value      Non-null value of the attribute
    * @param buf        Buffer to append lucene terms to
    * @param andOp      If true apply the '+' AND operator as the prefix to the attribute term
    * @param notOp      If true apply the '-' NOT operator as the prefix to the attribute term
    */
   private static void processSearchAttribute(QName qname, String value, StringBuilder buf, boolean andOp, boolean notOp)
   {
      if (andOp) buf.append('+');
      else if (notOp) buf.append('-');
      buf.append('@').append(Repository.escapeQName(qname)).append(":\"")
         .append(value).append("\" ");
   }
   
   /**
    * Build the lucene search terms required for the specified attribute and append to multiple buffers.
    * Supports text values with a wildcard '*' character as the prefix and/or the suffix. 
    * 
    * @param qname      QName.toString() of the attribute
    * @param value      Non-null value of the attribute
    * @param attrBuf    Attribute search buffer to append lucene terms to
    * @param textBuf    Text search buffer to append lucene terms to
    */
   private static void processSearchTextAttribute(String qname, String value, StringBuilder attrBuf, StringBuilder textBuf)
   {
      textBuf.append("TEXT:\"").append(value).append('"');
      attrBuf.append('@').append(qname).append(":\"")
             .append(value).append('"');
   }
   
   /**
    * Returns a String where those characters that QueryParser
    * expects to be escaped are escaped by a preceding <code>\</code>.
    * '*' and '?' are not escaped.
    */
   private static String escape(String s)
   {
      StringBuffer sb = new StringBuffer(s.length() + 4);
      for (int i = 0; i < s.length(); i++)
      {
         char c = s.charAt(i);
         if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':' ||
             c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~')
         {
            sb.append('\\');
         }
         sb.append(c);
      }
      return sb.toString();
   }
   
   /**
    * Generate a search XPATH pointing to the specified node, optionally return an XPATH
    * that includes the child nodes.
    *  
    * @param id         Of the node to generate path too
    * @param children   Whether to include children of the node
    * 
    * @return the path
    */
   public static String getPathFromSpaceRef(NodeRef ref, boolean children)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      Path path = Repository.getServiceRegistry(context).getNodeService().getPath(ref);
      NamespaceService ns = Repository.getServiceRegistry(context).getNamespaceService();
      StringBuilder buf = new StringBuilder(64);
      for (int i=0; i<path.size(); i++)
      {
         String elementString = "";
         Path.Element element = path.get(i);
         if (element instanceof Path.ChildAssocElement)
         {
            ChildAssociationRef elementRef = ((Path.ChildAssocElement)element).getRef();
            if (elementRef.getParentRef() != null)
            {
               Collection prefixes = ns.getPrefixes(elementRef.getQName().getNamespaceURI());
               if (prefixes.size() >0)
               {
                  elementString = '/' + (String)prefixes.iterator().next() + ':' + ISO9075.encode(elementRef.getQName().getLocalName());
               }
            }
         }
         
         buf.append(elementString);
      }
      if (children == true)
      {
         // append syntax to get all children of the path
         buf.append("//*");
      }
      else
      {
         // append syntax to just represent the path, not the children
         buf.append("/*");
      }
      
      return buf.toString();
   }
   
   /**
    * @return Returns the categories to use for the search
    */
   public String[] getCategories()
   {
      return this.categories;
   }
   
   /**
    * @param categories    The categories to set as a list of search XPATHs
    */
   public void setCategories(String[] categories)
   {
      if (categories != null)
      {
         this.categories = categories;
      }
   }
   
   /**
    * @return Returns the node XPath to search in or null for all.
    */
   public String getLocation()
   {
      return this.location;
   }
   
   /**
    * @param location      The node XPATH to search from or null for all..
    */
   public void setLocation(String location)
   {
      this.location = location;
   }
   
   /**
    * @return Returns the mode to use during the search (see constants)
    */
   public int getMode()
   {
      return this.mode;
   }
   
   /**
    * @param mode The mode to use during the search (see constants)
    */
   public void setMode(int mode)
   {
      this.mode = mode;
   }
   
   /**
    * @return Returns the search text string.
    */
   public String getText()
   {
      return this.text;
   }
   
   /**
    * @param text       The search text string.
    */
   public void setText(String text)
   {
      this.text = text;
   }

   /**
    * @return Returns the contentType.
    */
   public String getContentType()
   {
      return this.contentType;
   }

   /**
    * @param contentType The content type to restrict attribute search against.
    */
   public void setContentType(String contentType)
   {
      this.contentType = contentType;
   }
   
   /**
    * @return Returns the folderType.
    */
   public String getFolderType()
   {
      return this.folderType;
   }

   /**
    * @param folderType The folder type to restrict attribute search against.
    */
   public void setFolderType(String folderType)
   {
      this.folderType = folderType;
   }
   
   /**
    * @return Returns the mimeType.
    */
   public String getMimeType()
   {
      return this.mimeType;
   }
   /**
    * @param mimeType The mimeType to set.
    */
   public void setMimeType(String mimeType)
   {
      this.mimeType = mimeType;
   }
   
   /**
    * Add an additional attribute to search against for simple searches
    * 
    * @param qname      QName of the attribute to search against
    * @param value      Value of the attribute to use
    */
   public void addSimpleAttributeQuery(QName qname)
   {
      this.simpleSearchAdditionalAttrs.add(qname);
   }
   
   /**
    * Sets the additional attribute to search against for simple searches.
    * 
    * @param attrs      The list of attributes to search against
    */
   public void setSimpleSearchAdditionalAttributes(List<QName> attrs)
   {
      if (attrs != null)
      {
         this.simpleSearchAdditionalAttrs = attrs;
      }
   }
   
   /**
    * Add an additional attribute to search against
    * 
    * @param qname      QName of the attribute to search against
    * @param value      Value of the attribute to use
    */
   public void addAttributeQuery(QName qname, String value)
   {
      this.queryAttributes.put(qname, value);
   }
   
   public String getAttributeQuery(QName qname)
   {
      return this.queryAttributes.get(qname);
   }
   
   /**
    * Add an additional range attribute to search against
    * 
    * @param qname      QName of the attribute to search against
    * @param lower      Lower value for range
    * @param upper      Upper value for range
    * @param inclusive  True for inclusive within the range, false otherwise
    */
   public void addRangeQuery(QName qname, String lower, String upper, boolean inclusive)
   {
      this.rangeAttributes.put(qname, new RangeProperties(qname, lower, upper, inclusive));
   }
   
   public RangeProperties getRangeProperty(QName qname)
   {
      return this.rangeAttributes.get(qname);
   }
   
   /**
    * Add an additional fixed value attribute to search against
    * 
    * @param qname      QName of the attribute to search against
    * @param value      Fixed value of the attribute to use
    */
   public void addFixedValueQuery(QName qname, String value)
   {
      this.queryFixedValues.put(qname, value);
   }
   
   public String getFixedValueQuery(QName qname)
   {
      return this.queryFixedValues.get(qname);
   }
   
   /**
    * @return Returns if AND is forced between text terms. False (OR terms) is the default.
    */
   public boolean getForceAndTerms()
   {
      return this.forceAndTerms;
   }

   /**
    * @param forceAndTerms Set true to force AND between text terms. Otherwise OR is the default.
    */
   public void setForceAndTerms(boolean forceAndTerms)
   {
      this.forceAndTerms = forceAndTerms;
   }

   /**
    * @return this SearchContext as XML
    * 
    * Example:
    * <code>
    * <?xml version="1.0" encoding="UTF-8"?>
    * <search>
    *    <text>CDATA</text>
    *    <mode>int</mode>
    *    <location>XPath</location>
    *    <categories>
    *       <category>XPath</category>
    *    </categories>
    *    <content-type>String</content-type>
    *    <folder-type>String</folder-type>
    *    <mimetype>String</mimetype>
    *    <attributes>
    *       <attribute name="String">String</attribute>
    *    </attributes>
    *    <ranges>
    *       <range name="String">
    *          <lower>String</lower>
    *          <upper>String</upper>
    *          <inclusive>boolean</inclusive>
    *       </range>
    *    </ranges>
    *    <fixed-values>
    *       <value name="String">String</value>
    *    </fixed-values>
    *    <query>CDATA</query>
    * </search>
    * </code>
    */
   public String toXML()
   {
      try
      {
         NamespaceService ns = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
         
         Document doc = DocumentHelper.createDocument();
         
         Element root = doc.addElement(ELEMENT_SEARCH);
         
         root.addElement(ELEMENT_TEXT).addCDATA(this.text);
         root.addElement(ELEMENT_MODE).addText(Integer.toString(this.mode));
         if (this.location != null)
         {
            root.addElement(ELEMENT_LOCATION).addText(this.location);
         }
         
         Element categories = root.addElement(ELEMENT_CATEGORIES);
         for (String path : this.categories)
         {
            categories.addElement(ELEMENT_CATEGORY).addText(path);
         }
         
         if (this.contentType != null)
         {
            root.addElement(ELEMENT_CONTENT_TYPE).addText(this.contentType);
         }
         if (this.folderType != null)
         {
            root.addElement(ELEMENT_FOLDER_TYPE).addText(this.folderType);
         }
         if (this.mimeType != null && this.mimeType.length() != 0)
         {
            root.addElement(ELEMENT_MIMETYPE).addText(this.mimeType);
         }
         
         Element attributes = root.addElement(ELEMENT_ATTRIBUTES);
         for (QName attrName : this.queryAttributes.keySet())
         {
            attributes.addElement(ELEMENT_ATTRIBUTE)
                      .addAttribute(ELEMENT_NAME, attrName.toPrefixString(ns))
                      .addCDATA(this.queryAttributes.get(attrName));
         }
         
         Element ranges = root.addElement(ELEMENT_RANGES);
         for (QName rangeName : this.rangeAttributes.keySet())
         {
            RangeProperties rangeProps = this.rangeAttributes.get(rangeName);
            Element range = ranges.addElement(ELEMENT_RANGE);
            range.addAttribute(ELEMENT_NAME, rangeName.toPrefixString(ns));
            range.addElement(ELEMENT_LOWER).addText(rangeProps.lower);
            range.addElement(ELEMENT_UPPER).addText(rangeProps.upper);
            range.addElement(ELEMENT_INCLUSIVE).addText(Boolean.toString(rangeProps.inclusive));
         }
         
         Element values = root.addElement(ELEMENT_FIXED_VALUES);
         for (QName valueName : this.queryFixedValues.keySet())
         {
            values.addElement(ELEMENT_VALUE)
                  .addAttribute(ELEMENT_NAME, valueName.toPrefixString(ns))
                  .addCDATA(this.queryFixedValues.get(valueName));
         }
         
         // outputing the full lucene query may be useful for some situations
         Element query = root.addElement(ELEMENT_QUERY);
         String queryString = buildQuery(0);
         if (queryString != null)
         {
            query.addCDATA(queryString);
         }
         
         StringWriter out = new StringWriter(1024);
         XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
         writer.setWriter(out);
         writer.write(doc);
         
         return out.toString();
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Failed to export SearchContext to XML.", err);
      }
   }
   
   /**
    * Restore a SearchContext from an XML definition
    * 
    * @param xml     XML format SearchContext @see #toXML()
    */
   public SearchContext fromXML(String xml)
   {
      try
      {
         NamespaceService ns = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getNamespaceService();
         
         // get the root element
         SAXReader reader = new SAXReader();
         Document document = reader.read(new StringReader(xml));
         Element rootElement = document.getRootElement();
         Element textElement = rootElement.element(ELEMENT_TEXT);
         if (textElement != null)
         {
            this.text = textElement.getText();
         }
         Element modeElement = rootElement.element(ELEMENT_MODE);
         if (modeElement != null)
         {
            this.mode = Integer.parseInt(modeElement.getText());
         }
         Element locationElement = rootElement.element(ELEMENT_LOCATION);
         if (locationElement != null)
         {
            this.location = locationElement.getText();
         }
         Element categoriesElement = rootElement.element(ELEMENT_CATEGORIES);
         if (categoriesElement != null)
         {
            List<String> categories = new ArrayList<String>(4);
            for (Iterator i=categoriesElement.elementIterator(ELEMENT_CATEGORY); i.hasNext(); /**/)
            {
               Element categoryElement = (Element)i.next();
               categories.add(categoryElement.getText());
            }
            this.categories = categories.toArray(this.categories);
         }
         Element contentTypeElement = rootElement.element(ELEMENT_CONTENT_TYPE);
         if (contentTypeElement != null)
         {
            this.contentType = contentTypeElement.getText();
         }
         Element folderTypeElement = rootElement.element(ELEMENT_FOLDER_TYPE);
         if (folderTypeElement != null)
         {
            this.folderType = folderTypeElement.getText();
         }
         Element mimetypeElement = rootElement.element(ELEMENT_MIMETYPE);
         if (mimetypeElement != null)
         {
            this.mimeType = mimetypeElement.getText();
         }
         Element attributesElement = rootElement.element(ELEMENT_ATTRIBUTES);
         if (attributesElement != null)
         {
            for (Iterator i=attributesElement.elementIterator(ELEMENT_ATTRIBUTE); i.hasNext(); /**/)
            {
               Element attrElement = (Element)i.next();
               QName qname = QName.createQName(attrElement.attributeValue(ELEMENT_NAME), ns);
               addAttributeQuery(qname, attrElement.getText());
            }
         }
         Element rangesElement = rootElement.element(ELEMENT_RANGES);
         if (rangesElement != null)
         {
            for (Iterator i=rangesElement.elementIterator(ELEMENT_RANGE); i.hasNext(); /**/)
            {
               Element rangeElement = (Element)i.next();
               Element lowerElement = rangeElement.element(ELEMENT_LOWER);
               Element upperElement = rangeElement.element(ELEMENT_UPPER);
               Element incElement = rangeElement.element(ELEMENT_INCLUSIVE);
               if (lowerElement != null && upperElement != null && incElement != null)
               {
                  QName qname = QName.createQName(rangeElement.attributeValue(ELEMENT_NAME), ns);
                  addRangeQuery(qname,
                        lowerElement.getText(), upperElement.getText(),
                        Boolean.parseBoolean(incElement.getText()));
               }
            }
         }
         
         Element valuesElement = rootElement.element(ELEMENT_FIXED_VALUES);
         if (valuesElement != null)
         {
            for (Iterator i=valuesElement.elementIterator(ELEMENT_VALUE); i.hasNext(); /**/)
            {
               Element valueElement = (Element)i.next();
               QName qname = QName.createQName(valueElement.attributeValue(ELEMENT_NAME), ns);
               addFixedValueQuery(qname, valueElement.getText());
            }
         }
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Failed to import SearchContext from XML.", err);
      }
      return this;
   }
   
   /**
    * Simple wrapper class for range query attribute properties 
    */
   static class RangeProperties implements Serializable
   {
      private static final long serialVersionUID = 5627339191207625169L;
      
      QName qname;
      String lower;
      String upper;
      boolean inclusive;
      
      RangeProperties(QName qname, String lower, String upper, boolean inclusive)
      {
         this.qname = qname;
         this.lower = lower;
         this.upper = upper;
         this.inclusive = inclusive;
      }
   }
}
