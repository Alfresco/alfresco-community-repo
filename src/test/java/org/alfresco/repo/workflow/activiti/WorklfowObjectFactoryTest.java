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
package org.alfresco.repo.workflow.activiti;

import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.workflow.WorkflowObjectFactory;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import junit.framework.TestCase;

public class WorklfowObjectFactoryTest extends TestCase
{

    /**
     * Test to validate ALF-18332 (default description label key).
     */
    public void testTransitionDefaultLabel()
    {
        MessageService mockedMessageService = Mockito.mock(MessageService.class);
        Mockito.when(mockedMessageService.getMessage(Mockito.anyString())).thenAnswer(new Answer<String>()
        {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                String arg = (String) invocation.getArguments()[0];
                if ("base.key.title".equals(arg))
                {
                    return "The title";
                }
                else if ("base.key.description".equals(arg))
                {
                    return "The description";
                }
                return null;
            }
        });

        WorkflowObjectFactory factory = new WorkflowObjectFactory(null, null, mockedMessageService, null, ActivitiConstants.ENGINE_ID, null);
        WorkflowTransition createTransition = factory.createTransition("test-transition", "title", null, true, "base.key");
        assertNotNull(createTransition);
        assertEquals("The title", createTransition.getTitle());
        assertEquals("The description", createTransition.getDescription());
    }
}
