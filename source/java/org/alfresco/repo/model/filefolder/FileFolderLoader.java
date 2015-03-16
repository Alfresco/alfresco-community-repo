/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.model.filefolder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.SpoofedTextContentReader;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.random.NormalDistributionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to aid in the generation of file-folder data structures for load test purposes.
 * <p/>
 * All paths referenced are in relation to the standard Alfresco "Company Home" folder,
 * which acts as the root for accessing documents and folders via many APIs.
 * <p/>
 * <strong>WARNING:  This class may be used but will probably NOT be considered part of the public API i.e.
 * will probably change in line with Alfresco's internal requirements; nevertheless, backward
 * compatibility will be maintained where practical.</strong>
 * <p/>
 * Timestamp propagation to the containing folder is disabled in order to reduce overhead.
 * 
 * @author Derek Hulley
 * @since 5.1
 */
public class FileFolderLoader
{
    private static Log logger = LogFactory.getLog(FileFolderLoader.class);
    
    private final RepositoryState repoState;
    private final TransactionService transactionService;
    private final Repository repositoryHelper;
    private final FileFolderService fileFolderService;
    private final NodeService nodeService;
    private final ContentService contentService;
    private final BehaviourFilter policyBehaviourFilter;
    private final NormalDistributionHelper normalDistribution;
    
    /**
     * @param repoState             keep track of repository readiness
     * @param transactionService    ensure proper rollback, where required
     * @param repositoryHelper      access standard repository paths
     * @param fileFolderService     perform actual file-folder manipulation
     */
    public FileFolderLoader(
            RepositoryState repoState,
            TransactionService transactionService,
            Repository repositoryHelper,
            FileFolderService fileFolderService,
            NodeService nodeService,
            ContentService contentService,
            BehaviourFilter policyBehaviourFilter)
    {
        this.repoState = repoState;
        this.transactionService = transactionService;
        this.repositoryHelper = repositoryHelper;
        this.fileFolderService = fileFolderService;
        this.nodeService = nodeService;
        this.contentService = contentService;
        this.policyBehaviourFilter = policyBehaviourFilter;
        
        this.normalDistribution = new NormalDistributionHelper();
    }
    
    /**
     * @return                      the helper for accessing common repository paths
     */
    public Repository getRepository()
    {
        return repositoryHelper;
    }
    
    /** <p>
     * Attempt to create a given number of text files within a specified folder.  The load tolerates failures unless these
     * prevent <b>any</b> files from being created.  Options exist to control the file size and text content distributions.
     * The <b>cm:auditable</b> aspect automatically applied to each node as part of Alfresco.
     * Additionally, extra residual text properties can be added in order to increase the size of the database storage.</p>
     * <p>
     * The files are created regardless of the read-write state of the server.</p>
     * <p>
     * The current thread's authentication determines the user context and the authenticated user has to have sufficient
     * permissions to {@link PermissionService#CREATE_CHILDREN create children} within the folder.  This will be enforced
     * by the {@link FileFolderService}.</p>
     * 
     * @param folderPath                        the full path to the folder within the context of the
     *                                          {@link Repository#getCompanyHome() Alfresco Company Home} folder e.g.
     *                                          <pre>/Sites/Site.default.00009/documentLibrary</pre>.
     * @param fileCount                         the number of files to create
     * @param filesPerTxn                       the number of files to create in a transaction.  Any failures within a
     *                                          transaction (batch) will force the transaction to rollback; normal
     *                                          {@link RetryingTransactionHelper#doInTransaction(org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback) retrying semantics }
     *                                          are employed.
     * @param minFileSize                       the smallest file size (all sizes within 1 standard deviation of the mean)
     * @param maxFileSize                       the largest file size (all sizes within 1 standard deviation of the mean)
     * @param maxUniqueDocuments                the maximum number of unique documents that should be created globally.
     *                                          A value of <tt>1</tt> means that all documents will be the same document;
     *                                          <tt>10,000,000</tt> will mean that there will be 10M unique text sequences used.
     * @param forceBinaryStorage                <tt>true</tt> to actually write the spoofed text data to the binary store
     *                                          i.e. the physical underlying storage will contain the binary data, allowing
     *                                          IO to be realistically stressed if that is a requirement.  To save disk
     *                                          space, set this value to <tt>false</tt>, which will see all file data get
     *                                          generated on request using a repeatable algorithm.
     * @param descriptionCount                  the number of <b>cm:description</b> multilingual entries to create.  The current locale
     *                                          is used for the first entry and additional locales are added using the
     *                                          {@link Locale#getISOLanguages() Java basic languages list}.  The total count cannot
     *                                          exceed the total number of languages available.
     *                                          TODO: Note that the actual text stored is not (yet) localized.
     * @param descriptionSize                   the size (in bytes) for each <b>cm:description</b> property created; values from 16 bytes to 1024 bytes are supported
     * @return                                  the number of files successfully created
     * @throws FileNotFoundException            if the folder path does not exist
     * @throws IllegalStateException            if the repository is not ready
     */
    public int createFiles(
            final String folderPath,
            final int fileCount,
            final int filesPerTxn,
            final long minFileSize, long maxFileSize,
            final long maxUniqueDocuments,
            final boolean forceBinaryStorage,
            final int descriptionCount, final long descriptionSize
            ) throws FileNotFoundException
    {
        if (repoState.isBootstrapping())
        {
            throw new IllegalStateException("Repository is still bootstrapping.");
        }
        if (minFileSize > maxFileSize)
        {
            throw new IllegalArgumentException("Min/max file sizes incorrect: " + minFileSize + "-" + maxFileSize);
        }
        if (filesPerTxn < 1)
        {
            throw new IllegalArgumentException("'filesPerTxn' must be 1 or more.");
        }
        if (descriptionCount < 0 || descriptionCount > Locale.getISOLanguages().length)
        {
            throw new IllegalArgumentException("'descriptionCount' exceeds the number of languages available.");
        }
        if (descriptionSize < 16L || descriptionSize > 1024L)
        {
            throw new IllegalArgumentException("'descriptionSize' can be anything from 16 to 1024 bytes.");
        }
        
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        // Locate the folder; this MUST work
        RetryingTransactionCallback<NodeRef> findFolderWork = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                String folderPathFixed = folderPath;
                // Homogenise the path
                if (!folderPath.startsWith("/"))
                {
                    folderPathFixed = "/" + folderPath;
                }
                NodeRef companyHomeNodeRef = repositoryHelper.getCompanyHome();
                // Special case for the root
                if (folderPath.equals("/"))
                {
                    return companyHomeNodeRef;
                }
                List<String> folderPathElements = Arrays.asList(folderPathFixed.substring(1).split("/"));
                FileInfo folderInfo = fileFolderService.resolveNamePath(companyHomeNodeRef, folderPathElements, true);
                // Done
                return folderInfo.getNodeRef();
            }
        };
        NodeRef folderNodeRef = txnHelper.doInTransaction(findFolderWork, false, true);
        // Create files
        int created = createFiles(
                folderNodeRef, fileCount, filesPerTxn, minFileSize, maxFileSize, maxUniqueDocuments, forceBinaryStorage,
                descriptionCount, descriptionSize);
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Created " + created + " files in folder " + folderPath);
        }
        return created;
    }
    
    private int createFiles(
            final NodeRef folderNodeRef,
            final int fileCount,
            final int filesPerTxn,
            final long minFileSize, final long maxFileSize,
            final long maxUniqueDocuments,
            final boolean forceBinaryStorage,
            final int descriptionCount, final long descriptionSize)
    {
        final String nameBase = UUID.randomUUID().toString();
        
        final AtomicInteger count = new AtomicInteger(0);
        RetryingTransactionCallback<Void> createFilesWork = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Disable timestamp propagation to the parent by disabling cm:auditable
                policyBehaviourFilter.disableBehaviour(folderNodeRef, ContentModel.ASPECT_AUDITABLE);
                
                for (int i = 0; i < filesPerTxn; i++)
                {
                    // Only create files while we need; we may need to do fewer in the last txn
                    if (count.get() >= fileCount)
                    {
                        break;
                    }
                    // Each load has it's own base name
                    String name = String.format("%s-%6d.txt", nameBase, count.get());
                    // Create a file
                    FileInfo fileInfo = fileFolderService.create(
                            folderNodeRef,
                            name,
                            ContentModel.TYPE_CONTENT, ContentModel.ASSOC_CONTAINS);
                    NodeRef fileNodeRef = fileInfo.getNodeRef();
                    // Spoofed document
                    Locale locale = Locale.ENGLISH;
                    long seed = (long) (Math.random() * maxUniqueDocuments);
                    long size = normalDistribution.getValue(minFileSize, maxFileSize);
                    String contentUrl = SpoofedTextContentReader.createContentUrl(locale, seed, size);
                    SpoofedTextContentReader reader = new SpoofedTextContentReader(contentUrl);
                    if (forceBinaryStorage)
                    {
                        // Stream the text into the real storage
                        ContentWriter writer = contentService.getWriter(fileNodeRef, ContentModel.PROP_CONTENT, true);
                        writer.setEncoding("UTF-8");
                        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
                        writer.putContent(reader);
                    }
                    else
                    {
                        // Just use the URL
                        ContentData contentData = reader.getContentData();
                        nodeService.setProperty(fileNodeRef, ContentModel.PROP_CONTENT, contentData);
                    }
                    // Store the description, if required
                    if (descriptionCount > 0)
                    {
                        // Add the cm:description additional properties
                        boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
                        MLText descriptions = new MLText();
                        String[] languages = Locale.getISOLanguages();
                        String defaultLanguage = Locale.getDefault().getLanguage();
                        // Create cm:description translations
                        for (int descriptionNum = -1; descriptionNum < (descriptionCount-1); descriptionNum++)
                        {
                            String language = null;
                            // Use the default language for the first description
                            if (descriptionNum == -1)
                            {
                                language = defaultLanguage;
                            }
                            else if (languages[descriptionNum].equals(defaultLanguage))
                            {
                                // Skip the default language, if we hit it
                                continue;
                            }
                            else
                            {
                                language = languages[descriptionNum];
                            }
                            Locale languageLocale = new Locale(language);
                            // For the cm:description, create new reader with a seed that changes each time
                            String descriptionUrl = SpoofedTextContentReader.createContentUrl(locale, seed + descriptionNum, descriptionSize);
                            SpoofedTextContentReader readerDescription = new SpoofedTextContentReader(descriptionUrl);
                            String description = readerDescription.getContentString();
                            descriptions.put(languageLocale, description);
                        }
                        nodeService.setProperty(fileNodeRef, ContentModel.PROP_DESCRIPTION, descriptions);
                        MLPropertyInterceptor.setMLAware(wasMLAware);
                    }
                    // Success
                    count.incrementAndGet();
                }
                return null;
            }
        };
        // Batches
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        int txnCount = (int) Math.ceil((double)fileCount / (double)filesPerTxn);
        for (int i = 0; i < txnCount; i++)
        {
            txnHelper.doInTransaction(createFilesWork, false, true);
        }
        // Done
        return count.get();
    }
}
