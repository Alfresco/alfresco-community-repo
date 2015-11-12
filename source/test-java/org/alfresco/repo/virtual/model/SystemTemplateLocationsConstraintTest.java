/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.model;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.config.NodeRefExpression;
import org.alfresco.repo.virtual.config.NodeRefPathExpression;
import org.alfresco.repo.virtual.config.NodeRefPathExpressionFactory;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Test;

public class SystemTemplateLocationsConstraintTest extends VirtualizationIntegrationTest
{
    private ServiceRegistry serviceRegistry;

    private NodeRefPathExpressionFactory nrPathExpressionFactory;

    private NodeRefExpression templatesParentRepositoryPath;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
        nrPathExpressionFactory = (NodeRefPathExpressionFactory) ctx.getBean("config.NodeRefPathExpressionFactory");

    }

    @Override
    public void tearDown() throws Exception
    {
        if (templatesParentRepositoryPath != null)
        {
            constraints.setTemplatesParentRepositoryPath(templatesParentRepositoryPath);
            templatesParentRepositoryPath = null;
        }
        super.tearDown();
    }

    @Test
    public void testNullConstraints() throws Exception
    {
        configuredTemplatesClassPath = constraints.getTemplatesParentClasspath();
        constraints.setTemplatesParentClasspath("/org/alfresco/repo/virtual");

        templatesParentRepositoryPath = constraints.getTemplatesParentRepositoryPath();
        NodeRefPathExpression aNewPath = nrPathExpressionFactory.createInstance();
        constraints.setTemplatesParentRepositoryPath(aNewPath);

        List<String> rawAllowedValues = constraints.getRawAllowedValues();
        assertEquals(1,
                     rawAllowedValues.size());
        assertEquals(SystemTemplateLocationsConstraint.NULL_SYSTEM_TEMPLATE,
                     rawAllowedValues.get(0));

    }

    @Test
    public void testConfiguredConstraints() throws Exception
    {
        List<String> rawAllowedValues = constraints.getRawAllowedValues();
        NodeRefExpression sysTemplatesPath = virtualizationConfigTestBootstrap.getSystemTemplatesPath();

        NodeRef templatesLocation = sysTemplatesPath.resolve(true);

        PagingResults<FileInfo> templates = fileAndFolderService
                    .list(templatesLocation,
                          Collections.singleton(VirtualContentModel.TYPE_VIRTUAL_FOLDER_TEMPLATE),
                          null,
                          null,
                          new PagingRequest(1000));

        List<FileInfo> templatesPage = templates.getPage();
        if (!templatesPage.isEmpty())
        {
            assertEquals(templatesPage.size(),
                         rawAllowedValues.size());

            List<String> expectedSysPaths = new LinkedList<>();
            for (FileInfo fi : templatesPage)
            {
                expectedSysPaths.add("N" + fi.getNodeRef().toString());
            }

            assertTrue(rawAllowedValues.containsAll(expectedSysPaths));
        }

    }

    @Test
    public void testAllConstraints() throws Exception
    {
        configuredTemplatesClassPath = constraints.getTemplatesParentClasspath();
        constraints.setTemplatesParentClasspath("/org/alfresco/repo/virtual/template");

        NodeRefExpression sysTemplatesPath = virtualizationConfigTestBootstrap.getSystemTemplatesPath();

        NodeRef templatesLocation = sysTemplatesPath.resolve(true);

        assertNotNull(templatesLocation);

        InputStream testTemplsteJsonIS = getClass().getResourceAsStream(TEST_TEMPLATE_1_JSON_CLASSPATH);
        ChildAssociationRef templateAssoc = createContent(templatesLocation,
                                                          TEST_TEMPLATE_1_JSON_NAME,
                                                          testTemplsteJsonIS,
                                                          "application/json",
                                                          "UTF-8",
                                                          QName.createQName(virtualizationConfigTestBootstrap
                                                                                        .getSystemTemplateType(),
                                                                            serviceRegistry.getNamespaceService()));
        testTemplsteJsonIS = getClass().getResourceAsStream(TEST_TEMPLATE_1_JSON_CLASSPATH);
        createContent(templatesLocation,
                      "non" + TEST_TEMPLATE_1_JSON_NAME,
                      testTemplsteJsonIS,
                      "application/json",
                      "UTF-8",
                      ContentModel.TYPE_CONTENT);

        List<String> rawAllowedValues = constraints.getRawAllowedValues();

        assertTrue(rawAllowedValues.size() >= 5);

        assertTrue("Invalid values " + rawAllowedValues,
                   rawAllowedValues.contains(TEST_TEMPLATE_1_JSON_SYS_PATH));
        assertTrue("Invalid values " + rawAllowedValues,
                   rawAllowedValues.contains(TEST_TEMPLATE_2_JSON_SYS_PATH));
        assertTrue("Invalid values " + rawAllowedValues,
                   rawAllowedValues.contains(TEST_TEMPLATE_3_JSON_SYS_PATH));
        assertTrue("Invalid values " + rawAllowedValues,
                   rawAllowedValues.contains(TEST_TEMPLATE_4_JSON_SYS_PATH));

        assertTrue("Invalid values " + rawAllowedValues,
                   rawAllowedValues.contains("N" + templateAssoc.getChildRef()));
    }
}
