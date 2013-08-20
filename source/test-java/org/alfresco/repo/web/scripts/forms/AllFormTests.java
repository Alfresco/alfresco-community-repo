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
package org.alfresco.repo.web.scripts.forms;

import org.alfresco.repo.forms.FormService;
import org.alfresco.repo.forms.FormServiceImplTest;
import org.alfresco.repo.forms.processor.action.ActionFormProcessorTest;
import org.alfresco.repo.forms.processor.node.FieldProcessorTest;
import org.alfresco.repo.forms.processor.workflow.TaskFormProcessorTest;
import org.alfresco.repo.forms.processor.workflow.WorkflowFormProcessorTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class is a holder for the various test classes associated with the {@link FormService}.
 * It is not (at the time of writing) intended to be incorporated into the automatic build
 * which will find the various test classes and run them individually.
 * 
 * @author Neil McErlean
 * @since 4.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FormServiceImplTest.class,
        FieldProcessorTest.class,
        TaskFormProcessorTest.class,
        WorkflowFormProcessorTest.class,
        ActionFormProcessorTest.class,
        FormRestApiGet_Test.class,
        FormRestApiJsonPost_Test.class
})
public class AllFormTests
{
    // Intentionally empty
}
