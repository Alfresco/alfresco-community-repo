/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

public class TestActionPropertySubs extends RMActionExecuterAbstractBase
{
    public static final String NAME = "testActionPropertySubs";
        
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl("shortMonth", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("longMonth", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("year", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("name", DataTypeDefinition.TEXT, false, ""));   
        paramList.add(new ParameterDefinitionImpl("company", DataTypeDefinition.TEXT, false, ""));   
        paramList.add(new ParameterDefinitionImpl("combo", DataTypeDefinition.TEXT, false, ""));
    }
    
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        Calendar myToday = Calendar.getInstance();
        
        String shortMonth = myToday.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        String longMonth = myToday.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        String year = Integer.toString(myToday.get(Calendar.YEAR));
        String name = (String)nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);
        String company = I18NUtil.getMessage("test.company");
        
        assertEquals(shortMonth, (String)action.getParameterValue("shortMonth"));
        assertEquals(longMonth, (String)action.getParameterValue("longMonth"));
        assertEquals(year, (String)action.getParameterValue("year"));
        assertEquals(name, (String)action.getParameterValue("name"));
        assertEquals(company, (String)action.getParameterValue("company"));
        assertEquals(year + "/" + shortMonth + "/" + name + "-" + company +".txt", (String)action.getParameterValue("combo"));
    }   
    
    private void assertEquals(String expected, String actual)
    {
        if (expected.equals(actual) == false)
        {
            throw new AlfrescoRuntimeException("Expected value " + expected + " does not match actual value " + actual);
        }
    }
}
