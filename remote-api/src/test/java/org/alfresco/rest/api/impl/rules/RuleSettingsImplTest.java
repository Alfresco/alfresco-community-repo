/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.impl.rules;

import static java.util.Collections.emptyMap;

import static org.alfresco.repo.rule.RuleModel.ASPECT_IGNORE_INHERITED_RULES;
import static org.alfresco.rest.api.model.rules.RuleSetting.IS_INHERITANCE_ENABLED_KEY;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import junit.framework.TestCase;
import org.alfresco.rest.api.model.rules.RuleSetting;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link RuleSettingsImpl}. */
@Experimental
@RunWith (MockitoJUnitRunner.class)
public class RuleSettingsImplTest extends TestCase
{
    private static final String FOLDER_ID = "dummy-folder-id";
    private static final NodeRef FOLDER_NODE = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, FOLDER_ID);

    @InjectMocks
    private RuleSettingsImpl ruleSettings;
    @Mock
    private NodeValidator nodeValidatorMock;
    @Mock
    private NodeService nodeServiceMock;

    @Before
    @Override
    public void setUp()
    {
        given(nodeValidatorMock.validateFolderNode(eq(FOLDER_ID), anyBoolean())).willReturn(FOLDER_NODE);
    }

    @Test
    public void testGetRuleSetting_disabled()
    {
        given(nodeServiceMock.hasAspect(FOLDER_NODE, ASPECT_IGNORE_INHERITED_RULES)).willReturn(true);

        // Call the method under test.
        RuleSetting ruleSetting = ruleSettings.getRuleSetting(FOLDER_ID, IS_INHERITANCE_ENABLED_KEY);

        RuleSetting expected = RuleSetting.builder().key(IS_INHERITANCE_ENABLED_KEY).value(false).create();
        assertEquals(expected, ruleSetting);
    }

    @Test
    public void testGetRuleSetting_enabled()
    {
        given(nodeServiceMock.hasAspect(FOLDER_NODE, ASPECT_IGNORE_INHERITED_RULES)).willReturn(false);

        // Call the method under test.
        RuleSetting ruleSetting = ruleSettings.getRuleSetting(FOLDER_ID, IS_INHERITANCE_ENABLED_KEY);

        RuleSetting expected = RuleSetting.builder().key(IS_INHERITANCE_ENABLED_KEY).value(true).create();
        assertEquals(expected, ruleSetting);
    }

    @Test
    public void testGetRuleSetting_unrecognisedKey()
    {
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> ruleSettings.getRuleSetting(FOLDER_ID, "-fakeSetting-"));
    }

    @Test
    public void testSetRuleSetting_enable()
    {
        RuleSetting ruleSetting = RuleSetting.builder().key(IS_INHERITANCE_ENABLED_KEY).value(true).create();

        // Call the method under test.
        RuleSetting actual = ruleSettings.setRuleSetting(FOLDER_ID, ruleSetting);

        assertEquals(ruleSetting, actual);
        then(nodeServiceMock).should().removeAspect(FOLDER_NODE, ASPECT_IGNORE_INHERITED_RULES);
    }

    @Test
    public void testSetRuleSetting_disable()
    {
        RuleSetting ruleSetting = RuleSetting.builder().key(IS_INHERITANCE_ENABLED_KEY).value(false).create();

        // Call the method under test.
        RuleSetting actual = ruleSettings.setRuleSetting(FOLDER_ID, ruleSetting);

        assertEquals(ruleSetting, actual);
        then(nodeServiceMock).should().addAspect(FOLDER_NODE, ASPECT_IGNORE_INHERITED_RULES, emptyMap());
    }

    @Test
    public void testSetRuleSetting_unrecognisedKey()
    {
        RuleSetting ruleSetting = RuleSetting.builder().key("-fakeSetting-").value(true).create();
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> ruleSettings.setRuleSetting(FOLDER_ID, ruleSetting));
    }

    @Test
    public void testSetRuleSetting_nonBooleanValue()
    {
        RuleSetting ruleSetting = RuleSetting.builder().key(IS_INHERITANCE_ENABLED_KEY).value(123456).create();

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> ruleSettings.setRuleSetting(FOLDER_ID, ruleSetting));
    }
}
