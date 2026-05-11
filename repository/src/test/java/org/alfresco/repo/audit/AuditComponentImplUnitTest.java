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
package org.alfresco.repo.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.openMocks;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelRegistryImpl;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.repo.domain.propval.PropertyValueDAO;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.transaction.TransactionService;

/**
 * Unit tests for the disabled-paths cache behaviour in {@link AuditComponentImpl}.
 *
 * Covers cache-hit/miss on {@code getDisabledPaths} and cache-invalidation on {@code enableAudit}, {@code disableAudit}, and {@code resetDisabledPaths}.
 */
public class AuditComponentImplUnitTest
{
    private static final String APPLICATION_NAME = "TestApp";
    private static final Long DISABLED_PATHS_ID = 42L;
    private static final String APP_KEY = "test-app";

    @InjectMocks
    private AuditComponentImpl auditComponent;

    @Mock
    private AuditModelRegistryImpl auditModelRegistry;
    @Mock
    private PropertyValueDAO propertyValueDAO;
    @Mock
    private AuditDAO auditDAO;
    @Mock
    private TransactionService transactionService;
    @Mock
    private AuditFilter auditFilter;
    @Mock
    private UserAuditFilter userAuditFilter;
    @Mock
    private AuditRecordReporter auditRecordReporter;
    @Mock
    private SimpleCache<Long, Set<String>> disabledPathsCache;
    @Mock
    private AuditApplication auditApplication;

    private MockedStatic<AlfrescoTransactionSupport> mockedTxnSupport;

    @Before
    public void setUp()
    {
        openMocks(this);

        given(auditModelRegistry.getAuditApplicationByName(APPLICATION_NAME)).willReturn(auditApplication);
        given(auditApplication.getDisabledPathsId()).willReturn(DISABLED_PATHS_ID);
        given(auditApplication.getApplicationKey()).willReturn(APP_KEY);

        auditComponent.setDisabledPathsCacheEnabled(true);

        mockedTxnSupport = mockStatic(AlfrescoTransactionSupport.class);

        // Allow write-transaction check to pass without a real transaction context.
        mockedTxnSupport.when(() -> AlfrescoTransactionSupport.checkTransactionReadState(true)).thenAnswer(inv -> null);
    }

    @After
    public void tearDown()
    {
        mockedTxnSupport.close();
    }

    @Test
    public void testGetDisabledPaths_cacheHit_doesNotCallDAO()
    {
        given(disabledPathsCache.get(DISABLED_PATHS_ID)).willReturn(new HashSet<>());

        auditComponent.enableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should(never()).getPropertyById(any());
        then(disabledPathsCache).should(never()).put(any(), any());
    }

    @Test
    public void testGetDisabledPaths_cacheMiss_loadsFromDAOAndPopulatesCache()
    {
        Set<String> disabledPaths = new HashSet<>();
        given(disabledPathsCache.get(DISABLED_PATHS_ID)).willReturn(null);
        given(propertyValueDAO.getPropertyById(DISABLED_PATHS_ID)).willReturn((Serializable) disabledPaths);

        auditComponent.enableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should().getPropertyById(DISABLED_PATHS_ID);
        then(disabledPathsCache).should().put(eq(DISABLED_PATHS_ID), any());
    }

    @Test
    public void testCacheInvalidated_afterEnableAudit_whenPathsChanged()
    {
        Set<String> disabledPaths = new HashSet<>();
        disabledPaths.add("/" + APP_KEY);
        given(disabledPathsCache.get(DISABLED_PATHS_ID)).willReturn(disabledPaths);

        // null path resolves to "/" + APP_KEY, which is present in the disabled set.
        auditComponent.enableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should().updateProperty(eq(DISABLED_PATHS_ID), any(Serializable.class));
        then(disabledPathsCache).should().remove(DISABLED_PATHS_ID);
    }

    @Test
    public void testCacheNotInvalidated_afterEnableAudit_whenNoPathsChanged()
    {
        given(disabledPathsCache.get(DISABLED_PATHS_ID)).willReturn(new HashSet<>());

        auditComponent.enableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should(never()).updateProperty(any(), any(Serializable.class));
        then(disabledPathsCache).should(never()).remove(any());
    }

    @Test
    public void testCacheInvalidated_afterDisableAudit_whenPathAdded()
    {
        given(disabledPathsCache.get(DISABLED_PATHS_ID)).willReturn(new HashSet<>());

        auditComponent.disableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should().updateProperty(eq(DISABLED_PATHS_ID), any(Serializable.class));
        then(disabledPathsCache).should().remove(DISABLED_PATHS_ID);
    }

    @Test
    public void testCacheNotInvalidated_afterDisableAudit_whenPathAlreadyDisabled()
    {
        Set<String> disabledPaths = new HashSet<>();
        disabledPaths.add("/" + APP_KEY);
        given(disabledPathsCache.get(DISABLED_PATHS_ID)).willReturn(disabledPaths);

        // null path resolves to "/" + APP_KEY, which is already disabled — short-circuits.
        auditComponent.disableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should(never()).updateProperty(any(), any(Serializable.class));
        then(disabledPathsCache).should(never()).remove(any());
    }

    @Test
    public void testCacheInvalidated_afterResetDisabledPaths()
    {
        auditComponent.resetDisabledPaths(APPLICATION_NAME);

        then(propertyValueDAO).should().updateProperty(eq(DISABLED_PATHS_ID), any(Serializable.class));
        then(disabledPathsCache).should().remove(DISABLED_PATHS_ID);
    }

    @Test
    public void testGetDisabledPaths_cacheDisabled_alwaysCallsDAO()
    {
        auditComponent.setDisabledPathsCacheEnabled(false);
        Set<String> disabledPaths = new HashSet<>();
        given(propertyValueDAO.getPropertyById(DISABLED_PATHS_ID)).willReturn((Serializable) disabledPaths);

        auditComponent.enableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should().getPropertyById(DISABLED_PATHS_ID);
        then(disabledPathsCache).should(never()).get(any());
        then(disabledPathsCache).should(never()).put(any(), any());
    }

    @Test
    public void testCacheNotInvalidated_afterEnableAudit_whenCacheDisabled()
    {
        auditComponent.setDisabledPathsCacheEnabled(false);
        Set<String> disabledPaths = new HashSet<>();
        disabledPaths.add("/" + APP_KEY);
        given(propertyValueDAO.getPropertyById(DISABLED_PATHS_ID)).willReturn((Serializable) disabledPaths);

        auditComponent.enableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should().updateProperty(eq(DISABLED_PATHS_ID), any(Serializable.class));
        then(disabledPathsCache).should(never()).remove(any());
    }

    @Test
    public void testCacheNotInvalidated_afterDisableAudit_whenCacheDisabled()
    {
        auditComponent.setDisabledPathsCacheEnabled(false);
        given(propertyValueDAO.getPropertyById(DISABLED_PATHS_ID)).willReturn((Serializable) new HashSet<>());

        auditComponent.disableAudit(APPLICATION_NAME, null);

        then(propertyValueDAO).should().updateProperty(eq(DISABLED_PATHS_ID), any(Serializable.class));
        then(disabledPathsCache).should(never()).remove(any());
    }

    @Test
    public void testCacheNotInvalidated_afterResetDisabledPaths_whenCacheDisabled()
    {
        auditComponent.setDisabledPathsCacheEnabled(false);

        auditComponent.resetDisabledPaths(APPLICATION_NAME);

        then(propertyValueDAO).should().updateProperty(eq(DISABLED_PATHS_ID), any(Serializable.class));
        then(disabledPathsCache).should(never()).remove(any());
    }
}
