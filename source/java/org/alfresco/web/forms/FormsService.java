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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Stack;

import javax.faces.context.FacesContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMConstants;
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
   
   private static final RenderingEngine[] RENDERING_ENGINES = new RenderingEngine[] 
   {
      new FreeMarkerRenderingEngine(),
      new XSLTRenderingEngine(),
      new XSLFORenderingEngine()
   };
   
   private final ContentService contentService;
   private final NodeService nodeService;
   private final NamespaceService namespaceService;
   private final SearchService searchService;
   private final AVMService avmService;

   private NodeRef contentFormsNodeRef;
   
   /** instantiated using spring */
   public FormsService(final ContentService contentService,
                       final NodeService nodeService,
                       final NamespaceService namespaceService,
                       final SearchService searchService,
                       final AVMService avmService)
   {
      this.contentService = contentService;
      this.nodeService = nodeService;
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
    * Provides all registered rendering engines.
    */
   public RenderingEngine[] getRenderingEngines()
   {
      return FormsService.RENDERING_ENGINES;
   }

   /**
    * Returns the rendering engine with the given name.
    *
    * @param name the name of the rendering engine.
    *
    * @return the rendering engine or <tt>null</tt> if not found.
    */
   public RenderingEngine getRenderingEngine(final String name)
   {
      for (RenderingEngine re : this.getRenderingEngines())
      {
         if (re.getName().equals(name))
         {
            return re;
         }
      }
      return null;
   }

   public RenderingEngine guessRenderingEngine(final String fileName)
   {
      for (RenderingEngine re : this.getRenderingEngines())
      {
         if (fileName.endsWith(re.getDefaultTemplateFileExtension()))
         {
            return re;
         }
      }
      return null;
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
      final SearchParameters sp = new SearchParameters();
      sp.addStore(Repository.getStoreRef());
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      sp.setQuery("ASPECT:\"" + WCMAppModel.ASPECT_FORM + "\"");
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("running query [" + sp.getQuery() + "]");
      final ResultSet rs = this.searchService.query(sp);
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("received " + rs.length() + " results");
      final Collection<Form> result = new LinkedList<Form>();
      for (ResultSetRow row : rs)
      {
         final NodeRef nodeRef = row.getNodeRef();
         result.add(this.getForm(nodeRef));
      }
      return result;
   }
   
   /** 
    * return the form by name or <tt>null</tt> if not found 
    *
    * @return the form by name or <tt>null</tt> if not found 
    */
   public Form getForm(final String name)
   {
      final SearchParameters sp = new SearchParameters();
      sp.addStore(Repository.getStoreRef());
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      sp.setQuery("ASPECT:\"" + WCMAppModel.ASPECT_FORM + 
                  "\" AND @" + Repository.escapeQName(ContentModel.PROP_NAME) + 
                  ":\"" + name + "\"");
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("running query [" + sp.getQuery() + "]");
      final ResultSet rs = this.searchService.query(sp);
      NodeRef result = null;
      for (ResultSetRow row : rs)
      {
         final NodeRef nr = row.getNodeRef();
         if (this.nodeService.getProperty(nr, ContentModel.PROP_NAME).equals(name))
         {
            result = nr;
            break;
         }
      }
      if (result == null && LOGGER.isDebugEnabled())
         LOGGER.debug("unable to find template type " + name);
      return result != null ? this.getForm(result) : null;
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
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("loading form for " + nodeRef);
      final Form result = new FormImpl(nodeRef);
      if (LOGGER.isDebugEnabled())
         LOGGER.debug("loaded form " + result);
      return result;
   }
   
   /**
    * Generates renditions for the provided formInstanceData.
    *
    * @param formInstanceDataNodeRef the noderef containing the form instance data
    */
   public List<Rendition> generateRenditions(final FormInstanceData formInstanceData)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException
   {
      final Form form = formInstanceData.getForm();

      final Document formInstanceDataDocument = this.parseXML(formInstanceData.getNodeRef());

      final String formInstanceDataFileName = formInstanceData.getName();
      final String formInstanceDataAvmPath = formInstanceData.getPath();
      LOGGER.debug("generating renditions for " + formInstanceDataAvmPath);

      final List<Rendition> result = new LinkedList<Rendition>();
      for (RenderingEngineTemplate ret : form.getRenderingEngineTemplates())
      {
         // get the node ref of the node that will contain the content
         final String renditionAvmPath = ret.getOutputPathForRendition(formInstanceData);
         final String parentAVMPath = AVMNodeConverter.SplitBase(renditionAvmPath)[0];
         this.makeAllDirectories(parentAVMPath);
         final OutputStream out = this.avmService.createFile(parentAVMPath,
                                                             AVMNodeConverter.SplitBase(renditionAvmPath)[1]);
         
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("Created file node for file: " + renditionAvmPath);

         final HashMap<String, String> parameters =
            this.getRenderingEngineParameters(formInstanceDataAvmPath,
                                              renditionAvmPath);
         ret.getRenderingEngine().render(formInstanceDataDocument, ret, parameters, out);
         out.close();
            
         final NodeRef renditionNodeRef = AVMNodeConverter.ToNodeRef(-1, renditionAvmPath);
         final Rendition rendition = new RenditionImpl(renditionNodeRef);
         form.registerFormInstanceData(renditionNodeRef);
         ret.registerRendition(rendition, formInstanceData);

         Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
         props.put(ContentModel.PROP_TITLE, AVMNodeConverter.SplitBase(renditionAvmPath)[1]);
         
         final ResourceBundle bundle = Application.getBundle(FacesContext.getCurrentInstance());
         props.put(ContentModel.PROP_DESCRIPTION, 
                   MessageFormat.format(bundle.getString("default_rendition_description"), 
                                        ret.getTitle(),
                                        AVMConstants.getSandboxRelativePath(renditionAvmPath)));
         nodeService.addAspect(renditionNodeRef, ContentModel.ASPECT_TITLED, props);
         result.add(rendition);
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("generated " + renditionAvmPath + " using " + ret);
      }
      return result;
   }
   
   /**
    * Regenerates all renditions of the provided form instance data.
    *
    * @param formInstanceDataNodeRef the node ref containing the form instance data.
    */
   public List<Rendition> regenerateRenditions(final FormInstanceData formInstanceData)
      throws IOException,
      SAXException,
      RenderingEngine.RenderingException
   {
      final Form form = formInstanceData.getForm();
         
      final Document formInstanceDataDocument = this.parseXML(formInstanceData.getNodeRef());
      final String formInstanceDataFileName = formInstanceData.getName();

      // other parameter values passed to rendering engine
      final String formInstanceDataAvmPath = formInstanceData.getPath();
      LOGGER.debug("regenerating renditions for " + formInstanceDataAvmPath);
      final List<Rendition> result = new LinkedList<Rendition>();
      for (RenderingEngineTemplate ret : form.getRenderingEngineTemplates())
      {
         final String renditionAvmPath = ret.getOutputPathForRendition(formInstanceData);

         if (LOGGER.isDebugEnabled())
            LOGGER.debug("regenerating file node for : " + formInstanceDataFileName + 
                         " (" + formInstanceData.toString() + 
                         ") to " + renditionAvmPath);
            
         // get a writer for the content and put the file
         OutputStream out = null;
         try
         {
            out = this.avmService.getFileOutputStream(renditionAvmPath);
         }
         catch (AVMNotFoundException e)
         {
            out = this.avmService.createFile(AVMNodeConverter.SplitBase(renditionAvmPath)[0], 
                                             AVMNodeConverter.SplitBase(renditionAvmPath)[1]);
         }

         final HashMap<String, String> parameters =
            this.getRenderingEngineParameters(formInstanceDataAvmPath, renditionAvmPath);
         ret.getRenderingEngine().render(formInstanceDataDocument, ret, parameters, out);
         out.close();

         final NodeRef renditionNodeRef = AVMNodeConverter.ToNodeRef(-1, renditionAvmPath);
         result.add(new RenditionImpl(renditionNodeRef));
         if (LOGGER.isDebugEnabled())
            LOGGER.debug("regenerated " + renditionAvmPath + " using " + ret);
      }
      return result;
   }

   private static HashMap<String, String> getRenderingEngineParameters(final String formInstanceDataAvmPath,
                                                                       final String renditionAvmPath)
   {
      final HashMap<String, String> parameters = new HashMap<String, String>();      
      parameters.put("avm_sandbox_url", AVMConstants.buildStoreUrl(formInstanceDataAvmPath));
      parameters.put("form_instance_data_file_name", AVMNodeConverter.SplitBase(formInstanceDataAvmPath)[1]);
      parameters.put("rendition_file_name", AVMNodeConverter.SplitBase(renditionAvmPath)[1]);
      parameters.put("parent_path", AVMNodeConverter.SplitBase(formInstanceDataAvmPath)[0]);
      final FacesContext fc = FacesContext.getCurrentInstance();
      parameters.put("request_context_path", fc.getExternalContext().getRequestContextPath());
      return parameters;
   }

   // XXXarielb relocate
   public void makeAllDirectories(final String avmDirectoryPath)
   {
      LOGGER.debug("mkdir -p " + avmDirectoryPath);
      String s = avmDirectoryPath;
      final Stack<String[]> dirNames = new Stack<String[]>();
      while (s != null)
      {
         try
         {
            if (this.avmService.lookup(-1, s) != null)
            {
               LOGGER.debug("path " + s + " exists");
               break;
            }
         }
         catch (AVMNotFoundException avmfe)
         {
         }
         final String[] sb = AVMNodeConverter.SplitBase(s);
         s = sb[0];
         LOGGER.debug("pushing " + sb[1]);
         dirNames.push(sb);
      }

      while (!dirNames.isEmpty())
      {
         final String[] sb = dirNames.pop();
         LOGGER.debug("creating " + sb[1] + " in " + sb[0]);
         this.avmService.createDirectory(sb[0], sb[1]);
      }
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
