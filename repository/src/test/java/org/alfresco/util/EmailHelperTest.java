/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import freemarker.cache.TemplateLoader;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

/**
 * Unit tests for {@link EmailHelper} class.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class EmailHelperTest
{

    // this is just a path, the template does not exist!
    private static final String FALLBACK_TEMPLATE_PATH = "alfresco/templates/email-templates/test-email-template.ftl";
    private static final String CLIENT_NAME = "test-client";

    @Mock
    private ServiceRegistry serviceRegistryMock;
    @Mock
    private NodeService nodeServiceMock;
    @Mock
    private SearchService searchServiceMock;
    @Mock
    private NamespaceService namespaceServiceMock;
    @Mock
    private FileFolderService fileFolderServiceMock;
    @Mock
    private PersonService personServiceMock;
    @Mock
    private PreferenceService preferenceServiceMock;
    @Mock
    private Repository repositoryHelperMock;
    @Mock
    private TemplateLoader templateLoaderMock;

    private EmailHelper emailHelper;

    private NodeRef dummyTemplateNodeRef;

    @Before
    public void setup() throws Exception
    {
        this.dummyTemplateNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate());
        // Mock the required services
        this.serviceRegistryMock = mock(ServiceRegistry.class);
        this.nodeServiceMock = mock(NodeService.class);
        this.searchServiceMock = mock(SearchService.class);
        this.namespaceServiceMock = mock(NamespaceService.class);
        this.fileFolderServiceMock = mock(FileFolderService.class);
        this.personServiceMock = mock(PersonService.class);
        this.preferenceServiceMock = mock(PreferenceService.class);
        this.repositoryHelperMock = mock(Repository.class);
        this.templateLoaderMock = mock(TemplateLoader.class);

        when(serviceRegistryMock.getNodeService()).thenReturn(nodeServiceMock);
        when(serviceRegistryMock.getSearchService()).thenReturn(searchServiceMock);
        when(serviceRegistryMock.getNamespaceService()).thenReturn(namespaceServiceMock);
        when(serviceRegistryMock.getFileFolderService()).thenReturn(fileFolderServiceMock);
        when(serviceRegistryMock.getPersonService()).thenReturn(personServiceMock);
        when(serviceRegistryMock.getContentService()).thenReturn(mock(ContentService.class));
        when(repositoryHelperMock.getRootHome()).thenReturn(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()));
        when(fileFolderServiceMock.getLocalizedSibling(dummyTemplateNodeRef)).thenReturn(dummyTemplateNodeRef);

        this.emailHelper = new EmailHelper();
        emailHelper.setServiceRegistry(serviceRegistryMock);
        emailHelper.setPreferenceService(preferenceServiceMock);
        emailHelper.setRepositoryHelper(repositoryHelperMock);
        emailHelper.setCompanyHomeChildName("app:company_home");
        //test init
        emailHelper.init();
        // Note: set the template loader after the init method
        emailHelper.setTemplateLoader(templateLoaderMock);
    }

    @Test
    public void testGetEmailTemplate() throws Exception
    {
        String result = emailHelper.getEmailTemplate(CLIENT_NAME, null, FALLBACK_TEMPLATE_PATH);
        assertEquals("The given template path is null, so the fallback template should be returned.", result, FALLBACK_TEMPLATE_PATH);

        // XPath
        {
            final String emailTemplateXPath = "app:company_home/app:dictionary/app:email_templates/example-email.ftl";

            when(searchServiceMock.selectNodes(repositoryHelperMock.getRootHome(), emailTemplateXPath, null, namespaceServiceMock, false))
                        .thenReturn(Collections.emptyList());
            result = emailHelper.getEmailTemplate(CLIENT_NAME, emailTemplateXPath, FALLBACK_TEMPLATE_PATH);
            assertEquals("Couldn't find email template with the given XPath, so the fallback template should be returned.", result,
                        FALLBACK_TEMPLATE_PATH);

            when(searchServiceMock.selectNodes(repositoryHelperMock.getRootHome(), emailTemplateXPath, null, namespaceServiceMock, false))
                        .thenReturn(Collections.singletonList(dummyTemplateNodeRef));
            result = emailHelper.getEmailTemplate(CLIENT_NAME, emailTemplateXPath, FALLBACK_TEMPLATE_PATH);
            // XPath search returned a valid result
            assertEquals(result, dummyTemplateNodeRef.toString());

            // Simulate a path that starts with '/'
            String xpath = "/" + emailTemplateXPath;
            result = emailHelper.getEmailTemplate(CLIENT_NAME, xpath, FALLBACK_TEMPLATE_PATH);
            // XPath search returned a valid result
            assertEquals(result, dummyTemplateNodeRef.toString());

            when(searchServiceMock.selectNodes(repositoryHelperMock.getRootHome(), emailTemplateXPath, null, namespaceServiceMock, false))
                        .thenReturn(Arrays.asList(dummyTemplateNodeRef, new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate()),
                                    new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, GUID.generate())));
            result = emailHelper.getEmailTemplate(CLIENT_NAME, emailTemplateXPath, FALLBACK_TEMPLATE_PATH);
            // XPath search returned 3 results. But after logging an error, we just return the first element
            assertEquals(result, dummyTemplateNodeRef.toString());
        }

        // NodeRef
        {
            when(nodeServiceMock.exists(dummyTemplateNodeRef)).thenReturn(false);
            result = emailHelper.getEmailTemplate(CLIENT_NAME, dummyTemplateNodeRef.toString(), FALLBACK_TEMPLATE_PATH);
            assertEquals("Couldn't find email template with the given NodeRef, so the fallback template should be returned.", result,
                        FALLBACK_TEMPLATE_PATH);

            when(nodeServiceMock.exists(dummyTemplateNodeRef)).thenReturn(true);
            result = emailHelper.getEmailTemplate(CLIENT_NAME, dummyTemplateNodeRef.toString(), FALLBACK_TEMPLATE_PATH);
            // The NodeRef exists
            assertEquals(result, dummyTemplateNodeRef.toString());
        }

        // Class path
        {
            String classPathTemplate = "alfresco/templates/email-templates/new-template.html";
            when(templateLoaderMock.findTemplateSource(classPathTemplate)).thenReturn(null);
            result = emailHelper.getEmailTemplate(CLIENT_NAME, classPathTemplate, FALLBACK_TEMPLATE_PATH);
            assertEquals("Couldn't find email template with the given class path, so the fallback template should be returned.", result,
                        FALLBACK_TEMPLATE_PATH);

            when(templateLoaderMock.findTemplateSource(classPathTemplate)).thenReturn(new Object());
            result = emailHelper.getEmailTemplate(CLIENT_NAME, classPathTemplate, FALLBACK_TEMPLATE_PATH);
            // The class path is valid and a template is found
            assertEquals(result, classPathTemplate);

            when(templateLoaderMock.findTemplateSource(classPathTemplate)).thenThrow(new IOException());
            result = emailHelper.getEmailTemplate(CLIENT_NAME, classPathTemplate, FALLBACK_TEMPLATE_PATH);
            assertEquals("Error occurred while finding the email template with the class path, so the fallback template should be returned.", result,
                        FALLBACK_TEMPLATE_PATH);
        }

        // test null/empty value
        NodeRef nodeRef = emailHelper.getLocalizedEmailTemplateNodeRef(null);
        assertNull(nodeRef);
        nodeRef = emailHelper.getLocalizedEmailTemplateNodeRef("");
        assertNull(nodeRef);
    }

    @Test
    public void testGetUserLocaleOrDefault() throws Exception
    {
        String userId = "testUser";

        Locale locale = emailHelper.getUserLocaleOrDefault(null);
        assertEquals(I18NUtil.getLocale(), locale);

        when(personServiceMock.personExists(userId)).thenReturn(false);
        locale = emailHelper.getUserLocaleOrDefault(userId);
        assertEquals(I18NUtil.getLocale(), locale);

        when(personServiceMock.personExists(userId)).thenReturn(true);
        when(preferenceServiceMock.getPreference(userId, "locale")).thenReturn(null);
        locale = emailHelper.getUserLocaleOrDefault(userId);
        assertEquals(I18NUtil.getLocale(), locale);

        when(preferenceServiceMock.getPreference(userId, "locale")).thenReturn("fr-FR");
        locale = emailHelper.getUserLocaleOrDefault(userId);
        assertEquals(Locale.FRANCE, locale);
    }
}
