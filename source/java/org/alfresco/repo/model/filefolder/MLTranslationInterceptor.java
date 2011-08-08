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
package org.alfresco.repo.model.filefolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.Pair;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An interceptor that replaces files nodes with their equivalent
 * translations according to the locale.  It is to be used with the
 * {@link FileFolderService}.
 * 
 * @since 2.1
 * @author Derek Hulley
 */
public class MLTranslationInterceptor implements MethodInterceptor
{
    /**
     * Names of methods that return a <code>List</code> or <code>FileInfo</code> instances.
     */
    private static final Set<String> METHOD_NAMES_LIST;
    /**
     * Names of methods that return a <code>FileInfo</code>.
     */
    private static final Set<String> METHOD_NAMES_SINGLE;
    /**
     * Names of methods that don't need interception.  This is used to catch any new methods
     * added to the interface.
     */
    private static final Set<String> METHOD_NAMES_OTHER;
    static
    {
        METHOD_NAMES_LIST = new HashSet<String>(13);
        METHOD_NAMES_LIST.add("list");
        METHOD_NAMES_LIST.add("listFiles");
        METHOD_NAMES_LIST.add("listFolders");
        METHOD_NAMES_LIST.add("search");
        METHOD_NAMES_LIST.add("getNamePath");
        
        METHOD_NAMES_SINGLE = new HashSet<String>(13);
        METHOD_NAMES_SINGLE.add("getLocalizedSibling");
        METHOD_NAMES_SINGLE.add("searchSimple");
        METHOD_NAMES_SINGLE.add("rename");
        METHOD_NAMES_SINGLE.add("move");
        METHOD_NAMES_SINGLE.add("copy");
        METHOD_NAMES_SINGLE.add("create");
        METHOD_NAMES_SINGLE.add("makeFolders");
        METHOD_NAMES_SINGLE.add("getNamePath");
        METHOD_NAMES_SINGLE.add("resolveNamePath");
        METHOD_NAMES_SINGLE.add("getFileInfo");
        
        METHOD_NAMES_OTHER = new HashSet<String>(13);
        METHOD_NAMES_OTHER.add("delete");
        METHOD_NAMES_OTHER.add("getReader");
        METHOD_NAMES_OTHER.add("getWriter");
        METHOD_NAMES_OTHER.add("getType");
    }
    
    private static Log logger = LogFactory.getLog(MLTranslationInterceptor.class);

    private NodeService nodeService;
    private MultilingualContentService multilingualContentService;
    private FileFolderService fileFolderService;
    
    /**
     * Constructor.
     */
    public MLTranslationInterceptor()
    {
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setMultilingualContentService(MultilingualContentService multilingualContentService)
    {
        this.multilingualContentService = multilingualContentService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * Converts the node referenice where an alternative translation should be used.
     * 
     * @param nodeRef       the basic nodeRef
     * @return              Returns the replacement if required
     */
    private NodeRef getTranslatedNodeRef(NodeRef nodeRef)
    {
        // Ignore null
        if (nodeRef == null)
        {
            return nodeRef;
        }
        // Ignore everything without the correct aspect
        if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            return nodeRef;
        }
        Locale filterLocale = I18NUtil.getContentLocaleOrNull();
        if (filterLocale == null)
        {
            // We aren't doing any filtering
            return nodeRef;
        }
        // Find the best translation.  This won't return null.
        NodeRef translatedNodeRef = multilingualContentService.getTranslationForLocale(nodeRef, filterLocale);
        // Done
        if (logger.isDebugEnabled())
        {
            if (nodeRef.equals(translatedNodeRef))
            {
                logger.debug("NodeRef substitution: " + nodeRef + " --> " + translatedNodeRef);
            }
            else
            {
                logger.debug("NodeRef substitution: " + nodeRef + " (no change)");
            }
        }
        return translatedNodeRef;
    }
    
    /**
     * Converts the file info where an alternative translation should be used.
     * 
     * @param fileInfo      the basic file or folder info
     * @return              Returns a replacement if required
     */
    private FileInfo getTranslatedFileInfo(FileInfo fileInfo)
    {
        // Ignore null
        if (fileInfo == null)
        {
            return null;
        }
        // Ignore folders
        if (fileInfo.isFolder())
        {
            return fileInfo;
        }
        NodeRef nodeRef = fileInfo.getNodeRef();
        // Get the best translation for the node
        NodeRef translatedNodeRef = getTranslatedNodeRef(nodeRef);
        // Convert to FileInfo, if required
        FileInfo translatedFileInfo = null;
        if (nodeRef.equals(translatedNodeRef))
        {
            // No need to do any more work
            translatedFileInfo = fileInfo;
        }
        else
        {
            // Get the FileInfo
            translatedFileInfo = fileFolderService.getFileInfo(translatedNodeRef);
        }
        return translatedFileInfo;
    }
    
    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        Object ret = null;
        String methodName = invocation.getMethod().getName();
        
        if (I18NUtil.getContentLocaleOrNull() == null)
        {
            // This can shortcut anything as there is no filtering going on
            return invocation.proceed();
        }
        else if (METHOD_NAMES_LIST.contains(methodName))
        {
        	final PagingResults<FileInfo> fileInfos = (PagingResults<FileInfo>) invocation.proceed();
            // Compile a set to ensure we don't get duplicates
            Map<FileInfo, FileInfo> translatedFileInfos = new HashMap<FileInfo, FileInfo>(17);
            for (FileInfo fileInfo : fileInfos.getPage())
            {
                FileInfo translatedFileInfo = getTranslatedFileInfo(fileInfo);
                // Add this to the set
                translatedFileInfos.put(fileInfo, translatedFileInfo);
            }
            // Convert the set back to a PagingResults
            final List<FileInfo> orderedResults = new ArrayList<FileInfo>(fileInfos.getPage().size());
            PagingResults<FileInfo> orderedPagingResults = new PagingResults<FileInfo>()
            {
                @Override
                public String getQueryExecutionId()
                {
                    return fileInfos.getQueryExecutionId();
                }
                @Override
                public List<FileInfo> getPage()
                {
                    return orderedResults;
                }
                @Override
                public boolean hasMoreItems()
                {
                    return fileInfos.hasMoreItems();
                }
                @Override
                public Pair<Integer, Integer> getTotalResultCount()
                {
                    return fileInfos.getTotalResultCount();
                }
            };
            Set<FileInfo> alreadyPresent = new HashSet<FileInfo>(fileInfos.getPage().size() * 2 + 1);
            for (FileInfo info : fileInfos.getPage())
            {
                FileInfo translatedFileInfo = translatedFileInfos.get(info);
                if (alreadyPresent.contains(translatedFileInfo))
                {
                    // We've done this one
                    continue;
                }
                alreadyPresent.add(translatedFileInfo);
                orderedResults.add(translatedFileInfo);
            }
            ret = orderedPagingResults;
        }
        else if (METHOD_NAMES_SINGLE.contains(methodName))
        {
            Object obj = invocation.proceed();
            if (obj instanceof FileInfo)
            {
                FileInfo fileInfo = (FileInfo) obj;
                ret = getTranslatedFileInfo(fileInfo);
            }
            else if (obj instanceof NodeRef)
            {
                NodeRef nodeRef = (NodeRef) obj;
                ret = getTranslatedNodeRef(nodeRef);
            }
        }
        else if (METHOD_NAMES_OTHER.contains(methodName))
        {
            // There is nothing to do
            ret = invocation.proceed();
        }
        else
        {
            throw new RuntimeException("Method not handled by interceptor: " + methodName);
        }
        
        // Done
        return ret;
    }
}