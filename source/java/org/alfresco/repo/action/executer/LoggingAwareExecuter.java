package org.alfresco.repo.action.executer;

import org.apache.commons.logging.Log;

public interface LoggingAwareExecuter
{
    /**
     * Optional logging of errors callback for the action executer
     * for the cases when the error might be ignored 
     * or shown in a different manner for the action
     * @param action the action
     * @param logger the logger
     * @param t the exception thrown
     * @param message the proposed message that will be logged
     * @return true if it was handled, false for default handling
     */
    boolean onLogException(Log logger, Throwable t, String message);
}
