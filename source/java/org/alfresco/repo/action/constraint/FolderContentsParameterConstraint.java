/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

package org.alfresco.repo.action.constraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Folder contents parameter constraint
 * 
 * @author Roy Wetherall
 */
public class FolderContentsParameterConstraint extends BaseParameterConstraint
{
    private NodeService nodeService;
    
    private SearchService searchService;
    
    private DictionaryService dictionaryService;
    
    private String searchPath;
    
    private List<String> nodeInclusionFilter = Collections.emptyList();
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setSearchPath(String searchPath)
    {
        this.searchPath = searchPath;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * This optional property defines a list of file extensions which should be included in the result set from
     * this class. By implication, all other file extensions will be excluded. (The dot should not be specified
     * i.e. "txt" not ".txt").
     * If present, the cm:name of each candidate node will be checked against the values in this list and
     * only those nodes whose cm:name ends with one of these file extensions will be included.
     * <p/>
     * If the property is not set then no inclusion filter is specified and all file extensions will
     * be included.
     * 
     * @param nodeInclusionFilter A list of file extensions
     * @since 3.5
     */
    public void setNodeInclusionFilter(List<String> nodeInclusionFilter)
    {
        // We'll convert the extensions from spring to dot+extension
        this.nodeInclusionFilter = new ArrayList<String>(nodeInclusionFilter.size());
        for (String extension : nodeInclusionFilter)
        {
            StringBuilder dotExt = new StringBuilder().append(".").append(extension);
            this.nodeInclusionFilter.add(dotExt.toString());
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {   
        ResultSet resultSet = searchService.query(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, 
                SearchService.LANGUAGE_LUCENE, 
                "PATH:\"" + searchPath + "\"");
        NodeRef rootFolder = null;
        if (resultSet.length() == 0)
        {
            throw new AlfrescoRuntimeException("The path '" + searchPath + "' did not return any results.");
        }
        else
        {
            rootFolder = resultSet.getNodeRef(0);
        }
        
        Map<String, String> result = new HashMap<String, String>(23);
        buildMap(result, rootFolder);        
        return result;
    }        
    
    private void buildMap(Map<String, String> result, NodeRef folderNodeRef)
    {
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(folderNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef nodeRef = assoc.getChildRef();
            QName className = nodeService.getType(nodeRef);
            if (dictionaryService.isSubClass(className, ContentModel.TYPE_CONTENT) == true)
            {
                if(isCmNameAcceptable(nodeRef))
                {
                    String title = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
                    if (title != null && title.length() > 0)
                    {
                        result.put(nodeRef.toString(), title);
                    }
                    else
                    {
                        result.put(nodeRef.toString(), (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
                    }
                }
            }
            else if (dictionaryService.isSubClass(className, ContentModel.TYPE_FOLDER) == true)
            {
                buildMap(result, nodeRef);
            }
        }
    }

    /**
     * Folder contents as returned by this class can be filtered based on the cm:name of the
     * contained content nodes. If no file extensions are included, then all content NodeRefs
     * will be included in the result set.
     * If however, there are any file extensions specified, then the cm:name must match one of
     * those file extensions to be included in the result set.
     * 
     * @param nodeRef the node whose cm:name is to be checked.
     * @return <code>true</code> if cm:name is acceptable, else <code>false</code>.
     */
    private boolean isCmNameAcceptable(NodeRef nodeRef)
    {
        // Implementation node: this is very similar to a MIME type check. However, it is
        // more forgiving in that content with the wrong MIME type will be correctly included.
        // e.g. it is fairly common for .js files to be saved with a MIME type of text/plain.
        boolean result = true;
        
        if (!nodeInclusionFilter.isEmpty())
        {
            result = false;
            String cmName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
            
            for (String extension : nodeInclusionFilter)
            {
                if (cmName.endsWith(extension))
                {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
}
