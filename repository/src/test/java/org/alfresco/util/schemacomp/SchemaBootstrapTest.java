/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.util.schemacomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.alfresco.repo.admin.patch.impl.SchemaUpgradeScriptPatch;
import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Category({OwnJVMTestsCategory.class})
public class SchemaBootstrapTest
{
    private static final String BOOTSTRAP_TEST_CONTEXT = "classpath*:alfresco/dbscripts/test-bootstrap-context.xml";
    private static final String MAIN_SCHEMA_REFERENCE_FILE = "classpath:alfresco/dbscripts/create/org.alfresco.repo.domain.dialect.PostgreSQLDialect/Schema-Reference-ALF.xml";
    private static final String TEST_SCHEMA_REFERENCE_FILE = "classpath:alfresco/dbscripts/create/org.alfresco.repo.domain.dialect.PostgreSQLDialect/Test-Schema-Reference-ALF.xml";
    private static final List<String> TEST_SCHEMA_REFERENCE_URLS = Arrays.asList(
            TEST_SCHEMA_REFERENCE_FILE,
            "classpath:alfresco/dbscripts/create/org.alfresco.repo.domain.dialect.PostgreSQLDialect/Schema-Reference-ACT.xml");

    private static ApplicationContextInit APP_CONTEXT_INIT = ApplicationContextInit.createStandardContextWithOverrides(BOOTSTRAP_TEST_CONTEXT);

    @ClassRule
    public static RuleChain staticRuleChain = RuleChain.outerRule(APP_CONTEXT_INIT);

    private SchemaBootstrap schemaBootstrap;
    private SchemaUpgradeScriptPatch optionalPatch;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        ResourcePatternResolver rpr = new PathMatchingResourcePatternResolver(SchemaBootstrapTest.class.getClassLoader());
        Document schemaRefXML = loadXML(rpr.getResource(MAIN_SCHEMA_REFERENCE_FILE));
        Node indexes = getIndexesNode(schemaRefXML);
        indexes.appendChild(createTestIndex(schemaRefXML));
        Resource testSchemaRef = rpr.getResource(TEST_SCHEMA_REFERENCE_FILE);
        updateTestSchemaReferenceFile(testSchemaRef, schemaRefXML);
    }

    @Before
    public void setUp() throws Exception
    {
        schemaBootstrap = (SchemaBootstrap) APP_CONTEXT_INIT.getApplicationContext().getBean("schemaBootstrap");
        schemaBootstrap.setSchemaReferenceUrls(TEST_SCHEMA_REFERENCE_URLS);
        optionalPatch = (SchemaUpgradeScriptPatch) APP_CONTEXT_INIT.getApplicationContext().getBean("patchDbVOAddIndexTest");
    }

    @Test
    public void shouldSchemaValidationReportProblemsCausedByUnappliedOptionalPatch()
    {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();

        PrintWriter out = new PrintWriter(buff);
        int numProblems = schemaBootstrap.validateSchema(null, out);
        out.flush();

        assertEquals(1, numProblems);
        String problems = buff.toString();
        assertTrue("Missing optional patch-specific problems report: \n" + problems,
                problems.contains("The following problems will be resolved once the long running patch "
                        + optionalPatch.getId() + " has been run"));
    }

    private static void updateTestSchemaReferenceFile(Resource testSchemaRef, Document newDoc) throws Exception
    {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(testSchemaRef.getFile());
        DOMSource source = new DOMSource(newDoc);
        transformer.transform(source, result);
    }

    private static Element createTestIndex(Document document)
    {
        Element testIndex = document.createElement("index");
        testIndex.setAttribute("name", "idx_alf_node_test");
        testIndex.setAttribute("unique", "false");

        Element columnNames = document.createElement("columnnames");
        for (String colName: Arrays.asList("acl_id", "audit_creator"))
        {
            Element columnName = document.createElement("columnname");
            columnName.setNodeValue(colName);
            columnNames.appendChild(columnName);
        }
        testIndex.appendChild(columnNames);

        return testIndex;
    }

    private static Node getIndexesNode(Document document) throws XPathExpressionException
    {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("/schema/objects/table[@name='alf_node']/indexes");
        Node indexes = (Node)expr.evaluate(document, XPathConstants.NODE);
        return indexes;
    }

    private static Document loadXML(Resource resource) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        return builder.parse(resource.getInputStream());
    }
}