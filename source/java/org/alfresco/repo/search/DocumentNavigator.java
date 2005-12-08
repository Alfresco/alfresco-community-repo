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
package org.alfresco.repo.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.jaxen.DefaultNavigator;
import org.jaxen.JaxenException;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;

/**
 * An implementation of the Jaxen xpath against the node service API
 * 
 * This means any node service can do xpath style navigation. Given any context
 * node we can navigate between nodes using xpath.
 * 
 * This allows simple path navigation and much more.
 * 
 * @author Andy Hind
 * 
 */
public class DocumentNavigator extends DefaultNavigator
{
    private static QName JCR_ROOT = QName.createQName("http://www.jcp.org/jcr/1.0", "root");
    
    private static QName JCR_PRIMARY_TYPE = QName.createQName("http://www.jcp.org/jcr/1.0", "primaryType");
    
    private static QName JCR_MIXIN_TYPES = QName.createQName("http://www.jcp.org/jcr/1.0", "mixinTypes");
    
    private static final long serialVersionUID = 3618984485740165427L;

    private DictionaryService dictionaryService;

    private NodeService nodeService;

    private SearchService searchService;

    private NamespacePrefixResolver nspr;

    // Support classes to encapsulate stuff more akin to xml

    public class Property
    {
        public final QName qname;

        public final Serializable value;

        public final NodeRef parent;

        public Property(QName qname, Serializable value, NodeRef parent)
        {
            this.qname = qname;
            this.value = value;
            this.parent = parent;
        }
    }

   
    public class Namespace
    {
        public final String prefix;

        public final String uri;

        public Namespace(String prefix, String uri)
        {
            this.prefix = prefix;
            this.uri = uri;
        }
    }
    
    public class JCRRootNodeChildAssociationRef extends ChildAssociationRef
    {

        /**
         * Comment for <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = -3890194577752476675L;

        public JCRRootNodeChildAssociationRef(QName assocTypeQName, NodeRef parentRef, QName childQName, NodeRef childRef)
        {
            super(assocTypeQName, parentRef, childQName, childRef);
        }
        
        public JCRRootNodeChildAssociationRef(QName assocTypeQName, NodeRef parentRef, QName childQName, NodeRef childRef, boolean isPrimary, int nthSibling)
        {
            super(assocTypeQName, parentRef, childQName, childRef, isPrimary, nthSibling);
        }
        
    }

    private boolean followAllParentLinks;
    
    private boolean useJCRRootNode;

    /**
     * @param dictionaryService
     *            used to resolve the <b>subtypeOf</b> function and other
     *            type-related functions
     * @param nodeService
     *            the <tt>NodeService</tt> against which to execute
     * @param searchService
     *            the service that helps resolve functions such as <b>like</b>
     *            and <b>contains</b>
     * @param nspr
     *            resolves namespaces in the xpath
     * @param followAllParentLinks
     *            true if the XPath should traverse all parent associations when
     *            going up the hierarchy; false if the only the primary
     *            parent-child association should be traversed
     */
    public DocumentNavigator(DictionaryService dictionaryService, NodeService nodeService, SearchService searchService,
            NamespacePrefixResolver nspr, boolean followAllParentLinks, boolean useJCRRootNode)
    {
        super();
        this.dictionaryService = dictionaryService;
        this.nodeService = nodeService;
        this.searchService = searchService;
        this.nspr = nspr;
        this.followAllParentLinks = followAllParentLinks;
        this.useJCRRootNode = useJCRRootNode;
    }

    
    
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return nspr;
    }



    /**
     * Allow this to be set as it commonly changes from one search to the next
     * 
     * @param followAllParentLinks
     *            true
     */
    public void setFollowAllParentLinks(boolean followAllParentLinks)
    {
        this.followAllParentLinks = followAllParentLinks;
    }

    public String getAttributeName(Object o)
    {
        // Get the local name
        String escapedLocalName = ISO9075.encode(((Property) o).qname.getLocalName());
        if(escapedLocalName == ((Property) o).qname.getLocalName())
        {
            return escapedLocalName;
        }
        return escapedLocalName;
    }

    public String getAttributeNamespaceUri(Object o)
    {
        return ((Property) o).qname.getNamespaceURI();
    }

    public String getAttributeQName(Object o)
    {
        QName qName = ((Property) o).qname;
        String escapedLocalName = ISO9075.encode(qName.getLocalName());
        if(escapedLocalName == qName.getLocalName())
        {
            return qName.toString();
        }
        else
        {
            return QName.createQName(qName.getNamespaceURI(), escapedLocalName).toString();
        }
    }

    public String getAttributeStringValue(Object o)
    {
        // Only the first property of multi-valued properties is displayed
        // A multivalue attribute makes no sense in the xml world
        return DefaultTypeConverter.INSTANCE.convert(String.class, ((Property) o).value);
    }

    public String getCommentStringValue(Object o)
    {
        // There is no attribute that is a comment
        throw new UnsupportedOperationException("Comment string values are unsupported");
    }

    public String getElementName(Object o)
    {
        return ISO9075.encode(((ChildAssociationRef) o).getQName().getLocalName());
    }

    public String getElementNamespaceUri(Object o)
    {
        return ((ChildAssociationRef) o).getQName().getNamespaceURI();
    }

    public String getElementQName(Object o)
    {
        QName qName = ((ChildAssociationRef) o).getQName();
        String escapedLocalName = ISO9075.encode(qName.getLocalName());
        if(escapedLocalName == qName.getLocalName())
        {
            return qName.toString();
        }
        else
        {
            return QName.createQName(qName.getNamespaceURI(), escapedLocalName).toString();
        }
    }

    public String getElementStringValue(Object o)
    {
        throw new UnsupportedOperationException("Element string values are unsupported");
    }

    public String getNamespacePrefix(Object o)
    {
        return ((Namespace) o).prefix;
    }

    public String getNamespaceStringValue(Object o)
    {
        return ((Namespace) o).uri;
    }

    public String getTextStringValue(Object o)
    {
        throw new UnsupportedOperationException("Text nodes are unsupported");
    }

    public boolean isAttribute(Object o)
    {
        return (o instanceof Property);
    }

    public boolean isComment(Object o)
    {
        return false;
    }

    public boolean isDocument(Object o)
    {
        if (!(o instanceof ChildAssociationRef))
        {
            return false;
        }
        ChildAssociationRef car = (ChildAssociationRef) o;
        return (car.getParentRef() == null) && (car.getQName() == null);
    }

    public boolean isElement(Object o)
    {
        return (o instanceof ChildAssociationRef);
    }

    public boolean isNamespace(Object o)
    {
        return (o instanceof Namespace);
    }

    public boolean isProcessingInstruction(Object o)
    {
        return false;
    }

    public boolean isText(Object o)
    {
        return false;
    }

    public XPath parseXPath(String o) throws JaxenException
    {
        return new NodeServiceXPath(o, this, null);
    }

    // Basic navigation support

    public Iterator getAttributeAxisIterator(Object o) throws UnsupportedAxisException
    {
        ArrayList<Property> properties = new ArrayList<Property>();
        NodeRef nodeRef = ((ChildAssociationRef) o).getChildRef();
        Map<QName, Serializable> map = nodeService.getProperties(nodeRef);
        for (QName qName : map.keySet())
        {
            if(map.get(qName) instanceof Collection)
            {
                for(Serializable ob : (Collection<Serializable>) map.get(qName))
                {
                    Property property = new Property(qName, ob, nodeRef);
                    properties.add(property);
                }
            }
            else
            {
                Property property = new Property(qName, map.get(qName), nodeRef);
                properties.add(property);
            }        
        }
        if(useJCRRootNode)
        {
            properties.add(new Property(JCR_PRIMARY_TYPE, nodeService.getType(nodeRef), nodeRef));
            for(QName mixin : nodeService.getAspects(nodeRef))
            {
                properties.add(new Property(JCR_MIXIN_TYPES, mixin, nodeRef));
            }
        }
        
        return properties.iterator();
    }

    public Iterator getChildAxisIterator(Object o) throws UnsupportedAxisException
    {
        // Iterator of ChildAxisRef
        ChildAssociationRef assocRef = (ChildAssociationRef) o;
        NodeRef childRef = assocRef.getChildRef();
        List<ChildAssociationRef> list;
        // Add compatability for JCR 170 by including the root node.
        if(isDocument(o) && useJCRRootNode)
        {
            list = new ArrayList<ChildAssociationRef>(1);
            list.add(new JCRRootNodeChildAssociationRef(ContentModel.ASSOC_CHILDREN, childRef, JCR_ROOT, childRef, true, 0));
        }
        else
        {
            list = nodeService.getChildAssocs(childRef);
        }
        return list.iterator();
    }

    public Iterator getNamespaceAxisIterator(Object o) throws UnsupportedAxisException
    {
        // Iterator of Namespace
        ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
        for (String prefix : nspr.getPrefixes())
        {
            String uri = nspr.getNamespaceURI(prefix);
            Namespace ns = new Namespace(prefix, uri);
            namespaces.add(ns);
        }
        return namespaces.iterator();
    }

    public Iterator getParentAxisIterator(Object o) throws UnsupportedAxisException
    {
        ArrayList<ChildAssociationRef> parents = new ArrayList<ChildAssociationRef>(1);
        // Iterator of ??
        if (o instanceof ChildAssociationRef)
        {
            ChildAssociationRef contextRef = (ChildAssociationRef) o;
            if (contextRef.getParentRef() != null)
            {
                if (followAllParentLinks)
                {
                    for (ChildAssociationRef car : nodeService.getParentAssocs(contextRef.getChildRef()))
                    {
                        parents.add(nodeService.getPrimaryParent(car.getParentRef()));
                    }
                }
                else
                {
                    parents.add(nodeService.getPrimaryParent(contextRef.getParentRef()));
                }
            }
        }
        if (o instanceof Property)
        {
            Property p = (Property) o;
            parents.add(nodeService.getPrimaryParent(p.parent));
        }
        return parents.iterator();
    }

    public Object getDocumentNode(Object o)
    {
        ChildAssociationRef assocRef = (ChildAssociationRef) o;
        StoreRef storeRef = assocRef.getChildRef().getStoreRef();
        return new ChildAssociationRef(null, null, null, nodeService.getRootNode(storeRef));
    }

    public Object getNode(NodeRef nodeRef)
    {
        return nodeService.getPrimaryParent(nodeRef);
    }
    
    public List<ChildAssociationRef> getNode(NodeRef nodeRef, QNamePattern qNamePattern)
    {
        return nodeService.getParentAssocs(nodeRef, RegexQNamePattern.MATCH_ALL, qNamePattern);
    }

    public Boolean like(NodeRef childRef, QName qname, String sqlLikePattern, boolean includeFTS)
    {
        return searchService.like(childRef, qname, sqlLikePattern, includeFTS);
    }

    public Boolean contains(NodeRef childRef, QName qname, String sqlLikePattern, SearchParameters.Operator defaultOperator)
    {
        return searchService.contains(childRef, qname, sqlLikePattern, defaultOperator);
    }

    public Boolean isSubtypeOf(NodeRef nodeRef, QName typeQName)
    {
        // get the type of the node
        QName nodeTypeQName = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(nodeTypeQName, typeQName);
    }
}
