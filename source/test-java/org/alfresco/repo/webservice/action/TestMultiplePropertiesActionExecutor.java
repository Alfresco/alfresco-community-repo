/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.webservice.action;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Test action executer for {@link ActionWebServiceTest#testActionMultipleProperties()}
 * 
 * @author alex.mukha
 * @since 4.2.4
 */
public class TestMultiplePropertiesActionExecutor extends ActionExecuterAbstractBase
{
    private static String PARAM_PATH = "param-path";
    private static String PARAM_CONTENT = "param-content";

    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(org.alfresco.service.cmr.action.Action action, NodeRef actionedUponNodeRef)
    {
        if (((ArrayList<String>) action.getParameterValues().get(PARAM_PATH)).size() != 3)
        {
            throw new AlfrescoRuntimeException("There should be 3 parameters!");
        }
        if (((ArrayList<String>) action.getParameterValues().get(PARAM_CONTENT)).size() != 3)
        {
            throw new AlfrescoRuntimeException("There should be 3 parameters!");
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_PATH, DataTypeDefinition.TEXT, true, "Path of the file to create", true));
        paramList.add(new ParameterDefinitionImpl(PARAM_CONTENT, DataTypeDefinition.TEXT, true, "String representation of the content", true));
    }
}
