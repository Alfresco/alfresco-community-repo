/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.xml.XMLConfigService;
import org.alfresco.util.BaseTest;
import org.alfresco.web.config.DefaultControlsConfigElement.ControlParam;

/**
 * JUnit tests to exercise the forms-related capabilities in to the web client
 * config service. These only include those override-related tests that require
 * multiple config xml files.
 * 
 * @author Neil McErlean
 */
public class WebClientFormsOverridingTest extends BaseTest
{
    private XMLConfigService configService;
    private Config globalConfig;
    private FormConfigElement formConfigElement;
    private static final String FORMS_CONFIG = "test-config-forms.xml";
    private static final String FORMS_OVERRIDE_CONFIG = "test-config-forms-override.xml";

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        configService = initXMLConfigService(FORMS_CONFIG,
                FORMS_OVERRIDE_CONFIG);
        assertNotNull("configService was null.", configService);
        globalConfig = configService.getGlobalConfig();
        assertNotNull("Global config was null.", globalConfig);

        Config contentConfig = configService.getConfig("content");
        assertNotNull("contentConfig was null.", contentConfig);

        ConfigElement confElement = contentConfig.getConfigElement("form");
        assertNotNull("confElement was null.", confElement);
        assertTrue("confElement should be instanceof FormConfigElement.",
                confElement instanceof FormConfigElement);
        formConfigElement = (FormConfigElement) confElement;
    }

    public void testDefaultControlsOverride()
    {
        ConfigElement globalDefaultControls = globalConfig
                .getConfigElement("default-controls");
        assertNotNull("global default-controls element should not be null",
                globalDefaultControls);
        assertTrue(
                "config element should be an instance of DefaultControlsConfigElement",
                (globalDefaultControls instanceof DefaultControlsConfigElement));
        DefaultControlsConfigElement dcCE = (DefaultControlsConfigElement) globalDefaultControls;

        assertTrue("New template is missing.", dcCE.getNames().contains("xyz"));
        assertEquals("Expected template incorrect.", "org/alfresco/xyz.ftl",
                dcCE.getTemplateFor("xyz"));

        ControlParam expectedNewControlParam = new ControlParam("c", "Never.");
        assertTrue("New control-param missing.", dcCE
                .getControlParamsFor("abc").contains(expectedNewControlParam));
    }

    public void testConstraintHandlersOverride()
    {
        ConfigElement globalConstraintHandlers = globalConfig
                .getConfigElement("constraint-handlers");
        assertNotNull("global constraint-handlers element should not be null",
                globalConstraintHandlers);
        assertTrue(
                "config element should be an instance of ConstraintHandlersConfigElement",
                (globalConstraintHandlers instanceof ConstraintHandlersConfigElement));
        ConstraintHandlersConfigElement chCE = (ConstraintHandlersConfigElement) globalConstraintHandlers;

        assertTrue("New type is missing.", chCE.getConstraintTypes().contains(
                "RANGE"));
        assertEquals("Expected handler incorrect.",
                "Alfresco.forms.validation.rangeMatch", chCE
                        .getValidationHandlerFor("RANGE"));

        assertEquals("Modified message is wrong.", "Overridden Message", chCE
                .getMessageFor("NUMERIC"));
    }

    public void testFormSubmissionUrlAndModelOverridePropsOverride()
    {
        assertEquals("Submission URL was incorrect.", "overridden/submission/url",
                formConfigElement.getSubmissionURL());

        List<StringPair> expectedModelOverrideProperties = new ArrayList<StringPair>();
        expectedModelOverrideProperties.add(new StringPair(
                "fields.title.mandatory", "false"));
        assertEquals("Expected property missing.",
                expectedModelOverrideProperties, formConfigElement
                        .getModelOverrideProperties());
    }

}
