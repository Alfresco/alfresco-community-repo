/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.service.cmr.action;

/**
 * This exception should be thrown when an {@link Action} has not been run successfully due to
 * a transient condition and where it is possible that a subsequent request to execute the
 * same action might succeed.
 * <p/>
 * An example of this would be the case where a request to create a thumbnail
 * has failed because the necessary thumbnailing software is not available e.g. because the OpenOffice.org process
 * is not currently running.
 * <p/>
 * The {@link ActionService} can be configured to run a {@link Action#setCompensatingAction(Action) compensating action}
 * when another action fails with an exception. If however the exception thrown is an instance of {@link ActionServiceTransientException}
 * then this compensating action will not be run.
 * 
 * @author Neil Mc Erlean
 * @since 4.0.1
 */
public class ActionServiceTransientException extends ActionServiceException 
{
    private static final long serialVersionUID = 3257571685241467958L;
    
    public ActionServiceTransientException(String msgId)
    {
        super(msgId);
    }
    
    public ActionServiceTransientException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }
    
    public ActionServiceTransientException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }
    
    public ActionServiceTransientException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
