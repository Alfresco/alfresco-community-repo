
package org.alfresco.repo.action.executer;

/**
 * Allows a class to be set in 'TestMode'. Used e.g. to turn off the
 * {@link MailActionExecuter} so it does not send unnecessary emails during tests.
 * 
 * @author Nick Smith
 */
public interface TestModeable
{
    boolean isTestMode();

    void setTestMode(boolean testMode);

}
