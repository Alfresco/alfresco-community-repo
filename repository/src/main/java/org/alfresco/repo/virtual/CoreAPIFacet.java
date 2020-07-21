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

package org.alfresco.repo.virtual;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Implementation of the {@link AlfrescoAPIFacet} interface that provides access
 * to "core" Alfresco services, i.e. services in a lower abstraction layer than
 * the ones provided in the Java API, by the {@link ServiceRegistry}.
 *
 * @author Bogdan Horje
 */
public class CoreAPIFacet implements AlfrescoAPIFacet
{

    private ScriptService scriptService;

    private NodeService nodeService;

    private ContentService contentService;

    private SearchService searchService;

    private DictionaryService dictionaryService;

    private FileFolderService fileFolderService;

    private PermissionService permissionService;

    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    @Override
    public ScriptService getScriptService()
    {
        return scriptService;
    }

    @Override
    public NodeService getNodeService()
    {
        return nodeService;
    }

    @Override
    public ContentService getContentService()
    {
        return contentService;
    }

    @Override
    public SearchService getSearchService()
    {
        return searchService;
    }

    @Override
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    @Override
    public FileFolderService getFileFolderService()
    {
        return fileFolderService;
    }

    @Override
    public PermissionService getPermissionService()
    {
        return permissionService;
    }

}
