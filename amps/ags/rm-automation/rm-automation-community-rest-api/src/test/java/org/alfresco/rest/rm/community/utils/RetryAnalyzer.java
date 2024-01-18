/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer
{
    private int retryCount;
    private int maxRetryCount = 3;

    public String getResultStatusName(int status)
    {
        String resultName = null;
        if (status == 1)
        {
            resultName = "SUCCESS";
        }
        if (status == 2)
        {
            resultName = "FAILURE";
        }
        return resultName;
    }

    @Override
    public boolean retry(ITestResult result)
    {
        if (!result.isSuccess())
        {
            if (retryCount < maxRetryCount)
            {
                result.setStatus(ITestResult.SKIP);
                retryCount++;
                return true;
            }
            else
            {
                result.setStatus(ITestResult.FAILURE);
            }
        }
        else
        {
            result.setStatus(ITestResult.SUCCESS);
        }
        return false;
    }
}