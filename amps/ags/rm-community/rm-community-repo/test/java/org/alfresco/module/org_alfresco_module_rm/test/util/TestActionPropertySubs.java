/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
        paramList.add(new ParameterDefinitionImpl("dayShort", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("dayShort2", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("dayLong", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("dayNumber", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("dayYear", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("monthShort", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("monthShort2", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("monthLong", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("monthNumber", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("yearShort", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("yearShort2", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("yearLong", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("yearWeek", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("name", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("company", DataTypeDefinition.TEXT, false, ""));
        paramList.add(new ParameterDefinitionImpl("combo", DataTypeDefinition.TEXT, false, ""));
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        Date date = new Date();
        String dayShort = new SimpleDateFormat("EE").format(date);
        String dayLong = new SimpleDateFormat("EEEE").format(date);
        String dayNumber = new SimpleDateFormat("uu").format(date);
        String dayYear = new SimpleDateFormat("DDD").format(date);
        String monthShort = new SimpleDateFormat("MMM").format(date);
        String monthLong = new SimpleDateFormat("MMMM").format(date);
        String monthNumber = new SimpleDateFormat("MM").format(date);
        String yearShort = new SimpleDateFormat("yy").format(date);
        String yearLong = new SimpleDateFormat("yyyy").format(date);
        String yearWeek = new SimpleDateFormat("ww").format(date);
        String name = (String) getNodeService().getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);
        String company = I18NUtil.getMessage("test.company");

        assertEquals(dayShort, (String) action.getParameterValue("dayShort"));
        assertEquals(dayShort, (String) action.getParameterValue("dayShort2"));
        assertEquals(dayLong, (String) action.getParameterValue("dayLong"));
        assertEquals(dayNumber, (String) action.getParameterValue("dayNumber"));
        assertEquals(dayYear, (String) action.getParameterValue("dayYear"));
        assertEquals(monthShort, (String) action.getParameterValue("monthShort"));
        assertEquals(monthShort, (String) action.getParameterValue("monthShort2"));
        assertEquals(monthLong, (String) action.getParameterValue("monthLong"));
        assertEquals(monthNumber, (String) action.getParameterValue("monthNumber"));
        assertEquals(yearShort, (String) action.getParameterValue("yearShort"));
        assertEquals(yearShort, (String) action.getParameterValue("yearShort2"));
        assertEquals(yearLong, (String) action.getParameterValue("yearLong"));
        assertEquals(yearWeek, (String) action.getParameterValue("yearWeek"));
        assertEquals(name, (String)action.getParameterValue("name"));
        assertEquals(company, (String)action.getParameterValue("company"));
        assertEquals(yearLong + "/" + monthShort + "/" + name + "-" + company +".txt", (String) action.getParameterValue("combo"));
    }

    private void assertEquals(String expected, String actual)
    {
        if (!expected.equals(actual))
        {
            throw new AlfrescoRuntimeException("Expected value " + expected + " does not match actual value " + actual);
        }
    }
}
