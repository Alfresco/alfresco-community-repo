/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.util;

import static org.testng.AssertJUnit.fail;

/**
 * @author Ross Gale
 * @since 3.1
 */
public class ActionExecutorUtil
{
    /**
     * Method to wait and retry when using the actions api
     *
     * @param evaluator the action specific check for completion
     */
    public void checkActionExecution(ActionEvaluator evaluator)
    {
        int counter = 0;
        int waitInMilliSeconds = 7000;
        while (counter < 4)
        {
            synchronized (this)
            {
                try
                {
                    this.wait(waitInMilliSeconds);
                } catch (InterruptedException e)
                {
                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            }

            if (evaluator.evaluate())
            {
                break;
            } else
            {
                counter++;
            }
        }
        if (counter == 4)
        {
            fail(evaluator.getErrorMessage());
        }
    }
}
