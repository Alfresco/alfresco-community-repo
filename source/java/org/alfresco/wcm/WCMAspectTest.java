/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.wcm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.webproject.WebProjectInfo;
import org.alfresco.wcm.webproject.WebProjectService;

public class WCMAspectTest extends AbstractWCMServiceImplTest
{

    private AssetService assetService = null;
    private WebProjectService wpService = null;
    private SandboxService sbService = null;
    private DictionaryDAO dictionaryDAO = null;

    private final static int SIZE = 1000;
    private final static String ADMIN = "admin";

    private static String TEST_TYPE_NAMESPACE = "http://www.alfresco.org/model/testaspectmodel/1.0";
    private static QName TEST_ASPECT_QNAME = QName.createQName(TEST_TYPE_NAMESPACE, "Aspect");
    private static QName PROP_QNAME = QName.createQName(TEST_TYPE_NAMESPACE, "applications");

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        wpService = (WebProjectService) ctx.getBean("WebProjectService");
        sbService = (SandboxService) ctx.getBean("SandboxService");
        assetService = (AssetService) ctx.getBean("AssetService");
        dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");

    }

    public void testAspect() throws Exception
    {
        try
        {
            AuthenticationUtil.setFullyAuthenticatedUser(ADMIN);

            WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS + "-aspectSimple", TEST_WEBPROJ_NAME + "-aspectSimple", TEST_WEBPROJ_TITLE,
                    TEST_WEBPROJ_DESCRIPTION, TEST_WEBPROJ_DEFAULT_WEBAPP, TEST_WEBPROJ_DONT_USE_AS_TEMPLATE, null);

            String wpStoreId = wpInfo.getStoreId();
            String defaultWebApp = wpInfo.getDefaultWebApp();

            SandboxInfo sbInfo = sbService.getAuthorSandbox(wpStoreId);
            String authorSandboxId = sbInfo.getSandboxId();

            String authorSandboxPath = sbInfo.getSandboxRootPath() + "/" + defaultWebApp;

            assetService.createFile(authorSandboxId, authorSandboxPath, "myFile", null);

            AssetInfo assetInfo = assetService.getAsset(authorSandboxId, authorSandboxPath + "/" + "myFile");
            attachAspect(assetInfo);
            checkAspect(assetInfo);
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    @SuppressWarnings("unchecked")
    private void checkAspect(AssetInfo assetInfo)
    {
        assertTrue(assetService.hasAspect(assetInfo, TEST_ASPECT_QNAME));

        Map<QName, Serializable> properties = assetService.getAssetProperties(assetInfo);

        List<String> list = (List<String>) properties.get(PROP_QNAME);
        assertEquals(list.size(), SIZE);
    }

    private void attachAspect(final AssetInfo assetInfo)
    {
        M2Model model = M2Model.createModel("custom:custom");
        model.createNamespace(TEST_TYPE_NAMESPACE, "custom");
        model.createImport(NamespaceService.DICTIONARY_MODEL_1_0_URI, NamespaceService.DICTIONARY_MODEL_PREFIX);
        model.createImport(NamespaceService.SYSTEM_MODEL_1_0_URI, NamespaceService.SYSTEM_MODEL_PREFIX);
        model.createImport(NamespaceService.CONTENT_MODEL_1_0_URI, NamespaceService.CONTENT_MODEL_PREFIX);

        M2Aspect testMandatoryAspect = model.createAspect("custom:" + TEST_ASPECT_QNAME.getLocalName());

        M2Property prop = testMandatoryAspect.createProperty("custom:" + PROP_QNAME.getLocalName());
        prop.setType("d:" + DataTypeDefinition.TEXT.getLocalName());
        prop.setMultiValued(true);
        prop.setIndexed(true);

        dictionaryDAO.putModel(model);

        final Map<QName, Serializable> aspectValues = new HashMap<QName, Serializable>();
        List<String> applications = new ArrayList<String>();

        for (int i = 0; i < SIZE; i++)
        {
            applications.add("Adding " + i);
        }

        aspectValues.put(PROP_QNAME, (Serializable) applications);

        // takes about 150 milliseconds to commit
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionCallback<Object>()
        {

            @Override
            public Object execute() throws Throwable
            {
                assetService.addAspect(assetInfo, TEST_ASPECT_QNAME, aspectValues);
                return null;
            }
            
        });
    }
}