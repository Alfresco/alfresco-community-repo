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
package org.alfresco.web.templating.xforms.schemabuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * XHTML implementation of WrapperElementsBuilder: allows to wrap the XForm document in XHTML tags.
 *
 * @author Sophie Ramel
 */
public class XHTMLWrapperElementsBuilder implements WrapperElementsBuilder {

    private final static String XHTML_NS = "http://www.w3.org/1999/xhtml";
    private final static String XHTML_PREFIX = "xhtml";
    //    private final static String XHTML_NS = "http://www.w3.org/2002/06/xhtml2";
    //    private final static String XHTML_PREFIX = "html";

    private String title;
    private final Vector links = new Vector();
    private final Vector meta = new Vector();
    private final Hashtable namespaces = new Hashtable();

    /**
     * Creates a new instance of XHTMLWrapperElementsBuilder
     */
    public XHTMLWrapperElementsBuilder() { }

    /**
     * add a tag "title" in the header of the HTML document
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * add a tag "link" in the header of the HTML document
     *
     * @param href the "href" parameter of the "link" tag
     * @param type the "type" parameter of the "link" tag
     * @param rel  the "rel" parameter of the "link" tag
     */
    public void addLink(String href, String type, String rel) {
        String[] l = { href, type, rel };
        links.add(l);
    }

    /**
     * add a tag "meta" in the header of the HTML document
     *
     * @param http_equiv the "http-equiv" parameter of the "META" tag
     * @param name       the "name" parameter of the "META" tag
     * @param content    the "content" parameter of the "META" tag
     * @param scheme     the "scheme" parameter of the "META" tag
     */
    public void addMeta(String http_equiv,
                        String name,
                        String content,
                        String scheme) {
        String[] s = new String[] { http_equiv, name, content, scheme};
        meta.add(s);
    }

    public void addNamespaceDeclaration(String prefix, String url) {
        namespaces.put(prefix, url);
    }

    /**
     * create the wrapper element of the different controls
     *
     * @param controlElement the control element (input, select, repeat, group, ...)
     * @return the wrapper element, already containing the control element
     */
    public Element createControlsWrapper(Element controlElement) {
        return controlElement;
    }

    /**
     * creates the global enveloppe of the resulting document, and puts it in the document
     *
     * @return the enveloppe
     */
    public Element createEnvelope(Document doc) {
        Element html = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":html");
        //set namespace attribute
        html.setAttributeNS(SchemaFormBuilder.XMLNS_NAMESPACE_URI,
                "xmlns:" + XHTML_PREFIX,
                XHTMLWrapperElementsBuilder.XHTML_NS);
        doc.appendChild(html);

        //other namespaces
        Enumeration enumeration = namespaces.keys();
        while (enumeration.hasMoreElements()) {
            String prefix = (String) enumeration.nextElement();
            String ns = (String) namespaces.get(prefix);
            html.setAttributeNS(SchemaFormBuilder.XMLNS_NAMESPACE_URI,
                    "xmlns:" + prefix,
                    ns);

        }

        return html;
    }

    /**
     * create the element that will contain the content of the group (or repeat) element
     *
     * @param groupElement the group or repeat element
     * @return the wrapper element
     */
    public Element createGroupContentWrapper(Element groupElement) {
        return groupElement;
    }

    /**
     * create the wrapper element of the form
     *
     * @param enveloppeElement the form element (chiba:form or other)
     * @return the wrapper element
     */

    public Element createFormWrapper(Element enveloppeElement) {
        Document doc = enveloppeElement.getOwnerDocument();
        Element body = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":body");
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
    public Element createModelWrapper(Element modelElement) {
        Document doc = modelElement.getOwnerDocument();
        Element head = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":head");
        head.appendChild(modelElement);

        //eventually add other info
        if ((title != null) && !title.equals("")) {
            Element title_el = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":title");
            Text title_text = doc.createTextNode(title);
            title_el.appendChild(title_text);
            head.appendChild(title_el);
        }

        if ((meta != null) && !meta.isEmpty()) {
            Iterator it = meta.iterator();

            while (it.hasNext()) {
                String[] m = (String[]) it.next();
                String http_equiv = m[0];
                String name = m[1];
                String content = m[2];
                String scheme = m[3];

                Element meta_el = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":META");
                head.appendChild(meta_el);

                //attributes
                if ((http_equiv != null) && !http_equiv.equals("")) {
                    meta_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":http-equiv", http_equiv);
                }

                if ((name != null) && !name.equals("")) {
                    meta_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":name", name);
                }

                if ((content != null) && !content.equals("")) {
                    meta_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":content", content);
                }

                if ((scheme != null) && !scheme.equals("")) {
                    meta_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":scheme", scheme);
                }
            }
        }

        if ((links != null) && !links.isEmpty()) {
            Iterator it = links.iterator();

            while (it.hasNext()) {
                String[] l = (String[]) it.next();
                String href = l[0];
                String type = l[1];
                String rel = l[2];

                Element link_el = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":LINK");
                head.appendChild(link_el);

                //attributes
                if ((href != null) && !href.equals("")) {
                    link_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":href", href);
                }

                if ((type != null) && !type.equals("")) {
                    link_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":type", type);
                }

                if ((rel != null) && !rel.equals("")) {
                    link_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":rel", rel);
                }
            }
        }
        return head;
    }
}
