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
    /** serial version uid */
    private static final long serialVersionUID = -7097573558438226725L;

    public ClassificationServiceException(String msgId) { super(msgId); }
    public ClassificationServiceException(String msgId, Throwable cause) { super(msgId, cause); }

    /** Represents a fatal error due to missing required configuration. */
    public static class MissingConfiguration extends ClassificationServiceException
    {
        /** serial version uid */
        private static final long serialVersionUID = -750162955179494445L;

        public MissingConfiguration(String msgId) { super(msgId); }
    }

    /** Represents a fatal error due to illegal configuration.
     *  The configuration was understood by the server, but was rejected as illegal. */
    public static class IllegalConfiguration extends ClassificationServiceException
    {
        /** serial version uid */
        private static final long serialVersionUID = -1139626996782741741L;

        public IllegalConfiguration(String msgId) { super(msgId); }
    }

    /** Represents a fatal error due to malformed configuration.
     *  The configuration could not be understood by the server. */
    public static class MalformedConfiguration extends ClassificationServiceException
    {
        /** serial version uid */
        private static final long serialVersionUID = 8191162359241035026L;

        public MalformedConfiguration(String msgId) { super(msgId); }
        public MalformedConfiguration(String msgId, Throwable cause) { super(msgId, cause); }
    }

    /** The supplied classification level id was not found in the configured list. */
    public static class LevelIdNotFound extends ClassificationServiceException
    {
        /** serial version uid */
        private static final long serialVersionUID = -8507186704795004383L;

        public LevelIdNotFound(String levelId)
        {
            super("Could not find classification level with id " + levelId);
        }
    }

    /** The supplied classification reason id was not found in the configured list. */
    public static class ReasonIdNotFound extends ClassificationServiceException
    {
        /** serial version uid */
        private static final long serialVersionUID = -643842413653375433L;

        public ReasonIdNotFound(String reasonId)
        {
            super("Could not find classification reason with id " + reasonId);
        }
    }
}
