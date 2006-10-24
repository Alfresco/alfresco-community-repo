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
package org.alfresco.web.forms;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import javax.faces.context.FacesContext;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.*;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.*;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.xforms.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Provides management of forms.
 *
 * @author Ariel Backenroth
 */
public final class FormsService 
   implements Serializable
{
   
   private static final Log LOGGER = LogFactory.getLog(FormsService.class);
   
   /** the single instance initialized using spring */
   private static FormsService INSTANCE;
   private static DocumentBuilder documentBuilder;
   
   /** internal storage of forms, keyed by the form name */
   private HashMap<String, Form> forms = new HashMap<String, Form>();
   
   private final ContentService contentService;
   private final NodeService nodeService;
   private final FileFolderService fileFolderService;
   private final DictionaryService dictionaryService;
   private final NamespaceService namespaceService;
   private final SearchService searchService;
   private final AVMService avmService;

   private NodeRef contentFormsNodeRef;
   
   /** instantiated using spring */
   public FormsService(final ContentService contentService,
                       final NodeService nodeService,
                       final FileFolderService fileFolderService,
                       final DictionaryService dictionaryService,
                       final NamespaceService namespaceService,
                       final SearchService searchService,
                       final AVMService avmService)
   {
      this.contentService = contentService;
      this.nodeService = nodeService;
      this.fileFolderService = fileFolderService;
      this.dictionaryService = dictionaryService;
      this.namespaceService = namespaceService;
      this.searchService = searchService;
      this.avmService = avmService;
      if (INSTANCE == null)
         INSTANCE = this;
   }
   
   /** Provides the forms service instance, loads config if necessary */
   public static FormsService getInstance()
   {
      return FormsService.INSTANCE;
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
   
   /** 
    * returns all registered forms 
    *
    * @return all registered forms.
    */
   public Collection<Form> getForms()
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
         final Collection<Form> result = new LinkedList<Form>();
         for (ResultSetRow row : rs)
         {
            final NodeRef nodeRef = row.getNodeRef();
            result.add(this.newForm(nodeRef));
         }
         return result;
      }
      catch (RuntimeException re)
      {
         LOGGER.error(re);
         throw re;
      }
   }
   
   /** 
    * return the form by name or <tt>null</tt> if not found 
    *
    * @return the form by name or <tt>null</tt> if not found 
    */
   public Form getForm(final String name)
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
         return result != null ? this.newForm(result) : null;
      }
      catch (RuntimeException re)
      {
         LOGGER.error(re);
         throw re;
      }
   }

   /**
    * Returns the form backed by the given NodeRef.  The NodeRef should
    * point to the schema for this form.
    *
    * @param nodeRef the node ref for the schema for the form
    * @return the form for the given node ref.
    */
   public Form getForm(final NodeRef nodeRef)
   {
      return this.newForm(nodeRef);
   }
   
   /** 
    * instantiate a form.  for now this will always generate the
    * xforms implementation, but will at some point be configurable such that
    * the form processor type implementation can be configured for the system,
    * or specified in the gui.
    */
   private Form newForm(final NodeRef schemaNodeRef)
   {
      LOGGER.debug("creating form for " + schemaNodeRef);
      final String title = (String)
         this.nodeService.getProperty(schemaNodeRef, ContentModel.PROP_TITLE);
      LOGGER.debug("title is " + title);
      final String schemaRootTagName = (String)
         this.nodeService.getProperty(schemaNodeRef, WCMModel.PROP_SCHEMA_ROOT_ELEMENT_NAME);
      LOGGER.debug("root tag name is " + schemaRootTagName);
      final Form tt = new FormImpl(title, schemaNodeRef, schemaRootTagName);
      for (AssociationRef assoc : this.nodeService.getTargetAssocs(schemaNodeRef, 
                                                                   WCMModel.ASSOC_RENDERING_ENGINES))
      {
         final NodeRef tomNodeRef = assoc.getTargetRef();
         try
         {
            final Class formDataRendererType = 
               Class.forName((String)this.nodeService.getProperty(tomNodeRef,
                                                                  WCMModel.PROP_RENDERING_ENGINE_TYPE));
            
            final Constructor c = formDataRendererType.getConstructor(NodeRef.class, NodeService.class, ContentService.class);
            final RenderingEngine tom = (RenderingEngine)
               c.newInstance(tomNodeRef, this.nodeService, this.contentService);
            LOGGER.debug("loaded form data renderer type " + tom.getClass().getName() +
                         " for extension " + tom.getFileExtension() + ", " + tomNodeRef);
            tt.addRenderingEngine(tom);
         }
         catch (Exception e)
         {
            LOGGER.error(e);
         }
      }
      return tt;
   }
   
   /**
    * Generates renditions for the provided formInstanceData.
    *
    * @param formInstanceDataNodeRef the noderef containing the form instance data
    * @param formInstanceData the parsed contents of the form.
    * @param form the form to use when generating renditions.
    */
   public void generateRenditions(final NodeRef formInstanceDataNodeRef,
                                  final Document formInstanceData, 
                                  final Form form)
      throws IOException,
      RenderingEngine.RenderingException
   {
      final String formInstanceDataFileName = (String)
         nodeService.getProperty(formInstanceDataNodeRef, ContentModel.PROP_NAME);
      final String formInstanceDataAvmPath = AVMNodeConverter.ToAVMVersionPath(formInstanceDataNodeRef).getSecond();
      final String parentPath = AVMNodeConverter.SplitBase(formInstanceDataAvmPath)[0];

      for (RenderingEngine re : form.getRenderingEngines())
      {
         // get the node ref of the node that will contain the content
         final String renditionFileName = 
            this.stripExtension(formInstanceDataFileName) + "." + re.getFileExtension();
         final OutputStream fileOut = this.avmService.createFile(parentPath, renditionFileName);
         final String renditionAvmPath = parentPath + '/' + renditionFileName;
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("Created file node for file: " + renditionAvmPath);
         final OutputStreamWriter out = new OutputStreamWriter(fileOut);

         final HashMap<String, String> parameters =
            this.getRenderingEngineParameters(formInstanceDataFileName, 
                                           renditionFileName, 
                                           parentPath);
         re.generate(formInstanceData, parameters, out);
         out.close();
            
         final NodeRef renditionNodeRef = 
            AVMNodeConverter.ToNodeRef(-1, parentPath + '/' + renditionFileName);

         Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
         props.put(WCMModel.PROP_PARENT_FORM, form.getNodeRef());
         props.put(WCMModel.PROP_PARENT_FORM_NAME, form.getName());
         nodeService.addAspect(renditionNodeRef, WCMModel.ASPECT_FORM_INSTANCE_DATA, props);

         props = new HashMap<QName, Serializable>(2, 1.0f);
         props.put(WCMModel.PROP_PARENT_RENDERING_ENGINE, re.getNodeRef());
         props.put(WCMModel.PROP_PRIMARY_FORM_INSTANCE_DATA, 
                   AVMNodeConverter.ToNodeRef(-1, parentPath + formInstanceDataFileName));
         nodeService.addAspect(renditionNodeRef, WCMModel.ASPECT_RENDITION, props);

         props = new HashMap<QName, Serializable>(1, 1.0f);
         props.put(ContentModel.PROP_TITLE, renditionFileName);
         nodeService.addAspect(renditionNodeRef, ContentModel.ASPECT_TITLED, props);
            
         LOGGER.debug("generated " + renditionFileName + " using " + re);
      }
   }
   
   /**
    * Regenerates all renditions of the provided form instance data.
    *
    * @param formInstanceDataNodeRef the node ref containing the form instance data.
    */
   public void regenerateRenditions(final NodeRef formInstanceDataNodeRef)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException
   {
      final NodeRef formNodeRef = (NodeRef)
         nodeService.getProperty(formInstanceDataNodeRef, WCMModel.PROP_PARENT_FORM);

      final Form form = this.getForm(formNodeRef);
         
      final ContentReader reader = contentService.getReader(formInstanceDataNodeRef, ContentModel.PROP_CONTENT);
      final Document formInstanceData = this.parseXML(reader.getContentInputStream());
      final String formInstanceDataFileName = (String)
         nodeService.getProperty(formInstanceDataNodeRef, ContentModel.PROP_NAME);

      // other parameter values passed to rendering engine
      final String formInstanceDataAvmPath = AVMNodeConverter.ToAVMVersionPath(formInstanceDataNodeRef).getSecond();
      final String parentPath = AVMNodeConverter.SplitBase(formInstanceDataAvmPath)[0];

      for (RenderingEngine re : form.getRenderingEngines())
      {
         final String renditionFileName = 
            this.stripExtension(formInstanceDataFileName) + "." + re.getFileExtension();

         if (LOGGER.isDebugEnabled())
            LOGGER.debug("regenerating file node for : " + formInstanceDataFileName + 
                         " (" + formInstanceDataNodeRef.toString() + 
                         ") to " + parentPath + 
                         "/" + renditionFileName);
            
         // get a writer for the content and put the file
         OutputStream out = null;
         try
         {
            out = this.avmService.getFileOutputStream(parentPath + "/" + renditionFileName);
         }
         catch (AVMNotFoundException e)
         {
            out = this.avmService.createFile(parentPath, renditionFileName);
         }

         final OutputStreamWriter writer = new OutputStreamWriter(out);
         final HashMap<String, String> parameters =
            this.getRenderingEngineParameters(formInstanceDataFileName, 
                                           renditionFileName, 
                                           parentPath);
         re.generate(formInstanceData, parameters, writer);
         writer.close();

         LOGGER.debug("generated " + renditionFileName + " using " + re);
      }
   }

   private static HashMap<String, String> getRenderingEngineParameters(final String formInstanceDataFileName,
                                                                       final String renditionFileName,
                                                                       final String parentAvmPath)
   {
      final HashMap<String, String> parameters = new HashMap<String, String>();      
      parameters.put("avm_sandbox_url", AVMConstants.buildAVMStoreUrl(parentAvmPath));
      parameters.put("form_instance_data_file_name", formInstanceDataFileName);
      parameters.put("rendition_file_name", renditionFileName);
      parameters.put("parent_path", parentAvmPath);
      final FacesContext fc = FacesContext.getCurrentInstance();
      parameters.put("request_context_path", fc.getExternalContext().getRequestContextPath());
      return parameters;
   }
   
   /** utility function for creating a document */
   public Document newDocument()
   {
      return this.getDocumentBuilder().newDocument();
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
      throws SAXException,
      IOException
   {
      return this.parseXML(new ByteArrayInputStream(source.getBytes()));
   }
   
   /** utility function for parsing xml */
   public Document parseXML(final NodeRef nodeRef)
      throws SAXException,
      IOException
   {
      final ContentReader contentReader = 
         this.contentService.getReader(nodeRef, ContentModel.TYPE_CONTENT);
      final InputStream in = contentReader.getContentInputStream();
      return this.parseXML(in);
   }
   
   /** utility function for parsing xml */
   public Document parseXML(final File source)
      throws SAXException,
      IOException
   {
      return this.parseXML(new FileInputStream(source));
   }
   
   /** utility function for parsing xml */
   public Document parseXML(final InputStream source)
      throws SAXException,
      IOException
   {
      final DocumentBuilder db = this.getDocumentBuilder();
      
      final Document result = db.parse(source);
      source.close();
      return result;
   }

   private static String stripExtension(final String s)
   {
      return s.replaceAll("(.+)\\..*", "$1");
   }

   public DocumentBuilder getDocumentBuilder()
   {
      if (FormsService.documentBuilder == null)
      {
         try
         {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            FormsService.documentBuilder = dbf.newDocumentBuilder();
         }
         catch (ParserConfigurationException pce)
         {
            LOGGER.error(pce);
         }
      }
      return FormsService.documentBuilder;
   }
}
