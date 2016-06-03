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

package org.alfresco.repo.virtual.template;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.VirtualContext;
import org.alfresco.repo.virtual.ref.ClasspathResource;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TemplateResourceProcessorTest extends VirtualizationIntegrationTest
{

    private VirtualFolderDefinition testTemplate2Definition;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        VirtualContext context = new VirtualContext(environment,
                                                    virtualFolder1NodeRef);
        nodeService.setProperty(virtualFolder1NodeRef, ContentModel.PROP_DESCRIPTION,"AContextDescription");
        InputStream vanillaIS = getClass().getResourceAsStream(TEST_TEMPLATE_2_JSON_CLASSPATH);
        String vanillaJSON = IOUtils.toString(vanillaIS,
                                              StandardCharsets.UTF_8);
        context.setParameter(ApplyTemplateMethod.VANILLA_JSON_PARAM_NAME,
                             vanillaJSON);
        TemplateResourceProcessor processor = new TemplateResourceProcessor(context);

        testTemplate2Definition = processor.process(new ClasspathResource(VANILLA_PROCESSOR_JS_CLASSPATH));
    }

    protected void assertReadonly(VirtualFolderDefinition vf)
    {
        FilingRule fr = vf.getFilingRule();
        assertNotNull(fr);
        assertTrue(fr.isNullFilingRule());
    }

    @Test
    public void testBasicNodeInfo() throws Exception
    {
        assertNotNull(testTemplate2Definition);
        assertNotNull(testTemplate2Definition.getChildren());
        assertEquals(4,
                     testTemplate2Definition.getChildren().size());

        assertEquals("Test",
                     testTemplate2Definition.getName());

        VirtualFolderDefinition node2 = testTemplate2Definition.findChildByName("Node2");

        assertEquals("Node2",
                     node2.getName());
        assertEquals("The2ndNode",
                     node2.getDescription());
        assertEquals("2",
                     node2.getId());

        VirtualQuery node2Query = node2.getQuery();
        assertNotNull(node2Query);

        Map<String, String> node2Properties = node2.getProperties();
        assertTrue(node2Properties.keySet().containsAll(Arrays.asList("cm:modifier",
                                                                      "cm:description",
                                                                      "sys:node-dbid")));
        
        assertEquals("admin",node2Properties.get("cm:modifier"));
        //there is no node so values with property placeholder remain as they are   
        assertEquals("AContextDescription",node2Properties.get("cm:description"));
        assertEquals("34567",node2Properties.get("sys:node-dbid"));
    }

    @Test
    public void testReadOnly() throws Exception
    {
        assertReadonly(testTemplate2Definition);
        assertReadonly(testTemplate2Definition.findChildByName("Node2"));
        assertReadonly(testTemplate2Definition.findChildByName("Node3"));
        assertReadonly(testTemplate2Definition.findChildByName("Node4"));
    }
}
