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

import static org.alfresco.rest.repo.resource.Files.FILE_NAME_PREFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Map;

import org.alfresco.rest.repo.resource.general.ContentCreator;
import org.alfresco.rest.repo.resource.general.Creator;
import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;

public class PlainFileCreator
    extends ContentCreator<FileModel, Creator.FileCreator>
    implements Creator.FileCreator, Specifier.FileSpecifier
{

    private final DataContent dataContent;
    private final Map<String, FileModel> filesCache;

    public PlainFileCreator(DataContent dataContent, Map<String, FileModel> filesCache)
    {
        super(new FileModel());
        this.dataContent = dataContent;
        this.filesCache = filesCache;
        this.contentModel.setFileType(FileType.TEXT_PLAIN);
    }

    @Override
    protected FileCreator self()
    {
        return this;
    }

    @Override
    public FileCreator withRandomName(String prefix)
    {
        withAlias(prefix);
        return super.withRandomName(prefix);
    }

    @Override
    public FileCreator file(String name)
    {
        return this.withName(name);
    }

    @Override
    public FileCreator randomFile()
    {
        return this.withRandomName();
    }

    @Override
    public FileCreator randomFile(String prefix)
    {
        return this.withRandomName(prefix);
    }

    @Override
    public MultiCreator.FilesCreator files(String... names)
    {
        return new SerialFilesCreator(dataContent, filesCache).withNames(names).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FilesCreator randomFiles(String... prefixes)
    {
        return new SerialFilesCreator(dataContent, filesCache).withRandomNames(prefixes).withinSite(site).asUser(user);
    }

    @Override
    public MultiCreator.FilesCreator randomFiles(int quantity)
    {
        return new SerialFilesCreator(dataContent, filesCache).withRandomNames(quantity).withinSite(site).asUser(user);
    }

    @Override
    public FileCreator ofType(FileType fileType)
    {
        contentModel.setFileType(fileType);
        return this;
    }

    @Override
    public FileCreator withContent(String fileContent)
    {
        contentModel.setContent(fileContent);
        return this;
    }

    @Override
    public FileModel create()
    {
        contentModel.setName(contentModel.getName() + "." + contentModel.getFileType().extension);
        FileModel createdFile = create(dataContent, dataContent::createContent);
        filesCache.put(alias, createdFile);

        return createdFile;
    }

    @Override
    protected String generateRandomName()
    {
        return this.generateRandomNameWith(EMPTY);
    }

    @Override
    protected String generateRandomNameWith(String prefix, String suffix)
    {
        return super.generateRandomNameWith(FILE_NAME_PREFIX + prefix + "_", suffix);
    }
}
