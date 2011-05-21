/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @author Dmitry Velichkevich
 */
public class AVMTemplateNodeTest extends TestCase
{
    private static final String TEST_WCM_NAMESPACE = "http://www.alfresco.org/model/testwcmmodel/1.0";

    private static final QName ASPECT_AUTHORED = QName.createQName(TEST_WCM_NAMESPACE, "authored");
    private static final QName PROP_AUTHORED_DATE = QName.createQName(TEST_WCM_NAMESPACE, "dateAuthored");

    private static final ApplicationContext APPLICATION_CONTEXT = ApplicationContextHelper.getApplicationContext(new String[] { "classpath:alfresco/application-context.xml",
            "classpath:test/alfresco/wcm-template-node-test-context.xml" });
    private static final ServiceRegistry SERVICE_REGISTRY = (ServiceRegistry) APPLICATION_CONTEXT.getBean(ServiceRegistry.SERVICE_REGISTRY);

    private AVMService avmService = SERVICE_REGISTRY.getAVMService();

    @Override
    protected void setUp() throws Exception
    {
        avmService.createStore("main");
        avmService.createDirectory("main:/", "root");
        avmService.createFile("main:/root", "testfile.txt");
    }

    @Override
    protected void tearDown() throws Exception
    {
        avmService.purgeStore("main");
    }

    @SuppressWarnings("unchecked")
    public void testDatePropertiesConversion() throws Exception
    {
        assertNotNull("Aspect 'twcm:authored' is not in the set of compiled dictionary models", SERVICE_REGISTRY.getDictionaryService().getAspect(ASPECT_AUTHORED));

        List<Serializable> values = new LinkedList<Serializable>();
        for (int i = 0; i < 5; i++)
        {
            values.add(new Date());
        }
        PropertyValue value = new PropertyValue(DataTypeDefinition.TEXT, (Serializable) values);

        avmService.addAspect("main:/root/testfile.txt", ASPECT_AUTHORED);
        avmService.setNodeProperty("main:/root/testfile.txt", PROP_AUTHORED_DATE, value);
        NodeRef nodeRef = AVMNodeConverter.ToNodeRef(-1, "main:/root/testfile.txt");
        AVMTemplateNode templateNode = new AVMTemplateNode(nodeRef, SERVICE_REGISTRY, null);

        Map<String, Serializable> properties = templateNode.getProperties();
        assertNotNull(properties);
        assertFalse(properties.isEmpty());
        assertTrue(properties.containsKey(PROP_AUTHORED_DATE));

        Collection<Serializable> authoredDates = (Collection<Serializable>) properties.get(PROP_AUTHORED_DATE);
        assertNotNull(authoredDates);
        assertFalse(authoredDates.isEmpty());

        for (Serializable date : authoredDates)
        {
            assertFalse(("Unexpected data type of 'twcm:authored' property values: " + ((null != date) ? (date.getClass().getName()) : ("null"))), date instanceof String);
        }
    }
}
