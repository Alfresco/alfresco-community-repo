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
package org.alfresco.web.templating.xforms;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.alfresco.model.WCMModel;
import org.alfresco.repo.avm.AVMRemote;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.templating.*;
import org.alfresco.web.templating.extension.ExtensionFunctions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xpath.objects.XObject;
import org.apache.xml.utils.QName;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

public class XSLTOutputMethod
    implements TemplateOutputMethod
{
   private static final String ALFRESCO_NS = "http://www.alfresco.org/alfresco";
   private static final String ALFRESCO_NS_PREFIX = "alfresco";

   private static final Log LOGGER = LogFactory.getLog(XSLTOutputMethod.class);
   
   private final NodeRef nodeRef;
   private final NodeService nodeService;

   public XSLTOutputMethod(final NodeRef nodeRef,
                           final NodeService nodeService,
                           final ContentService contentService)
   {
      this.nodeRef = nodeRef;
      this.nodeService = nodeService;
   }

   //XXXarielb this is totally dirty - need to figure a better way to do this
   private static AVMRemote getAVMRemote()
   {
      final javax.faces.context.FacesContext fc = 
         javax.faces.context.FacesContext.getCurrentInstance();
      final org.springframework.web.context.WebApplicationContext wac = 
         org.springframework.web.jsf.FacesContextUtils.getRequiredWebApplicationContext(fc);
      return (AVMRemote)wac.getBean("avmRemote");
   }

   private static ExtensionFunctions getExtensionFunctions()
   {
      return new ExtensionFunctions(XSLTOutputMethod.getAVMRemote());
   }

   private static String toAVMPath(final ExpressionContext ec, String path)
      throws TransformerException
   {
      final XObject o = ec.getVariableOrParam(new QName("parent_path"));
      if (o == null)
         return null;
      String avmPath = o.toString();
      if (path != null && path.length() != 0 && path.charAt(0) == '/')
      {
         avmPath = avmPath.substring(0, 
                                     avmPath.indexOf(':') + 
                                     ('/' + AVMConstants.DIR_APPBASE + 
                                      '/' + AVMConstants.DIR_WEBAPPS).length() + 1);
      }
      return avmPath + (avmPath.endsWith("/")  ?  path :  '/' + path);
   }

   public static Document getXMLDocument(final ExpressionContext ec, final String path)
      throws TransformerException,
      IOException,
      SAXException
   {
      final ExtensionFunctions ef = XSLTOutputMethod.getExtensionFunctions();
      return ef.getXMLDocument(XSLTOutputMethod.toAVMPath(ec, path));
   }

   public static NodeIterator getXMLDocuments(final ExpressionContext ec, 
                                              final String templateTypeName, 
                                              String path)
      throws TransformerException,
      IOException,
      SAXException
   {
      final ExtensionFunctions ef = XSLTOutputMethod.getExtensionFunctions();
      path = XSLTOutputMethod.toAVMPath(ec, path);
      final Map<String, Document> resultMap = ef.getXMLDocuments(templateTypeName, path);
      LOGGER.debug("received " + resultMap.size() + " documents in " + path);
      final List<Map.Entry<String, Document>> documents = 
         new ArrayList<Map.Entry<String, Document>>(resultMap.entrySet());

      return new NodeIterator()
      {
         private int index = 0;
         private boolean detached = false;
         
         public void detach() 
         { 
            LOGGER.debug("detaching NodeIterator");
            resultMap.clear(); 
            documents.clear();
            this.detached = true;
         }
         
         public boolean getExpandEntityReferences() 
         { 
            return true; 
         }

         public NodeFilter getFilter() 
         { 
            return new NodeFilter()
            {
               public short acceptNode(final Node n)
               {
                  return NodeFilter.FILTER_ACCEPT;
               }
            };
         }

         public Node getRoot()
         {
            LOGGER.error("NodeIterator.getRoot() unexpectedly called");
            throw new UnsupportedOperationException();
         }

         public int getWhatToShow()
         {
            return NodeFilter.SHOW_ALL;
         }

         public Node nextNode()
            throws DOMException
         {
            LOGGER.debug("NodeIterator.nextNode(" + index + ")");
            if (this.detached)
               throw new DOMException(DOMException.INVALID_STATE_ERR, null);
            if (index == documents.size())
               return null;
            return this.getNodeAt(index++);
         }

         public Node previousNode()
            throws DOMException
         {
            LOGGER.debug("NodeIterator.previousNode(" + index + ")");
            if (this.detached)
               throw new DOMException(DOMException.INVALID_STATE_ERR, null);
            if (index == -1)
               return null;
            return this.getNodeAt(index--);
         }

         private Document getNodeAt(int index)
         {
            final Document d = documents.get(index).getValue();
            final Element documentEl = d.getDocumentElement();
            documentEl.setAttribute("xmlns:" + ALFRESCO_NS_PREFIX, ALFRESCO_NS); 
            documentEl.setAttributeNS(ALFRESCO_NS, 
                                      ALFRESCO_NS_PREFIX + ":file-name", 
                                      documents.get(index).getKey());
            return d;
         }
      };
   }

   private void addScript(final Document d)
   {
      final Element docEl = d.getDocumentElement();
      final String XALAN_NS = "http://xml.apache.org/xalan";
      final String XALAN_NS_PREFIX = "xalan";
      docEl.setAttribute("xmlns:" + XALAN_NS_PREFIX, XALAN_NS);
      docEl.setAttribute("xmlns:" + ALFRESCO_NS_PREFIX, ALFRESCO_NS); 
      
      final Element compEl = d.createElementNS(XALAN_NS, XALAN_NS_PREFIX + ":component");
      compEl.setAttribute("prefix", "alfresco");
      docEl.appendChild(compEl);

      final Element scriptEl = d.createElementNS(XALAN_NS, XALAN_NS_PREFIX + ":script");
      scriptEl.setAttribute("lang", "javaclass");
      scriptEl.setAttribute("src", XALAN_NS_PREFIX + "://" + this.getClass().getName());
      compEl.appendChild(scriptEl);
   }

   private void addParameters(final Map<String, String> parameters,
                              final Document xslDocument)
   {
      final Element docEl = xslDocument.getDocumentElement();
      final String XSL_NS = docEl.getNamespaceURI();
      final String XSL_NS_PREFIX = docEl.getPrefix();
      
      for (Map.Entry<String, String> e : parameters.entrySet())
      {
         final Element el = xslDocument.createElementNS(XSL_NS, XSL_NS_PREFIX + ":variable");
         el.setAttribute("name", e.getKey());
         el.appendChild(xslDocument.createTextNode(e.getValue()));
         docEl.insertBefore(el, docEl.getFirstChild());
      }
   }

   public void generate(final Document xmlContent,
                        final TemplateType tt,
                        final Map<String, String> parameters,
                        final Writer out)
      throws ParserConfigurationException,
      TransformerConfigurationException,
      TransformerException,
      SAXException,
      IOException
   {
      // XXXarielb - dirty - fix this
      final String sandBoxUrl = (String)parameters.get("avm_store_url");
      final TransformerFactory tf = TransformerFactory.newInstance();
      final TemplatingService ts = TemplatingService.getInstance();
      final Document xslDocument = ts.parseXML(this.nodeRef);
      this.addScript(xslDocument);
      this.addParameters(parameters, xslDocument);

      final DOMSource source = new DOMSource(xslDocument);
      final Templates templates = tf.newTemplates(source);
      final Transformer t = templates.newTransformer();
      t.setURIResolver(new URIResolver()
      {
         public Source resolve(final String href, final String base)
            throws TransformerException
         {
            URI uri = null;
            try
            {
               uri = new URI(sandBoxUrl + href);
            }
            catch (URISyntaxException e)
            {
               throw new TransformerException("unable to create uri " + sandBoxUrl + href, e);
            }
            try
            {
               LOGGER.debug("loading " + uri);
               final Document d = ts.parseXML(uri.toURL().openStream());
               LOGGER.debug("loaded " + ts.writeXMLToString(d));
               return new DOMSource(d);
            }
            catch (Exception e)
            {
               LOGGER.warn(e);
               throw new TransformerException("unable to load " + uri, e);
            }
         }
      });

      final StreamResult result = new StreamResult(out);
      try
      {
         t.transform(new DOMSource(xmlContent), result);
      }
      catch (TransformerException e)
      {
         LOGGER.error(e.getMessageAndLocation());
         throw e;
      }
   }

   public String getFileExtension()
   {
      return (String)
         this.nodeService.getProperty(this.nodeRef, 
                                      WCMModel.PROP_TEMPLATE_OUTPUT_METHOD_DERIVED_FILE_EXTENSION);
   }
}
