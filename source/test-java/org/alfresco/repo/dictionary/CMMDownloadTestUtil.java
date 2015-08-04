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

package org.alfresco.repo.dictionary;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * A utility class to test custom models download.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class CMMDownloadTestUtil
{
    private static final Log logger = LogFactory.getLog(CMMDownloadTestUtil.class);

    private static final String SURF_CONFIG_PATH = "./app:company_home/st:sites/cm:surf-config";
    private static final String SHARE_EXTENSIONS_FOLDER = "extensions"; // app:company_home/st:sites/cm:surf-config/cm:extensions
    private static final String SHARE_PERSISTED_EXTENSION_FILE = "default-persisted-extension.xml"; // app:company_home/st:sites/cm:surf-config/cm:extensions/cm:default-persisted-extension.xml
    private static final String MKR = "{MKR}";
    private static final String MODULE = 
                "<extension>"
                +    "<modules>"
                +        "<module>"
                +           "<id>CMM_" + MKR + "</id>"
                +           "<auto-deploy>true</auto-deploy>"
                +           "<configurations>"
                +               "<config evaluator=\"string-compare\" condition=\"DocumentLibrary\" replace=\"false\">"
                +                   "<types>"
                +                       "<type name=\"cm:content\">"
                +                           "<subtype label=\"type1 title\" name=\"testprefix:type1\"/>"
                +                       "</type>"
                +                   "</types>"
                +               "</config>"
                +               "<config evaluator=\"string-compare\" condition=\"FormDefinition\">"
                +               "</config>"
                +           "</configurations>"
                +        "</module>"
                +    "</modules>"
                +"</extension>";

    private RetryingTransactionHelper transactionHelper;
    private ContentService contentService;
    private NodeService nodeService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private DownloadService downloadService;

    private NodeRef extensionsNodeRef;
    private NodeRef sharePersistedExtNodeRef;
    private boolean isExtFolderCreated = false;
    private File originalShareExtFile;

    public CMMDownloadTestUtil(ApplicationContext ctx)
    {
        this.transactionHelper = ctx.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        this.contentService = ctx.getBean("contentService", ContentService.class);
        this.searchService = ctx.getBean("searchService", SearchService.class);
        this.nodeService = ctx.getBean("nodeService", NodeService.class);
        this.namespaceService = ctx.getBean("namespaceService", NamespaceService.class);
        this.downloadService = ctx.getBean("downloadService", DownloadService.class);
    }

    private NodeRef getRootNode()
    {
        return nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    }

    public synchronized void createShareExtModule(final String moduleId)
    {
        List<NodeRef> results = searchService.selectNodes(getRootNode(), SURF_CONFIG_PATH, null, namespaceService, false,
                    SearchService.LANGUAGE_XPATH);
        assertTrue(results.size() == 1);
        final NodeRef surfConfigNodeRef = results.get(0);

        this.extensionsNodeRef = nodeService.getChildByName(surfConfigNodeRef, ContentModel.ASSOC_CONTAINS, SHARE_EXTENSIONS_FOLDER);
        if (this.extensionsNodeRef == null)
        {
            extensionsNodeRef = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
            {
                @Override
                public NodeRef execute() throws Throwable
                {
                    QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, SHARE_EXTENSIONS_FOLDER);
                    NodeRef nodeRef = nodeService.createNode(surfConfigNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_FOLDER,
                                Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, SHARE_EXTENSIONS_FOLDER)).getChildRef();

                    isExtFolderCreated = true;
                    return nodeRef;
                }
            });

            logger.info("Created 'cm:extensions' folder within the 'app:company_home/st:sites/cm:surf-config'");
        }

        this.sharePersistedExtNodeRef = nodeService.getChildByName(this.extensionsNodeRef, ContentModel.ASSOC_CONTAINS, SHARE_PERSISTED_EXTENSION_FILE);
        if (this.sharePersistedExtNodeRef == null)
        {
            this.sharePersistedExtNodeRef = transactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
            {

                @Override
                public NodeRef execute() throws Throwable
                {
                    QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, SHARE_PERSISTED_EXTENSION_FILE);
                    return nodeService.createNode(extensionsNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, ContentModel.TYPE_CONTENT,
                                Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, SHARE_PERSISTED_EXTENSION_FILE)).getChildRef();
                }
            });

            logger.info("Created 'cm:default-persisted-extension.xml' file within the 'app:company_home/st:sites/cm:surf-config/cm:extensions'");
        }
        else if(originalShareExtFile == null)
        {
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    ContentReader reader = contentService.getReader(sharePersistedExtNodeRef, ContentModel.PROP_CONTENT);
                    originalShareExtFile = TempFileProvider.createTempFile(CustomModelServiceImplTest.class.getName(), ".xml");
                    reader.getContent(originalShareExtFile);

                    return null;
                }
            });
        }

        transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                ContentWriter writer = contentService.getWriter(sharePersistedExtNodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_XML);
                writer.setEncoding("UTF-8");
                writer.putContent(MODULE.replace(MKR, moduleId));

                return null;
            }
        });
        logger.info("Added 'CM_" + moduleId + "' module.'");
    }

    public synchronized void cleanup()
    {
        if (isExtFolderCreated)
        {
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    nodeService.deleteNode(extensionsNodeRef);
                    return null;
                }
            });

            logger.info("Deleted 'cm:extensions' folder within the 'app:company_home/st:sites/cm:surf-config");
        }
        else if (originalShareExtFile != null)
        {
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    ContentWriter writer = contentService.getWriter(sharePersistedExtNodeRef, ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(MimetypeMap.MIMETYPE_XML);
                    writer.setEncoding("UTF-8");
                    writer.putContent(originalShareExtFile); // put back the original extension file
                    return null;
                }
            });

            logger.info("Reverted default-persisted-extension.xml content.");
        }

        if (originalShareExtFile != null)
        {
            originalShareExtFile.delete();
        }
    }

    public Set<String> getDownloadEntries(final NodeRef downloadNode)
    {
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<Set<String>>()
        {
            @Override
            public Set<String> execute() throws Throwable
            {
                Set<String> entryNames = new TreeSet<String>();
                ContentReader reader = contentService.getReader(downloadNode, ContentModel.PROP_CONTENT);
                try (ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(reader.getContentInputStream()))
                {
                    ZipArchiveEntry zipEntry = null;
                    while ((zipEntry = zipInputStream.getNextZipEntry()) != null)
                    {
                        String name = zipEntry.getName();
                        entryNames.add(name);
                    }
                }
                return entryNames;
            }
        });
    }

    public String getDownloadEntry(Collection<String> entryNames, String entryName)
    {
        for (String expectedEntry : entryNames)
        {
            if (expectedEntry.equals(entryName))
            {
                return expectedEntry;
            }
        }

        return null;
    }

    public DownloadStatus getDownloadStatus(final NodeRef downloadNode)
    {
        return transactionHelper.doInTransaction(new RetryingTransactionCallback<DownloadStatus>()
        {
            @Override
            public DownloadStatus execute() throws Throwable
            {
                return downloadService.getDownloadStatus(downloadNode);
            }
        });
    }
}
