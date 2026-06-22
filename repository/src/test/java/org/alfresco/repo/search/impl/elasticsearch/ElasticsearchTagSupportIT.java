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
package org.alfresco.repo.search.impl.elasticsearch;

import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.repo.security.authentication.AuthenticationUtil.getSystemUserName;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch.core.IndexRequest;

import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.elasticsearch.ElasticsearchCategoryService.TagSupport;
import org.alfresco.repo.search.impl.elasticsearch.query.IndexDocumentSourceBuilder;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;

public class ElasticsearchTagSupportIT extends ElasticsearchSpringTest
{
    @Rule
    public RunAsFullyAuthenticatedRule runAsAuthenticatedUser = new RunAsFullyAuthenticatedRule(getSystemUserName());

    private NodeService nodeService;
    private NodeService publicNodeService;
    private IndexerAndSearcher indexerAndSearcher;
    private NamespacePrefixResolver namespacePrefixResolver;

    @Before
    public void injectRequiredBeansAndIndexExistingTaggableNode()
    {
        nodeService = elasticsearchContext.getBean("nodeService", NodeService.class);
        assertNotNull(nodeService);

        publicNodeService = elasticsearchContext.getBean("NodeService", NodeService.class);
        assertNotNull(publicNodeService);

        indexerAndSearcher = elasticsearchContext.getBean("search.indexerAndSearcherFactory", IndexerAndSearcher.class);
        assertNotNull(indexerAndSearcher);

        namespacePrefixResolver = elasticsearchContext.getBean("namespaceService", NamespacePrefixResolver.class);
        assertNotNull(namespacePrefixResolver);

        indexExistingNode(new NodeRef("workspace://SpacesStore/tag:tag-root"));
    }

    @Test
    public void shouldCreateNewTag()
    {
        final TagSupport tagSupport = givenTagSupport();
        final String tagName = givenUniqueTagName("TEST-TAG");

        ChildAssociationRef tagAssociation = tagSupport.createTag(STORE_REF_WORKSPACE_SPACESSTORE, tagName);

        assertNotNull(tagAssociation);
        assertTrue(nodeService.exists(tagAssociation.getChildRef()));
        assertEquals(tagName, nodeService.getProperty(tagAssociation.getChildRef(), PROP_NAME));
    }

    @Test
    public void shouldNotFindNonexistentTag()
    {
        final TagSupport tagSupport = givenTagSupport();
        final String nonexistentTag = givenUniqueTagName("NonexistentTag");

        Optional<ChildAssociationRef> optionalTag = tagSupport.findExistingTag(STORE_REF_WORKSPACE_SPACESSTORE, nonexistentTag);

        assertNotNull(optionalTag);
        assertTrue(optionalTag.isEmpty());
    }

    @Test
    public void shouldFindExistingTag()
    {
        final TagSupport tagSupport = givenTagSupport();
        final String existingTag = givenExistingTag(tagSupport, "ExistingTag");

        Optional<ChildAssociationRef> optionalTag = tagSupport.findExistingTag(STORE_REF_WORKSPACE_SPACESSTORE, existingTag);

        assertNotNull(optionalTag);
        assertTrue(optionalTag.isPresent());
        assertTrue(nodeService.exists(optionalTag.get().getChildRef()));
        assertEquals(existingTag, nodeService.getProperty(optionalTag.get().getChildRef(), PROP_NAME));
    }

    @Test
    public void shouldFindAllTags()
    {
        final TagSupport tagSupport = givenTagSupport();
        final String t2 = givenExistingTag(tagSupport, "T2");
        final String t3 = givenExistingTag(tagSupport, "T3");
        final String t1 = givenExistingTag(tagSupport, "T1");

        final List<ChildAssociationRef> tags = tagSupport.findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, (Collection<String>) null, false);

        assertTagsInAnyOrder(tags, t1, t2, t3);
    }

    @Test
    public void shouldFindAllTagsSorted()
    {
        final TagSupport tagSupport = givenTagSupport();
        final String t2 = givenExistingTag(tagSupport, "T2");
        final String t3 = givenExistingTag(tagSupport, "T3");
        final String t1 = givenExistingTag(tagSupport, "T1");

        final List<ChildAssociationRef> tags = tagSupport.findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, (Collection<String>) null, true);

        assertTagsInExactOrder(tags, t1, t2, t3);
    }

    @Test
    public void shouldFindAllTagsFiltered()
    {
        final TagSupport tagSupport = givenTagSupport();
        final String t2X = givenExistingTag(tagSupport, "TXX2");
        final String t3X = givenExistingTag(tagSupport, "TXX3");
        final String t1X = givenExistingTag(tagSupport, "TXX1");
        final String t2Y = givenExistingTag(tagSupport, "TYY2");
        final String t3Y = givenExistingTag(tagSupport, "TYY3");
        final String t1Y = givenExistingTag(tagSupport, "TYY1");

        final List<ChildAssociationRef> tags = tagSupport.findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, "XX", false);

        assertTagsInAnyOrder(tags, t1X, t2X, t3X);
        assertToNotContain(tags, t1Y, t2Y, t3Y);
    }

    @Test
    public void shouldFindAllTagsFilteredAndSorted()
    {
        final TagSupport tagSupport = givenTagSupport();
        final String t2X = givenExistingTag(tagSupport, "TXX2");
        final String t3X = givenExistingTag(tagSupport, "TXX3");
        final String t1X = givenExistingTag(tagSupport, "TXX1");
        final String t2Y = givenExistingTag(tagSupport, "TYY2");
        final String t3Y = givenExistingTag(tagSupport, "TYY3");
        final String t1Y = givenExistingTag(tagSupport, "TYY1");

        final List<ChildAssociationRef> tags = tagSupport.findAllTags(STORE_REF_WORKSPACE_SPACESSTORE, "YY", true);

        assertTagsInExactOrder(tags, t1Y, t2Y, t3Y);
        assertToNotContain(tags, t1X, t2X, t3X);
    }

    private void assertTagsInAnyOrder(final List<ChildAssociationRef> tags, final String... tagNames)
    {
        assertNotNull(tags);
        assertFalse(tags.isEmpty());

        final Set<String> allNames = tags.stream()
                .map(ChildAssociationRef::getChildRef)
                .map(r -> nodeService.getProperty(r, PROP_NAME))
                .map(String.class::cast)
                .collect(Collectors.toUnmodifiableSet());

        assertTrue(allNames.containsAll(List.of(tagNames)));
    }

    private void assertTagsInExactOrder(List<ChildAssociationRef> tags, final String... tagNames)
    {
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        final Set<String> relevantTagNames = Set.of(tagNames);

        final List<String> relevantNames = tags.stream()
                .map(ChildAssociationRef::getChildRef)
                .map(r -> nodeService.getProperty(r, PROP_NAME))
                .map(String.class::cast)
                .filter(relevantTagNames::contains)
                .collect(Collectors.toUnmodifiableList());

        assertEquals(List.of(tagNames), relevantNames);
    }

    private void assertToNotContain(List<ChildAssociationRef> tags, final String... tagNames)
    {
        final Set<String> relevantTagNames = Set.of(tagNames);

        final Set<String> overlappingNames = tags.stream()
                .map(ChildAssociationRef::getChildRef)
                .map(r -> nodeService.getProperty(r, PROP_NAME))
                .map(String.class::cast)
                .filter(relevantTagNames::contains)
                .collect(Collectors.toUnmodifiableSet());

        assertEquals(Set.of(), overlappingNames);
    }

    private TagSupport givenTagSupport()
    {
        return new TagSupport(publicNodeService, indexerAndSearcher, nodeService);
    }

    private String givenExistingTag(TagSupport tagSupport, String tagName)
    {
        final String realTagName = givenUniqueTagName(tagName);

        final ChildAssociationRef tagAssociation = tagSupport.createTag(STORE_REF_WORKSPACE_SPACESSTORE, realTagName);
        indexExistingNode(tagAssociation.getChildRef());

        return realTagName;
    }

    private String givenUniqueTagName(final String name)
    {
        return name + "-" + UUID.randomUUID();
    }

    private void indexExistingNode(NodeRef nodeRef)
    {
        IndexDocumentSourceBuilder builder = IndexDocumentSourceBuilder.from(nodeService.getProperties(nodeRef));
        builder.withPath(nodeService.getPath(nodeRef).toPrefixString(namespacePrefixResolver));
        builder.withType(nodeService.getType(nodeRef).toPrefixString(namespacePrefixResolver));

        final IndexRequest req;

        try
        {
            req = new IndexRequest.Builder<>().index(indexName)
                    .id(String.valueOf(nodeRef.getId()))
                    .document(builder.buildSource())
                    .refresh(Refresh.True)
                    .build();
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
}
