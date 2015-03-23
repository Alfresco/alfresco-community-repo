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
package org.alfresco.module.org_alfresco_module_rm.classification;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Generic class for any runtime exception thrown within the {@link ClassificationService}.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationServiceException extends AlfrescoRuntimeException
{
    public ClassificationServiceException(String msgId) { super(msgId); }
    public ClassificationServiceException(String msgId, Throwable cause) { super(msgId, cause); }

    /** Represents a fatal error due to missing required configuration. */
    public static class MissingConfiguration extends ClassificationServiceException
    {
        public MissingConfiguration(String msgId) { super(msgId); }
    }

    /** Represents a fatal error due to illegal configuration.
     *  The configuration was understood by the server, but was rejected as illegal. */
    public static class IllegalConfiguration extends ClassificationServiceException
    {
        public IllegalConfiguration(String msgId) { super(msgId); }
    }

    /** Represents a fatal error due to malformed configuration.
     *  The configuration could not be understood by the server. */
    public static class MalformedConfiguration extends ClassificationServiceException
    {
        public MalformedConfiguration(String msgId) { super(msgId); }
        public MalformedConfiguration(String msgId, Throwable cause) { super(msgId, cause); }
    }
}
