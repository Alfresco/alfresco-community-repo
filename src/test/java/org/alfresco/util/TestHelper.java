/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.Supplier;

/**
 * A helper class to create a concise test.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.2.1
 */
public class TestHelper
{
    /**
     * Checks the thrown exception is the expected exception.
     *
     * @param action            the functional interface
     * @param expectedException the expected exception class
     * @param failMessage       the fail message
     */
    public static void assertThrows(final Runnable action, final Class<?> expectedException, final String failMessage)
    {
        try
        {
            action.run();
        }
        catch (Throwable ex)
        {
            assertTrue("The caught exception [" + ex.getClass().getSimpleName() + "] is not the expected exception:" + expectedException
                        .getSimpleName(), expectedException.isInstance(ex));
            return;
        }

        fail(failMessage + " So failed to throw expected exception: " + expectedException.getSimpleName());
    }

    /**
     * Waits for <b>{@code waitTimeInMillis}</b> before executing the given functional interface <b>({@code supplier})</b>.
     * <p>
     * If the returned result is not equal to the required result <b>{@code requiredResult}</b>,
     * it waits and re-executes the given functional interface again.
     * This will continue until the results are equal or the <b>{@code maxRetry}</b> has been reached.
     *
     * @param supplier         the functional interface
     * @param requiredResult   the required result so it can be checked against the returned result of the ({@code supplier})
     * @param maxRetry         the number of retries
     * @param waitTimeInMillis the pause time
     * @return result of the ({@code supplier})
     * @throws InterruptedException
     */
    public static <T> T waitBeforeRetry(final Supplier<T> supplier, final T requiredResult, int maxRetry, long waitTimeInMillis)
                throws InterruptedException
    {
        T t;
        int retryCount = 0;
        do
        {
            Thread.sleep(waitTimeInMillis);
            t = supplier.get();
            if (requiredResult.equals(t))
            {
                return t;
            }
            retryCount++;
        } while (retryCount < maxRetry);

        return t;
    }
}
