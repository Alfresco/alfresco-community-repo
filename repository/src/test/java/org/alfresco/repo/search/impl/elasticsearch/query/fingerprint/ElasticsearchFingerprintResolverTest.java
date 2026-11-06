/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query.fingerprint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.CONTENT_FINGERPRINT;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.GetResponse;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.search.impl.elasticsearch.query.fingerprint.ElasticsearchFingerprintResolver.FingerprintSource;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;

public class ElasticsearchFingerprintResolverTest
{
    private static final String INDEX = "alfresco";
    private static final String UUID = "3a5b3a3c-2d8e-4e4f-9d4c-1f2e3d4c5b6a";
    private static final NodeRef NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, UUID);
    private static final String HASH = "a".repeat(64);

    private OpenSearchClient client;
    private PermissionService permissionService;
    private NodeDAO nodeDAO;
    private ElasticsearchFingerprintResolver resolver;

    @Before
    public void setUp()
    {
        ElasticsearchHttpClientFactory clientFactory = mock(ElasticsearchHttpClientFactory.class);
        client = mock(OpenSearchClient.class);
        permissionService = mock(PermissionService.class);
        nodeDAO = mock(NodeDAO.class);
        when(clientFactory.getElasticsearchClient()).thenReturn(client);
        when(clientFactory.getIndexName()).thenReturn(INDEX);
        when(permissionService.hasReadPermission(NODE_REF)).thenReturn(AccessStatus.ALLOWED);
        resolver = new ElasticsearchFingerprintResolver(clientFactory, permissionService, nodeDAO);
    }

    @Test
    public void resolvesUuid() throws IOException
    {
        givenDocument(true, new FingerprintSource(HASH));

        assertEquals(Optional.of(HASH), resolver.resolve(UUID));

        GetRequest request = capturedRequest();
        assertEquals(INDEX, request.index());
        assertEquals(UUID, request.id());
        assertEquals(List.of(CONTENT_FINGERPRINT), request.sourceIncludes());
    }

    @Test
    public void resolvesNodeRef() throws IOException
    {
        givenDocument(true, new FingerprintSource(HASH));

        assertEquals(Optional.of(HASH), resolver.resolve(NODE_REF.toString()));
        assertEquals(UUID, capturedRequest().id());
    }

    @Test
    public void returnsEmptyWithoutCallingElasticsearchWhenReadDenied()
    {
        when(permissionService.hasReadPermission(NODE_REF)).thenReturn(AccessStatus.DENIED);

        assertEquals(Optional.empty(), resolver.resolve(UUID));
        verifyNoInteractions(client);
    }

    @Test
    public void returnsEmptyWhenDocumentNotFound() throws IOException
    {
        givenDocument(false, null);

        assertEquals(Optional.empty(), resolver.resolve(UUID));
    }

    @Test
    public void returnsEmptyWhenFieldMissing() throws IOException
    {
        givenDocument(true, new FingerprintSource(null));

        assertEquals(Optional.empty(), resolver.resolve(UUID));
    }

    @Test
    public void deserializesFingerprintFromSource()
    {
        JsonpMapper mapper = new JacksonJsonpMapper();
        String json = "{\"" + CONTENT_FINGERPRINT + "\":\"" + HASH + "\",\"other\":\"value\"}";

        FingerprintSource source = mapper.deserialize(mapper.jsonProvider().createParser(new StringReader(json)), FingerprintSource.class);

        assertEquals(HASH, source.fingerprint());
    }

    @Test
    public void returnsEmptyWhenClientThrows() throws IOException
    {
        when(client.get(any(GetRequest.class), eq(FingerprintSource.class))).thenThrow(new IOException("boom"));

        assertEquals(Optional.empty(), resolver.resolve(UUID));
    }

    @Test
    public void resolvesDbid() throws IOException
    {
        when(nodeDAO.getNodePair(123L)).thenReturn(new Pair<>(123L, NODE_REF));
        givenDocument(true, new FingerprintSource(HASH));

        assertEquals(Optional.of(HASH), resolver.resolve("123"));
        assertEquals(UUID, capturedRequest().id());
    }

    @Test
    public void returnsEmptyWhenDbidNotFound()
    {
        when(nodeDAO.getNodePair(123L)).thenReturn(null);

        assertEquals(Optional.empty(), resolver.resolve("123"));
        verifyNoInteractions(client);
    }

    @Test
    public void enabledFlag()
    {
        assertFalse(resolver.isEnabled());
        resolver.setEnabled(true);
        assertTrue(resolver.isEnabled());
    }

    private void givenDocument(boolean found, FingerprintSource source) throws IOException
    {
        GetResponse<FingerprintSource> response = GetResponse.of(b -> {
            b.index(INDEX).id(UUID).found(found);
            if (source != null)
            {
                b.source(source);
            }
            return b;
        });
        when(client.get(any(GetRequest.class), eq(FingerprintSource.class))).thenReturn(response);
    }

    private GetRequest capturedRequest() throws IOException
    {
        ArgumentCaptor<GetRequest> captor = ArgumentCaptor.forClass(GetRequest.class);
        verify(client).get(captor.capture(), eq(FingerprintSource.class));
        return captor.getValue();
    }
}
