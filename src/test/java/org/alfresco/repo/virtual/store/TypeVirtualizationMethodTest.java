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

package org.alfresco.repo.virtual.store;


import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.config.NodeRefExpression;
import org.alfresco.repo.virtual.ref.ClasspathResource;
import org.alfresco.repo.virtual.ref.Parameter;
import org.alfresco.repo.virtual.ref.Protocols;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.ref.RepositoryLocation;
import org.alfresco.repo.virtual.ref.RepositoryResource;
import org.alfresco.repo.virtual.ref.Resource;
import org.alfresco.repo.virtual.ref.ResourceParameter;
import org.alfresco.repo.virtual.ref.ResourceProcessingError;
import org.alfresco.repo.virtual.ref.ResourceProcessor;
import org.alfresco.repo.virtual.ref.VanillaProtocol;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsException;

public class TypeVirtualizationMethodTest extends TestSuite
{
    private static Log logger = LogFactory.getLog(TypeVirtualizationMethodTest.class);

    private static final QName TEST_FOLDER_TYPE = SiteModel.TYPE_SITE;

    private static final QName TEST_ASPECT = SiteModel.ASPECT_SITE_CONTAINER;

    public static class Integration extends VirtualizationIntegrationTest
    {
        private TypeVirtualizationMethod typeVirtualizationMethod;

        @Override
        protected void setUp() throws Exception
        {
            super.setUp();
            typeVirtualizationMethod = ctx.getBean("typeVirtualizationMethod",
                                                   TypeVirtualizationMethod.class);
        }

        @Override
        public void tearDown() throws Exception
        {
            super.tearDown();
            typeVirtualizationMethod.setQnameFilters(virtualizationConfigTestBootstrap.getTypeTemplatesQNameFilter());
        }

        public void testFiltering() throws Exception
        {
            typeVirtualizationMethod.setQnameFilters("st:site");

            ChildAssociationRef typedNodeAssocRef = createTypedNode(testRootFolder.getNodeRef(),
                                                                    "TypeVirtualized",
                                                                    TEST_FOLDER_TYPE);
            NodeRef virtuaChildRef = typedNodeAssocRef.getChildRef();
            assertFalse(typeVirtualizationMethod.canVirtualize(environment,
                                                               virtuaChildRef));

            addTypeTemplate(TEST_FOLDER_TYPE,
                            TEST_TEMPLATE_1_JS_CLASSPATH);

            assertTrue(typeVirtualizationMethod.canVirtualize(environment,
                                                              virtuaChildRef));


            assertTrue(typeVirtualizationMethod.canVirtualize(environment,
                                                              virtuaChildRef));

            typeVirtualizationMethod.setQnameFilters("alf:site");

            assertFalse(typeVirtualizationMethod.canVirtualize(environment,
                                                               virtuaChildRef));

            typeVirtualizationMethod.setQnameFilters("st:*");

            assertTrue(typeVirtualizationMethod.canVirtualize(environment,
                                                              virtuaChildRef));

            typeVirtualizationMethod.setQnameFilters("alf:site");

            assertFalse(typeVirtualizationMethod.canVirtualize(environment,
                                                               virtuaChildRef));

            typeVirtualizationMethod.setQnameFilters("*");

            assertTrue(typeVirtualizationMethod.canVirtualize(environment,
                                                              virtuaChildRef));

            typeVirtualizationMethod.setQnameFilters("none");

            assertFalse(typeVirtualizationMethod.canVirtualize(environment,
                                                               virtuaChildRef));

        }

        public void testVirtualizeCmFolder_recusive() throws Exception
        {
            ChildAssociationRef templateContentChildRef = addTypeTemplate(ContentModel.TYPE_FOLDER,
                                                                          TEST_TEMPLATE_1_JS_CLASSPATH);
            virtualize(templateContentChildRef.getChildRef(),
                       ContentModel.TYPE_FOLDER);
        }

        public void testCanVirtualize() throws Exception
        {
            ChildAssociationRef typedNodeAssocRef = createTypedNode(testRootFolder.getNodeRef(),
                                                                    "TypeVirtualized",
                                                                    TEST_FOLDER_TYPE);
            NodeRef typeNode = typedNodeAssocRef.getChildRef();

            assertFalse(typeVirtualizationMethod.canVirtualize(environment,
                                                               typeNode));

            addTypeTemplate(TEST_FOLDER_TYPE,
                            TEST_TEMPLATE_1_JS_CLASSPATH);

            assertTrue(typeVirtualizationMethod.canVirtualize(environment,
                                                              typeNode));

        }

        public void testCanVirtualizeAspect() throws Exception
        {
            ChildAssociationRef aspectNodeAssocRef = createTypedNode(testRootFolder.getNodeRef(),
                                                                     "TypeVirtualized",
                                                                     ContentModel.TYPE_FOLDER);
            NodeRef aspectNode = aspectNodeAssocRef.getChildRef();

            nodeService.addAspect(aspectNode,
                                  TEST_ASPECT,
                                  Collections.<QName, Serializable> emptyMap());

            assertFalse(typeVirtualizationMethod.canVirtualize(environment,
                                                               aspectNode));

            addTypeTemplate(TEST_ASPECT,
                            TEST_TEMPLATE_1_JS_CLASSPATH);

            assertTrue(typeVirtualizationMethod.canVirtualize(environment,
                                                              aspectNode));

        }

        public void testVirtualizeAspect() throws Exception
        {
            ChildAssociationRef templateContentChildRef = addTypeTemplate(TEST_ASPECT,
                                                                          TEST_TEMPLATE_1_JS_CLASSPATH);
            virtualize(templateContentChildRef.getChildRef(),
                       ContentModel.TYPE_FOLDER,
                       TEST_ASPECT);
        }

        public void testVirtualizeType() throws Exception
        {
            ChildAssociationRef templateContentChildRef = addTypeTemplate(TEST_FOLDER_TYPE,
                                                                          TEST_TEMPLATE_1_JS_CLASSPATH);

            virtualize(templateContentChildRef.getChildRef(),
                       TEST_FOLDER_TYPE);
        }

        private void virtualize(NodeRef expectedTemplateNodeRef, QName fodlerType, QName... aspects) throws Exception
        {

            ChildAssociationRef typedNodeAssocRef = createTypedNode(testRootFolder.getNodeRef(),
                                                                    "TypeVirtualized",
                                                                    fodlerType);
            NodeRef typeNode = typedNodeAssocRef.getChildRef();

            for (QName aspect : aspects)
            {
                nodeService.addAspect(typeNode,
                                      aspect,
                                      Collections.<QName, Serializable> emptyMap());
            }

            assertTrue(typeVirtualizationMethod.canVirtualize(environment,
                                                              typeNode));

            Reference theVirtualizedNode = typeVirtualizationMethod.virtualize(environment,
                                                                               typeNode);
            assertEquals(Protocols.VANILLA.protocol,
                         theVirtualizedNode.getProtocol());

            List<Parameter> parameters = theVirtualizedNode.getParameters();
            ResourceParameter vanillaResourceParameter = (ResourceParameter) parameters
                        .get(VanillaProtocol.VANILLA_TEMPLATE_PARAM_INDEX);
            Resource vanillaResource = vanillaResourceParameter.getValue();

            NodeRef resourceNodeRef = vanillaResource.processWith(new ResourceProcessor<NodeRef>()
            {

                @Override
                public NodeRef process(Resource resource) throws ResourceProcessingError
                {
                    fail("Inavlid resource type");
                    return null;
                }

                @Override
                public NodeRef process(ClasspathResource classpath) throws ResourceProcessingError
                {
                    fail("Inavlid resource type");
                    return null;
                }

                @Override
                public NodeRef process(RepositoryResource repository) throws ResourceProcessingError
                {
                    RepositoryLocation location = repository.getLocation();
                    return location.asNodeRef(environment);
                }
            });

            assertEquals(expectedTemplateNodeRef,
                         resourceNodeRef);
        }

        private synchronized ChildAssociationRef addTypeTemplate(QName theType, String cp)
        {
            NodeRefExpression templatesLocationExpr = virtualizationConfigTestBootstrap.getTypeTemplatesPath();
            NodeRef templatesLocation = templatesLocationExpr.resolve();

            assertNotNull(templatesLocation);

            final String prefixedType = theType.toPrefixString(environment.getNamespacePrefixResolver());
            String contentName = prefixedType;
            contentName = contentName.replaceAll(":",
                                                 "_") + ".json";

            InputStream testTemplsteJsonIS = getClass().getResourceAsStream(cp);
            ChildAssociationRef templateContentChildRef = createContent(templatesLocation,
                                                                        contentName,
                                                                        testTemplsteJsonIS,
                                                                        "application/json",
                                                                        "UTF-8",
                                                                        ContentModel.TYPE_CONTENT);

            typeVirtualizationMethod.setQnameFilters(prefixedType);

            return templateContentChildRef;
        }
    }

    public static class Unit extends TestCase
    {
        public void testQNameFiltersSetter_invalidFilters() throws Exception
        {
            assertIllegalQNameFilters(null,
                                      this);
            assertIllegalQNameFilters("",
                                      this);
            
        }

        public void testQNameFiltersSetter_validFilters() throws Exception
        {
            assertQNameFilters("st:site",
                               this);
            assertQNameFilters("st:site,cm:folder",
                               this);
            assertQNameFilters("st:site,cm:test-folder",
                               this);
            assertQNameFilters("st:*",
                               this);
            assertQNameFilters("st:*,cm:*",
                               this);

            assertQNameFilters("*",
                               this);

            assertQNameFilters("none",
                               this);

        }
    }

    private static NamespacePrefixResolver mockNamespacePrefixResolver()
    {
        NamespacePrefixResolver mockNamespacePrefixResolver = Mockito
                    .mock(NamespacePrefixResolver.class,

                          new ThrowsException(new NamespaceException("Mock exception ")));

        Mockito.doReturn(Arrays.<String> asList(SiteModel.SITE_MODEL_PREFIX))
                        .when(mockNamespacePrefixResolver)
                        .getPrefixes(SiteModel.SITE_MODEL_URL);
        Mockito.doReturn(SiteModel.SITE_MODEL_URL)
                        .when(mockNamespacePrefixResolver)
                        .getNamespaceURI(SiteModel.SITE_MODEL_PREFIX);

        Mockito.doReturn(Arrays.<String> asList(NamespaceService.CONTENT_MODEL_PREFIX))
                        .when(mockNamespacePrefixResolver)
                        .getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI);
        Mockito.doReturn(NamespaceService.CONTENT_MODEL_1_0_URI)
                        .when(mockNamespacePrefixResolver)
                        .getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX);

        Mockito.doReturn("mock(NamespacePrefixResolver)@" + TypeVirtualizationMethod.class.toString())
                        .when(mockNamespacePrefixResolver)
                        .toString();
        return mockNamespacePrefixResolver;
    }

    private static void assertIllegalQNameFilters(String filters, TestCase test)
    {
        TypeVirtualizationMethod tvm = new TypeVirtualizationMethod();
        try
        {
            tvm.setNamespacePrefixResolver(mockNamespacePrefixResolver());
            tvm.setQnameFilters(filters);
            TestCase.fail("Should not be able to set filters string " + filters);
        }
        catch (IllegalArgumentException e)
        {
            // void as expected
            logger.info(e.getMessage());
        }
    }

    private static void assertQNameFilters(String filters, TestCase test)
    {
        TypeVirtualizationMethod tvm = new TypeVirtualizationMethod();
        tvm.setNamespacePrefixResolver(mockNamespacePrefixResolver());
        tvm.setQnameFilters(filters);
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new JUnit4TestAdapter(Integration.class));
        suite.addTest(new JUnit4TestAdapter(Unit.class));
        return suite;
    }
}
