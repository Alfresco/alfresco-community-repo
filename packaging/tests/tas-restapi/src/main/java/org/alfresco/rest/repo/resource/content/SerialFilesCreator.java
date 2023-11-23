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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.MultipleContentsCreator;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FileType;
import org.alfresco.utility.model.FolderModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class SerialFilesCreator
    extends MultipleContentsCreator<FileModel, MultiCreator.FilesCreator>
    implements MultiCreator.FilesCreator
{
    private final DataContent dataContent;
    private List<FileType> fileTypes;
    private List<String> filesContents;
    private final Map<String, FileModel> filesCache;

    public SerialFilesCreator(DataContent dataContent, Map<String, FileModel> filesCache)
    {
        super();
        this.dataContent = dataContent;
        this.filesCache = filesCache;
    }

    @Override
    protected FilesCreator self()
    {
        return this;
    }

    @Override
    public FilesCreator ofTypes(FileType... fileTypes)
    {
        this.fileTypes = List.of(fileTypes);
        return this;
    }

    @Override
    public FilesCreator withContents(List<String> filesContents)
    {
        this.filesContents = filesContents;
        return this;
    }

    @Override
    public FilesCreator withRandomContents(int wordsCount, int wordsMaxLength)
    {
        return withContents(
            IntStream.of(0, names.size())
                .mapToObj(i -> IntStream.range(0, wordsCount)
                    .mapToObj(j -> RandomStringUtils.randomAlphanumeric(1, wordsMaxLength))
                    .collect(Collectors.joining(" ")))
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<FileModel> create()
    {
        verifyDataConsistency();
        return createRawFilesUnder(parent, names);
    }

    @Override
    protected void verifyDataConsistency()
    {
        super.verifyDataConsistency();
        if (CollectionUtils.isEmpty(fileTypes) || fileTypes.size() < names.size())
        {
            throw new IllegalArgumentException("Provided file types size is different from created files amount");
        }
        if (CollectionUtils.isEmpty(filesContents) || filesContents.size() < names.size())
        {
            throw new IllegalArgumentException("Provided file contents size is different from created files amount");
        }
    }

    @Override
    protected String generateRandomName()
    {
        return this.generateRandomNameWith(EMPTY);
    }

    @Override
    protected String generateRandomNameWith(String prefix)
    {
        return super.generateRandomNameWith(FILE_NAME_PREFIX + prefix + "_");
    }

    protected FileModel createFile(String fileName, FileType fileType, String title, String description, String fileContent, FolderModel parent, String alias)
    {
        return new PlainFileCreator(dataContent, filesCache)
            .withAlias(alias)
            .withName(fileName)
            .ofType(fileType)
            .withTitle(title)
            .withDescription(description)
            .withContent(fileContent)
            .underFolder(parent)
            .withinSite(site)
            .asUser(user)
            .create();
    }

    private List<FileModel> createRawFilesUnder(FolderModel parent, List<String> fileNames)
    {
        List<FileModel> createdFiles = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(0);
        fileNames.forEach(fileName -> {
            createdFiles.add(createFile(fileName, getOrNull(fileTypes, i.get()), getOrNull(titles, i.get()), getOrNull(descriptions, i.get()),
                getOrNull(filesContents, i.get()), parent, getOrNull(aliases, i.get())));
            i.getAndIncrement();
        });

        return createdFiles;
    }
}
