/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.security.permissions.dynamic;

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.alfresco.util.test.junitrules.TemporarySites;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.springframework.context.ApplicationContext;

/**
 * This test class demonstrates the permissions of the service account authorities.
 * The service account authorities are used to grant permissions to service accounts.
 * <p>
 * The service account authorities are defined in the <i>alfresco-global.properties</i> file.
 * Using the following naming convention:
 * <pre>
 *   {@code
 *     serviceaccount.role.<service-account-name>=<service-account-role>
 *   }
 * </pre>
 * The service account roles that currently supported are:
 * <ul>
 *   <li>{@link PermissionService#EDITOR_SVC_AUTHORITY}</li>
 *   <li>{@link PermissionService#COLLABORATOR_SVC_AUTHORITY}</li>
 *   <li>{@link PermissionService#ADMIN_SVC_AUTHORITY}</li>
 * </ul>
 * The test class relies on the following service accounts defined in the <i>alfresco-global.properties</i> file:
 * <ul>
 *   <li>serviceaccount.role.test-editor-sa=ROLE_EDITOR_SERVICE_ACCOUNT</li>
 *   <li>serviceaccount.role.test-collaborator-sa=ROLE_COLLABORATOR_SERVICE_ACCOUNT</li>
 *   <li>serviceaccount.role.test-admin-sa=ROLE_ADMIN_SERVICE_ACCOUNT</li>
 * </ul>
 * <p>
 * <b>Note:</b> There is no need to use public services (i.e., beans that start with a capital letter, such as NodeService)
 * to validate roles permissions. This is because the security enforcement of public services (i.e., ACL checks) ultimately relies on
 * the {@code permissionService.hasPermission()} method. Therefore, we can directly use the {@code permissionService.hasPermission()} method.
 *
 * @author Jamal Kaabi-Mofrad
 */
// Ignore the PMD warning about having too many test methods in this class; it makes the tests easier to read and maintain.
@SuppressWarnings("PMD.TooManyMethods")
public class ServiceAccountRoleTest
{
    // Rule to initialise the default Alfresco spring configuration
    private static final ApplicationContextInit APP_CONTEXT = new ApplicationContextInit();

    // A rule to manage a test site
    private static final TemporarySites TEST_SITES = new TemporarySites(APP_CONTEXT);

    // A rule to manage test nodes reused across all the test methods
    private static final TemporaryNodes TEST_NODES = new TemporaryNodes(APP_CONTEXT);

    // Rules to create the users for the tests
    private static final AlfrescoPerson NORMAL_USER = getAlfrescoPerson("john.doe" + currentTimeMillis());
    private static final AlfrescoPerson EDITOR_SA = getAlfrescoPerson("test-editor-sa");
    private static final AlfrescoPerson COLLABORATOR_SA = getAlfrescoPerson("test-collaborator-sa");
    private static final AlfrescoPerson ADMIN_SA = getAlfrescoPerson("test-admin-sa");

    private static final String TEST_TEXT_FILE_NAME = "testTextFile.txt";

    // Tie them together in a static Rule Chain
    @ClassRule
    public static final RuleChain STATIC_RULE_CHAIN = RuleChain.outerRule(APP_CONTEXT)
            .around(TEST_SITES)
            .around(TEST_NODES)
            .around(NORMAL_USER)
            .around(EDITOR_SA)
            .around(COLLABORATOR_SA)
            .around(ADMIN_SA);

    private static NodeService nodeService;
    private static SiteService siteService;
    private static PermissionService permissionService;
    private static Properties globalProperties;
    private static NodeRef testTextFile;

    @BeforeClass
    public static void initStaticData() throws Exception
    {
        ApplicationContext context = APP_CONTEXT.getApplicationContext();
        nodeService = context.getBean("NodeService", NodeService.class);
        siteService = context.getBean("SiteService", SiteService.class);
        permissionService = context.getBean("permissionService", PermissionService.class);
        globalProperties = context.getBean("global-properties", Properties.class);

        // Check that the service account roles are defined in the global properties before starting the tests.
        serviceAccountsShouldExistInGlobalProperties();

        // Create a test site
        SiteInfo testSite = createTestSite();
        // Create a test text file in the test site
        createTestFile(testSite);

        // Clear the current security context to avoid any issues with the test setup
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    private static AlfrescoPerson getAlfrescoPerson(String username)
    {
        return new AlfrescoPerson(APP_CONTEXT, username);
    }

    private static SiteInfo createTestSite()
    {
        // Create a private test site to make sure no other non-members or
        // non-site-admins can access the test site.
        return TEST_SITES.createSite("sitePreset", "saTestSite" + currentTimeMillis(), "SA Test Site",
                                     "sa test site desc", SiteVisibility.PRIVATE,
                                     AuthenticationUtil.getAdminUserName());
    }

    private static void createTestFile(SiteInfo testSite)
    {
        // Create a test text file in the test site as the admin user
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        final NodeRef docLib = siteService.getContainer(testSite.getShortName(), SiteService.DOCUMENT_LIBRARY);

        final NodeRef testFolder = TEST_NODES.createFolder(docLib, "testFolder", AuthenticationUtil.getAdminUserName());
        testTextFile = TEST_NODES.createNodeWithTextContent(testFolder, TEST_TEXT_FILE_NAME, ContentModel.TYPE_CONTENT,
                                                            AuthenticationUtil.getAdminUserName(),
                                                            "The quick brown fox jumps over the lazy dog.");

        Map<QName, Serializable> props = Map.of(ContentModel.PROP_NAME, TEST_TEXT_FILE_NAME,
                                                ContentModel.PROP_DESCRIPTION, "Desc added by Admin.");
        nodeService.setProperties(testTextFile, props);
    }

    private static void serviceAccountsShouldExistInGlobalProperties()
    {
        assertServiceAccountIsDefined(PermissionService.EDITOR_SVC_AUTHORITY, EDITOR_SA.getUsername());
        assertServiceAccountIsDefined(PermissionService.COLLABORATOR_SVC_AUTHORITY, COLLABORATOR_SA.getUsername());
        assertServiceAccountIsDefined(PermissionService.ADMIN_SVC_AUTHORITY, ADMIN_SA.getUsername());
    }

    private static void assertServiceAccountIsDefined(String expectedRole, String username)
    {
        assertEquals(expectedRole, globalProperties.getProperty("serviceaccount.role." + username));
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    @Test
    public void normalUserReadAccessShouldBeDenied()
    {
        assertAccessDenied(NORMAL_USER, PermissionService.READ);
    }

    @Test
    public void editorSaReadAccessShouldBeAllowed()
    {
        assertAccessAllowed(EDITOR_SA, PermissionService.READ);
    }

    @Test
    public void collaboratorSaReadAccessShouldBeAllowed()
    {
        assertAccessAllowed(COLLABORATOR_SA, PermissionService.READ);
    }

    @Test
    public void adminSaReadAccessShouldBeAllowed()
    {
        assertAccessAllowed(ADMIN_SA, PermissionService.READ);
    }

    @Test
    public void normalUserWriteAccessShouldBeDenied()
    {
        assertAccessDenied(NORMAL_USER, PermissionService.WRITE);
    }

    @Test
    public void editorSaWriteAccessShouldBeAllowed()
    {
        assertAccessAllowed(EDITOR_SA, PermissionService.WRITE);
    }

    @Test
    public void collaboratorSaWriteAccessShouldBeAllowed()
    {
        assertAccessAllowed(COLLABORATOR_SA, PermissionService.WRITE);
    }

    @Test
    public void adminSaWriteAccessShouldBeAllowed()
    {
        assertAccessAllowed(ADMIN_SA, PermissionService.WRITE);
    }

    @Test
    public void normalUserAddChildrenAccessShouldBeDenied()
    {
        assertAccessDenied(NORMAL_USER, PermissionService.ADD_CHILDREN);
    }

    @Test
    public void editorSaAddChildrenAccessShouldBeDenied()
    {
        assertAccessDenied(EDITOR_SA, PermissionService.ADD_CHILDREN);
    }

    @Test
    public void collaboratorSaAddChildrenAccessShouldBeAllowed()
    {
        assertAccessAllowed(COLLABORATOR_SA, PermissionService.ADD_CHILDREN);
    }

    @Test
    public void adminSaAddChildrenAccessShouldBeAllowed()
    {
        assertAccessAllowed(ADMIN_SA, PermissionService.ADD_CHILDREN);
    }

    @Test
    public void normalUserDeleteAccessShouldBeDenied()
    {
        assertAccessDenied(NORMAL_USER, PermissionService.DELETE);
    }

    @Test
    public void editorSaDeleteAccessShouldBeDenied()
    {
        assertAccessDenied(EDITOR_SA, PermissionService.DELETE);
    }

    @Test
    public void collaboratorSaDeleteAccessShouldBeDenied()
    {
        assertAccessDenied(COLLABORATOR_SA, PermissionService.DELETE);
    }

    @Test
    public void adminSaDeleteAccessShouldBeAllowed()
    {
        assertAccessAllowed(ADMIN_SA, PermissionService.DELETE);
    }

    @Test
    public void normalUserAssociationAccessShouldBeDenied()
    {
        assertAccessDenied(NORMAL_USER, PermissionService.READ_ASSOCIATIONS);
        assertAccessDenied(NORMAL_USER, PermissionService.CREATE_ASSOCIATIONS);
        assertAccessDenied(NORMAL_USER, PermissionService.DELETE_ASSOCIATIONS);
    }

    @Test
    public void editorSaAssociationAccessShouldBeDenied()
    {
        assertAccessDenied(EDITOR_SA, PermissionService.READ_ASSOCIATIONS);
        assertAccessDenied(EDITOR_SA, PermissionService.CREATE_ASSOCIATIONS);
        assertAccessDenied(EDITOR_SA, PermissionService.DELETE_ASSOCIATIONS);
    }

    @Test
    public void collaboratorSaAssociationAccessShouldBeDenied()
    {
        assertAccessDenied(COLLABORATOR_SA, PermissionService.READ_ASSOCIATIONS);
        assertAccessDenied(COLLABORATOR_SA, PermissionService.CREATE_ASSOCIATIONS);
        assertAccessDenied(COLLABORATOR_SA, PermissionService.DELETE_ASSOCIATIONS);
    }

    @Test
    public void adminSaAssociationAccessShouldBeAllowed()
    {
        assertAccessAllowed(ADMIN_SA, PermissionService.READ_ASSOCIATIONS);
        assertAccessAllowed(ADMIN_SA, PermissionService.CREATE_ASSOCIATIONS);
        assertAccessAllowed(ADMIN_SA, PermissionService.DELETE_ASSOCIATIONS);
    }

    @Test
    public void normalUserReadPermissionsAccessShouldBeDenied()
    {
        assertAccessDenied(NORMAL_USER, PermissionService.READ_PERMISSIONS);
    }

    @Test
    public void editorSaReadPermissionsAccessShouldBeDenied()
    {
        assertAccessDenied(EDITOR_SA, PermissionService.READ_PERMISSIONS);
    }

    @Test
    public void collaboratorSaReadPermissionsAccessShouldBeDenied()
    {
        assertAccessDenied(COLLABORATOR_SA, PermissionService.READ_PERMISSIONS);
    }

    @Test
    public void adminSaReadPermissionsAccessShouldBeAllowed()
    {
        assertAccessAllowed(ADMIN_SA, PermissionService.READ_PERMISSIONS);
    }

    private static void assertAccessDenied(AlfrescoPerson user, String permission)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user.getUsername());
        assertEquals(AccessStatus.DENIED, permissionService.hasPermission(testTextFile, permission));
    }

    private static void assertAccessAllowed(AlfrescoPerson user, String permission)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(user.getUsername());
        assertEquals(AccessStatus.ALLOWED, permissionService.hasPermission(testTextFile, permission));
    }
}
