/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.rendition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.util.FreeMarkerUtil;
import org.alfresco.util.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import freemarker.ext.dom.NodeModel;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;

public class StandardRenditionLocationResolverImpl implements RenditionLocationResolver
{
    private final static Log log = LogFactory.getLog(StandardRenditionLocationResolverImpl.class);

    private ServiceRegistry serviceRegistry;
    private Repository repositoryHelper;
    private String companyHomePath = "/app:company_home";

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setCompanyHomePath(String companyHomePath)
    {
        this.companyHomePath = companyHomePath;
    }
    
    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.rendition.RenditionLocationResolver#getRenditionLocation(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.rendition.RenditionDefinition, org.alfresco.service.cmr.repository.NodeRef)
     */
    public RenditionLocation getRenditionLocation(NodeRef sourceNode, RenditionDefinition definition, NodeRef tempRenditionLocation)
    {
        // If a destination NodeRef is specified then don't bother to find the location as one has already been specified.
        NodeRef destination = AbstractRenderingEngine.getCheckedParam(RenditionService.PARAM_DESTINATION_NODE, NodeRef.class, definition);
        if(destination != null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("No need to calculate rendition location, using " + RenditionService.PARAM_DESTINATION_NODE + "=" + destination);
            }
            
            RenditionLocationImpl location = createNodeLocation(destination);
            return location;
        }
        
        // If the templated path has been specified and can be resolved then
        // find or create the templated path location and return it.
        String pathTemplate = (String) definition.getParameterValue(RenditionService.PARAM_DESTINATION_PATH_TEMPLATE);
        if (pathTemplate != null)
        {
            
            NodeRef companyHome = getCompanyHomeNode(sourceNode.getStoreRef());
            String path = renderPathTemplate(pathTemplate, sourceNode, tempRenditionLocation, companyHome);
            if(path!=null)
            {
                return findOrCreateTemplatedPath(sourceNode, path, companyHome);
            }
        }

        // Otherwise the rendition will be created as a child of the source content node.
        return new RenditionLocationImpl(sourceNode, null, null);
    }

    /**
     * This method creates a {@link RenditionLocation} object from the specified destination node.
     * This is formed from the specified destination NodeRef, its cm:name and its primary parent.
     * 
     * @param destination
     * @return
     * @throws RenditionServiceException if the destination node does not exist.
     */
    private RenditionLocationImpl createNodeLocation(NodeRef destination)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        if(nodeService.exists(destination)==false)
            throw new RenditionServiceException("The rendition destination node does not exist! NodeRef: "+destination);
        
        NodeRef parentRef = nodeService.getPrimaryParent(destination).getParentRef();
        String destinationCmName = (String) nodeService.getProperty(destination, ContentModel.PROP_NAME);
        RenditionLocationImpl location  = new RenditionLocationImpl(parentRef, destination, destinationCmName);
        return location;
    }

    private RenditionLocationImpl findOrCreateTemplatedPath(NodeRef sourceNode, String path, NodeRef companyHome)
    {
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("FindOrCreateTemplatedPath for ").append(sourceNode).append(", ").append(path);
            log.debug(msg.toString());
        }
        
        NodeService nodeService = serviceRegistry.getNodeService();

        List<String> pathElements = Arrays.asList(path.split("/"));
        LinkedList<String> folderElements = new LinkedList<String>(pathElements);
        
        // We need to strip out any empty strings within the path elements.
        // prior to passing this path to the fileFolderService for creation.
        // e.g. "//foo//bar///item.txt" would cause an exception.
        folderElements.removeAll(Arrays.asList(new String[]{""}));

        // Remove 'Company Home' if it is at the start of the path.
        Serializable companyHomeName = nodeService.getProperty(companyHome, ContentModel.PROP_NAME);
        if(folderElements.getFirst().equals(companyHomeName))
        {
            folderElements.removeFirst();
        }
        
        String fileName = folderElements.removeLast();
        if (fileName == null || fileName.length() == 0)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("The path must include a valid filename! Path: ").append(path);
            if (log.isDebugEnabled())
            {
                log.debug(msg.toString());
            }
            throw new RenditionServiceException(msg.toString());
        }
        
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        NodeRef parent = companyHome;
        if (!folderElements.isEmpty())
        {
            FileInfo parentInfo = FileFolderUtil.makeFolders(fileFolderService,
                        companyHome, folderElements,
                        ContentModel.TYPE_FOLDER);
            parent = parentInfo.getNodeRef();
        }
        
        if (log.isDebugEnabled())
        {
            log.debug("folderElements: " + folderElements);
            log.debug("parent: " + parent);
            log.debug("   " + nodeService.getType(parent) + " " + nodeService.getPath(parent));
            log.debug("fileName: " + fileName);
        }
        
        NodeRef child = fileFolderService.searchSimple(parent, fileName);
        
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("RenditionLocation parent=").append(parent)
               .append(", child=").append(child)
               .append(", fileName=").append(fileName);
            log.debug(msg.toString());
            
            if (child != null)
            {
                log.debug("child path = " + nodeService.getPath(child));
            }
        }
        return new RenditionLocationImpl(parent, child, fileName);
    }

    private String renderPathTemplate(String pathTemplate, NodeRef sourceNode, NodeRef tempRenditionLocation, NodeRef companyHome)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        
        final Map<String, Object> root = new HashMap<String, Object>();

        List<FileInfo> sourcePathInfo;
        String fullSourceName;
        String cwd;
        try
        {
            //Since the root of the store is typically not a folder, we use company home as the root of this tree
            sourcePathInfo = fileFolderService.getNamePath(companyHome, sourceNode);
            //Remove the last element (the actual source file name)
            FileInfo sourceFileInfo = sourcePathInfo.remove(sourcePathInfo.size() - 1);
            fullSourceName = sourceFileInfo.getName();
            
            StringBuilder cwdBuilder = new StringBuilder("/");
            for (FileInfo file : sourcePathInfo)
            {
                cwdBuilder.append(file.getName());
                cwdBuilder.append('/');
            }
            cwd = cwdBuilder.toString();
        }
        catch (FileNotFoundException e)
        {
            log.warn("Failed to resolve path to source node: " + sourceNode + ". Default to Company Home");
            fullSourceName = nodeService.getPrimaryParent(sourceNode).getQName().getLocalName();
            cwd = "/";
        }

        String trimmedSourceName = fullSourceName;
        String sourceExtension = "";
        int extensionIndex = fullSourceName.lastIndexOf('.');
        if (extensionIndex != -1)
        {
            trimmedSourceName = (extensionIndex == 0) ? "" : fullSourceName.substring(0, extensionIndex);
            sourceExtension = (extensionIndex == fullSourceName.length() - 1) ? "" : fullSourceName
                        .substring(extensionIndex + 1);
        }

        root.put("name", trimmedSourceName);
        root.put("extension", sourceExtension);
        root.put("date", new SimpleDate(new Date(), SimpleDate.DATETIME));
        root.put("cwd", cwd);
        TemplateNode companyHomeNode = new TemplateNode(companyHome, serviceRegistry, null); 
        root.put("companyHome", companyHomeNode); 
        root.put("companyhome", companyHomeNode);   //Added this to be consistent with the script API
        root.put("sourceNode", new TemplateNode(sourceNode, serviceRegistry, null));
        root.put("sourceContentType", nodeService.getType(sourceNode).getLocalName());
        root.put("renditionContentType", nodeService.getType(tempRenditionLocation).getLocalName());
        NodeRef person = serviceRegistry.getPersonService().getPerson(AuthenticationUtil.getFullyAuthenticatedUser());
        root.put("person", new TemplateNode(person, serviceRegistry, null));

        if (sourceNodeIsXml(sourceNode))
        {
            try
            {
                Document xml = XMLUtil.parse(sourceNode, serviceRegistry.getContentService());
                pathTemplate = FreeMarkerUtil.buildNamespaceDeclaration(xml) + pathTemplate;
                root.put("xml", NodeModel.wrap(xml));
            } catch (Exception ex)
            {
                log.warn("Failed to parse XML content into path template model: Node = " + sourceNode);
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("Path template model: " + root);
        }

        String result = null;
        try
        {
            if (log.isDebugEnabled())
            {
                log.debug("Processing " + pathTemplate + " using source node " + cwd + fullSourceName);
            }
            result = serviceRegistry.getTemplateService().processTemplateString("freemarker", pathTemplate,
                        new SimpleHash(root));
        } catch (TemplateException te)
        {
            log.error("Error while trying to process rendition path template: " + pathTemplate);
            log.error(te.getMessage(), te);
        }
        if (log.isDebugEnabled())
        {
            log.debug("processed pattern " + pathTemplate + " as " + result);
        }
        return result;
    }

    private NodeRef getCompanyHomeNode(StoreRef store)
    {
        return this.repositoryHelper.getCompanyHome();
    }

    protected boolean sourceNodeIsXml(NodeRef sourceNode)
    {
        boolean result = false;

        // TODO: BJR 20100211: We can do better than this...
        ContentReader reader = serviceRegistry.getContentService().getReader(sourceNode, ContentModel.PROP_CONTENT);
        if ((reader != null) && reader.exists())
        {
            result = (reader.getContentData().getMimetype().equals("text/xml"));
        }
        return result;
    }

}
