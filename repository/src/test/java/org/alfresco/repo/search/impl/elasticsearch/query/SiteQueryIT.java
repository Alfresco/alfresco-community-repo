/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query;

import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName;
import static org.alfresco.service.cmr.site.SiteService.DOCUMENT_LIBRARY;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.IndexRequest;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters.Operator;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PathUtil;

@SuppressWarnings("PMD")
public class SiteQueryIT extends LuceneOrAFTSQueryIT
{
    private NodeService nodeService;
    private SiteService siteService;
    private TransactionService transactionService;
    private ContentService contentService;

    @Rule(order = Integer.MAX_VALUE - 1)
    public TestRule runAsAdminRule = this::runAsAdmin;
    private NamespacePrefixResolver namespacePrefixResolver;
    private String f1Content;
    private String f2Content;
    private String s1Id;
    private String s2Id;
    private NodeRef s1f1Ref;
    private NodeRef s1f2Ref;
    private NodeRef s2f1Ref;
    private NodeRef s2f2Ref;
    private NodeRef s1DocLibRef;
    private NodeRef s2DocLibRef;
    private Repository repositoryHelper;
    private FileFolderService fileFolderService;
    private NodeRef f1Ref;
    private NodeRef f2Ref;

    public SiteQueryIT(String language)
    {
        super(language);

    }

    @Before
    public void injectRequiredServicesAndCreateTestData()
    {
        nodeService = elasticsearchContext.getBean("nodeService", NodeService.class);
        assertNotNull(nodeService);

        namespacePrefixResolver = elasticsearchContext.getBean("namespaceService", NamespacePrefixResolver.class);
        assertNotNull(namespacePrefixResolver);

        siteService = elasticsearchContext.getBean("siteService", SiteService.class);
        assertNotNull(siteService);

        transactionService = elasticsearchContext.getBean("transactionService", TransactionService.class);
        assertNotNull(transactionService);

        contentService = elasticsearchContext.getBean("ContentService", ContentService.class);
        assertNotNull(contentService);

        repositoryHelper = elasticsearchContext.getBean("repositoryHelper", Repository.class);
        assertNotNull(repositoryHelper);

        fileFolderService = elasticsearchContext.getBean("FileFolderService", FileFolderService.class);
        assertNotNull(fileFolderService);

        String s1Content = uniqueString("s1");
        String s2Content = uniqueString("s2");
        f1Content = uniqueString("f1");
        f2Content = uniqueString("f2");

        s1Id = createSite("site1");
        s1DocLibRef = siteService.getContainer(s1Id, DOCUMENT_LIBRARY);
        s1f1Ref = createFile(s1Id, s1Content, f1Content);
        s1f2Ref = createFile(s1Id, s1Content, f2Content);

        s2Id = createSite("site2");
        s2DocLibRef = siteService.getContainer(s2Id, DOCUMENT_LIBRARY);
        s2f1Ref = createFile(s2Id, s2Content, f1Content);
        s2f2Ref = createFile(s2Id, s2Content, f2Content);

        f1Ref = createFileUnderRoot(f1Content);
        f2Ref = createFileUnderRoot(f2Content);
    }

    @Test
    public void shouldReturnEmptyResultWhenSearchingForNonexistentSite()
    {
        ResultSet result = searchFor(language, siteQuery(uniqueString("Nonexistent")));
        assertZeroResults(result);
    }

    @Test
    public void shouldReturnContentUnderSites()
    {
        ResultSet result = searchFor(language, f1Content);
        assertContainsOnly(result, f1Ref, s1f1Ref, s2f1Ref);
    }

    @Test
    public void shouldReturnContentLimitedToSingleSite()
    {
        ResultSet result = searchFor(language, f2Content + " " + siteQuery(s2Id), Operator.AND);
        assertContainsOnly(result, s2f2Ref);
    }

    @Test
    public void shouldReturnContentLimitedToAllSites()
    {
        ResultSet result = searchFor(language, f2Content + " " + siteQuery("_ALL_SITES_"), Operator.AND);
        assertContainsOnly(result, s1f2Ref, s2f2Ref);
    }

    @Test
    public void shouldReturnContentLimitedToEverything()
    {
        ResultSet result = searchFor(language, f2Content + " " + siteQuery("_EVERYTHING_"), Operator.AND);
        assertContainsOnly(result, f2Ref, s1f2Ref, s2f2Ref);
    }

    @Test
    public void shouldReturnNodesFromSingleSite()
    {
        ResultSet result = searchFor(language, siteQuery(s1Id));
        assertContainsOnly(result, s1DocLibRef, s1f1Ref, s1f2Ref);
    }

    @Test
    public void shouldReturnNodesFromAllSites()
    {
        final NodeRef s1Ref = siteService.getSite(s1Id).getNodeRef();
        final NodeRef s2Ref = siteService.getSite(s2Id).getNodeRef();

        ResultSet result = searchFor(language, siteQuery("_ALL_SITES_"));
        assertContains(result, s1DocLibRef, s1f1Ref, s1f2Ref, s2DocLibRef, s2f1Ref, s2f2Ref);
        assertNotContain(result, s1Ref, s2Ref);
    }

    @Test
    public void shouldSearchWhenSiteIsDoubleQuoted()
    {
        ResultSet result = searchFor(language, siteQuery("\"" + s1Id + "\""));
        assertContainsOnly(result, s1DocLibRef, s1f1Ref, s1f2Ref);
    }

    private String siteQuery(String siteName)
    {
        return "SITE:" + siteName;
    }

    private String createSite(String siteIdPrefix)
    {
        final SiteInfo site = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            final String siteId = uniqueString(siteIdPrefix);
            return siteService.createSite(null, siteId, "Test site", "My test site", SiteVisibility.PUBLIC);
        });
        indexExistingNode(site.getNodeRef());

        NodeRef docLib = siteService.createContainer(site.getShortName(), DOCUMENT_LIBRARY, TYPE_FOLDER, null);
        indexExistingNode(docLib);

        return site.getShortName();
    }

    private NodeRef createFile(String siteId, String... content)
    {
        final NodeRef docLib = siteService.getContainer(siteId, DOCUMENT_LIBRARY);
        return createFile(docLib, content);
    }

    private NodeRef createFileUnderRoot(String... content)
    {
        final NodeRef root = repositoryHelper.getCompanyHome();
        return createFile(root, content);
    }

    private NodeRef createFile(NodeRef parent, String... content)
    {
        NodeRef fileRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            FileInfo file = fileFolderService.create(parent, uniqueString(), ContentModel.TYPE_CONTENT);

            ContentWriter writer = fileFolderService.getWriter(file.getNodeRef());
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            writer.setEncoding("UTF-8");
            writer.putContent(Stream.of(content).collect(Collectors.joining(" ")));

            return file.getNodeRef();
        });
        indexExistingNode(fileRef);
        return fileRef;
    }

    private void indexExistingNode(NodeRef nodeRef)
    {
        IndexDocumentSourceBuilder builder = IndexDocumentSourceBuilder.from(nodeService.getProperties(nodeRef));
        final Path path = nodeService.getPath(nodeRef);
        builder.withPath(path.toPrefixString(namespacePrefixResolver));
        builder.withType(nodeService.getType(nodeRef).toPrefixString(namespacePrefixResolver));
        builder.withPrimaryHierarchy(PathUtil.getNodeIdsInReverse(path, false));
        Optional.ofNullable(contentService.getReader(nodeRef, ContentModel.PROP_CONTENT))
                .map(ContentReader::getContentString)
                .ifPresent(builder::withContent);

        final IndexRequest req;

        try
        {
            req = new IndexRequest.Builder<>().index(indexName)
                    .id(String.valueOf(nodeRef.getId()))
                    .refresh(Refresh.True).document(builder.buildSource()).build();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unexpected.", e);
        }

        Awaitility.await().until(() -> {
            try
            {
                client.index(req);
                return true;
            }
            catch (OpenSearchException e)
            {
                return false;
            }
        });
    }

    private static String uniqueString(String prefix)
    {
        return prefix + uniqueString();
    }

    private static String uniqueString()
    {
        final UUID unique = UUID.randomUUID();
        return Long.toHexString(unique.getMostSignificantBits()) + Long.toHexString(unique.getLeastSignificantBits());
    }

    private Statement runAsAdmin(Statement statement, Description description)
    {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable
            {
                try
                {
                    AuthenticationUtil.runAs(() -> {
                        try
                        {
                            statement.evaluate();
                            return null;
                        }
                        catch (Exception e)
                        {
                            throw e;
                        }
                        catch (Throwable t)
                        {
                            throw new WrappedThrowable(t);
                        }
                    }, getAdminUserName());
                }
                catch (WrappedThrowable wt)
                {
                    throw wt.getCause();
                }
            }
        };
    }

    private static class WrappedThrowable extends RuntimeException
    {
        WrappedThrowable(Throwable toWrap)
        {
            super(toWrap);
        }
    }
}
