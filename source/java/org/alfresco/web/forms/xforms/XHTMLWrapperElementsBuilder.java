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
package org.alfresco.web.forms.xforms;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.chiba.xml.ns.NamespaceConstants;
import java.util.*;

/**
 * XHTML implementation of WrapperElementsBuilder: allows to wrap the XForm document in XHTML tags.
 *
 * @author Sophie Ramel
 */
public class XHTMLWrapperElementsBuilder 
   implements WrapperElementsBuilder 
{

   ////////////////////////////////////////////////////////////////////////////

   private static class Link
   {
      public final String href;
      public final String type;
      public final String rel;

      public Link(final String href, final String type, final String rel)
      {
         this.href = href;
         this.type = type;
         this.rel = rel;
      }
   }

   ////////////////////////////////////////////////////////////////////////////

   private static class Meta
   {
      public final String httpEquiv;
      public final String name;
      public final String content;
      public final String scheme;

      public Meta(final String httpEquiv,
                  final String name,
                  final String content,
                  final String scheme)
      {
         this.httpEquiv = httpEquiv;
         this.name = name;
         this.content = content;
         this.scheme = scheme;
      }
   }

   ////////////////////////////////////////////////////////////////////////////

   private String title;
   private final Collection<Link> links = new LinkedList<Link>();
   private final Collection<Meta> meta = new LinkedList<Meta>();
   private final HashMap<String, String> namespaces = 
      new HashMap<String, String>();

   /**
    * Creates a new instance of XHTMLWrapperElementsBuilder
    */
   public XHTMLWrapperElementsBuilder() { }

   /**
    * add a tag "title" in the header of the HTML document
    */
   public void setTitle(String title) 
   {
      this.title = title;
   }

   /**
    * add a tag "link" in the header of the HTML document
    *
    * @param href the "href" parameter of the "link" tag
    * @param type the "type" parameter of the "link" tag
    * @param rel  the "rel" parameter of the "link" tag
    */
   public void addLink(final String href, 
                       final String type, 
                       final String rel) 
   {
      links.add(new Link(href, type, rel));
   }

   /**
    * add a tag "meta" in the header of the HTML document
    *
    * @param http_equiv the "http-equiv" parameter of the "META" tag
    * @param name       the "name" parameter of the "META" tag
    * @param content    the "content" parameter of the "META" tag
    * @param scheme     the "scheme" parameter of the "META" tag
    */
   public void addMeta(final String httpEquiv,
                       final String name,
                       final String content,
                       final String scheme) 
   {
      meta.add(new Meta(httpEquiv, name, content, scheme));
   }

   public void addNamespaceDeclaration(final String prefix,
                                       final String url) 
   {
      namespaces.put(prefix, url);
   }

   /**
    * create the wrapper element of the different controls
    *
    * @param controlElement the control element (input, select, repeat, group, ...)
    * @return the wrapper element, already containing the control element
    */
   public Element createControlsWrapper(final Element controlElement) 
   {
      return controlElement;
   }

   /**
    * creates the global enveloppe of the resulting document, and puts it in the document
    *
    * @return the enveloppe
    */
   public Element createEnvelope(Document doc) 
   {
      final Element html = doc.createElementNS(NamespaceConstants.XHTML_NS, 
                                         NamespaceConstants.XHTML_PREFIX + ":html");
      //set namespace attribute
      html.setAttributeNS(NamespaceConstants.XMLNS_NS,
                          NamespaceConstants.XMLNS_PREFIX + ':' + NamespaceConstants.XHTML_PREFIX,
                          NamespaceConstants.XHTML_NS);
      doc.appendChild(html);

      //other namespaces
      for (String prefix : this.namespaces.keySet())
      {
         html.setAttributeNS(NamespaceConstants.XMLNS_NS,
                             NamespaceConstants.XMLNS_PREFIX + ":" + prefix,
                             this.namespaces.get(prefix));

      }
      return html;
   }

   /**
    * create the element that will contain the content of the group (or repeat) element
    *
    * @param groupElement the group or repeat element
    * @return the wrapper element
    */
   public Element createGroupContentWrapper(Element groupElement) 
   {
      return groupElement;
   }

   /**
    * create the wrapper element of the form
    *
    * @param enveloppeElement the form element (chiba:form or other)
    * @return the wrapper element
    */
   public Element createFormWrapper(Element enveloppeElement) 
   {
      Document doc = enveloppeElement.getOwnerDocument();
      Element body = doc.createElementNS(NamespaceConstants.XHTML_NS, NamespaceConstants.XHTML_PREFIX + ":body");
      //body.appendChild(formElement);
      enveloppeElement.appendChild(body);
      return body;
   }

   /**
    * create the wrapper element of the xforms:model element
    *
    * @param modelElement the xforms:model element
    * @return the wrapper element, already containing the model
    */
   public Element createModelWrapper(final Element modelElement) 
   {
      Document doc = modelElement.getOwnerDocument();
      Element head = doc.createElementNS(NamespaceConstants.XHTML_NS,
                                         NamespaceConstants.XHTML_PREFIX + ":head");
      head.appendChild(modelElement);

      //eventually add other info
      if (title != null && title.length() != 0) 
      {
         final Element title_el = doc.createElementNS(NamespaceConstants.XHTML_NS,
                                                      NamespaceConstants.XHTML_PREFIX + ":title");
         title_el.appendChild(doc.createTextNode(title));
         head.appendChild(title_el);
      }

      for (Meta m : this.meta) 
      {
         final Element meta_el = doc.createElementNS(NamespaceConstants.XHTML_NS,
                                                     NamespaceConstants.XHTML_PREFIX + ":META");
         head.appendChild(meta_el);
	    
         //attributes
         if (m.httpEquiv != null && m.httpEquiv.length() != 0)
         {
            meta_el.setAttributeNS(NamespaceConstants.XHTML_NS,
                                   NamespaceConstants.XHTML_PREFIX + ":http-equiv", 
                                   m.httpEquiv);
         }
         if (m.name != null && m.name.length() != 0)
         {
            meta_el.setAttributeNS(NamespaceConstants.XHTML_NS, 
                                   NamespaceConstants.XHTML_PREFIX + ":name", 
                                   m.name);
         }
         if (m.content != null && m.content.length() != 0)
         {
            meta_el.setAttributeNS(NamespaceConstants.XHTML_NS, 
                                   NamespaceConstants.XHTML_PREFIX + ":content", 
                                   m.content);
         }
         if (m.scheme != null && m.scheme.length() != 0)
         {
            meta_el.setAttributeNS(NamespaceConstants.XHTML_NS, 
                                   NamespaceConstants.XHTML_PREFIX + ":scheme", 
                                   m.scheme);
         }
      }

      for (Link l : this.links)
      {
         final Element link_el = doc.createElementNS(NamespaceConstants.XHTML_NS, 
                                                     NamespaceConstants.XHTML_PREFIX + ":LINK");
         head.appendChild(link_el);

         //attributes
         if (l.href != null && l.href.length() != 0)
         {
            link_el.setAttributeNS(NamespaceConstants.XHTML_NS, 
                                   NamespaceConstants.XHTML_PREFIX + ":href", 
                                   l.href);
         }
         if (l.type != null && l.type.length() != 0)
         {
            link_el.setAttributeNS(NamespaceConstants.XHTML_NS, 
                                   NamespaceConstants.XHTML_PREFIX + ":type", 
                                   l.type);
         }
         if (l.rel != null && l.rel.length() != 0)
         {
            link_el.setAttributeNS(NamespaceConstants.XHTML_NS, 
                                   NamespaceConstants.XHTML_PREFIX + ":rel", 
                                   l.rel);
        
         }
      }
      return head;
   }
}
