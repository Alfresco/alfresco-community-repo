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
import java.util.stream.Stream;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestTagModel;
import org.alfresco.rest.repo.resource.Files;
import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.ContentModel;
import org.alfresco.utility.model.FolderModel;

public class MultiContentCreator extends RepoFolderCreator implements Specifier.MultiContentSpecifier
{
    private final RestWrapper restClient;
    private final Files files;

    protected MultiContentCreator(DataContent dataContent, RestWrapper restClient, Files files, Map<String, FolderModel> folders)
    {
        super(dataContent, folders);
        this.files = files;
        this.restClient = restClient;
    }

    @Override
    public FileCreator file(String name)
    {
        return files.add().file(name).underFolder(parent).withinSite(site).asUser(user);
    }

    @Override
    public FileCreator randomFile()
    {
        return files.add().randomFile().underFolder(parent).withinSite(site).asUser(user);
    }

    @Override
    public FileCreator randomFile(String prefix)
    {
        return files.add().randomFile(prefix).underFolder(parent).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FilesCreator files(String... names)
    {
        return files.add().files(names).underFolder(parent).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FilesCreator randomFiles(String... prefixes)
    {
        return files.add().randomFiles(prefixes).underFolder(parent).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FilesCreator randomFiles(int quantity)
    {
        return files.add().randomFiles(quantity).underFolder(parent).withinSite(site).asUser(user);
    }

    @Override
    public void secondaryContent(ContentModel content)
    {
        buildNodeRestRequest(restClient, parent).addSecondaryChild(content);
    }

    @Override
    public void secondaryContent(ContentModel... contents)
    {
        buildNodeRestRequest(restClient, parent).addSecondaryChildren(contents);
    }

    @Override
    public void tag(RestTagModel tag)
    {
        buildNodeRestRequest(restClient, parent).addTag(tag.getTag());
    }

    @Override
    public void tags(RestTagModel... tags)
    {
        buildNodeRestRequest(restClient, parent).addTags(Stream.of(tags).map(RestTagModel::getTag).toArray(String[]::new));
    }
}
