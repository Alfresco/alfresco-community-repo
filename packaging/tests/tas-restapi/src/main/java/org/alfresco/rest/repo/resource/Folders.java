package org.alfresco.rest.repo.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.alfresco.rest.core.RestWrapper;
import org.alfresco.rest.model.RestTagModel;
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
import org.alfresco.utility.model.ContentModel;
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
    private static final String FOLDER_NAME_PREFIX = "folder";

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

    public static class RepoFolderCreator
        extends ContentCreator<FolderModel, Creator.FolderCreator>
        implements Creator.FolderCreator, Specifier.FolderSpecifier
    {
        private final DataContent dataContent;
        private final Map<String, FolderModel> foldersCache;

        private RepoFolderCreator(DataContent dataContent, Map<String, FolderModel> foldersCache)
        {
            super(new FolderModel());
            this.dataContent = dataContent;
            this.foldersCache = foldersCache;
        }

        @Override
        protected RepoFolderCreator self()
        {
            return this;
        }

        @Override
        public FolderCreator folder(String name)
        {
            return this.withName(name);
        }

        @Override
        public FolderCreator randomFolder()
        {
            return this.withRandomName();
        }

        @Override
        public FolderCreator randomFolder(String nameSuffix)
        {
            return this.withRandomName(nameSuffix);
        }

        @Override
        public MultiCreator.FoldersCreator folders(String... names)
        {
            return new SerialFoldersCreator(dataContent, foldersCache).withNames(names).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FoldersCreator randomFolders(String... nameSuffixes)
        {
            return new SerialFoldersCreator(dataContent, foldersCache).withRandomNames(nameSuffixes).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FoldersCreator randomFolders(int quantity)
        {
            return new SerialFoldersCreator(dataContent, foldersCache).withRandomNames(quantity).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FoldersCreator nestedFolders(String... names)
        {
            return new NestedFoldersCreator(dataContent, foldersCache).withNames(names).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FoldersCreator nestedRandomFolders(String... nameSuffixes)
        {
            return new NestedFoldersCreator(dataContent, foldersCache).withRandomNames(nameSuffixes).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FoldersCreator nestedRandomFolders(int depth)
        {
            return new NestedFoldersCreator(dataContent, foldersCache).withRandomNames(depth).withinSite(site).asUser(user);
        }

        @Override
        public FolderCreator withRandomName(String nameSuffix)
        {
            withAlias(nameSuffix);
            return super.withRandomName(FOLDER_NAME_PREFIX + nameSuffix + "_");
        }

        @Override
        public FolderModel create()
        {
            FolderModel createdFolder = create(dataContent, dataContent::createFolder);
            foldersCache.put(alias, createdFolder);

            return createdFolder;
        }

        @Override
        protected String generateRandomName()
        {
            return super.generateRandomNameWith(FOLDER_NAME_PREFIX + "_");
        }
    }

    public static class SerialFoldersCreator
        extends MultipleContentsCreator<FolderModel, MultiCreator.FoldersCreator>
        implements MultiCreator.FoldersCreator
    {
        private final DataContent dataContent;
        private final Map<String, FolderModel> foldersCache;

        private SerialFoldersCreator(DataContent dataContent, Map<String, FolderModel> foldersCache)
        {
            this.dataContent = dataContent;
            this.foldersCache = foldersCache;
        }

        @Override
        protected SerialFoldersCreator self()
        {
            return this;
        }

        @Override
        protected String generateRandomName()
        {
            return super.generateRandomNameWith(FOLDER_NAME_PREFIX + "_");
        }

        @Override
        protected String generateRandomNameWith(String prefix)
        {
            return super.generateRandomNameWith(FOLDER_NAME_PREFIX + prefix + "_");
        }

        @Override
        public List<FolderModel> create()
        {
            verifyDataConsistency();
            return createRawFoldersUnder(parent, names);
        }

        private List<FolderModel> createRawFoldersUnder(FolderModel parent, List<String> folderNames)
        {
            List<FolderModel> createdFolders = new ArrayList<>();
            AtomicInteger i = new AtomicInteger();
            folderNames.forEach(folderName -> {
                FolderModel createdFolder = createFolder(folderName, getOrNull(titles, i.get()), getOrNull(descriptions, i.get()), parent, getOrNull(aliases, i.get()));
                createdFolders.add(createdFolder);
                i.getAndIncrement();
            });

            return createdFolders;
        }

        protected FolderModel createFolder(String folderName, String title, String description, FolderModel parent, String alias)
        {
            return new RepoFolderCreator(dataContent, foldersCache)
                .withAlias(alias)
                .withName(folderName)
                .withTitle(title)
                .withDescription(description)
                .underFolder(parent)
                .withinSite(site)
                .asUser(user)
                .create();
        }
    }

    public static class NestedFoldersCreator extends SerialFoldersCreator
    {
        private NestedFoldersCreator(DataContent dataContent, Map<String, FolderModel> folders)
        {
            super(dataContent, folders);
        }

        @Override
        public List<FolderModel> create()
        {
            verifyDataConsistency();
            return createNestedFoldersUnder(parent, names, 0);
        }

        private List<FolderModel> createNestedFoldersUnder(FolderModel parent, List<String> folderNames, int index)
        {
            List<FolderModel> createdFolders = new ArrayList<>();
            folderNames.stream().findFirst().ifPresent(folderName -> {
                FolderModel createdFolder = createFolder(folderName, getOrNull(titles, index), getOrNull(descriptions, index), parent, getOrNull(aliases, index));
                createdFolders.add(createdFolder);
                List<String> remainingNames = folderNames.stream().skip(1).toList();
                if (!remainingNames.isEmpty())
                {
                    createdFolders.addAll(createNestedFoldersUnder(createdFolder, remainingNames, index + 1));
                }
            });

            return createdFolders;
        }
    }

    public static class MultiContentCreator extends RepoFolderCreator implements Specifier.MultiContentSpecifier
    {
        private final RestWrapper restClient;
        private final Files files;

        public MultiContentCreator(DataContent dataContent, RestWrapper restClient, Files files, Map<String, FolderModel> folders)
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
        public FileCreator randomFile(String nameSuffix)
        {
            return files.add().randomFile(nameSuffix).underFolder(parent).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FilesCreator files(String... names)
        {
            return files.add().files(names).underFolder(parent).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FilesCreator randomFiles(String... nameSuffixes)
        {
            return files.add().randomFiles(nameSuffixes).underFolder(parent).withinSite(site).asUser(user);
        }

        @Override
        public MultiCreator.FilesCreator randomFiles(int quantity)
        {
            return files.add().randomFiles(quantity).underFolder(parent).withinSite(site).asUser(user);
        }

        @Override
        public <CONTENT extends ContentModel> void secondaryContent(CONTENT content)
        {
            buildNodeRestRequest(restClient, parent).addSecondaryChild(content);
        }

        @Override
        @SafeVarargs
        public final <CONTENT extends ContentModel> void secondaryContent(CONTENT... contents)
        {
            buildNodeRestRequest(restClient, parent).addSecondaryChildren(contents);
        }

        @Override
        public <TAG extends RestTagModel> void tag(TAG tag)
        {
            buildNodeRestRequest(restClient, parent).addTag(tag.getTag());
        }

        @Override
        @SafeVarargs
        public final <TAG extends RestTagModel> void tags(TAG... tags)
        {
            buildNodeRestRequest(restClient, parent).addTags(Stream.of(tags).map(RestTagModel::getTag).toArray(String[]::new));
        }
    }

    public static class RepoFolderModifier
        extends ContentModifier<FolderModel, Modifier.FolderModifier>
        implements Modifier.FolderModifier
    {
        private final RestWrapper restClient;
        private final DataContent dataContent;
        private final Files files;
        private final Map<String, FolderModel> foldersCache;

        private RepoFolderModifier(DataContent dataContent, RestWrapper restClient, FolderModel folder, Files files, Map<String, FolderModel> foldersCache)
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
                public <CONTENT extends ContentModel> void secondaryContent(CONTENT content)
                {
                    buildNodeRestRequest(restClient, contentModel).removeSecondaryChild(content);
                }

                @Override
                public <TAG extends RestTagModel> void tag(TAG tag)
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
}
