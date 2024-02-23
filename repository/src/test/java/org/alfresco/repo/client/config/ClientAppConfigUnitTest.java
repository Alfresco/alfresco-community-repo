/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.client.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.client.config.ClientAppConfig.ClientApp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Jamal Kaabi-Mofrad
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientAppConfigUnitTest
{
    private ClientAppConfig clientAppConfig;
    @Mock
    private SysAdminParams  sysAdminParams;

    private AtomicBoolean initialised;

    @Before
    public void setUp() throws Exception

    {    // This in not initialised yet. i.e. the properties are not processed yet.
        // The processing will start when you call the 'onBootstrap()' method
        clientAppConfig = buildClientAppConfig();

        initialised = new AtomicBoolean(false);

        when(sysAdminParams.getAlfrescoProtocol()).thenReturn("http");
        when(sysAdminParams.getAlfrescoHost()).thenReturn("localhost");
        when(sysAdminParams.getAlfrescoPort()).thenReturn(8080);

        when(sysAdminParams.getShareProtocol()).thenReturn("http");
        when(sysAdminParams.getShareHost()).thenReturn("localhost");
        when(sysAdminParams.getSharePort()).thenReturn(8081);
        when(sysAdminParams.getShareContext()).thenReturn("share");
    }

    private ClientAppConfig buildClientAppConfig()
    {
        Properties defaultProps = getWorkspaceAppProperties();
        defaultProps.putAll(getCoolAppProperties());
        defaultProps.putAll(getShareProperties());
        Properties globalProps = new Properties();

        ClientAppConfig config = new ClientAppConfig();
        config.setDefaultProperties(defaultProps);
        config.setGlobalProperties(globalProps);
        return config;
    }

    @Test
    public void testWorkspaceClient()
    {
        ClientApp client = getClientApp("workspace");
        assertEquals("workspace", client.getName());
        assertEquals("${workspaceUrl}/images", client.getTemplateAssetsUrl());
        assertEquals("${repoBaseUrl}/workspace", client.getClientUrl());
        assertEquals("workspaceUrl", client.getClientUrlPropKey());
        assertEquals("\\$\\{workspaceUrl}", client.getClientUrlPlaceholderPattern()
                    .pattern());

        Map<String, String> properties = client.getProperties();
        assertNotNull(properties);
        assertNull("Not Set", properties.get("inviteModeratedTemplatePath"));
        assertEquals("alfresco/templates/test-email-templates/test-email-template.ftl",
                     properties.get("requestResetPasswordTemplatePath"));
        assertEquals("${workspaceUrl}/reset-password", properties.get("resetPasswordPageUrl"));
        assertEquals("some/path", properties.get("confirmResetPasswordTemplatePath"));
    }

    @Test
    public void testCoolAppClient()
    {
        ClientApp client = getClientApp("coolApp");
        assertEquals("coolApp", client.getName());
        assertEquals("${coolAppUrl}/images", client.getTemplateAssetsUrl());
        assertEquals("http://localhost:8090/cool-app", client.getClientUrl());
        assertEquals("coolAppUrl", client.getClientUrlPropKey());
        assertEquals("\\$\\{coolAppUrl}", client.getClientUrlPlaceholderPattern()
                    .pattern());

        Map<String, String> properties = client.getProperties();
        assertNotNull(properties);
        assertEquals("${coolAppUrl}/page-one/page-two", properties.get("testPropUrl"));
    }

    @Test
    public void resolveWorkspacePlaceholders()
    {
        ClientApp client = getClientApp("workspace");
        // Raw properties
        assertEquals("${repoBaseUrl}/workspace", client.getClientUrl());
        assertEquals("workspaceUrl", client.getClientUrlPropKey());
        assertEquals("\\$\\{workspaceUrl}", client.getClientUrlPlaceholderPattern()
                    .pattern());
        assertEquals("${workspaceUrl}/images", client.getTemplateAssetsUrl());
        assertEquals("${workspaceUrl}/reset-password", client.getProperty("resetPasswordPageUrl"));

        // Resolved properties
        //  String clientUrl = UrlUtil.replaceRepoBaseUrlPlaceholder(client.getClientUrl(), sysAdminParams);
        assertEquals("http://localhost:8080/workspace", client.getResolvedClientUrl(sysAdminParams));
        assertEquals("http://localhost:8080/workspace/images", client.getResolvedTemplateAssetsUrl(sysAdminParams));
        assertEquals("http://localhost:8080/workspace/reset-password",
                     client.getResolvedProperty("resetPasswordPageUrl", sysAdminParams));
    }

    @Test
    public void resolveCoolAppPlaceholders()
    {
        ClientApp client = getClientApp("coolApp");
        // Resolved properties
        assertEquals("http://localhost:8090/cool-app", client.getResolvedClientUrl(sysAdminParams));
        assertEquals("http://localhost:8090/cool-app/images", client.getResolvedTemplateAssetsUrl(sysAdminParams));
        assertEquals("http://localhost:8090/cool-app/page-one/page-two",
                     client.getResolvedProperty("testPropUrl", sysAdminParams));
    }

    @Test
    public void resolveSharePlaceholders()
    {
        ClientApp client = getClientApp("share");
        // Resolved properties
        assertEquals("http://localhost:8081/share", client.getResolvedClientUrl(sysAdminParams));
        assertEquals("http://localhost:8081/share/res/components/images",
                     client.getResolvedTemplateAssetsUrl(sysAdminParams));
        assertEquals("http://localhost:8081/share/page/reset-password",
                     client.getResolvedProperty("resetPasswordPageUrl", sysAdminParams));
        assertEquals("http://localhost:8081/share/s", client.getResolvedProperty("sharedLinkBaseUrl", sysAdminParams));
    }

    @Test
    public void testClientsPropertiesOverride()
    {
        Properties globalProps = new Properties();
        globalProps.put("repo.client-app.workspace.workspaceUrl", "https://develop.envalfresco.com/#");
        globalProps.put("repo.client-app.share.shareUrl", "https://develop.envalfresco.com/share");
        globalProps.put("repo.client-app.coolApp.coolAppUrl", "https://develop.envalfresco.com/eval/cool-app");

        clientAppConfig.setGlobalProperties(globalProps);

        /*
         * Workspace client URL Override
         */
        ClientApp workspaceClient = getClientApp("workspace");
        // Resolved properties
        assertEquals("https://develop.envalfresco.com/#", workspaceClient.getResolvedClientUrl(sysAdminParams));
        assertEquals("https://develop.envalfresco.com/#/images",
                     workspaceClient.getResolvedTemplateAssetsUrl(sysAdminParams));
        assertEquals("https://develop.envalfresco.com/#/reset-password",
                     workspaceClient.getResolvedProperty("resetPasswordPageUrl", sysAdminParams));

        /*
         * Share client URL Override
         */
        ClientApp shareClient = getClientApp("share");
        // Resolved properties
        assertEquals("https://develop.envalfresco.com/share", shareClient.getResolvedClientUrl(sysAdminParams));
        assertEquals("https://develop.envalfresco.com/share/res/components/images",
                     shareClient.getResolvedTemplateAssetsUrl(sysAdminParams));
        assertEquals("https://develop.envalfresco.com/share/page/reset-password",
                     shareClient.getResolvedProperty("resetPasswordPageUrl", sysAdminParams));
        assertEquals("https://develop.envalfresco.com/share/s",
                     shareClient.getResolvedProperty("sharedLinkBaseUrl", sysAdminParams));

        /*
         * coolApp client URL Override
         */
        ClientApp coolAppClient = getClientApp("coolApp");
        // Resolved properties
        assertEquals("https://develop.envalfresco.com/eval/cool-app",
                     coolAppClient.getResolvedClientUrl(sysAdminParams));
        assertEquals("https://develop.envalfresco.com/eval/cool-app/images",
                     coolAppClient.getResolvedTemplateAssetsUrl(sysAdminParams));
        assertEquals("https://develop.envalfresco.com/eval/cool-app/page-one/page-two",
                     coolAppClient.getResolvedProperty("testPropUrl", sysAdminParams));
    }

    private Properties getWorkspaceAppProperties()
    {
        Properties props = new Properties();
        props.put("repo.client-app.workspace.inviteModeratedTemplatePath", "");
        props.put("repo.client-app.workspace.workspaceUrl", "${repoBaseUrl}/workspace");
        props.put("repo.client-app.workspace.templateAssetsUrl", "${workspaceUrl}/images");
        props.put("repo.client-app.workspace.requestResetPasswordTemplatePath",
                  "alfresco/templates/test-email-templates/test-email-template.ftl");
        props.put("repo.client-app.workspace.resetPasswordPageUrl", "${workspaceUrl}/reset-password");
        props.put("repo.client-app.workspace.confirmResetPasswordTemplatePath", "some/path");

        return props;
    }

    private Properties getShareProperties()
    {
        Properties props = new Properties();
        props.put("repo.client-app.share.templateAssetsUrl", "${shareUrl}/res/components/images");
        props.put("repo.client-app.share.resetPasswordPageUrl", "${shareUrl}/page/reset-password");
        props.put("repo.client-app.share.sharedLinkBaseUrl", "${shareUrl}/s");

        return props;
    }

    private Properties getCoolAppProperties()
    {
        Properties props = new Properties();
        props.put("repo.client-app.coolApp.coolAppUrl", "http://localhost:8090/cool-app");
        props.put("repo.client-app.coolApp.templateAssetsUrl", "${coolAppUrl}/images");
        props.put("repo.client-app.coolApp.testPropUrl", "${coolAppUrl}/page-one/page-two");

        return props;
    }

    private ClientApp getClientApp(String clientName)
    {
        if (!initialised.get())
        {
            clientAppConfig.onBootstrap(null);
            initialised.set(true);
        }
        Map<String, ClientApp> clients = clientAppConfig.getClients();
        assertFalse(clients.isEmpty());
        assertTrue(clientName + " client is expected.", clientAppConfig.exists(clientName));
        ClientApp client = clients.get(clientName);
        assertNotNull(clientName + " client can't be null.", client);
        return client;
    }
}
