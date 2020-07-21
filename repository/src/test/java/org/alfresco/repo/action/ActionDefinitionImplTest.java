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
package org.alfresco.repo.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * @author Roy Wetherall
 */
public class ActionDefinitionImplTest extends BaseParameterizedItemDefinitionImplTest
{
    private static final String RULE_ACTION_EXECUTOR = "ruleActionExector";
    
    protected ParameterizedItemDefinitionImpl create()
    {    
        // Test duplicate param name
        Map<Locale, List<ParameterDefinition>> localizedParams = new HashMap<Locale, List<ParameterDefinition>>();
        try
        {
            ActionDefinitionImpl temp = new ActionDefinitionImpl(NAME);
            localizedParams.put(Locale.ROOT, duplicateParamDefs);
            temp.setLocalizedParameterDefinitions(localizedParams);
            fail("Duplicate param names are not allowed.");
        }
        catch (RuleServiceException exception)
        {
            // Indicates that there are duplicate param names
        }
        
        // Create a good one
        ActionDefinitionImpl temp = new ActionDefinitionImpl(NAME);
        assertNotNull(temp);
        //temp.setTitle(TITLE);
       // temp.setDescription(DESCRIPTION);
        localizedParams.put(Locale.ROOT, paramDefs);
        temp.setLocalizedParameterDefinitions(localizedParams);
        temp.setRuleActionExecutor(RULE_ACTION_EXECUTOR);
        return temp;
    }
    
    /**
     * Test getRuleActionExecutor
     */
    public void testGetRuleActionExecutor()
    {
        ActionDefinitionImpl temp = (ActionDefinitionImpl)create();
        assertEquals(RULE_ACTION_EXECUTOR, temp.getRuleActionExecutor());
    }

    /**
     * REPO-2253: Community: ALF-21854 Action parameter lookup for "de_DE" falls back to "root" locale instead of "de"
     */
    public void testParameterDefinitionLocaleFallback()
    {
        Locale originalLocale = I18NUtil.getLocale();
        try
        {
            ActionDefinitionImpl actionDef = new ActionDefinitionImpl(NAME);
            Map<Locale, List<ParameterDefinition>> localizedParams = new HashMap<>();
            
            
            localizedParams.put(Locale.ROOT, exampleFieldList("English Label"));
            localizedParams.put(Locale.ENGLISH, exampleFieldList("English Label"));
            localizedParams.put(Locale.UK, exampleFieldList("UK-specific Label"));
            localizedParams.put(Locale.GERMAN, exampleFieldList("German Label"));
            actionDef.setLocalizedParameterDefinitions(localizedParams);

            I18NUtil.setLocale(null);
            assertEquals("English Label", actionDef.getParameterDefintion("example-field").getDisplayLabel());

            I18NUtil.setLocale(Locale.ENGLISH);
            assertEquals("English Label", actionDef.getParameterDefintion("example-field").getDisplayLabel());

            // en-GB does not need to fallback to en
            I18NUtil.setLocale(Locale.UK);
            assertEquals("UK-specific Label", actionDef.getParameterDefintion("example-field").getDisplayLabel());

            I18NUtil.setLocale(Locale.GERMAN);
            assertEquals("German Label", actionDef.getParameterDefintion("example-field").getDisplayLabel());
            
            I18NUtil.setLocale(Locale.GERMANY);
            // de-DE falls back to de
            assertEquals("German Label", actionDef.getParameterDefintion("example-field").getDisplayLabel());
        }
        finally
        {
            I18NUtil.setLocale(originalLocale);
        }
    }
    
    private List<ParameterDefinition> exampleFieldList(String label)
    {
        List<ParameterDefinition> paramDefs = new ArrayList<>();
        paramDefs.add(new ParameterDefinitionImpl("example-field", DataTypeDefinition.TEXT, false, label));
        return paramDefs;
    }
}
