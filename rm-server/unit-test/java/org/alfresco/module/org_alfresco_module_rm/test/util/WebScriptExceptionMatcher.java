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