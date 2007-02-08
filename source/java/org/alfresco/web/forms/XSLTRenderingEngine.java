/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.forms;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.wcm.AVMConstants;
import org.alfresco.web.forms.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xpath.objects.XObject;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.apache.xml.utils.Constants;
import org.springframework.util.StringUtils;
//import org.apache.xml.utils.QName;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;
import org.apache.bsf.BSFManager;

/**
 * A rendering engine which uses xsl templates to render renditions of
 * form instance data.
 *
 * @author Ariel Backenroth
 */
public class XSLTRenderingEngine
   implements RenderingEngine
{

   /////////////////////////////////////////////////////////////////////////////

   public static class ProcessorMethodInvoker
   {
      private final static HashMap<String, TemplateProcessorMethod> PROCESSOR_METHODS =
         new HashMap<String, TemplateProcessorMethod>();
         
      public ProcessorMethodInvoker() { }

      private Object[] convertArguments(final Object[] arguments)
      {
         final List result = new LinkedList();
         for (int i = 0; i < arguments.length; i++)
         {
            LOGGER.debug("args[" + i + "] = " + arguments[i] + 
                         "(" + (arguments[i] != null 
                                ? arguments[i].getClass().getName() 
                                : "null") + ")");
            if (arguments[i] == null ||
                arguments[i] instanceof String ||
                arguments[i] instanceof Number)
            {
               result.add(arguments[i]);
            }
            else if (arguments[i] instanceof DTMNodeProxy)
            {
               result.add(((DTMNodeProxy)arguments[i]).getStringValue());
            }
            else if (arguments[i] instanceof Node)
            {
               LOGGER.debug("node type is " + ((Node)arguments[i]).getNodeType() +
                            " content " + ((Node)arguments[i]).getTextContent());
               result.add(((Node)arguments[i]).getNodeValue());
            }
            else if (arguments[i] instanceof NodeIterator)
            {
               Node n = ((NodeIterator)arguments[i]).nextNode();
               while (n != null)
               {
                  LOGGER.debug("iterated to node " + n + " type " + n.getNodeType() +
                               " value " + n.getNodeValue() +
                               " tc " + n.getTextContent() +
                               " nn " + n.getNodeName() +
                               " sv " + ((org.apache.xml.dtm.ref.DTMNodeProxy)n).getStringValue());
                  if (n instanceof DTMNodeProxy)
                  {
                     result.add(((DTMNodeProxy)n).getStringValue());
                  }
                  else
                  {
                     result.add(n);
                  }
                  n = ((NodeIterator)arguments[i]).nextNode();
               }
            }
            else
            {
               throw new IllegalArgumentException("unable to convert argument " + arguments[i]);
            }
         }
         
         return result.toArray(new Object[result.size()]);
      }

      public Object invokeMethod(final String id, Object[] arguments)
         throws Exception
      {
         if (!PROCESSOR_METHODS.containsKey(id))
         {
            throw new NullPointerException("unable to find method " + id);
         }

         final TemplateProcessorMethod method = PROCESSOR_METHODS.get(id);
         arguments = this.convertArguments(arguments);
         LOGGER.debug("invoking " + id + " with " + arguments.length);

         Object result = method.exec(arguments);
         LOGGER.debug(id + " returned a " + result);
         if (result == null)
         {
            return null;
         }
         else if (result.getClass().isArray() &&
                  Node.class.isAssignableFrom(result.getClass().getComponentType()))
         {
            LOGGER.debug("converting " + result + " to a node iterator");
            final Node[] array = (Node[])result;
            return new NodeIterator()
            {
               private int index = 0;
               private boolean detached = false;
               
               public void detach() 
               { 
                  if (LOGGER.isDebugEnabled())
                     LOGGER.debug("detaching NodeIterator");
                  this.detached = true;
               }
               
               public boolean getExpandEntityReferences() { return true; }
               public int getWhatToShow() { return NodeFilter.SHOW_ALL; }
               
               public Node getRoot()
               {
                  return (array.length == 0 
                          ? null 
                          : array[0].getOwnerDocument().getDocumentElement());
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
               
               public Node nextNode()
                  throws DOMException
               {
                  if (LOGGER.isDebugEnabled())
                     LOGGER.debug("NodeIterator.nextNode(" + index + ")");
                  if (this.detached)
                     throw new DOMException(DOMException.INVALID_STATE_ERR, null);
                  return index == array.length ? null : array[index++];
               }
               
               public Node previousNode()
                  throws DOMException
               {
                  if (LOGGER.isDebugEnabled())
                     LOGGER.debug("NodeIterator.previousNode(" + index + ")");
                  if (this.detached)
                     throw new DOMException(DOMException.INVALID_STATE_ERR, null);
                  return index == -1 ? null : array[index--];
               }
            };
         }
         else if (result instanceof String ||
                  result instanceof Number ||
                  result instanceof Node)
         {
            LOGGER.debug("returning " + result + " as is");
            return result;
         }
         else
         {
            throw new IllegalArgumentException("unable to convert " + result.getClass().getName());
         }
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   private static final Log LOGGER = LogFactory.getLog(XSLTRenderingEngine.class);

   public static final QName PROP_URI_RESOLVER_BASE_URI = 
      QName.createQName(NamespaceService.ALFRESCO_URI, "xslt_resolver_base_uri");

   public XSLTRenderingEngine() { }

   public String getName() { return "XSLT"; }

   public String getDefaultTemplateFileExtension() { return "xsl"; }

   /**
    * Adds a script element to the xsl which makes static methods on this
    * object available to the xsl tempalte.
    *
    * @param xslTemplate the xsl template
    */
   protected List<String> addScripts(final Map<QName, Object> model,
                                     final Document xslTemplate)
   {
      final Map<QName, List<Map.Entry<QName, Object>>> methods = 
         new HashMap<QName, List<Map.Entry<QName, Object>>>();
      for (final Map.Entry<QName, Object> entry : model.entrySet())
      {
         if (entry.getValue() instanceof TemplateProcessorMethod)
         {
            final String prefix = QName.splitPrefixedQName(entry.getKey().toPrefixString())[0];
            final QName qn = QName.createQName(entry.getKey().getNamespaceURI(),
                                               prefix);
            if (!methods.containsKey(qn))
            {
               methods.put(qn, new LinkedList());
            }
            methods.get(qn).add(entry);
         }
      }

      final Element docEl = xslTemplate.getDocumentElement();
      final String XALAN_NS = Constants.S_BUILTIN_EXTENSIONS_URL;
      final String XALAN_NS_PREFIX = "xalan";
      docEl.setAttribute("xmlns:" + XALAN_NS_PREFIX, XALAN_NS);

      final Set<String> excludePrefixes = new HashSet<String>();
      if (docEl.hasAttribute("exclude-result-prefixes"))
      {
         excludePrefixes.addAll(Arrays.asList(docEl.getAttribute("exclude-result-prefixes").split(" ")));
      }
      excludePrefixes.add(XALAN_NS_PREFIX);

      final List<String> result = new LinkedList<String>();
      for (QName ns : methods.keySet())
      {
         final String prefix = ns.getLocalName();
         docEl.setAttribute("xmlns:" + prefix, ns.getNamespaceURI()); 
         excludePrefixes.add(prefix);

         final Element compEl = xslTemplate.createElementNS(XALAN_NS, 
                                                            XALAN_NS_PREFIX + ":component");
         compEl.setAttribute("prefix", prefix);
         docEl.appendChild(compEl);
         String functionNames = null;
         final Element scriptEl = xslTemplate.createElementNS(XALAN_NS, 
                                                              XALAN_NS_PREFIX + ":script");
         scriptEl.setAttribute("lang", "javascript");
         final StringBuilder js = 
            new StringBuilder("var _xsltp_invoke = java.lang.Class.forName('" + ProcessorMethodInvoker.class.getName() + 
                              "').newInstance();\n" +
                              "function _xsltp_to_java_array(js_array) {\n" +
                              "var java_array = java.lang.reflect.Array.newInstance(java.lang.Object, js_array.length);\n" +
                              "for (var i = 0; i < js_array.length; i++) { java_array[i] = js_array[i]; }\n" +
                              "return java_array; }\n");
         for (final Map.Entry<QName, Object> entry : methods.get(ns))
         {
            if (functionNames == null)
            {
               functionNames = entry.getKey().getLocalName();
            }
            else
            {
               functionNames += " " + entry.getKey().getLocalName();
            }
            final String id = entry.getKey().getLocalName() + entry.getValue().hashCode();
            js.append("function " + entry.getKey().getLocalName() + 
                      "() { return _xsltp_invoke.invokeMethod('" + id +
                      "', _xsltp_to_java_array(arguments)); }\n");
            ProcessorMethodInvoker.PROCESSOR_METHODS.put(id, (TemplateProcessorMethod)entry.getValue());
            result.add(id);
         }
         LOGGER.debug("generated JavaScript bindings:\n" + js);
         scriptEl.appendChild(xslTemplate.createTextNode(js.toString()));
         compEl.setAttribute("functions", functionNames);
         compEl.appendChild(scriptEl);
      }
      docEl.setAttribute("exclude-result-prefixes",
                         StringUtils.arrayToDelimitedString(excludePrefixes.toArray(new String[excludePrefixes.size()]), " "));
      return result;
   }

   /**
    * Adds the specified parameters to the xsl template as variables within the 
    * alfresco namespace.
    *
    * @param parameters the variables to place within the xsl template
    * @param xslTemplate the xsl template
    */
   protected void addParameters(final Map<QName, Object> model,
                                final Document xslTemplate)
   {
      final Element docEl = xslTemplate.getDocumentElement();
      final String XSL_NS = docEl.getNamespaceURI();
      final String XSL_NS_PREFIX = docEl.getPrefix();
      
      for (Map.Entry<QName, Object> e : model.entrySet())
      {
         if (RenderingEngine.ROOT_NAMESPACE.equals(e.getKey()))
         {
            continue;
         }
         final Element el = xslTemplate.createElementNS(XSL_NS, XSL_NS_PREFIX + ":variable");
         el.setAttribute("name",  e.getKey().toPrefixString());
         final Object o = e.getValue();
         if (o instanceof String || o instanceof Number || o instanceof Boolean)
         {
            el.appendChild(xslTemplate.createTextNode(o.toString()));
            docEl.insertBefore(el, docEl.getFirstChild());
         }
      }
   }
   
   protected Source getXMLSource(final Map<QName, Object> model)
   {
      if (!model.containsKey(RenderingEngine.ROOT_NAMESPACE))
      {
         return null;
      }
      final Object o = model.get(RenderingEngine.ROOT_NAMESPACE);
      if (!(o instanceof Document))
      {
         throw new IllegalArgumentException("expected root namespace object to be a  " + Document.class.getName() +
                                            ".  found a " + o.getClass().getName());
      }
      return new DOMSource((Document)o);
   }

   public void render(final Map<QName, Object> model,
                      final RenderingEngineTemplate ret,
                      final OutputStream out)
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException
   {
      this.render(model, ret, new StreamResult(out));
   }

   public void render(final Map<QName, Object> model,
                      final RenderingEngineTemplate ret,
                      final Result result)
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException
   {
      System.setProperty("org.apache.xalan.extensions.bsf.BSFManager",
                         BSFManager.class.getName());
      Document xslTemplate = null;
      try
      {
         xslTemplate = XMLUtil.parse(ret.getInputStream());
      }
      catch (final SAXException sax)
      {
         throw new RenderingEngine.RenderingException(sax);
      }
      this.addScripts(model, xslTemplate);
      this.addParameters(model, xslTemplate);

      Source xmlSource = this.getXMLSource(model);

      Transformer t = null;
      try 
      {
         final TransformerFactory tf = TransformerFactory.newInstance();
         t = tf.newTransformer(new DOMSource(xslTemplate));
         t.setParameter("versionParam", "2.0");
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
         public Source resolve(final String href, String base)
            throws TransformerException
         {
            LOGGER.debug("request to resolve href " + href +
                         " using base " + base);
            if (model.containsKey(PROP_URI_RESOLVER_BASE_URI))
            {
               base = (String)model.get(PROP_URI_RESOLVER_BASE_URI);
               LOGGER.debug("overriding base with " + base);
            }
               
            URI uri = null;
            try
            {
               uri = new URI(base + href);
            }
            catch (URISyntaxException e)
            {
               throw new TransformerException("unable to create uri " + base + href, 
                                              e);
            }
            try
            {
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("loading " + uri);
               final Document d = XMLUtil.parse(uri.toURL().openStream());
               if (LOGGER.isDebugEnabled())
                  LOGGER.debug("loaded " + XMLUtil.toString(d));
               return new DOMSource(d);
            }
            catch (Exception e)
            {
               throw new TransformerException("unable to load " + uri, e);
            }
         }
      });

      try
      {
         t.transform(xmlSource, result);
      }
      catch (TransformerException te)
      {
         LOGGER.error(te.getMessageAndLocation());
         throw new RenderingEngine.RenderingException(te);
      }
      catch (Exception e)
      {
         LOGGER.error("unexpected error " + e);
         throw new RenderingEngine.RenderingException(e);
      }
   }
}
