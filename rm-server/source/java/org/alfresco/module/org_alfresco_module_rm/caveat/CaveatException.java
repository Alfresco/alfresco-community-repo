/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.module.org_alfresco_module_rm.caveat;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Generic class for any runtime exception to do with caveats.
 *
 * @author Tom Page
 * @since 2.4.a
 */
public class CaveatException extends AlfrescoRuntimeException
{
    /** serial version uid */
    private static final long serialVersionUID = -1678248996340040195L;

    public CaveatException(String msgId)
    {
        super(msgId);
    }

    public CaveatException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /** The supplied caveat group id was not found in the configured list. */
    public static class CaveatGroupNotFound extends CaveatException
    {
        /** serial version uid */
        private static final long serialVersionUID = -3547790332616121911L;

        public CaveatGroupNotFound(String caveatGroupId)
        {
            super("Could not find caveat group with id " + caveatGroupId);
        }
    }

    /** The supplied caveat mark id was not found in the configured list. */
    public static class CaveatMarkNotFound extends CaveatException
    {
        /** serial version uid */
        private static final long serialVersionUID = -7605943340899129129L;

        public CaveatMarkNotFound(String caveatMarkId)
        {
            super("Could not find caveat mark with id " + caveatMarkId);
        }
    }

    /** The caveat configuration file contains errors. */
    public static class MalformedConfiguration extends CaveatException
    {
        /** serial version uid */
        private static final long serialVersionUID = -4486048933410071424L;

        public MalformedConfiguration(String message)
        {
            super(message);
        }

        public MalformedConfiguration(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
}
