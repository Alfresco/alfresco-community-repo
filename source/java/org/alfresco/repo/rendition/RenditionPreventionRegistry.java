/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.rendition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * This class holds a registry of content class names (types and aspects) which if they are present on a sourceNode will prevent any
 * renditions from being created for that node.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.1
 */
public class RenditionPreventionRegistry
{
    private final Set<QName> registeredContentClasses = new HashSet<QName>();
    private NamespaceService namespaceService;
    
    public void setNamespaceService(NamespaceService service)
    {
        this.namespaceService = service;
    }
    
    public void register(String contentClass)
    {
        QName qname = QName.createQName(contentClass, namespaceService);
        registeredContentClasses.add(qname);
    }
    
    /**
     * @return a Set of QNames of types/aspects which will prevent renditions from occurring.
     */
    public Set<QName> getRegisteredQNames()
    {
        return Collections.unmodifiableSet(registeredContentClasses);
    }
    
    /**
     * Checks if the specified type/aspect is registered as a marker for rendition prevention.
     * @param contentClassName aspect name.
     * @return <code>true</code> if this class will prevent renditions, else <code>false</code>
     */
    public boolean isContentClassRegistered(String contentClassName)
    {
        QName qname = QName.createQName(contentClassName, namespaceService);
        return isContentClassRegistered(qname);
    }
    
    /**
     * Checks if the specified type/aspect is registered as a marker for rendition prevention.
     * @param aspectQName aspect name.
     * @return <code>true</code> if this aspect will prevent renditions, else <code>false</code>
     */
    public boolean isContentClassRegistered(QName aspectQName)
    {
        return registeredContentClasses.contains(aspectQName);
    }
    
    /**
     * A utility class which ensures that the specified aspect/type name is registered.
     */
    public static class SelfRegisteringClassName
    {
        private final String contentClassName;
        private RenditionPreventionRegistry registry;
        
        public SelfRegisteringClassName(String className)
        {
            this.contentClassName = className;
        }
        
        public void setRegistry(RenditionPreventionRegistry registry)
        {
            this.registry = registry;
        }
        
        public void register()
        {
            registry.register(contentClassName);
        }
    }
}
