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
package org.alfresco.repo.search.impl.elasticsearch.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import static org.alfresco.repo.search.impl.elasticsearch.admin.SearchEngineDetector.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.generic.Body;
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient;
import org.opensearch.client.opensearch.generic.Request;
import org.opensearch.client.opensearch.generic.Response;

import org.alfresco.repo.search.impl.elasticsearch.client.ElasticsearchHttpClientFactory;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.transaction.TransactionService;

/**
 * Unit tests for {@link SearchEngineDetector}.
 * <p>
 * The search engine root endpoint ({@code GET /}) is mocked at the OpenSearch generic-client level so the provider/version detection and the {@link AttributeService} persistence can be verified without a running Elasticsearch/OpenSearch instance.
 */
public class SearchEngineDetectorTest
{
    private static final String SYSTEM_USER = "System";

    private static final String ES_BODY = "{"
            + "\"name\":\"node-1\",\"cluster_name\":\"docker-cluster\","
            + "\"version\":{\"number\":\"8.17.0\",\"build_flavor\":\"default\"},"
            + "\"tagline\":\"You Know, for Search\"}";

    private static final String OS_BODY = "{"
            + "\"name\":\"node-1\",\"cluster_name\":\"docker-cluster\","
            + "\"version\":{\"distribution\":\"opensearch\",\"number\":\"2.17.0\"},"
            + "\"tagline\":\"The OpenSearch Project: https://opensearch.org/\"}";

    private static final String UNKNOWN_BODY = "{"
            + "\"version\":{\"number\":\"1.2.3\"},"
            + "\"tagline\":\"Some other search engine\"}";

    @Mock
    private ElasticsearchHttpClientFactory httpClientFactory;
    @Mock
    private AttributeService attributeService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private RetryingTransactionHelper txnHelper;
    @Mock
    private OpenSearchClient openSearchClient;
    @Mock
    private OpenSearchGenericClient genericClient;
    @Mock
    private Response response;
    @Mock
    private Body body;

    private AutoCloseable mocks;
    private MockedStatic<AuthenticationUtil> authUtil;

    private SearchEngineDetector detector;

    @Before
    public void setUp()
    {
        mocks = openMocks(this);

        // Run the "run as system" work and the transactional work inline so the persistence can be verified.
        authUtil = mockStatic(AuthenticationUtil.class);
        authUtil.when(AuthenticationUtil::getSystemUserName).thenReturn(SYSTEM_USER);
        authUtil.when(() -> AuthenticationUtil.runAs(any(), any()))
                .thenAnswer(call -> ((RunAsWork<?>) call.getArgument(0)).doWork());

        lenient().when(transactionService.getRetryingTransactionHelper()).thenReturn(txnHelper);
        lenient().when(txnHelper.doInTransaction(any(), anyBoolean(), anyBoolean()))
                .thenAnswer(call -> ((RetryingTransactionCallback<?>) call.getArgument(0)).execute());

        detector = new SearchEngineDetector();
        detector.setHttpClientFactory(httpClientFactory);
        detector.setAttributeService(attributeService);
        detector.setTransactionService(transactionService);
    }

    @After
    public void tearDown() throws Exception
    {
        authUtil.close();
        mocks.close();
    }

    @Test
    public void shouldStoreElasticsearchProviderAndVersion() throws Exception
    {
        givenRootBody(ES_BODY);

        detector.detectAndStore();

        verify(attributeService).setAttribute(eq("elasticsearch"), eq(ATTR_ROOT), eq(ATTR_SEARCH_ENGINE_NAME));
        verify(attributeService).setAttribute(eq("8.17.0"), eq(ATTR_ROOT), eq(ATTR_SEARCH_ENGINE_VERSION));
    }

    @Test
    public void shouldStoreOpenSearchProviderAndVersion() throws Exception
    {
        givenRootBody(OS_BODY);

        detector.detectAndStore();

        verify(attributeService).setAttribute(eq("opensearch"), eq(ATTR_ROOT), eq(ATTR_SEARCH_ENGINE_NAME));
        verify(attributeService).setAttribute(eq("2.17.0"), eq(ATTR_ROOT), eq(ATTR_SEARCH_ENGINE_VERSION));
    }

    @Test
    public void shouldStoreUnknownWhenTaglineNotRecognised() throws Exception
    {
        givenRootBody(UNKNOWN_BODY);

        detector.detectAndStore();

        verify(attributeService).setAttribute(eq("unknown"), eq(ATTR_ROOT), eq(ATTR_SEARCH_ENGINE_NAME));
        verify(attributeService).setAttribute(eq("1.2.3"), eq(ATTR_ROOT), eq(ATTR_SEARCH_ENGINE_VERSION));
    }

    @Test
    public void shouldMatchTaglineCaseInsensitively() throws Exception
    {
        givenRootBody("{\"version\":{\"number\":\"8.1.0\"},\"tagline\":\"YOU KNOW, FOR SEARCH\"}");

        detector.detectAndStore();

        verify(attributeService).setAttribute(eq("elasticsearch"), eq(ATTR_ROOT), eq(ATTR_SEARCH_ENGINE_NAME));
    }

    @Test
    public void shouldPersistAsSystemUserInWritableRequiresNewTransaction() throws Exception
    {
        givenRootBody(ES_BODY);

        detector.detectAndStore();

        authUtil.verify(() -> AuthenticationUtil.runAs(any(RunAsWork.class), eq(SYSTEM_USER)));
        verify(txnHelper).setForceWritable(true);
        // readOnly = false, requiresNew = true
        verify(txnHelper).doInTransaction(any(RetryingTransactionCallback.class), eq(false), eq(true));
    }

    @Test
    public void shouldNotStoreWhenBodyIsEmpty() throws Exception
    {
        givenClientChain();
        when(response.getBody()).thenReturn(Optional.empty());

        detector.detectAndStore();

        verifyNoInteractions(attributeService);
    }

    @Test
    public void shouldNotStoreWhenBodyIsMalformedJson() throws Exception
    {
        givenRootBody("this is not json");

        detector.detectAndStore();

        verifyNoInteractions(attributeService);
    }

    @Test
    public void shouldNotStoreWhenVersionIsMissing() throws Exception
    {
        givenRootBody("{\"tagline\":\"You Know, for Search\"}");

        detector.detectAndStore();

        verifyNoInteractions(attributeService);
    }

    @Test
    public void shouldNotStoreWhenTaglineIsMissing() throws Exception
    {
        givenRootBody("{\"version\":{\"number\":\"8.17.0\"}}");

        detector.detectAndStore();

        verifyNoInteractions(attributeService);
    }

    @Test
    public void shouldNotStoreAndNotThrowWhenClientFails() throws Exception
    {
        givenClientChain();
        when(genericClient.execute(any(Request.class))).thenThrow(new IOException("engine unreachable"));

        detector.detectAndStore();

        verifyNoInteractions(attributeService);
    }

    private void givenClientChain()
    {
        when(httpClientFactory.getElasticsearchClient()).thenReturn(openSearchClient);
        when(openSearchClient.generic()).thenReturn(genericClient);
    }

    private void givenRootBody(String rawJson) throws IOException
    {
        givenClientChain();
        when(genericClient.execute(any(Request.class))).thenReturn(response);
        when(response.getBody()).thenReturn(Optional.of(body));
        when(body.bodyAsString()).thenReturn(rawJson);
    }
}
