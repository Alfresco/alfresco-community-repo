/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.templating;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

import javax.xml.parsers.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.alfresco.web.templating.xforms.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.model.*;
import org.alfresco.service.namespace.NamespaceService;
import javax.faces.context.FacesContext;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;

/**
 * Provides management of template types.
 */
public final class TemplatingService implements Serializable
{
   
   private static final Log LOGGER = LogFactory.getLog(TemplatingService.class);
   
   /** the single instance initialized using spring */
   private static TemplatingService INSTANCE;
   
   /** internal storage of template types, keyed by the template name */
   private HashMap<String, TemplateType> templateTypes = 
      new HashMap<String, TemplateType>();
   
   private final ContentService contentService;
   private final NodeService nodeService;
   private final FileFolderService fileFolderService;
   private final DictionaryService dictionaryService;
   private final NamespaceService namespaceService;
   private final SearchService searchService;

   private NodeRef contentFormsNodeRef;
   
   /** instantiated using spring */
   public TemplatingService(final ContentService contentService,
                            final NodeService nodeService,
                            final FileFolderService fileFolderService,
                            final DictionaryService dictionaryService,
                            final NamespaceService namespaceService,
                            final SearchService searchService)
   {
      this.contentService = contentService;
      this.nodeService = nodeService;
      this.fileFolderService = fileFolderService;
      this.dictionaryService = dictionaryService;
      this.namespaceService = namespaceService;
      this.searchService = searchService;
      if (INSTANCE == null)
         INSTANCE = this;
   }
   
   /** Provides the templating service instance, loads config if necessary */
   public static TemplatingService getInstance()
   {
      return TemplatingService.INSTANCE;
   }

   /**
    * @return the cached reference to the WCM Content Forms folder
    */
   public NodeRef getContentFormsNodeRef()
   {
      if (this.contentFormsNodeRef == null)
      {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final String xpath = (Application.getRootPath(fc) + "/" +
                               Application.getGlossaryFolderName(fc) + "/" +
                               Application.getContentFormsFolderName(fc));
         LOGGER.debug("locating content forms at " + xpath);
         final List<NodeRef> results = 
            searchService.selectNodes(nodeService.getRootNode(Repository.getStoreRef()),
                                      xpath,
                                      null,
                                      namespaceService,
                                      false);
         this.contentFormsNodeRef = (results != null && results.size() == 1 ? results.get(0) : null);
      }
      return this.contentFormsNodeRef;
   }
   
   /** returns all registered template types */
   public Collection<TemplateType> getTemplateTypes()
   {
      try
      {
         final SearchParameters sp = new SearchParameters();
         sp.addStore(Repository.getStoreRef());
         sp.setLanguage(SearchService.LANGUAGE_LUCENE);
         sp.setQuery("ASPECT:\"" + WCMModel.ASPECT_FORM + "\"");
         LOGGER.debug("running query [" + sp.getQuery() + "]");
         final ResultSet rs = this.searchService.query(sp);
         LOGGER.debug("received " + rs.length() + " results");
         final Collection<TemplateType> result = new LinkedList<TemplateType>();
         for (ResultSetRow row : rs)
         {
            final NodeRef nodeRef = row.getNodeRef();
            result.add(this.newTemplateType(nodeRef));
         }
         return result;
      }
      catch (RuntimeException re)
      {
         LOGGER.error(re);
         throw re;
      }
   }
   
   /** return the template type by name or <tt>null</tt> if not found */
   public TemplateType getTemplateType(final String name)
   {
      try
      {
         final SearchParameters sp = new SearchParameters();
         sp.addStore(Repository.getStoreRef());
         sp.setLanguage(SearchService.LANGUAGE_LUCENE);
         sp.setQuery("ASPECT:\"" + WCMModel.ASPECT_FORM + 
                     "\" AND @" + Repository.escapeQName(ContentModel.PROP_TITLE) + 
                     ":\"" + name + "\"");
         LOGGER.debug("running query [" + sp.getQuery() + "]");
         final ResultSet rs = this.searchService.query(sp);
         NodeRef result = null;
         for (ResultSetRow row : rs)
         {
            final NodeRef nr = row.getNodeRef();
            if (this.nodeService.getProperty(nr, ContentModel.PROP_TITLE).equals(name))
            {
               result = nr;
               break;
            }
         }
         if (result == null && LOGGER.isDebugEnabled())
            LOGGER.debug("unable to find tempalte type " + name);
         return result != null ? this.newTemplateType(result) : null;
      }
      catch (RuntimeException re)
      {
         LOGGER.error(re);
         throw re;
      }
   }

   public TemplateType getTemplateType(final NodeRef nodeRef)
   {
      return this.newTemplateType(nodeRef);
   }
   
   /** 
    * instantiate a template type.  for now this will always generate the
    * xforms implementation, but will at some point be configurable such that
    * the template type implementation can be configured for the system,
    * or specified in the gui.
    */
   private TemplateType newTemplateType(final NodeRef schemaNodeRef)
   {
      LOGGER.debug("creating template type for " + schemaNodeRef);
      final String title = (String)
         this.nodeService.getProperty(schemaNodeRef, ContentModel.PROP_TITLE);
      LOGGER.debug("title is " + title);
      final String schemaRootTagName = (String)
         this.nodeService.getProperty(schemaNodeRef, WCMModel.PROP_SCHEMA_ROOT_TAG_NAME);
      LOGGER.debug("root tag name is " + schemaRootTagName);
      final TemplateType tt = new TemplateTypeImpl(title, schemaNodeRef, schemaRootTagName);
      for (AssociationRef assoc : this.nodeService.getTargetAssocs(schemaNodeRef, 
                                                                   WCMModel.ASSOC_FORM_TRANSFORMERS))
      {
         final NodeRef tomNodeRef = assoc.getTargetRef();
         try
         {
            final Class templateOutputMethodType = 
               Class.forName((String)this.nodeService.getProperty(tomNodeRef,
                                                                  WCMModel.PROP_FORM_TRANSFORMER_TYPE));
            
            final Constructor c = templateOutputMethodType.getConstructor(NodeRef.class, NodeService.class, ContentService.class);
            final TemplateOutputMethod tom = (TemplateOutputMethod)
               c.newInstance(tomNodeRef, this.nodeService, this.contentService);
            LOGGER.debug("loaded template output method type " + tom.getClass().getName() +
                         " for extension " + tom.getFileExtension() + ", " + tomNodeRef);
            tt.addOutputMethod(tom);
         }
         catch (Exception e)
         {
            LOGGER.error(e);
         }
      }
      return tt;
   }
   
   /** utility function for creating a document */
   public Document newDocument()
   {
      try
      {
         final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware(true);
         dbf.setValidating(false);
         final DocumentBuilder db = dbf.newDocumentBuilder();
         return db.newDocument();
      }
      catch (ParserConfigurationException pce)
      {
         assert false : pce;
      LOGGER.error(pce);
      return null;
      }
//    catch (SAXException saxe)
//    {
//    LOGGER.error(saxe);
//    }
//    catch (IOException ioe)
//    {
//    LOGGER.error(ioe);
//    }
   }
   
   /** utility function for serializing a node */
   public void writeXML(final Node n, final Writer output)
   {
      try 
      {
         final TransformerFactory tf = TransformerFactory.newInstance();
         final Transformer t = tf.newTransformer();
         t.setOutputProperty(OutputKeys.INDENT, "yes");
         
         if (LOGGER.isDebugEnabled())
         {
            LOGGER.debug("writing out a document for " + 
			 (n instanceof Document
			  ? ((Document)n).getDocumentElement()
			  : n).getNodeName() + 
			 " to " + (output instanceof StringWriter
				   ? "string"
				   : output));
            final StringWriter sw = new StringWriter();
            t.transform(new DOMSource(n), new StreamResult(sw));
            LOGGER.debug(sw.toString());
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
   public void writeXML(final Node n, final File output)
   throws IOException
   {
      this.writeXML(n, new FileWriter(output));
   }
   
   /** utility function for serializing a node */
   public String writeXMLToString(final Node n)
   {
      final StringWriter result = new StringWriter();
      this.writeXML(n, result);
      return result.toString();
   }
   
   /** utility function for parsing xml */
   public Document parseXML(final String source)
   throws ParserConfigurationException,
   SAXException,
   IOException
   {
      return this.parseXML(new ByteArrayInputStream(source.getBytes()));
   }
   
   /** utility function for parsing xml */
   public Document parseXML(final NodeRef nodeRef)
      throws ParserConfigurationException,
      SAXException,
      IOException
   {
      final ContentReader contentReader = 
         this.contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
      final InputStream in = contentReader.getContentInputStream();
      return this.parseXML(in);
   }
   
   /** utility function for parsing xml */
   public Document parseXML(final File source)
      throws ParserConfigurationException,
      SAXException,
      IOException
   {
      return this.parseXML(new FileInputStream(source));
   }
   
   /** utility function for parsing xml */
   public Document parseXML(final InputStream source)
      throws ParserConfigurationException,
      SAXException,
      IOException
   {
      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      dbf.setValidating(false);
      final DocumentBuilder db = dbf.newDocumentBuilder();
      final Document result = db.parse(source);
      source.close();
      return result;
   }
}
