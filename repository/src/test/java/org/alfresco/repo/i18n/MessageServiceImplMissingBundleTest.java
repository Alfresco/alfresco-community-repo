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
package org.alfresco.repo.i18n;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Verifies that {@link MessageServiceImpl} does not call {@code getRepoResourceBundle()} on every {@code getMessage()} invocation when a repository-based message bundle cannot be found.
 */
public class MessageServiceImplMissingBundleTest
{
    private static final String TENANT_DOMAIN = "";

    private MessageServiceImpl messageService;
    private NodeService nodeService;

    @Before
    public void setUp()
    {
        TenantService tenantService = mock(TenantService.class);
        when(tenantService.getCurrentUserDomain()).thenReturn(TENANT_DOMAIN);
        when(tenantService.isTenantUser()).thenReturn(false);
        when(tenantService.getBaseName(any(String.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tenantService.getName(any(StoreRef.class))).thenAnswer(inv -> inv.getArgument(0));

        NodeRef rootNodeRef = new NodeRef("workspace://SpacesStore/root-node");
        nodeService = mock(NodeService.class);
        when(nodeService.getRootNode(any(StoreRef.class))).thenReturn(rootNodeRef);
        when(nodeService.getChildAssocs(any(NodeRef.class), any(), any(QName.class)))
                .thenReturn(Collections.emptyList());

        NamespaceService namespaceService = mock(NamespaceService.class);
        when(namespaceService.getNamespaceURI(any(String.class)))
                .thenReturn("http://www.alfresco.org/model/content/1.0");

        Set<String> bundleBaseNames = new HashSet<>();
        bundleBaseNames.add("workspace://SpacesStore/app:company_home/app:dictionary/app:messages/cm:missing-bundle");

        SimpleCache<String, Set<String>> resourceBundleBaseNamesCache = new DefaultSimpleCache<>();
        resourceBundleBaseNamesCache.put(TENANT_DOMAIN, Collections.unmodifiableSet(new HashSet<>(bundleBaseNames)));

        SimpleCache<String, Map<Locale, Set<String>>> loadedResourceBundlesCache = new DefaultSimpleCache<>();
        loadedResourceBundlesCache.put(TENANT_DOMAIN, Collections.unmodifiableMap(new HashMap<>()));

        SimpleCache<String, Map<Locale, Map<String, String>>> messagesCache = new DefaultSimpleCache<>();
        messagesCache.put(TENANT_DOMAIN, Collections.unmodifiableMap(new HashMap<>()));

        messageService = new MessageServiceImpl();
        messageService.setTenantService(tenantService);
        messageService.setNodeService(nodeService);
        messageService.setNamespaceService(namespaceService);
        messageService.setResourceBundleBaseNamesCache(resourceBundleBaseNamesCache);
        messageService.setLoadedResourceBundlesCache(loadedResourceBundlesCache);
        messageService.setMessagesCache(messagesCache);
        messageService.setTryLockTimeout(60_000L);
    }

    @Test
    public void testMissingRepoBundleDoesNotCauseRepeatedDbCalls()
    {
        Locale locale = Locale.ENGLISH;

        // First call — enters loading block, attempts to resolve the bundle path
        messageService.getMessage("some.key", locale);
        Mockito.clearInvocations(nodeService);

        // Second call — should NOT enter the loading block
        messageService.getMessage("another.key", locale);

        verify(nodeService, Mockito.never()).getChildAssocs(any(NodeRef.class), any(), any(QName.class));
    }

    @Test
    public void testEachLocaleTrackedIndependently()
    {
        messageService.getMessage("some.key", Locale.ENGLISH);
        Mockito.clearInvocations(nodeService);
        messageService.getMessage("some.key", Locale.ENGLISH);
        verify(nodeService, Mockito.never()).getChildAssocs(any(NodeRef.class), any(), any(QName.class));
        Mockito.clearInvocations(nodeService);

        messageService.getMessage("some.key", Locale.FRENCH);
        verify(nodeService, Mockito.atLeastOnce()).getChildAssocs(any(NodeRef.class), any(), any(QName.class));
        Mockito.clearInvocations(nodeService);
        messageService.getMessage("some.key", Locale.FRENCH);
        verify(nodeService, Mockito.never()).getChildAssocs(any(NodeRef.class), any(), any(QName.class));
    }

    @Test
    public void testMultipleMissingBundlesAllTrackedInSinglePass()
    {
        messageService.registerResourceBundle(
                "workspace://SpacesStore/app:company_home/app:dictionary/app:messages/cm:missing-bundle-2");
        messageService.registerResourceBundle(
                "workspace://SpacesStore/app:company_home/app:dictionary/app:messages/cm:missing-bundle-3");
        Mockito.clearInvocations(nodeService);
        Locale locale = Locale.ENGLISH;

        messageService.getMessage("key.1", locale);
        verify(nodeService, Mockito.atLeastOnce()).getChildAssocs(any(NodeRef.class), any(), any(QName.class));
        Mockito.clearInvocations(nodeService);

        for (int i = 0; i < 100; i++)
        {
            messageService.getMessage("key." + i, locale);
        }
        verify(nodeService, Mockito.never()).getChildAssocs(any(NodeRef.class), any(), any(QName.class));
    }

}
