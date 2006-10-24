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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
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
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.forms.FormsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xpath.objects.XObject;
import org.apache.xml.utils.QName;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;
import org.apache.bsf.BSFManager;

/**
 * A rendering engine which uses xsl templates to generate renditions of
 * form instance data.
 *
 * @author Ariel Backenroth
 */
public class XSLTRenderingEngine
   extends AbstractRenderingEngine
{

   private static final Log LOGGER = LogFactory.getLog(XSLTRenderingEngine.class);

   public XSLTRenderingEngine(final NodeRef nodeRef,
                              final NodeService nodeService,
                              final ContentService contentService)
   {
      super(nodeRef, nodeService, contentService);
   }

   private static String toAVMPath(final ExpressionContext ec, String path)
      throws TransformerException
   {
      final XObject o = ec.getVariableOrParam(new QName(ALFRESCO_NS, ALFRESCO_NS_PREFIX, "parent_path"));
      return o == null ? null : XSLTRenderingEngine.toAVMPath(o.toString(), path);
   }

   public static Node parseXMLDocument(final ExpressionContext ec, final String path)
      throws TransformerException,
      IOException,
      SAXException
   {
      final FormDataFunctions ef = XSLTRenderingEngine.getFormDataFunctions();
      final Document d = ef.parseXMLDocument(XSLTRenderingEngine.toAVMPath(ec, path));
      return d != null ? d.getDocumentElement() : null;
   }

   public static NodeIterator parseXMLDocuments(final ExpressionContext ec, 
                                                final String formName)
      throws TransformerException,
      IOException,
      SAXException
   {
      return XSLTRenderingEngine.parseXMLDocuments(ec, formName, "");
   }

   public static NodeIterator parseXMLDocuments(final ExpressionContext ec, 
                                                final String formName, 
                                                String path)
      throws TransformerException,
      IOException,
      SAXException
   {
      final FormDataFunctions ef = XSLTRenderingEngine.getFormDataFunctions();
      path = XSLTRenderingEngine.toAVMPath(ec, path);
      final Map<String, Document> resultMap = ef.parseXMLDocuments(formName, path);
      LOGGER.debug("received " + resultMap.size() + " documents in " + path);

      // create a root document for rooting all the results.  we do this
      // so that each document root element has a common parent node
      // and so that xpath axes work properly
      final FormsService fs = FormsService.getInstance();
      final DocumentBuilder documentBuilder = fs.getDocumentBuilder();
      final Document rootNodeDocument = documentBuilder.newDocument();
      final Element rootNodeDocumentEl = 
         rootNodeDocument.createElementNS(ALFRESCO_NS,
                                          ALFRESCO_NS_PREFIX + ":file_list");
      rootNodeDocumentEl.setAttribute("xmlns:" + ALFRESCO_NS_PREFIX, ALFRESCO_NS); 
      rootNodeDocument.appendChild(rootNodeDocumentEl);

      final List<Node> documents = new ArrayList<Node>(resultMap.size());
      for (Map.Entry<String, Document> mapEntry : resultMap.entrySet())
      {
         final Element documentEl = mapEntry.getValue().getDocumentElement();
         documentEl.setAttributeNS(ALFRESCO_NS, 
                                   ALFRESCO_NS_PREFIX + ":file_name", 
                                   mapEntry.getKey());
         final Node n = rootNodeDocument.importNode(documentEl, true);
         documents.add(n);
         rootNodeDocumentEl.appendChild(n);
      }

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
            return rootNodeDocumentEl;
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
            return documents.get(index++);
         }

         public Node previousNode()
            throws DOMException
         {
            LOGGER.debug("NodeIterator.previousNode(" + index + ")");
            if (this.detached)
               throw new DOMException(DOMException.INVALID_STATE_ERR, null);
            if (index == -1)
               return null;
            return documents.get(index--);
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
         el.setAttribute("name", ALFRESCO_NS_PREFIX + ':' + e.getKey());
         el.appendChild(xslDocument.createTextNode(e.getValue()));
         docEl.insertBefore(el, docEl.getFirstChild());
      }
   }

   public void generate(final Document xmlContent,
                        final Map<String, String> parameters,
                        final Writer out)
      throws IOException,
      RenderingEngine.RenderingException
   {
      System.setProperty("org.apache.xalan.extensions.bsf.BSFManager",
                         BSFManager.class.getName());
      final TransformerFactory tf = TransformerFactory.newInstance();
      final FormsService ts = FormsService.getInstance();
      Document xslDocument = null;
      try
      {
         xslDocument = ts.parseXML(this.getNodeRef());
      }
      catch (final SAXException sax)
      {
         throw new RenderingEngine.RenderingException(sax);
      }
      this.addScript(xslDocument);
      this.addParameters(parameters, xslDocument);

      final DOMSource source = new DOMSource(xslDocument);

      Transformer t = null;
      try 
      {
         final Templates templates = tf.newTemplates(source);
         t = templates.newTransformer();
      }
      catch (TransformerConfigurationException tce)
      {
         LOGGER.error(tce);
         throw new RenderingEngine.RenderingException(tce);
      }

      // create a uri resolver to resolve document() calls to the virtualized
      // web application
      t.setURIResolver(new URIResolver()
      {
         public Source resolve(final String href, final String base)
            throws TransformerException
         {
            // XXXarielb - dirty - fix this
            final String sandBoxUrl = (String)parameters.get("avm_sandbox_url");

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
         throw new RenderingEngine.RenderingException(e);
      }
   }
}
