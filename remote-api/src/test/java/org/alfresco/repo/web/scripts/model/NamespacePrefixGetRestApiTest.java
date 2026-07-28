/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.model;

import java.util.Collection;

import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * * Unit Tests for the namespace-prefix api.
 */
public class NamespacePrefixGetRestApiTest extends BaseWebScriptTest
{
    private static final String URL = "/api/model/namespace-prefix";
    private static final String KEY = "prefixUriMap";
    private static final String CM_URI = "http://www.alfresco.org/model/content/1.0";
    private static final String SYS_URI = "http://www.alfresco.org/model/system/1.0";
    private static final String MODULE_URI = "http://www.alfresco.org/system/modules/1.0";

    private NamespaceService namespaceService;
    private TransactionService transactionService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.namespaceService = (NamespaceService) getServer().getApplicationContext().getBean("NamespaceService");
        this.transactionService = (TransactionService) getServer().getApplicationContext().getBean("TransactionService");
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
    }

    @Override
    protected void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        super.tearDown();
    }

    private JSONObject getPrefixUriMap() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL), 200);
        return new JSONObject(response.getContentAsString()).getJSONObject(KEY);
    }

    /** Happy path: the response has the {@code prefixUriMap} envelope and the always-present content model. */
    public void testReturnsPrefixUriMap() throws Exception
    {
        JSONObject map = getPrefixUriMap();

        assertEquals("cm", map.getString(CM_URI));
        assertTrue("Expected the registry to contain many namespaces", map.length() > 1);
    }

    public void testIncludesPlatformNamespaces() throws Exception
    {
        JSONObject map = getPrefixUriMap();

        assertEquals("sys", map.getString(SYS_URI));
        assertEquals("module", map.getString(MODULE_URI));
    }

    /**
     * Fidelity: every namespace registered in the live {@link NamespaceService} must appear in the response, so the endpoint is a faithful projection of the registry (no silent filtering/drift).
     */
    public void testMatchesNamespaceService() throws Exception
    {
        JSONObject map = getPrefixUriMap();

        Collection<String> prefixes = transactionService.getRetryingTransactionHelper()
                .doInTransaction(() -> namespaceService.getPrefixes(), true);

        for (String prefix : prefixes)
        {
            String uri = transactionService.getRetryingTransactionHelper()
                    .doInTransaction(() -> namespaceService.getNamespaceURI(prefix), true);
            assertTrue("Endpoint is missing registered namespace URI: " + uri, map.has(uri));
        }
    }

    /**
     * A custom content model registered at runtime (as a customer would via a deployed model / Custom Model Management) introduces a namespace that is <em>not</em> in the bootstrapped set. Because the endpoint sources from {@link NamespaceService}, that user-registered prefix must appear — this is the core drift/maintenance problem the old static prefixes file could not solve.
     */
    public void testIncludesUserRegisteredCustomNamespace() throws Exception
    {
        final String testUri = "http://www.example.com/model/testmodel/1.0";
        final String testPrefix = "tpf";

        final DictionaryDAO dictionaryDAO = (DictionaryDAO) getServer().getApplicationContext()
                .getBean("dictionaryDAO");

        // Register the custom model + namespace at runtime.
        final QName modelName = transactionService.getRetryingTransactionHelper()
                .doInTransaction((RetryingTransactionCallback<QName>) () -> {
                    M2Model model = M2Model.createModel(testPrefix + ":testmodel");
                    model.createNamespace(testUri, testPrefix);
                    model.createType(testPrefix + ":testType");
                    return dictionaryDAO.putModel(model);
                }, false);

        try
        {
            JSONObject map = getPrefixUriMap();

            assertEquals("A user-registered custom namespace must be returned by the endpoint",
                    testPrefix, map.getString(testUri));
        }

        finally
        {
            // Always remove the temporary model so it does not leak into other tests.
            transactionService.getRetryingTransactionHelper()
                    .doInTransaction((RetryingTransactionCallback<Void>) () -> {
                        dictionaryDAO.removeModel(modelName);
                        return null;
                    }, false);
        }
    }

    /**
     * The descriptor declares {@code <authentication>user</authentication>}: an unauthenticated (guest) call is rejected. (Verify the exact status against the running server; the web-script framework returns 401 for a missing/guest authentication on a user-scoped script.)
     */
    public void testRequiresAuthentication() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        sendRequest(new GetRequest(URL), 401);
    }
}
