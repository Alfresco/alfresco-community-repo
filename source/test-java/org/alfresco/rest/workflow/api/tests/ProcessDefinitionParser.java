/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.tests;

import org.alfresco.rest.workflow.api.model.ProcessDefinition;
import org.json.simple.JSONObject;

public class ProcessDefinitionParser extends ListParser<ProcessDefinition>
{
    public static ProcessDefinitionParser INSTANCE = new ProcessDefinitionParser();

    @Override
    public ProcessDefinition parseEntry(JSONObject entry)
    {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setId((String) entry.get("id"));
        processDefinition.setKey((String) entry.get("key"));
        processDefinition.setVersion(((Number) entry.get("version")).intValue());
        processDefinition.setName((String) entry.get("name"));
        processDefinition.setDeploymentId((String) entry.get("deploymentId"));
        processDefinition.setTitle((String) entry.get("title"));
        processDefinition.setDescription((String) entry.get("description"));
        processDefinition.setCategory((String) entry.get("category"));
        processDefinition.setStartFormResourceKey((String) entry.get("startFormResourceKey"));
        processDefinition.setGraphicNotationDefined((Boolean) entry.get("graphicNotationDefined"));
        return processDefinition;
    }
}
