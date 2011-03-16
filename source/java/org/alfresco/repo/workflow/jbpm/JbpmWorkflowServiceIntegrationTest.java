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
package org.alfresco.repo.workflow.jbpm;

import org.alfresco.repo.workflow.AbstractWorkflowServiceIntegrationTest;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * JBPM Workflow Service Implementation Tests
 * 
 * @author Nick Smith
 * @since 3.4.e
 */
public class JbpmWorkflowServiceIntegrationTest extends AbstractWorkflowServiceIntegrationTest
{

    @Override
    protected String getEngine()
    {
        return "jbpm";
    }

    @Override
    protected String getTestDefinitionPath()
    {
        return "jbpmresources/test_simple_processdefinition.xml";
    }

    @Override
    protected String getAdhocDefinitionPath()
    {
        return "alfresco/workflow/adhoc_processdefinition.xml";
    }
    
    @Override
    protected String getPooledReviewDefinitionPath()
    {
        return "alfresco/workflow/review_pooled_processdefinition.xml";
    }

    @Override
    protected String getParallelReviewDefinitionPath()
    {
        return "alfresco/workflow/parallelreview_processdefinition.xml";
    }
    
	@Override
	protected String getTestTimerDefinitionPath() 
	{
		return "jbpmresources/test_timer.xml";
	}

	@Override
	protected QName getAdhocProcessName() 
	{
		return QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "adhoc");
	}
}
