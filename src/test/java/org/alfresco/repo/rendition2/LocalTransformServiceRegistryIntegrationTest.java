/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.transform.client.registry.TransformServiceRegistry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_PDF;

/**
 * Integration tests for {@link LocalTransformServiceRegistry}
 */
public class LocalTransformServiceRegistryIntegrationTest extends AbstractRenditionIntegrationTest
{
    private static final String RENDITION_NAME = "pdf";

    @Autowired
    private LocalTransformServiceRegistry localTransformServiceRegistry;

    protected TransformServiceRegistry transformServiceRegistry;

    private Map<String, String> options;

    @BeforeClass
    public static void before()
    {
        AbstractRenditionIntegrationTest.before();
        local();
    }

    @AfterClass
    public static void after()
    {
        AbstractRenditionIntegrationTest.after();
    }

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        transformServiceRegistry = localTransformServiceRegistry;

        RenditionDefinition2 definition2 = renditionDefinitionRegistry2.getRenditionDefinition(RENDITION_NAME);
        options = definition2.getTransformOptions();
    }

    protected void setEnabled(boolean enabled) throws Exception
    {
        localTransformServiceRegistry.setEnabled(enabled);
        localTransformServiceRegistry.afterPropertiesSet();
    }

    protected boolean isEnabled()
    {
        return localTransformServiceRegistry.isEnabled();
    }

    @Test
    public void testIsSupported()
    {
        // +ve
        // No props
        Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, RENDITION_NAME));

        // -ve
        // Bad Source
        Assert.assertFalse(transformServiceRegistry.isSupported("docxBad", 1234, MIMETYPE_PDF, options, RENDITION_NAME));
        // Bad Target
        Assert.assertFalse(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, "pdfBad", options, "pdfBad"));

        // Good MaxSize docx max size is 768K
        Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 768L*1024, MIMETYPE_PDF, options, RENDITION_NAME));

        // -ve
        // Bad MaxSize docx max size is 768K
        Assert.assertFalse(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 768L*1024+1, MIMETYPE_PDF, options, RENDITION_NAME));
    }

    @Test
    public void testNoOptions()
    {
        // The options for "pdf" are empty once "timeout" has been removed. As a result the converter just creates a
        // basic TransformationOption object for a custom transformer and rendition without any options.
        Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, "custom"));
    }

    @Test
    public void testBadOptions()
    {
        // Source, Target and Props are in dictionary.properties
        Map<String, String> options = new HashMap<>();
        options.put("timeout", "true");
        options.put("unknown", "optionValue");
        Assert.assertFalse(transformServiceRegistry.isSupported("docxBad", 1234, MIMETYPE_PDF, options, ""));
    }

    @Test
    public void testEnabledDisabled() throws Exception
    {
        boolean origEnabled = isEnabled(); // should be true
        try
        {
            Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, RENDITION_NAME));

            setEnabled(false);
            Assert.assertFalse(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, RENDITION_NAME));

            setEnabled(true);
            Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, RENDITION_NAME));
        }
        finally
        {
            setEnabled(origEnabled);
        }
    }
}
