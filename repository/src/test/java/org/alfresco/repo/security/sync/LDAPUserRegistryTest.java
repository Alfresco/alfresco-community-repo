/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.security.sync;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.ldap.LDAPInitialDirContextFactory;
import org.alfresco.repo.security.sync.ldap.LDAPUserRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.alfresco.repo.security.sync.ldap.LDAPUserRegistry}
 * @author amukha
 */
@RunWith(MockitoJUnitRunner.class)
public class LDAPUserRegistryTest
{
    @Mock private LDAPInitialDirContextFactory contextFactory;
    @Mock private InitialDirContext initialDirContext;
    @Mock private NamespaceService namespaceService;
    @Mock private SearchResult searchResult;
    @Mock private NamingEnumeration<SearchResult> searchResults;
    @Mock private Attributes attributes;
    @Mock private Attribute groupAttribute;
    @Mock private Attribute rangeRestrictedAttribute;

    private static final String GROUP_SEARCH_BASE = "ou=groups,DC=example,DC=foo";
    private static final String USER_SEARCH_BASE = "cn=Users,DC=example,DC=foo";
    private static final String GROUP_QUERY = "groupQuery=(objectclass=groupOfNames)";
    private static final String PERSON_QUERY = "(objectclass=inetOrgPerson)";
    private static final String GROUP_DIFFERENTIAL_QUERY = "(objectclass=groupOfNames)";
    private static final String PERSON_DIFFERENTIAL_QUERY = "(objectclass=inetOrgPerson)";
    private static final String GROUP_ID_ATTRIBUTE_NAME = "cn";
    private static final String USER_ID_ATTRIBUTE_NAME = "uid";
    private static final String GROUP_ATTRIBUTE = "cn: foogroup10";
    private static final String MEMBER_ATTRIBUTE_NAME = "member";
    private static final String MEMBER_ATTRIBUTE_VALUE = "cn=foouser10,cn=Users,dc=example,dc=foo";

    private LDAPUserRegistry createRegistry() throws Exception
    {
        LDAPUserRegistry registry = new LDAPUserRegistry();
        registry.setLDAPInitialDirContextFactory(contextFactory);
        registry.setNamespaceService(namespaceService);
        registry.setGroupSearchBase(GROUP_SEARCH_BASE);
        registry.setUserSearchBase(USER_SEARCH_BASE);
        registry.setGroupQuery(GROUP_QUERY);
        registry.setPersonQuery(PERSON_QUERY);
        registry.setGroupDifferentialQuery(GROUP_DIFFERENTIAL_QUERY);
        registry.setPersonDifferentialQuery(PERSON_DIFFERENTIAL_QUERY);
        registry.setGroupIdAttributeName(GROUP_ID_ATTRIBUTE_NAME);
        registry.setUserIdAttributeName(USER_ID_ATTRIBUTE_NAME);
        registry.setMemberAttribute(MEMBER_ATTRIBUTE_NAME);

        Set<String> prefixes = new HashSet<>();
        prefixes.add(NamespaceService.CONTENT_MODEL_PREFIX);

        when(namespaceService.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI))
                .thenReturn(prefixes);
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX))
                .thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        when(contextFactory.getDefaultIntialDirContext()).thenReturn(initialDirContext);
        when(contextFactory.getDefaultIntialDirContext(0)).thenReturn(initialDirContext);
        when(initialDirContext.search(eq(GROUP_SEARCH_BASE), eq(GROUP_DIFFERENTIAL_QUERY), any())).thenReturn(searchResults);
        when(searchResults.hasMore()).thenReturn(true);
        when(searchResults.next()).thenReturn(searchResult);
        when(searchResult.getAttributes()).thenReturn(attributes);
        when(attributes.get(GROUP_ID_ATTRIBUTE_NAME)).thenReturn(groupAttribute);
        when(groupAttribute.get(0)).thenReturn(GROUP_ATTRIBUTE);
        when(attributes.get(MEMBER_ATTRIBUTE_NAME)).thenReturn(rangeRestrictedAttribute);
        when(rangeRestrictedAttribute.size()).thenReturn(1);
        when(rangeRestrictedAttribute.get(0)).thenReturn(MEMBER_ATTRIBUTE_VALUE);


        registry.afterPropertiesSet();
        return registry;
    }

    /**
     * Test for MNT-17966
     */
    @Test
    public void testTimeoutDuringSync() throws Exception
    {
        LDAPUserRegistry userRegistry = createRegistry();

        when(initialDirContext.getAttributes(eq(LDAPUserRegistry.jndiName(MEMBER_ATTRIBUTE_VALUE)), any()))
                .thenThrow(new NamingException(LDAPUserRegistry.NAMING_TIMEOUT_EXCEPTION_MESSAGE + " test."));

        try
        {
            userRegistry.getGroups(new Date());
            fail("The process should fail with an exception");
        }
        catch (AlfrescoRuntimeException are)
        {
            assertEquals("The error message is not of the right format.",
                    "synchronization.err.ldap.search", are.getMsgId());
            assertTrue("The error message was not caused by timeout.",
                    are.getCause().getMessage().contains(LDAPUserRegistry.NAMING_TIMEOUT_EXCEPTION_MESSAGE));
        }
    }
}
