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
import org.alfresco.util.TempFileProvider;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
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
   ////////////////////////////////////////////////////////////////////////////
   
   /**
    * Encapsulation of configuration file management.
    */
   private static class Configuration
   {
      /** indicates whether or not the configuration file has been loaded */
      public static boolean loaded = false;
      private static NodeRef configFileNodeRef = null;
      
      /**
       * locate the configuration file.  currently it is stored as 
       * <tt>templating_config.xml</tt> at the root of the data dictionary.
       *
       * @return the configuration file, which is currently all the
       * <tt>TemplateTypes</tt> serialized using object serialization.
       */
      private static NodeRef getConfigFile()
      {
         if (configFileNodeRef == null)
         {
            final TemplatingService ts = TemplatingService.INSTANCE;
            LOGGER.debug("loading config file");
            // get the template from the special Email Templates folder
            FacesContext fc = FacesContext.getCurrentInstance();
            String xpath = (Application.getRootPath(fc) + "/" + 
                  Application.getGlossaryFolderName(fc));
            NodeRef rootNodeRef = ts.nodeService.getRootNode(Repository.getStoreRef());
            List<NodeRef> results = ts.searchService.selectNodes(rootNodeRef, xpath, null, ts.namespaceService, false);
            if (results.size() != 1)
               throw new RuntimeException("expected one result for " + xpath);
            NodeRef dataDictionaryNodeRef =  results.get(0);
            LOGGER.debug("loaded data dictionary " + dataDictionaryNodeRef);
            try
            {
               Configuration.configFileNodeRef = 
                  ts.fileFolderService.create(dataDictionaryNodeRef,
                        "templating_configuration.xml",
                        ContentModel.TYPE_CONTENT).getNodeRef();
            }
            catch (FileExistsException fee)
            {
               Configuration.configFileNodeRef = 
                  ts.fileFolderService.searchSimple(dataDictionaryNodeRef, "templating_configuration.xml");
            }
            LOGGER.debug("loaded config file " + configFileNodeRef);
            assert Configuration.configFileNodeRef != null : "unable to load templating_configuration.xml";
         }
         return Configuration.configFileNodeRef;
      }
      
      /**
       * Load the configuration file into the templating service.
       */
      public static void load()
         throws IOException
      {
         final TemplatingService ts = TemplatingService.INSTANCE;
         final NodeRef configFileNodeRef = getConfigFile();
         FacesContext fc = FacesContext.getCurrentInstance();
         final ContentReader contentReader = ts.contentService.getReader(configFileNodeRef, 
               ContentModel.TYPE_CONTENT);
         if (contentReader == null)
            LOGGER.debug("templating_config.xml is empty");
         else
         {
            LOGGER.debug("parsing templating_config.xml");
            final InputStream contentIn = contentReader.getContentInputStream();
            final ObjectInputStream in = new ObjectInputStream(contentIn);
            try
            {
               while (true)
               {
                  try
                  {
                     final TemplateType tt = (TemplateType)in.readObject();
                     TemplatingService.INSTANCE.registerTemplateType(tt);
                  }
                  catch (EOFException eof)
                  {
                     break;
                  }
                  
               }
               in.close();
            }
            catch (ClassNotFoundException cnfe)
            {
               TemplatingService.LOGGER.error(cnfe);
            }
         }
         loaded = true;
      }
      
      /**
       * Save the current state of the templating service to the configuration file.
       */
      public static void save()
         throws IOException
      {
         final TemplatingService ts = TemplatingService.INSTANCE;
         FacesContext fc = FacesContext.getCurrentInstance();
         final NodeRef configFileNodeRef = getConfigFile();
         final OutputStream contentOut = ts.contentService.getWriter(configFileNodeRef, ContentModel.TYPE_CONTENT, true).getContentOutputStream();
         final ObjectOutputStream out = new ObjectOutputStream(contentOut);
         for (TemplateType tt : TemplatingService.INSTANCE.getTemplateTypes())
         {
            out.writeObject(tt);
         }
         out.close();
      }
   }
   
   ////////////////////////////////////////////////////////////////////////////
   
   /**
    * temporary location of the property on nodes that are xml files created
    * by templating.
    */
   public static final org.alfresco.service.namespace.QName TT_QNAME = 
      org.alfresco.service.namespace.QName.createQName(org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI, "tt");
   
   /**
    * temporary location of the property on nodes generated from xml assets.
    */
   public static final org.alfresco.service.namespace.QName TT_GENERATED_OUTPUT_QNAME = 
      org.alfresco.service.namespace.QName.createQName(org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI, "tt_generated_output");
   
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
      if (!Configuration.loaded)
      {
         LOGGER.debug("loading configuration");
         try
         {
            Configuration.load();
         }
         catch (Throwable t)
         {
            LOGGER.error(t);
            t.printStackTrace();
         }
      }
      
      return TemplatingService.INSTANCE;
   }
   
   /** returns all registered template types */
   public Collection<TemplateType> getTemplateTypes()
   {
      return this.templateTypes.values();
   }
   
   /** return the template type by name or <tt>null</tt> if not found */
   public TemplateType getTemplateType(final String name)
   {
      return this.templateTypes.get(name);
   }
   
   /** registers a template type.  if one exists with the same name, it is replaced */
   public void registerTemplateType(final TemplateType tt)
   {
      this.templateTypes.put(tt.getName(), tt);
      try
      {
         Configuration.save();
      }
      catch (IOException ioe)
      {
         LOGGER.error(ioe);
      }
   }
   
   /** 
    * instantiate a template type.  for now this will always generate the
    * xforms implementation, but will at some point be configurable such that
    * the template type implementation can be configured for the system,
    * or specified in the gui.
    */
   public TemplateType newTemplateType(final String name,
         final NodeRef schemaNodeRef)
   {
      return new TemplateTypeImpl(name, schemaNodeRef);
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
