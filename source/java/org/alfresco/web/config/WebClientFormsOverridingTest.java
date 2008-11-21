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

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.xml.XMLConfigService;
import org.alfresco.util.BaseTest;
import org.alfresco.web.config.DefaultControlsConfigElement.ControlParam;

/**
 * JUnit tests to exercise the forms-related capabilities in to the web client config
 * service. These only include those override-related tests that require multiple
 * config xml files.
 * 
 * @author Neil McErlean
 */
public class WebClientFormsOverridingTest extends BaseTest
{
   /**
    * @see junit.framework.TestCase#setUp()
    */
   protected void setUp() throws Exception
   {
      super.setUp();
   }

   public void testDefaultControlsOverride()
    {
        XMLConfigService svc = initXMLConfigService("test-config-forms.xml",
                "test-config-forms-override.xml");

        // get hold of the default-controls config from the global section
        Config globalConfig = svc.getGlobalConfig();
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
        XMLConfigService svc = initXMLConfigService("test-config-forms.xml",
                "test-config-forms-override.xml");

        // get hold of the constraint-handlers config from the global section
        Config globalConfig = svc.getGlobalConfig();
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
}
