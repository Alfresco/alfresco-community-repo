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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.virtual.config.NodeRefResolver;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class AlfrescoEnviroment implements ActualEnvironment
{
    private AlfrescoAPIFacet apiFacet;

    private ServiceRegistry serviceRegistry;

    private NamespacePrefixResolver namespacePrefixResolver;

    private Repository repositoryHelper;
    
    private NodeRefResolver nodeRefResolver;

    public void setNodeRefResolver(NodeRefResolver nodeRefResolver)
    {
        this.nodeRefResolver = nodeRefResolver;
    }
    
    public void setAlfrescoAPIFacet(AlfrescoAPIFacet apiFacet)
    {
        this.apiFacet = apiFacet;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public ServiceRegistry getServiceRegistry()
    {
        return this.serviceRegistry;
    }
    
    public void setRepositoryHelper(Repository repository)
    {
        this.repositoryHelper = repository;
    }

    @Override
    public Object executeScript(String classpath, Map<String, Object> model)
    {

        return apiFacet.getScriptService().executeScript(new ClasspathScriptLocation(classpath.substring(1)),
                                                         model);
    }

    @Override
    public Object executeScript(NodeRef templateNodeRef, Map<String, Object> model)
    {

        return apiFacet.getScriptService().executeScript(templateNodeRef,
                                                         null,
                                                         model);
    }

    @Override
    public boolean hasAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        return apiFacet.getNodeService().hasAspect(nodeRef,
                                                   aspectTypeQName);
    }

    @Override
    public Set<QName> getAspects(NodeRef nodeRef)
    {
        NodeService nodeService = apiFacet.getNodeService();
        return nodeService.getAspects(nodeRef);
    }

    @Override
    public NodeRef getTargetAssocs(NodeRef nodeRef, QName associationQName)
    {
        List<AssociationRef> assocs = apiFacet.getNodeService().getTargetAssocs(nodeRef,
                                                                                associationQName);

        if (assocs != null && assocs.size() >= 1)
        {
            AssociationRef associationRef = assocs.get(0);
            NodeRef targetRef = associationRef.getTargetRef();
            return targetRef;
        }
        else
        {
            return null;
        }
    }

    @Override
    public Serializable getProperty(NodeRef nodeRef, QName qname)
    {
        return apiFacet.getNodeService().getProperty(nodeRef,
                                                     qname);
    }

    @Override
    public Map<QName, Serializable> getProperties(NodeRef nodeRef)
    {
        return apiFacet.getNodeService().getProperties(nodeRef);
    }

    @Override
    public InputStream openContentStream(NodeRef nodeRef) throws ActualEnvironmentException
    {
        ContentReader contentReader = apiFacet.getContentService().getReader(nodeRef,
                                                                             ContentModel.PROP_CONTENT);
        return contentReader.getContentInputStream();
    }

    @Override
    public InputStream openContentStream(String classpath) throws ActualEnvironmentException
    {
        return getClass().getResourceAsStream(classpath);
    }

    @Override
    public ResultSet query(SearchParameters searchParameters)
    {
        return apiFacet.getSearchService().query(searchParameters);
    }

    @Override
    public Object createScriptVirtualContext(VirtualContext context) throws ActualEnvironmentException
    {
        return new AlfrescoScriptVirtualContext(context,
                                                serviceRegistry);
    }

    @Override
    public QName getType(final NodeRef nodeRef)
    {
        return apiFacet.getNodeService().getType(nodeRef);
    }

    @Override
    public boolean isSubClass(QName className, QName ofClassName)
    {
        return apiFacet.getDictionaryService().isSubClass(className,
                                                          ofClassName);
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    @Override
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return namespacePrefixResolver;
    }

    @Override
    public String getCurrentUser()
    {
        return serviceRegistry.getAuthenticationService().getCurrentUserName();
    }

    @Override
    public Path getPath(NodeRef nodeRef)
    {
        return apiFacet.getNodeService().getPath(nodeRef);
    }

    @Override
    public ChildAssociationRef getPrimaryParent(NodeRef nodeRef)
    {
        NodeService nodeService = apiFacet.getNodeService();
        return nodeService.getPrimaryParent(nodeRef);
    }

    @Override
    public List<ChildAssociationRef> getChildAssocs(NodeRef nodeRef, QNamePattern typeQNamePattern,
                QNamePattern qnamePattern, int maxResults, boolean preload) throws InvalidNodeRefException
    {
        NodeService nodeService = apiFacet.getNodeService();
        return nodeService.getChildAssocs(nodeRef,
                                          typeQNamePattern,
                                          qnamePattern,
                                          maxResults,
                                          preload);
    }

    @Override
    public NodeRef findNodeRef(String referenceType, String[] reference)
    {
        return repositoryHelper.findNodeRef(referenceType,
                                            reference);
    }

    @Override
    public boolean exists(NodeRef nodeRef)
    {
        return apiFacet.getNodeService().exists(nodeRef);
    }

    @Override
    public NodeRef getChildByName(NodeRef nodeRef, QName assocTypeQName, String childName)
    {
        return apiFacet.getNodeService().getChildByName(nodeRef,
                                                        assocTypeQName,
                                                        childName);
    }

    @Override
    public void delete(NodeRef nodeRef)
    {
        apiFacet.getNodeService().deleteNode(nodeRef);
    }

    @Override
    public FileInfo create(NodeRef parentNodeRef, String name, QName typeQName) throws FileExistsException
    {
        return apiFacet.getFileFolderService().create(parentNodeRef,
                                                      name,
                                                      typeQName);
    }

    @Override
    public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update)
                throws InvalidNodeRefException, InvalidTypeException
    {
        return apiFacet.getContentService().getWriter(nodeRef,
                                                      propertyQName,
                                                      update);
    }

    @Override
    public void addAspect(NodeRef nodeRef, QName aspectTypeQName, Map<QName, Serializable> aspectProperties)
                throws InvalidNodeRefException, InvalidAspectException
    {
        apiFacet.getNodeService().addAspect(nodeRef,
                                            aspectTypeQName,
                                            aspectProperties);
    }

    @Override
    public NodeRef findQNamePath(String[] patheElements)
    {
        return nodeRefResolver.resolveQNameReference(patheElements);
    }

    @Override
    public boolean exists(String classpath)
    {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource resource = resolver.getResource("classpath:" + classpath);
        return resource.exists();
    }
}
