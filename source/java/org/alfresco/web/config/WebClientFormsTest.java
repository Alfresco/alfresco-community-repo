/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.xml.XMLConfigService;
import org.alfresco.util.BaseTest;
import org.alfresco.web.config.DefaultControlsConfigElement.ControlParam;
import org.alfresco.web.config.FormConfigElement.FormField;
import org.alfresco.web.config.FormConfigElement.FormSet;
import org.alfresco.web.config.FormConfigElement.Mode;

/**
 * JUnit tests to exercise the forms-related capabilities in to the web client
 * config service. These tests only include those that require a single config
 * xml file. Override-related tests, which use multiple config xml files, are
 * located in peer classes in this package.
 * 
 * @author Neil McErlean
 */
public class WebClientFormsTest extends BaseTest
{
    private static final String TEST_CONFIG_XML = "test-config-forms.xml";
    private XMLConfigService configService;
    private Config globalConfig;
    private ConfigElement globalDefaultControls;
    private ConfigElement globalConstraintHandlers;
    private FormConfigElement formConfigElement;
    private DefaultControlsConfigElement defltCtrlsConfElement;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        configService = initXMLConfigService(TEST_CONFIG_XML);
        assertNotNull("configService was null.", configService);

        Config contentConfig = configService.getConfig("content");
        assertNotNull("contentConfig was null.", contentConfig);

        ConfigElement confElement = contentConfig.getConfigElement("form");
        assertNotNull("confElement was null.", confElement);
        assertTrue("confElement should be instanceof FormConfigElement.",
                confElement instanceof FormConfigElement);
        formConfigElement = (FormConfigElement) confElement;

        globalConfig = configService.getGlobalConfig();

        globalDefaultControls = globalConfig
                .getConfigElement("default-controls");
        assertNotNull("global default-controls element should not be null",
                globalDefaultControls);
        assertTrue(
                "config element should be an instance of DefaultControlsConfigElement",
                (globalDefaultControls instanceof DefaultControlsConfigElement));
        defltCtrlsConfElement = (DefaultControlsConfigElement) globalDefaultControls;

        globalConstraintHandlers = globalConfig
                .getConfigElement("constraint-handlers");
        assertNotNull("global constraint-handlers element should not be null",
                globalConstraintHandlers);
        assertTrue(
                "config element should be an instance of ConstraintHandlersConfigElement",
                (globalConstraintHandlers instanceof ConstraintHandlersConfigElement));
    }

    public void testDefaultControlsMappingNameToTemplate()
    {
        // Test that the default-control types are read from the config file
        Map<String, String> expectedDataMappings = new HashMap<String, String>();
        expectedDataMappings.put("d:text",
                "org/alfresco/forms/controls/textfield.ftl");
        expectedDataMappings.put("d:boolean",
                "org/alfresco/forms/controls/checkbox.ftl");
        expectedDataMappings.put("association",
                "org/alfresco/forms/controls/association-picker.ftl");
        expectedDataMappings.put("abc", "org/alfresco/abc.ftl");

        Set<String> actualNames = defltCtrlsConfElement.getNames();
        assertEquals("Incorrect name count, expected "
                + expectedDataMappings.size(), expectedDataMappings.size(),
                actualNames.size());

        // Ugly hack to get around JUnit 3.8.1 not having
        // assertEquals(Collection, Collection)
        for (String nextName : expectedDataMappings.keySet())
        {
            assertTrue("actualNames was missing " + nextName, actualNames
                    .contains(nextName));
        }
        for (String nextName : actualNames)
        {
            assertTrue("expectedDataMappings was missing " + nextName,
                    expectedDataMappings.keySet().contains(nextName));
        }

        // Test that the datatypes map to the expected template.
        for (String nextKey : expectedDataMappings.keySet())
        {
            String nextExpectedValue = expectedDataMappings.get(nextKey);
            String nextActualValue = defltCtrlsConfElement.getTemplateFor(nextKey);
            assertTrue("Incorrect template for " + nextKey + ": "
                    + nextActualValue, nextExpectedValue
                    .equals(nextActualValue));
        }
    }

    @SuppressWarnings("unchecked")
    public void testReadControlParamsFromConfigXml()
    {
        Map<String, List<ControlParam>> expectedControlParams = new HashMap<String, List<ControlParam>>();

        List<ControlParam> textParams = new ArrayList<ControlParam>();
        textParams.add(new ControlParam("size", "50"));

        List<ControlParam> abcParams = new ArrayList<ControlParam>();
        abcParams.add(new ControlParam("a", "1"));
        abcParams.add(new ControlParam("b", "Hello"));
        abcParams.add(new ControlParam("c", "For ever and ever."));
        abcParams.add(new ControlParam("d", ""));

        expectedControlParams.put("d:text", textParams);
        expectedControlParams.put("d:boolean", Collections.EMPTY_LIST);
        expectedControlParams.put("association", Collections.EMPTY_LIST);
        expectedControlParams.put("abc", abcParams);

        for (String name : expectedControlParams.keySet())
        {
            List<ControlParam> actualControlParams = defltCtrlsConfElement
                    .getControlParamsFor(name);
            assertEquals("Incorrect params for " + name, expectedControlParams
                    .get(name), actualControlParams);
        }
    }

    public void testDefaultControlsConfigElementShouldHaveNoChildren()
    {
        try
        {
            defltCtrlsConfElement.getChildren();
            fail("getChildren() did not throw an exception");
        } catch (ConfigException ce)
        {
            // expected
        }
    }

    /**
     * Tests the combination of a DefaultControlsConfigElement with another that
     * contains additional data.
     */
    public void testCombineDefaultControlsWithAddedParam()
    {
        DefaultControlsConfigElement basicElement = new DefaultControlsConfigElement();
        basicElement.addDataMapping("text", "path/textbox.ftl", null);

        // This element is the same as the above, but adds a control-param.
        DefaultControlsConfigElement parameterisedElement = new DefaultControlsConfigElement();
        List<ControlParam> testParams = new ArrayList<ControlParam>();
        testParams.add(new ControlParam("A", "1"));
        parameterisedElement.addDataMapping("text", "path/textbox.ftl",
                testParams);

        ConfigElement combinedElem = basicElement.combine(parameterisedElement);
        assertEquals("Combined elem incorrect.", parameterisedElement,
                combinedElem);
    }

    /**
     * Tests the combination of a DefaultControlsConfigElement with another that
     * contains modified data.
     */
    public void testCombineDefaultControlsWithModifiedParam()
    {
        DefaultControlsConfigElement initialElement = new DefaultControlsConfigElement();
        List<ControlParam> testParams = new ArrayList<ControlParam>();
        testParams.add(new ControlParam("A", "1"));
        initialElement.addDataMapping("text", "path/textbox.ftl", testParams);

        // This element is the same as the above, but modifies the
        // control-param.
        DefaultControlsConfigElement modifiedElement = new DefaultControlsConfigElement();
        List<ControlParam> modifiedTestParams = new ArrayList<ControlParam>();
        modifiedTestParams.add(new ControlParam("A", "5"));
        modifiedElement.addDataMapping("text", "path/textbox.ftl",
                modifiedTestParams);

        ConfigElement combinedElem = initialElement.combine(modifiedElement);
        assertEquals("Combined elem incorrect.", modifiedElement, combinedElem);
    }

    /**
     * Tests the combination of a DefaultControlsConfigElement with another that
     * contains deleted data. TODO Do we actually need to support this type of
     * customisation?
     */
    public void testCombineDefaultControlsWithDeletedParam()
    {
        DefaultControlsConfigElement initialElement = new DefaultControlsConfigElement();
        List<ControlParam> testParams = new ArrayList<ControlParam>();
        testParams.add(new ControlParam("A", "1"));
        initialElement.addDataMapping("text", "path/textbox.ftl", testParams);

        // This element is the same as the above, but deletes the
        // control-param.
        DefaultControlsConfigElement modifiedElement = new DefaultControlsConfigElement();
        modifiedElement.addDataMapping("text", "path/textbox.ftl", null);

        ConfigElement combinedElem = initialElement.combine(modifiedElement);
        assertEquals("Combined elem incorrect.", modifiedElement, combinedElem);
    }

    public void testReadConstraintHandlersFromConfigXml()
    {
        // Test that the constraint-handlers' constraints are read from the
        // config file
        Map<String, String> expectedValidationHandlers = new HashMap<String, String>();
        expectedValidationHandlers.put("REGEX",
                "Alfresco.forms.validation.regexMatch");
        expectedValidationHandlers.put("NUMERIC",
                "Alfresco.forms.validation.numericMatch");

        ConstraintHandlersConfigElement chConfigElement = (ConstraintHandlersConfigElement) globalConstraintHandlers;
        List<String> actualTypes = chConfigElement.getConstraintTypes();
        assertEquals("Incorrect type count.",
                expectedValidationHandlers.size(), actualTypes.size());

        // Ugly hack to get around JUnit 3.8.1 not having
        // assertEquals(Collection, Collection)
        for (String nextType : expectedValidationHandlers.keySet())
        {
            assertTrue("actualTypes was missing " + nextType, actualTypes
                    .contains(nextType));
        }
        for (String nextType : actualTypes)
        {
            assertTrue("expectedValidationHandlers missing " + nextType,
                    expectedValidationHandlers.keySet().contains(nextType));
        }

        // Test that the types map to the expected validation handler.
        for (String nextKey : expectedValidationHandlers.keySet())
        {
            String nextExpectedValue = expectedValidationHandlers.get(nextKey);
            String nextActualValue = chConfigElement
                    .getValidationHandlerFor(nextKey);
            assertTrue("Incorrect handler for " + nextKey + ": "
                    + nextActualValue, nextExpectedValue
                    .equals(nextActualValue));
        }

        // Test that the constraint-handlers' messages are read from the config
        // file
        Map<String, String> expectedMessages = new HashMap<String, String>();
        expectedMessages.put("REGEX", null);
        expectedMessages.put("NUMERIC", "Test Message");

        // Test that the types map to the expected message.
        for (String nextKey : expectedValidationHandlers.keySet())
        {
            String nextExpectedValue = expectedMessages.get(nextKey);
            String nextActualValue = chConfigElement.getMessageFor(nextKey);
            assertEquals("Incorrect message for " + nextKey + ".",
                    nextExpectedValue, nextActualValue);
        }

        // Test that the constraint-handlers' message-ids are read from the
        // config file
        Map<String, String> expectedMessageIDs = new HashMap<String, String>();
        expectedMessageIDs.put("REGEX", null);
        expectedMessageIDs.put("NUMERIC", "regex_error");

        // Test that the types map to the expected message-id.
        for (String nextKey : expectedValidationHandlers.keySet())
        {
            String nextExpectedValue = expectedMessageIDs.get(nextKey);
            String nextActualValue = chConfigElement.getMessageIdFor(nextKey);
            assertEquals("Incorrect message-id for " + nextKey + ".",
                    nextExpectedValue, nextActualValue);
        }
    }

    public void testConstraintHandlerElementShouldHaveNoChildren()
    {
        try
        {
            ConstraintHandlersConfigElement chConfigElement = (ConstraintHandlersConfigElement) globalConstraintHandlers;
            chConfigElement.getChildren();
            fail("getChildren() did not throw an exception");
        } catch (ConfigException ce)
        {
            // expected
        }

    }

    public void testFormSubmissionUrlAndModelOverrideProps()
    {
        assertEquals("Submission URL was incorrect.", "submission/url",
                formConfigElement.getSubmissionURL());

        List<StringPair> expectedModelOverrideProperties = new ArrayList<StringPair>();
        expectedModelOverrideProperties.add(new StringPair(
                "fields.title.mandatory", "true"));
        assertEquals("Expected property missing.",
                expectedModelOverrideProperties, formConfigElement
                        .getModelOverrideProperties());
    }
    
    @SuppressWarnings("unchecked")
    public void testGetFormTemplatesForModesAndRoles()
    {
        // Get the form templates. Testing the mode and role combinations.
        // For this config xml, there are no templates available to a user
        // without a role.
        assertNull("Incorrect template.", formConfigElement.getFormTemplate(
                Mode.CREATE, null));
        assertNull("Incorrect template.", formConfigElement.getFormTemplate(
                Mode.EDIT, null));
        assertNull("Incorrect template.", formConfigElement.getFormTemplate(
                Mode.VIEW, null));
        assertNull("Incorrect template.", formConfigElement.getFormTemplate(
                Mode.CREATE, Collections.EMPTY_LIST));
        assertNull("Incorrect template.", formConfigElement.getFormTemplate(
                Mode.EDIT, Collections.EMPTY_LIST));
        assertNull("Incorrect template.", formConfigElement.getFormTemplate(
                Mode.VIEW, Collections.EMPTY_LIST));

        List<String> roles = new ArrayList<String>();
        roles.add("Consumer");
        roles.add("Manager");
        assertEquals("Incorrect template.", "/path/create/template",
                formConfigElement.getFormTemplate(Mode.CREATE, roles));
        assertEquals("Incorrect template.", "/path/edit/template/manager",
                formConfigElement.getFormTemplate(Mode.EDIT, roles));
        assertEquals("Incorrect template.", "/path/view/template",
                formConfigElement.getFormTemplate(Mode.VIEW, roles));
    }

    public void testGetFormFieldVisibilitiesForModes()
    {
        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("name", Mode.CREATE));
        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("title", Mode.CREATE));
        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("quota", Mode.CREATE));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("rubbish", Mode.CREATE));

        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("name", Mode.EDIT));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("title", Mode.EDIT));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("quota", Mode.EDIT));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("rubbish", Mode.EDIT));

        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("name", Mode.VIEW));
        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("title", Mode.VIEW));
        assertTrue("Field should be visible.", formConfigElement
                .isFieldVisible("quota", Mode.VIEW));
        assertFalse("Field should be invisible.", formConfigElement
                .isFieldVisible("rubbish", Mode.VIEW));
    }

    public void testGetSetsFromForm()
    {
        List<String> expectedSetIds = new ArrayList<String>();
        expectedSetIds.add("details");
        expectedSetIds.add("user");
        assertEquals("Set IDs were wrong.", expectedSetIds, formConfigElement
                .getSetIDs());

        Map<String, FormSet> sets = formConfigElement.getSets();
        assertEquals("Set parent was wrong.", "details", sets.get("user")
                .getParentId());
        assertEquals("Set parent was wrong.", null, sets.get("details")
                .getParentId());

        assertEquals("Set parent was wrong.", "fieldset", sets.get("details")
                .getAppearance());
        assertEquals("Set parent was wrong.", "panel", sets.get("user")
                .getAppearance());
    }
    
    public void testAccessAllFieldRelatedData()
    {
        // Field checks
        Map<String, FormField> fields = formConfigElement.getFields();
        assertEquals("Wrong number of Fields.", 4, fields.size());

        FormField usernameField = fields.get("username");
        assertNotNull("usernameField was null.", usernameField);
        assertTrue("Missing attribute.", usernameField.getAttributes()
                .containsKey("set"));
        assertEquals("Incorrect attribute.", "user", usernameField
                .getAttributes().get("set"));
        assertNull("username field's template should be null.", usernameField
                .getTemplate());

        FormField nameField = fields.get("name");
        String nameTemplate = nameField.getTemplate();
        assertNotNull("name field had null template", nameTemplate);
        assertEquals("name field had incorrect template.",
                "alfresco/extension/formcontrols/my-name.ftl", nameTemplate);

        List<StringPair> controlParams = nameField.getControlParams();
        assertNotNull("name field should have control params.", controlParams);
        assertEquals("name field has incorrect number of control params.", 1,
                controlParams.size());

        assertEquals("Control param has wrong name.", "foo", controlParams.get(
                0).getName());
        assertEquals("Control param has wrong value.", "bar", controlParams
                .get(0).getValue());

        assertEquals("name field had incorrect type.", "REGEX", nameField
                .getConstraintType());
        assertEquals("name field had incorrect message.",
                "The name can not contain the character '{0}'", nameField
                        .getConstraintMessage());
        assertEquals("name field had incorrect message-id.",
                "field_error_name", nameField.getConstraintMessageId());
    }

    public void testFormConfigElementShouldHaveNoChildren()
    {
        try
        {
            formConfigElement.getChildren();
            fail("getChildren() did not throw an exception.");
        } catch (ConfigException expectedException)
        {
            // intentionally empty
        }
    }
}
