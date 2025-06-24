/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jaxen.DefaultNavigator;
import org.jaxen.JaxenException;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;

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
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.ISO9075;

/**
 * An implementation of the Jaxen xpath against the node service API
 * 
 * This means any node service can do xpath style navigation. Given any context node we can navigate between nodes using xpath.
 * 
 * This allows simple path navigation and much more.
 * 
 * @author Andy Hind
 * 
 */
public class DocumentNavigator extends DefaultNavigator implements NamedAccessNavigator
{
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

    private boolean followAllParentLinks;

    /**
     * @deprecated useJCRRootNode parameter is now obsolete.
     */
    public DocumentNavigator(DictionaryService dictionaryService, NodeService nodeService, SearchService searchService,
            NamespacePrefixResolver nspr, boolean followAllParentLinks, boolean useJCRRootNode)
    {
        this(dictionaryService, nodeService, searchService, nspr, followAllParentLinks);
    }

    /**
     * @param dictionaryService
     *            used to resolve the <b>subtypeOf</b> function and other type-related functions
     * @param nodeService
     *            the <tt>NodeService</tt> against which to execute
     * @param searchService
     *            the service that helps resolve functions such as <b>like</b> and <b>contains</b>
     * @param nspr
     *            resolves namespaces in the xpath
     * @param followAllParentLinks
     *            true if the XPath should traverse all parent associations when going up the hierarchy; false if the only the primary parent-child association should be traversed
     */
    public DocumentNavigator(DictionaryService dictionaryService, NodeService nodeService, SearchService searchService,
            NamespacePrefixResolver nspr, boolean followAllParentLinks)
    {
        super();
        this.dictionaryService = dictionaryService;
        this.nodeService = nodeService;
        this.searchService = searchService;
        this.nspr = nspr;
        this.followAllParentLinks = followAllParentLinks;
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
        return ISO9075.encode(((Property) o).qname.getLocalName());
    }

    public String getAttributeNamespaceUri(Object o)
    {
        return ((Property) o).qname.getNamespaceURI();
    }

    public String getAttributeQName(Object o)
    {
        QName qName = ((Property) o).qname;
        String escapedLocalName = ISO9075.encode(qName.getLocalName());
        if (EqualsHelper.nullSafeEquals(escapedLocalName, qName.getLocalName()))
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
        QName qName = ((ChildAssociationRef) o).getQName();
        if (qName == null)
        {
            return "";
        }
        return ISO9075.encode(qName.getLocalName());
    }

    public String getElementNamespaceUri(Object o)
    {
        QName qName = ((ChildAssociationRef) o).getQName();
        if (qName == null)
        {
            return "";
        }
        return (qName.getNamespaceURI());
    }

    public String getElementQName(Object o)
    {
        QName qName = ((ChildAssociationRef) o).getQName();
        if (qName == null)
        {
            return "";
        }
        String escapedLocalName = ISO9075.encode(qName.getLocalName());
        if (EqualsHelper.nullSafeEquals(escapedLocalName, qName.getLocalName()))
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

    public Iterator getAttributeAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) throws UnsupportedAxisException
    {
        // decode the localname
        localName = ISO9075.decode(localName);

        NodeRef nodeRef = ((ChildAssociationRef) contextNode).getChildRef();
        QName qName = QName.createQName(namespaceURI, localName);
        Serializable value = nodeService.getProperty(nodeRef, qName);
        List<Property> properties = null;
        if (value != null)
        {
            if (value instanceof Collection)
            {
                Collection<Serializable> values = (Collection<Serializable>) value;
                properties = new ArrayList<Property>(values.size());
                for (Serializable collectionValue : values)
                {
                    Property property = new Property(qName, collectionValue, nodeRef);
                    properties.add(property);
                }
            }
            else
            {
                Property property = new Property(qName, value, nodeRef);
                properties = Collections.singletonList(property);
            }
        }
        else
        {
            properties = Collections.emptyList();
        }
        // done
        return properties.iterator();
    }

    public Iterator getAttributeAxisIterator(Object o) throws UnsupportedAxisException
    {
        ArrayList<Property> properties = new ArrayList<Property>();
        NodeRef nodeRef = ((ChildAssociationRef) o).getChildRef();
        Map<QName, Serializable> map = nodeService.getProperties(nodeRef);
        for (QName qName : map.keySet())
        {
            if (map.get(qName) instanceof Collection)
            {
                for (Serializable ob : (Collection<Serializable>) map.get(qName))
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

        return properties.iterator();
    }

    public Iterator getChildAxisIterator(Object contextNode, String localName, String namespacePrefix, String namespaceURI) throws UnsupportedAxisException
    {
        // decode the localname
        localName = ISO9075.decode(localName);

        // MNT-10730
        if (localName != null && (localName.equalsIgnoreCase("true") || localName.equalsIgnoreCase("false")))
        {
            return Collections.singletonList(Boolean.valueOf(Boolean.parseBoolean(localName))).iterator();
        }

        ChildAssociationRef assocRef = (ChildAssociationRef) contextNode;
        NodeRef childRef = assocRef.getChildRef();
        QName qName = QName.createQName(namespaceURI, localName);
        List<? extends ChildAssociationRef> list = null;
        list = nodeService.getChildAssocs(childRef, RegexQNamePattern.MATCH_ALL, qName);
        // done
        return list.iterator();
    }

    public Iterator getChildAxisIterator(Object o) throws UnsupportedAxisException
    {
        // Iterator of ChildAxisRef
        ChildAssociationRef assocRef = (ChildAssociationRef) o;
        NodeRef childRef = assocRef.getChildRef();
        List<ChildAssociationRef> list;
        list = nodeService.getChildAssocs(childRef);
        return list.iterator();
    }

    /** Used to prevent crazy ordering code in from repeatedly getting child association */
    private static final UnsupportedAxisException EXCEPTION_NOT_SUPPORTED = new UnsupportedAxisException("");

    /**
     * @see #EXCEPTION_NOT_SUPPORTED always thrown
     */
    @Override
    public Iterator getFollowingSiblingAxisIterator(Object arg0) throws UnsupportedAxisException
    {
        throw EXCEPTION_NOT_SUPPORTED;
    }

    /**
     * @see #EXCEPTION_NOT_SUPPORTED always thrown
     */
    @Override
    public Iterator getFollowingAxisIterator(Object arg0) throws UnsupportedAxisException
    {
        throw EXCEPTION_NOT_SUPPORTED;
    }

    /**
     * @see #EXCEPTION_NOT_SUPPORTED always thrown
     */
    @Override
    public Iterator getPrecedingAxisIterator(Object arg0) throws UnsupportedAxisException
    {
        throw EXCEPTION_NOT_SUPPORTED;
    }

    /**
     * @see #EXCEPTION_NOT_SUPPORTED always thrown
     */
    @Override
    public Iterator getPrecedingSiblingAxisIterator(Object arg0) throws UnsupportedAxisException
    {
        throw EXCEPTION_NOT_SUPPORTED;
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

    public Boolean hasAspect(NodeRef nodeRef, QName typeQName)
    {
        return nodeService.hasAspect(nodeRef, typeQName);
    }
}
