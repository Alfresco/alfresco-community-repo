/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;

/**
 * Representation of a simple path e.g.
 * <b><pre>
 *   /x/y/z
 * </pre></b>
 * In the above example, there will be <b>4</b> elements, the first being a reference
 * to the root node, followed by qname elements for <b>x</b>, <b>y</b> and <b>z</b>.    
 * <p>
 * Methods and constructors are available to construct a <code>Path</code> instance
 * from a path string or by building the path incrementally, including the ability to
 * append and prepend path elements.
 * <p>
 * Path elements supported:
 * <ul>
 *   <li><b>/{namespace}name</b> fully qualified element</li>
 *   <li><b>/name</b> element using default namespace</li>
 *   <li><b>/{namespace}name[n]</b> nth sibling</li>
 *   <li><b>/name[n]</b> nth sibling using default namespace</li>
 *   <li><b>/descendant-or-self::node()</b> descendent or self</li>
 *   <li><b>/.</b> self</li>
 *   <li><b>/..</b> parent</li>
 * </ul>
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public final class Path implements Iterable<Path.Element>, Serializable
{
    private static final long serialVersionUID = 3905520514524328247L;
    private LinkedList<Element> elements;
    
    public Path()
    {
        // use linked list so as random access is not required, but both prepending and appending is
        elements = new LinkedList<Element>();
    }
    
    /**
     * @return Returns a typed iterator over the path elements
     */
    public Iterator<Path.Element> iterator()
    {
       return elements.iterator();
    }
    
    /**
     * Add a path element to the beginning of the path.  This operation is useful in cases where
     * a path is built by traversing up a hierarchy.
     * 
     * @param pathElement Path.Element
     * @return Returns this instance of the path
     */
    public Path prepend(Path.Element pathElement)
    {
        elements.addFirst(pathElement);
        return this;
    }
    
    /**
     * Merge the given path into the beginning of this path.
     * 
     * @param path Path
     * @return Returns this instance of the path
     */
    public Path prepend(Path path)
    {
       elements.addAll(0, path.elements);
       return this;
    }
    
    /**
     * Appends a path element to the end of the path
     * 
     * @param pathElement Path.Element
     * @return Returns this instance of the path
     */
    public Path append(Path.Element pathElement)
    {
        elements.addLast(pathElement);
        return this;
    }
    
    /**
     * Append the given path of this path.
     * 
     * @param path Path
     * @return Returns this instance of the path
     */
    public Path append(Path path)
    {
       elements.addAll(path.elements);
       return this;
    }
    
    /**
     * @return Returns the first element in the path or null if the path is empty
     */
    public Element first()
    {
        return elements.getFirst();
    }
    
    /**
     * @return Returns the last element in the path or null if the path is empty
     */
    public Element last()
    {
        return elements.getLast();
    }
    
    public int size()
    {
        return elements.size();
    }
    
    public Element get(int n)
    {
        return elements.get(n);
    }
    
    /**
     * @return Returns a string path made up of the component elements of this instance
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        for (Element element : elements)
        {
            if((sb.length() > 1) || ((sb.length() == 1) && (sb.charAt(0) != '/')))
            {
                sb.append("/");
            }
            sb.append(element.getElementString());
        }
        return sb.toString();
    }

    /**
     * @return Returns a string path made up of the component elements of this instance (prefixed where appropriate)
     */
    public String toPrefixString(NamespacePrefixResolver resolver)
    {
        StringBuilder sb = new StringBuilder(128);
        for (Element element : elements)
        {
            if((sb.length() > 1) || ((sb.length() == 1) && (sb.charAt(0) != '/')))
            {
                sb.append("/");
            }
            sb.append(element.getPrefixedString(resolver));
        }
        return sb.toString();
    }
    
    /**
     * Return the human readable form of the specified node Path. Slow version of the method
     * that extracts the name of each node in the Path from the supplied NodeService.
     * 
     * @return human readable form of the Path excluding the final element
     */
    public String toDisplayPath(NodeService nodeService, PermissionService permissionService)
    {
        StringBuilder buf = new StringBuilder(64);
        
        for (int i=0; i<elements.size()-1; i++)
        {
            String elementString = null;
            Element element = elements.get(i);
            if (element instanceof ChildAssocElement)
            {
                ChildAssociationRef elementRef = ((ChildAssocElement)element).getRef();
                if (elementRef.getParentRef() != null)
                {
                    Serializable nameProp = null;
                    if (permissionService.hasPermission(
                            elementRef.getChildRef(), PermissionService.READ) == AccessStatus.ALLOWED)
                    {
                        nameProp = nodeService.getProperty(elementRef.getChildRef(), ContentModel.PROP_NAME);
                        // use the name property if we are allowed access to it
                        elementString = nameProp.toString();
                    }
                    else
                    {
                        // revert to using QName if not
                        elementString = elementRef.getQName().getLocalName();
                    }
                }
            }
            else
            {
                elementString = element.getElementString();
            }
            
            if (elementString != null)
            {
                buf.append("/");
                buf.append(elementString);
            }
        }
        
        return buf.toString();
    }
    
    /**
     * Return a new Path representing this path to the specified depth
     *  
     * @param depth  the path depth (0 based)
     * @return  the sub-path
     */
    public Path subPath(int depth)
    {
        return subPath(0, depth);
    }

    /**
     * Return a new Path representing this path to the specified depth
     * 
     * For example, subPath(2, 4) would return the third and forth elements in the Path.
     *  
     * @param start position  (0 based)
     * @param end position (0 based)
     * @return  the sub-path
     */
    public Path subPath(int start, int end)
    {
        if (start < 0 || start > (elements.size() -1))
        {
            throw new IndexOutOfBoundsException("Start index " + start + " must be between 0 and " + (elements.size() -1));
        }
        if (end < 0 || end > (elements.size() -1))
        {
            throw new IndexOutOfBoundsException("End index " + end + " must be between 0 and " + (elements.size() -1));
        }
        if (end < start)
        {
            throw new IndexOutOfBoundsException("End index " + end + " cannot be before start index " + start);
        }
        Path subPath = new Path();
        for (int i = start; i <= end; i++)
        {
            subPath.append(this.get(i));
        }
        return subPath;
    }
    
    /**
     * Override equals to check equality of Path instances
     */
    public boolean equals(Object o)
    {
        if(o == this)
        {
            return true;
        }
        if(!(o instanceof Path))
        {
            return false;
        }
        Path other = (Path)o;
        return this.elements.equals(other.elements);
    }

    /**
     * Override hashCode to check hash equality of Path instances
     */
    public int hashCode()
    {
        return elements.hashCode();
    }
    
    public Path getBaseNamePath(TenantService tenantService)
    {
        Path basePath = new Path();
        for(Element element : elements)
        {
            basePath.append(element.getBaseNameElement(tenantService));
        }
        return basePath;
    }
    
    /**
     * Represents a path element.
     * <p>
     * In <b>/x/y/z</b>, elements are <b>x</b>, <b>y</b> and <b>z</b>.
     */
    @AlfrescoPublicApi
    public abstract static class Element implements Serializable
    {
        private static final long serialVersionUID = 5396069341092867660L;

        /**
         * @return Returns the path element portion including leading '/' and never null
         */
        public abstract String getElementString();

        /**
         * @param tenantService TenantService
         * @return Element
         */
        @AlfrescoPublicApi
        public abstract Element getBaseNameElement(TenantService tenantService);

        /**
         * @param resolver  namespace prefix resolver
         * @return  the path element portion (with namespaces converted to prefixes)
         */
        public String getPrefixedString(NamespacePrefixResolver resolver)
        {
            return getElementString();
        }
        
        /**
         * @see #getElementString()
         */
        public String toString()
        {
            return getElementString();
        }
    }
    
    /**
     * Represents a qualified path between a parent and a child node,
     * including the sibling to retrieve e.g. <b>/{namespace}name[5]</b> 
     */
    @AlfrescoPublicApi
    public static class ChildAssocElement extends Element
    {
        private static final long serialVersionUID = 3689352104636790840L;

        private ChildAssociationRef ref;
        
        /**
         * @param ref a reference to the specific parent-child association
         */
        public ChildAssocElement(ChildAssociationRef ref)
        {
            this.ref = ref;
        }

        @Override
        public String getElementString()
        {
            return createElementString(null);
        }

        @Override
        public String getPrefixedString(NamespacePrefixResolver resolver)
        {
            return createElementString(resolver);
        }

        public ChildAssociationRef getRef()
        {
            return ref;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if(o == this)
            {
                return true;
            }
            if(!(o instanceof ChildAssocElement))
            {
                return false;
            }
            ChildAssocElement other = (ChildAssocElement)o;
            return this.ref.equals(other.ref);
        }

        @Override
        public int hashCode()
        {
            return ref.hashCode();
        }
        
        private String createElementString(NamespacePrefixResolver resolver)
        {
            StringBuilder sb = new StringBuilder(32);
            if (ref.getParentRef() == null)
            {
                sb.append("/");
            }
            else
            {
                // a parent is present
                sb.append(resolver == null ? ISO9075.getXPathName(ref.getQName()) : ISO9075.getXPathName(ref.getQName(), resolver));
            }
            if (ref.getNthSibling() > -1)
            {
                sb.append("[").append(ref.getNthSibling()).append("]");
            }
            return sb.toString();
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.Path.Element#getBaseNameElement(org.alfresco.repo.tenant.TenantService)
         */
        @Override
        public Element getBaseNameElement(TenantService tenantService)
        {
            return new ChildAssocElement(new ChildAssociationRef(ref.getTypeQName(), tenantService.getBaseName(ref.getParentRef(), true), ref.getQName(), tenantService.getBaseName(ref.getChildRef(), true), ref.isPrimary(), ref.getNthSibling()));
        }
    }

    /**
     * Represents a qualified path to an attribute,
     * including the sibling for repeated properties/attributes to retrieve e.g. <b>/@{namespace}name[5]</b> 
     */
    @AlfrescoPublicApi
    public static class AttributeElement extends Element
    {
        private static final long serialVersionUID = 3256727281668863544L;

        private QName attribute;
        private int position = -1;
        
        /**
         * @param attribute QName
         */
        public AttributeElement(QName attribute)
        {
            this.attribute = attribute;
        }
        
        public AttributeElement(QName attribute, int position)
        {
            this(attribute);
            this.position = position;
        }

        public AttributeElement(String attribute)
        {
            String qNameStr = null;
            int idx = attribute.indexOf("[");
            if(idx != -1)
            {
                String positionStr = attribute.substring(idx + 1, attribute.length() - 1);
                position = Integer.parseInt(positionStr);
                qNameStr = attribute.substring(1, idx);
            }
            else
            {
                qNameStr = attribute.substring(1);
            }
            this.attribute = ISO9075.parseXPathName(qNameStr);
        }
        
        @Override
        public String getElementString()
        {
            return createElementString(null);
        }
        
        @Override
        public String getPrefixedString(NamespacePrefixResolver resolver)
        {
            return createElementString(resolver);
        }
        
        private String createElementString(NamespacePrefixResolver resolver)
        {
            StringBuilder sb = new StringBuilder(32);
            sb.append("@").append(resolver == null ? ISO9075.getXPathName(attribute) : ISO9075.getXPathName(attribute, resolver));
            
            if (position > -1)
            {
                sb.append("[").append(position).append("]");
            }
            return sb.toString();
        }
        
        public QName getQName()
        {
            return attribute;
        }
        
        public int position()
        {
            return position;
        }
        
        public boolean equals(Object o)
        {
            if(o == this)
            {
                return true;
            }
            if(!(o instanceof AttributeElement))
            {
                return false;
            }
            AttributeElement other = (AttributeElement)o;
            return this.getQName().equals(other.getQName()) && (this.position() == other.position());
        }
        
        public int hashCode()
        {
            return getQName().hashCode()*32 + position();
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.Path.Element#getBaseNameElement(org.alfresco.repo.tenant.TenantService)
         */
        @Override
        public Element getBaseNameElement(TenantService tenantService)
        {
            return new AttributeElement(attribute, position);
        }

    }

    /**
     * Represents the <b>//</b> or <b>/descendant-or-self::node()</b> xpath element
     */
    @AlfrescoPublicApi
    public static class DescendentOrSelfElement extends Element
    {
        private static final long serialVersionUID = 3258410616875005237L;

        public String getElementString()
        {
            return "descendant-or-self::node()";
        }
        
        public boolean equals(Object o)
        {
            if(o == this)
            {
                return true;
            }
            if(!(o instanceof DescendentOrSelfElement))
            {
                return false;
            }
            return true;
        }
        
        public int hashCode()
        {
            return "descendant-or-self::node()".hashCode();
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.Path.Element#getBaseNameElement(org.alfresco.repo.tenant.TenantService)
         */
        @Override
        public Element getBaseNameElement(TenantService tenantService)
        {
           return new DescendentOrSelfElement();
        }

    }
    
    /**
     * Represents the <b>/.</b> xpath element
     */
    @AlfrescoPublicApi
    public static class SelfElement extends Element
    {
        private static final long serialVersionUID = 3834311739151300406L;

        public String getElementString()
        {
            return ".";
        }
        
        public boolean equals(Object o)
        {
            if(o == this)
            {
                return true;
            }
            if(!(o instanceof SelfElement))
            {
                return false;
            }
            return true;
        }
        
        public int hashCode()
        {
            return ".".hashCode();
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.Path.Element#getBaseNameElement(org.alfresco.repo.tenant.TenantService)
         */
        @Override
        public Element getBaseNameElement(TenantService tenantService)
        {
            return new SelfElement();
        }
    }
    
    /**
     * Represents the <b>/..</b> xpath element
     */
    @AlfrescoPublicApi
    public static class ParentElement extends Element
    {
        private static final long serialVersionUID = 3689915080477456179L;

        public String getElementString()
        {
            return "..";
        }
        
        public boolean equals(Object o)
        {
            if(o == this)
            {
                return true;
            }
            if(!(o instanceof ParentElement))
            {
                return false;
            }
            return true;
        }
        
        public int hashCode()
        {
            return "..".hashCode();
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.Path.Element#getBaseNameElement(org.alfresco.repo.tenant.TenantService)
         */
        @Override
        public Element getBaseNameElement(TenantService tenantService)
        {
            return new ParentElement();
        }
    }
}
