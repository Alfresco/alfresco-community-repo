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
package org.alfresco.repo.action.scheduled;

/**
 * Exception for invalid cron expressions
 * 
 * @author andyh
 *
 */
public class InvalidCronExpression extends ScheduledActionException
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -6618964886875008727L;

    /**
     * Invalid cron expression
     * 
     * @param msgId String
     */
    public InvalidCronExpression(String msgId)
    {
        super(msgId);
    }

    /**
     * Invalid cron expression
     * 
     * @param msgId String
     * @param msgParams Object[]
     */
    public InvalidCronExpression(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Invalid cron expression
     * 
     * @param msgId String
     * @param cause Throwable
     */
    public InvalidCronExpression(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Invalid cron expression
     * @param msgId String
     * @param msgParams Object[]
     * @param cause Throwable
     */
    public InvalidCronExpression(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

}
