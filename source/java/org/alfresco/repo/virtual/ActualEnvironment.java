/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

/**
 * Dependency inversion facade of the Alfresco repository environment. It offers
 * an interface to Alfresco repository capabilities needed for virtualization.
 * Implementors should consider loose repository beans coupling when
 * implementing the environment operations.
 * 
 * @author Bogdan Horje
 */
public interface ActualEnvironment
{

    QName getType(NodeRef nodeRef);

    boolean isSubClass(QName className, QName ofClassName);

    NodeRef getTargetAssocs(NodeRef nodeRef, QName aspectTypeQName);

    Serializable getProperty(NodeRef nodeRef, QName qname) throws ActualEnvironmentException;

    Map<QName, Serializable> getProperties(NodeRef nodeRef);

    boolean hasAspect(NodeRef nodeRef, QName aspectTypeQName);

    Set<QName> getAspects(NodeRef nodeRef);

    String getCurrentUser();

    Path getPath(NodeRef nodeRef);

    ChildAssociationRef getPrimaryParent(NodeRef nodeRef);

    List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern, QNamePattern qnamePattern,
                int maxResults, boolean preload) throws InvalidNodeRefException;

    NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName);

    NamespacePrefixResolver getNamespacePrefixResolver();

    InputStream openContentStream(NodeRef nodeRef) throws ActualEnvironmentException;

    InputStream openContentStream(String classpath) throws ActualEnvironmentException;

    ResultSet query(SearchParameters searchParameters);

    Object executeScript(String classpath, Map<String, Object> model) throws ActualEnvironmentException;

    Object executeScript(NodeRef templateNodeRef, Map<String, Object> model) throws ActualEnvironmentException;

    Object createScriptVirtualContext(VirtualContext context) throws ActualEnvironmentException;

    NodeRef findNodeRef(String referenceType, String[] reference);
    
    NodeRef findQNamePath(String[] patheElements);

    boolean exists(NodeRef nodeRef);

    boolean exists(String classpath);

    void delete(NodeRef nodeRef);

    FileInfo create(NodeRef parentNodeRef, String name, QName typeQName) throws FileExistsException;

    ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update) throws InvalidNodeRefException,
                InvalidTypeException;

    void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> aspectProperties)
                throws InvalidNodeRefException, InvalidAspectException;

}
