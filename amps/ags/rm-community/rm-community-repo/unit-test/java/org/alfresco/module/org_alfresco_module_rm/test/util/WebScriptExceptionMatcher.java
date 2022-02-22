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

import org.junit.internal.matchers.TypeSafeMatcher;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Web script exception matcher.
 * <p>
 * Allows use to check whether the raised web script exception has the correct
 * status number or not.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
@SuppressWarnings("deprecation")
public class WebScriptExceptionMatcher extends TypeSafeMatcher<WebScriptException>
{
    /**
     * Helper method to create a matcher for the file not found (404)
     * exception status.
     * 
     * @return  {@link WebScriptExceptionMatcher}   
     */
    public static WebScriptExceptionMatcher fileNotFound()
    {
        return new WebScriptExceptionMatcher(Status.STATUS_NOT_FOUND);
    }
    
    /**
     * Helper method to create a matcher for the bad request status (400)
     * exception status.
     * 
     * @return  {@link WebScriptExceptionMatcher}
     */
    public static WebScriptExceptionMatcher badRequest()
    {
        return new WebScriptExceptionMatcher(Status.STATUS_BAD_REQUEST);
    }
    
    /** expected status */
    public int expectedStatus;

    /** actual status */
    public int actualStatus;

    /**
     * Constructor
     * 
     * @param expectedStatus    expected status
     */
    public WebScriptExceptionMatcher(int expectedStatus)
    {
        this.expectedStatus = expectedStatus;
    }
    
    /**
     * Determines if the expected outcome matches the actual 
     * outcome.
     * 
     * @return  true if matches, false otherwise
     */
    @Override
    public boolean matchesSafely(WebScriptException exception)
    {
        actualStatus = exception.getStatus();
        return (actualStatus == expectedStatus);
    }

    /**
     *  Describe unexpected outcome.
     */
    @Override
    public void describeTo(org.hamcrest.Description description)
    {
        description.appendValue(actualStatus)
                   .appendText(" was found instead of ")
                   .appendValue(expectedStatus);            
    }
}
