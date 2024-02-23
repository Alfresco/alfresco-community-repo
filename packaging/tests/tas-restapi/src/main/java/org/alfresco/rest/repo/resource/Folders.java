/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.rest.repo.resource;

import java.util.Map;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.repo.resource.content.RepoFolderCreator;
import org.alfresco.rest.repo.resource.content.RepoFolderModifier;
import org.alfresco.rest.repo.resource.cache.MultiKeyResourceMap;
import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Helper class simplifying things related with repository folders management.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Folders implements ResourceManager<FolderModel, Specifier.FolderSpecifier, Modifier.FolderModifier>
{
    public static final String FOLDER_NAME_PREFIX = "folder";

    private final DataContent dataContent;
    private final RestWrapper restClient;
    private final SiteModel site;
    private final UserModel user;
    private final Files files;

    private final Map<String, FolderModel> foldersCache = new MultiKeyResourceMap<>(FolderModel::getNodeRef, FolderModel::getName);

    public Folders(DataContent dataContent, RestWrapper restClient, UserModel user, SiteModel site, Files files)
    {
        this.dataContent = dataContent;
        this.restClient = restClient;
        this.site = site;
        this.user = user;
        this.files = files;
    }

    public Folders(DataContent dataContent, RestWrapper restClient, UserModel user, SiteModel site)
    {
        this(dataContent, restClient, user, site, new Files(dataContent, restClient, user, site));
    }

    public Folders(DataContent dataContent, RestWrapper restClient, DataUser dataUser)
    {
        this(dataContent, restClient, dataUser.getAdminUser(), null);
    }

    @Autowired
    public Folders(DataContent dataContent, RestWrapper restClient, DataUser dataUser, DataSite dataSite)
    {
        this(dataContent, restClient, dataUser.getAdminUser(), dataSite.usingUser(dataUser.getAdminUser()).createPrivateRandomSite());
    }

    @Override
    public Specifier.FolderSpecifier add()
    {
        return (Specifier.FolderSpecifier) new RepoFolderCreator(dataContent, foldersCache).withinSite(site).asUser(user);
    }

    @Override
    public FolderModel get(String key)
    {
        return foldersCache.get(key);
    }

    @Override
    public Modifier.FolderModifier modify(FolderModel folder)
    {
        return new RepoFolderModifier(dataContent, restClient, folder, files, foldersCache).withinSite(site).asUser(user);
    }

    @Override
    public void delete(FolderModel folder)
    {
        new RepoFolderModifier(dataContent, restClient, folder, files, foldersCache).withinSite(site).asUser(user).delete();
    }
}
