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
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl;
import org.alfresco.repo.rendition.executer.AbstractRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
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
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
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
    private String companyHomePath = "/app:company_home";

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setCompanyHomePath(String companyHomePath)
    {
        this.companyHomePath = companyHomePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.alfresco.repo.rendition.RenditionLocationResolver#
     * resolveRenditionPrimaryParentAssoc
     * (org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.cmr.rendition.RenditionDefinition,
     * org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    public RenditionLocation getRenditionLocation(NodeRef sourceNode, RenditionDefinition definition, NodeRef tempRenditionLocation)
    {
        // If a destination NodeRef is specified then don't botther to find the location as one has already been specified.
        NodeRef destination = AbstractRenderingEngine.getCheckedParam(RenditionService.PARAM_DESTINATION_NODE, NodeRef.class, definition);
        if(destination!=null)
        {
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

    private RenditionLocationImpl createNodeLocation(NodeRef destination)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        if(nodeService.exists(destination)==false)
            throw new RenditionServiceException("The rendition destination node does not exist! NodeRef: "+destination);
        NodeRef parentRef = nodeService.getPrimaryParent(destination).getParentRef();
        String childName = (String) nodeService.getProperty(destination, ContentModel.PROP_NAME);
        RenditionLocationImpl location  = new RenditionLocationImpl(parentRef, destination, childName);
        return location;
    }

    private RenditionLocationImpl findOrCreateTemplatedPath(NodeRef sourceNode, String path, NodeRef companyHome)
    {
        NodeService nodeService = serviceRegistry.getNodeService();

        List<String> pathElements = Arrays.asList(path.split("/"));
        LinkedList<String> folderElements = new LinkedList<String>(pathElements);

        // Remove empty folder caused by path starting with / .
        if(folderElements.getFirst().length() == 0)
        {
            folderElements.removeFirst();
        }
        // Remove 'Company Home' if it is at the start of the path.
        Serializable companyHomeName = nodeService.getProperty(companyHome, ContentModel.PROP_NAME);
        if(folderElements.getFirst().equals(companyHomeName))
        {
            folderElements.removeFirst();
        }
        
        String fileName = folderElements.removeLast();
        if (fileName == null || fileName.length() == 0)
        {
            throw new RenditionServiceException("The path must include a valid filename! Path: " + path);
        }
        FileFolderService fileFolderService = serviceRegistry.getFileFolderService();
        FileInfo parentInfo = FileFolderServiceImpl.makeFolders(fileFolderService,
                    companyHome, folderElements,
                    ContentModel.TYPE_FOLDER);
        NodeRef parent = parentInfo.getNodeRef();
        NodeRef child = fileFolderService.searchSimple(parent, fileName);
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
        root.put("companyHome", new TemplateNode(companyHome, serviceRegistry, null));
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
        SearchService searchService = serviceRegistry.getSearchService();
        ResultSet result = searchService.query(store, SearchService.LANGUAGE_XPATH, companyHomePath);
        if(result.length()==0)
            return null;
        else
            return result.getNodeRef(0);
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
