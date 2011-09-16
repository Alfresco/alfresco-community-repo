/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.workflow.activiti;

import org.alfresco.repo.workflow.AbstractMultitenantWorkflowTest;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ActivitiMultitenantWorkflowTest extends AbstractMultitenantWorkflowTest
{
    @Override
    protected String getEngine()
    {
        return ActivitiConstants.ENGINE_ID;
    }

    @Override
    protected String getTestDefinitionPath()
    {
        return "activiti/testTransaction.bpmn20.xml";
    }
    
    @Override
    protected String getTestDefinitionKey()
    {
        return "activiti$testTask";
    }

    @Override
    protected String getAdhocDefinitionKey()
    {
        return "activiti$activitiAdhoc";
    }
   
}
