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
package org.alfresco.rest.repo.resource.content;

import java.util.Map;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.repo.resource.Files;
import org.alfresco.rest.repo.resource.general.ContentModifier;
import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FolderModel;

public class RepoFolderModifier
    extends ContentModifier<FolderModel, Modifier.FolderModifier>
    implements Modifier.FolderModifier
{

    private final RestWrapper restClient;
    private final DataContent dataContent;
    private final Files files;
    private final Map<String, FolderModel> foldersCache;

    public RepoFolderModifier(DataContent dataContent, RestWrapper restClient, FolderModel folder, Files files, Map<String, FolderModel> foldersCache)
    {
        super(dataContent, restClient, folder);
        this.dataContent = dataContent;
        this.restClient = restClient;
        this.files = files;
        this.foldersCache = foldersCache;
    }

    @Override
    protected FolderModifier self()
    {
        return this;
    }

    @Override
    public Specifier.MultiContentSpecifier add()
    {
        return (Specifier.MultiContentSpecifier) new MultiContentCreator(dataContent, restClient, files, foldersCache)
            .underFolder(contentModel).withinSite(site).asUser(user);
    }

    @Override
    public Specifier.AssociationSpecifier remove()
    {
        return new Specifier.AssociationSpecifier()
        {
            @Override
            public void secondaryContent(ContentModel content)
            {
                buildNodeRestRequest(restClient, contentModel).removeSecondaryChild(content);
            }

            @Override
            public void tag(RestTagModel tag)
            {
                buildNodeRestRequest(restClient, contentModel).deleteTag(tag);
            }
        };
    }

    @Override
    public FolderModel get(String id)
    {
        return super.get(id, FolderModel::new);
    }

    @Override
    public FolderModel copyTo(FolderModel target)
    {
        return super.copyTo(target, FolderModel::new);
    }

    @Override
    public void delete()
    {
        foldersCache.remove(contentModel.getName());
        super.delete();
    }
}
