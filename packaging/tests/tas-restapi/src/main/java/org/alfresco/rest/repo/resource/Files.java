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
import org.alfresco.rest.repo.resource.content.PlainFileCreator;
import org.alfresco.rest.repo.resource.content.PlainFileModifier;
import org.alfresco.rest.repo.resource.cache.MultiKeyResourceMap;
import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Helper class simplifying things related with repository files management.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Files implements ResourceManager<FileModel, Specifier.FileSpecifier, Modifier.FileModifier>
{
    public static final String FILE_NAME_PREFIX = "file";

    private final DataContent dataContent;
    private final RestWrapper restClient;
    private final SiteModel site;
    private final UserModel user;
    private final Map<String, FileModel> filesCache = new MultiKeyResourceMap<>(FileModel::getNodeRef, FileModel::getName);

    public Files(DataContent dataContent, RestWrapper restClient, UserModel user, SiteModel site)
    {
        this.dataContent = dataContent;
        this.restClient = restClient;
        this.user = user;
        this.site = site;
    }

    public Files(DataContent dataContent, RestWrapper restClient, DataUser dataUser)
    {
        this(dataContent, restClient, dataUser.getAdminUser(), null);
    }

    @Autowired
    public Files(DataContent dataContent, RestWrapper restClient, DataUser dataUser, DataSite dataSite)
    {
        this(dataContent, restClient, dataUser.getAdminUser(), dataSite.usingUser(dataUser.getAdminUser()).createPrivateRandomSite());
    }

    @Override
    public Specifier.FileSpecifier add()
    {
        return (Specifier.FileSpecifier) new PlainFileCreator(dataContent, filesCache).withinSite(site).asUser(user);
    }

    @Override
    public FileModel get(String id)
    {
        return filesCache.get(id);
    }

    @Override
    public Modifier.FileModifier modify(FileModel file)
    {
        return new PlainFileModifier(dataContent, restClient, file, filesCache).withinSite(site).asUser(user);
    }

    @Override
    public void delete(FileModel file)
    {
        new PlainFileModifier(dataContent, restClient, file, filesCache).withinSite(site).asUser(user).delete();
    }
}
