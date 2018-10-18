/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING;
import static org.alfresco.repo.content.MimetypeMap.MIMETYPE_PDF;

/**
 * Integration tests for {@link LegacyLocalTransformServiceRegistry}
 */
public class LegacyLocalTransformServiceRegistryTest extends AbstractRenditionIntegrationTest
{
    private static final String RENDITION_NAME = "pdf";

    @Autowired
    private LegacyLocalTransformServiceRegistry transformServiceRegistry;

    @Autowired
    private RenditionDefinitionRegistry2 renditionDefinitionRegistry2;

    private Map<String, String> options;

    @Before
    public void setUp() throws Exception
    {
        RenditionDefinition2 definition2 = renditionDefinitionRegistry2.getRenditionDefinition(RENDITION_NAME);
        options = definition2.getTransformOptions();
    }

    @Test
    public void testIsSupported()
    {
        // +ve
        // Source, Target and Props are in dictionary.properties
        Map<String, String> badValidationProps = new HashMap<>();
        badValidationProps.put("timeout", "true");
        // No props
        Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, RENDITION_NAME));

        // -ve
        // Bad Source
        Assert.assertFalse(transformServiceRegistry.isSupported("docxBad", 1234, MIMETYPE_PDF, options, ""));
        // Bad Target
        Assert.assertFalse(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, "pdfBad", options, RENDITION_NAME));

        // Good MaxSize docx max size is 768K
        Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 768L*1024, MIMETYPE_PDF, options, RENDITION_NAME));

        // -ve
        // Bad MaxSize docx max size is 768K
        Assert.assertFalse(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 768L*1024+1, MIMETYPE_PDF, options, RENDITION_NAME));
    }

    @Test
    public void testEnabledDisabled()
    {
        Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, RENDITION_NAME));
        try
        {
            transformServiceRegistry.setEnabled(false);
            Assert.assertFalse(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, RENDITION_NAME));
        }
        finally
        {
            transformServiceRegistry.setEnabled(true);
        }
        Assert.assertTrue(transformServiceRegistry.isSupported(MIMETYPE_OPENXML_WORDPROCESSING, 1234, MIMETYPE_PDF, options, RENDITION_NAME));
    }
}
