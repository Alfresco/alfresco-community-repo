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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Implementation of the {@link AlfrescoAPIFacet} interface that provides access
 * to Alfresco services at the level of the Java API, through its
 * {@link ServiceRegistry} instance.
 *
 * @author Bogdan Horje
 */
public class PublicAPIFacet implements AlfrescoAPIFacet
{
    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public ScriptService getScriptService()
    {
        return serviceRegistry.getScriptService();
    }

    @Override
    public NodeService getNodeService()
    {
        return serviceRegistry.getNodeService();
    }

    @Override
    public ContentService getContentService()
    {
        return serviceRegistry.getContentService();
    }

    @Override
    public SearchService getSearchService()
    {
        return serviceRegistry.getSearchService();
    }

    @Override
    public DictionaryService getDictionaryService()
    {
        return serviceRegistry.getDictionaryService();
    }

    @Override
    public FileFolderService getFileFolderService()
    {
        return serviceRegistry.getFileFolderService();
    }

    @Override
    public PermissionService getPermissionService()
    {
        return serviceRegistry.getPermissionService();
    }
}
