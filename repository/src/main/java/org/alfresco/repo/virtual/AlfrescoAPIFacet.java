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

import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * Dependency inversion facade of the Alfresco services. It provides access to
 * the implementations of Alfresco services.
 * 
 * @author Bogdan Horje
 */
public interface AlfrescoAPIFacet
{

    @NotAuditable
    ScriptService getScriptService();

    @NotAuditable
    NodeService getNodeService();

    @NotAuditable
    ContentService getContentService();

    @NotAuditable
    SearchService getSearchService();

    @NotAuditable
    DictionaryService getDictionaryService();

    @NotAuditable
    FileFolderService getFileFolderService();

    @NotAuditable
    PermissionService getPermissionService();

}
