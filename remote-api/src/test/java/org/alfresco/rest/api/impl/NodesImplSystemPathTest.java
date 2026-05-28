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
package org.alfresco.rest.api.impl;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

@RunWith(MockitoJUnitRunner.class)
public class NodesImplSystemPathTest
{
    @Mock
    private NodeService nodeService;

    @Mock
    private Repository repositoryHelper;

    @Mock
    private DictionaryService dictionaryService;

    @InjectMocks
    private NodesImpl nodesImpl;

    private NodeRef companyHomeRef;
    private NodeRef dataDictionaryRef;

    private MockedStatic<AuthenticationUtil> authUtilMock;

    @Before
    public void setUp()
    {
        authUtilMock = Mockito.mockStatic(AuthenticationUtil.class);
        authUtilMock.when(() -> AuthenticationUtil.runAsSystem(any(RunAsWork.class)))
                .thenAnswer(invocation -> {
                    RunAsWork<?> work = invocation.getArgument(0);
                    return work.doWork();
                });

        companyHomeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "company-home");
        dataDictionaryRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "data-dictionary");

        when(repositoryHelper.getCompanyHome()).thenReturn(companyHomeRef);

        ChildAssociationRef ddToCompanyHome = new ChildAssociationRef(
                ContentModel.ASSOC_CONTAINS,
                companyHomeRef,
                QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary"),
                dataDictionaryRef);

        when(nodeService.getChildAssocs(
                companyHomeRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "dictionary")))
                        .thenReturn(Collections.singletonList(ddToCompanyHome));
    }

    @After
    public void tearDown()
    {
        if (authUtilMock != null)
        {
            authUtilMock.close();
        }
    }

    @Test
    public void testNodeWithSpecialAncestor_PermissionDenied()
    {
        NodeRef parentRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "parent-uuid");
        NodeRef childRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "child-uuid");

        when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getPrimaryParent(childRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, parentRef, null, childRef));

        when(nodeService.getType(parentRef)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeService.getPrimaryParent(parentRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, dataDictionaryRef, null, parentRef));
        when(dictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_FOLDER)).thenReturn(true);
        when(nodeService.getProperty(parentRef, ContentModel.PROP_NAME)).thenReturn("Web Scripts");

        assertThrows(PermissionDeniedException.class,
                () -> nodesImpl.checkNotSystemPath(childRef));
    }

    @Test
    public void testCompanyHome_ShouldNotTraverseFurther()
    {
        when(nodeService.getType(companyHomeRef)).thenReturn(ContentModel.TYPE_FOLDER);

        nodesImpl.checkNotSystemPath(companyHomeRef);

        verify(nodeService, never()).getPrimaryParent(companyHomeRef);
    }

    @Test
    public void testDirectParentIsSpecialNode_ThrowPermissionDenied()
    {
        // Simulate the actual "Scripts" folder (cm:folder) directly under Data Dictionary
        NodeRef childRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "child-under-dd");

        when(nodeService.getType(childRef)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeService.getPrimaryParent(childRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, dataDictionaryRef, null, childRef));
        when(dictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_FOLDER)).thenReturn(true);
        when(nodeService.getProperty(childRef, ContentModel.PROP_NAME)).thenReturn("Scripts");

        assertThrows(PermissionDeniedException.class,
                () -> nodesImpl.checkNotSystemPath(childRef));
    }

    @Test
    public void testNodeUnderNonProtectedDDChild_ShouldNotBeBlocked()
    {
        // Simulate: file inside "Node Templates" (a DD child that is NOT in the protected list)
        // Path: fileRef -> nodeTemplatesFolder -> dataDictionary -> companyHome
        NodeRef nodeTemplatesRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "node-templates");
        NodeRef fileRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "file-in-node-templates");

        when(nodeService.getType(fileRef)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getPrimaryParent(fileRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, nodeTemplatesRef, null, fileRef));

        when(nodeService.getType(nodeTemplatesRef)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeService.getPrimaryParent(nodeTemplatesRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, dataDictionaryRef, null, nodeTemplatesRef));
        when(dictionaryService.isSubClass(ContentModel.TYPE_FOLDER, ContentModel.TYPE_FOLDER)).thenReturn(true);
        when(nodeService.getProperty(nodeTemplatesRef, ContentModel.PROP_NAME)).thenReturn("Node Templates");

        when(nodeService.getType(dataDictionaryRef)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeService.getPrimaryParent(dataDictionaryRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, companyHomeRef, null, dataDictionaryRef));

        // Should NOT throw — "Node Templates" is not a protected executable folder
        nodesImpl.checkNotSystemPath(fileRef);

        // Traversal stops at Data Dictionary — should never go above it
        verify(nodeService, never()).getPrimaryParent(companyHomeRef);
    }

    @Test
    public void testNodeInsideSite_ShouldNotBeBlocked()
    {
        NodeRef siteNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "site-node");
        NodeRef docLibRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "doc-lib");
        NodeRef fileRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "file-in-site");

        when(nodeService.getType(fileRef)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getPrimaryParent(fileRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, docLibRef, null, fileRef));

        when(nodeService.getType(docLibRef)).thenReturn(ContentModel.TYPE_FOLDER);
        when(nodeService.getPrimaryParent(docLibRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, siteNodeRef, null, docLibRef));

        when(nodeService.getType(siteNodeRef)).thenReturn(SiteModel.TYPE_SITE);

        nodesImpl.checkNotSystemPath(fileRef);

        verify(nodeService, never()).getPrimaryParent(siteNodeRef);
    }

    @Test
    public void testNodeInsideSiteSubtype_ShouldNotBeBlocked()
    {
        NodeRef siteSubtypeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "custom-site-node");
        NodeRef fileRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "file-in-custom-site");

        QName siteSubtype = QName.createQName("http://www.alfresco.org/model/site/1.0", "customSite");
        when(nodeService.getType(fileRef)).thenReturn(ContentModel.TYPE_CONTENT);
        when(nodeService.getPrimaryParent(fileRef)).thenReturn(
                new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, siteSubtypeRef, null, fileRef));
        when(nodeService.getType(siteSubtypeRef)).thenReturn(siteSubtype);
        when(dictionaryService.isSubClass(siteSubtype, SiteModel.TYPE_SITE)).thenReturn(true);

        nodesImpl.checkNotSystemPath(fileRef);

        verify(nodeService, never()).getPrimaryParent(siteSubtypeRef);
    }

    @Test
    public void testDownloadNode_ShouldNotBeBlockedByDataDictionaryAncestor()
    {
        NodeRef downloadNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "download-node");

        when(nodeService.getType(downloadNodeRef)).thenReturn(DownloadModel.TYPE_DOWNLOAD);

        nodesImpl.checkNotSystemPath(downloadNodeRef);

        verify(nodeService, never()).getPrimaryParent(downloadNodeRef);
    }
}
