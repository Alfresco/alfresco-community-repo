package org.alfresco.rest.repo.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.repo.resource.cache.MultiKeyResourceMap;
import org.alfresco.rest.repo.resource.general.ContentCreator;
import org.alfresco.rest.repo.resource.general.ContentModifier;
import org.alfresco.rest.repo.resource.general.Creator;
import org.alfresco.rest.repo.resource.general.Modifier;
import org.alfresco.rest.repo.resource.general.MultiCreator;
import org.alfresco.rest.repo.resource.general.MultipleContentsCreator;
import org.alfresco.rest.repo.resource.general.Specifier;
import org.alfresco.utility.data.DataContent;
import org.alfresco.utility.data.DataSite;
import org.alfresco.utility.data.DataUser;
import org.alfresco.utility.model.FileModel;
import org.alfresco.utility.model.FolderModel;
import org.alfresco.utility.model.SiteModel;
import org.alfresco.utility.model.UserModel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
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
    private static final String FILE_NAME_PREFIX = "file";
    private static final String FILE_EXTENSION = ".txt";

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

    public static class PlainFileCreator
        extends ContentCreator<FileModel, Creator.FileCreator>
        implements Creator.FileCreator, Specifier.FileSpecifier
    {
        private final DataContent dataContent;
        private final Map<String, FileModel> filesCache;

        private PlainFileCreator(DataContent dataContent, Map<String, FileModel> filesCache)
        {
            super(new FileModel());
            this.dataContent = dataContent;
            this.filesCache = filesCache;
        }

        @Override
        protected FileCreator self()
        {
            return this;
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
        public FileCreator randomFile(String nameSuffix)
        {
            return this.withRandomName(nameSuffix);
        }

        @Override
        public FileCreator withRandomName(String nameSuffix)
        {
            withAlias(nameSuffix);
            return super.withRandomName(FILE_NAME_PREFIX + nameSuffix + "_", FILE_EXTENSION);
        }

        @Override
        public MultiCreator.FilesCreator files(String... names)
        {
            return new SerialFilesCreator(dataContent, filesCache).withNames(names).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FilesCreator randomFiles(String... nameSuffixes)
        {
            return new SerialFilesCreator(dataContent, filesCache).withRandomNames(nameSuffixes).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FilesCreator randomFiles(int quantity)
        {
            return new SerialFilesCreator(dataContent, filesCache).withRandomNames(quantity).withinSite(site).asUser(user);
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
            FileModel createdFile = create(dataContent, dataContent::createContent);
            filesCache.put(alias, createdFile);

            return createdFile;
        }

        @Override
        protected String generateRandomName()
        {
            return FILE_NAME_PREFIX + "_" + UUID.randomUUID() + FILE_EXTENSION;
        }
    }

    public static class SerialFilesCreator
        extends MultipleContentsCreator<FileModel, MultiCreator.FilesCreator>
        implements MultiCreator.FilesCreator
    {
        private final DataContent dataContent;
        private List<String> filesContents;
        private final Map<String, FileModel> filesCache;

        private SerialFilesCreator(DataContent dataContent, Map<String, FileModel> filesCache)
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
            if (CollectionUtils.isEmpty(filesContents) || filesContents.size() < names.size())
            {
                throw new IllegalArgumentException("Provided file contents size is different from created files/folders amount");
            }
        }

        private List<FileModel> createRawFilesUnder(FolderModel parent, List<String> fileNames)
        {
            List<FileModel> createdFiles = new ArrayList<>();
            AtomicInteger i = new AtomicInteger(0);
            fileNames.forEach(fileName -> {
                createdFiles.add(createFile(fileName, getOrNull(titles, i.get()), getOrNull(descriptions, i.get()),
                    getOrNull(filesContents, i.get()), parent, getOrNull(aliases, i.get())));
                i.getAndIncrement();
            });

            return createdFiles;
        }

        protected FileModel createFile(String fileName, String title, String description, String fileContent, FolderModel parent, String alias)
        {
            return new PlainFileCreator(dataContent, filesCache)
                .withAlias(alias)
                .withName(fileName)
                .withTitle(title)
                .withDescription(description)
                .withContent(fileContent)
                .underFolder(parent)
                .withinSite(site)
                .asUser(user)
                .create();
        }
    }

    public static class PlainFileModifier
        extends ContentModifier<FileModel, Modifier.FileModifier>
        implements Modifier.FileModifier
    {
        private final Map<String, FileModel> filesCache;

        private PlainFileModifier(DataContent dataContent, RestWrapper restClient, FileModel file, Map<String, FileModel> filesCache)
        {
            super(dataContent, restClient, file);
            this.filesCache = filesCache;
        }

        @Override
        protected FileModifier self()
        {
            return this;
        }

        @Override
        public FileModel get(String id)
        {
            return super.get(id, FileModel::new);
        }

        @Override
        public FileModel copyTo(FolderModel target)
        {
            return super.copyTo(target, FileModel::new);
        }

        @Override
        public void delete()
        {
            filesCache.remove(contentModel.getNodeRef());
            super.delete();
        }
    }
}
