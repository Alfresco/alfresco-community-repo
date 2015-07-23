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

import java.util.Collections;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Generic class for any runtime exception to do with classified records.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class ClassificationException extends AlfrescoRuntimeException
{
    /** serial version uid */
    private static final long serialVersionUID = -7097573558438226725L;

    public ClassificationException(String msgId) { super(msgId); }
    public ClassificationException(String msgId, Throwable cause) { super(msgId, cause); }

    /** Represents a fatal error due to missing required configuration. */
    public static class MissingConfiguration extends ClassificationException
    {
        /** serial version uid */
        private static final long serialVersionUID = -750162955179494445L;

        public MissingConfiguration(String msgId) { super(msgId); }
    }

    /** Represents a fatal error due to illegal configuration.
     *  The configuration was understood by the server, but was rejected as illegal. */
    public static class IllegalConfiguration extends ClassificationException
    {
        /** serial version uid */
        private static final long serialVersionUID = -1139626996782741741L;

        public IllegalConfiguration(String msgId) { super(msgId); }
    }

    /** Represents a fatal error due to illegal {@link ClassificationLevel#getId() classification level ID} configuration.
     *  The configuration was understood by the server, but was rejected as illegal. */
    public static class IllegalAbbreviationChars extends IllegalConfiguration
    {
        /** serial version uid */
        private static final long serialVersionUID = 98787676565465454L;

        private final List<Character> illegalChars;
        public IllegalAbbreviationChars(String msgId, List<Character> illegalChars)
        {
            super(msgId);
            this.illegalChars = illegalChars;
        }
        public List<Character> getIllegalChars() { return Collections.unmodifiableList(illegalChars); }
    }

    /** Represents a fatal error due to malformed configuration.
     *  The configuration could not be understood by the server. */
    public static class MalformedConfiguration extends ClassificationException
    {
        /** serial version uid */
        private static final long serialVersionUID = 8191162359241035026L;

        public MalformedConfiguration(String msgId) { super(msgId); }
        public MalformedConfiguration(String msgId, Throwable cause) { super(msgId, cause); }
    }

    /** The supplied classification level id was not found in the configured list. */
    public static class LevelIdNotFound extends ClassificationException
    {
        /** serial version uid */
        private static final long serialVersionUID = -8507186704795004383L;

        public LevelIdNotFound(String levelId)
        {
            super("Could not find classification level with id " + levelId);
        }
    }

    /** The supplied classification reason id was not found in the configured list. */
    public static class ReasonIdNotFound extends ClassificationException
    {
        /** serial version uid */
        private static final long serialVersionUID = -643842413653375433L;

        public ReasonIdNotFound(String reasonId)
        {
            super("Could not find classification reason with id " + reasonId);
        }
    }

    /** The supplied exemption category id was not found in the configured list. */
    public static class ExemptionCategoryIdNotFound extends ClassificationException
    {
        /** serial version uid */
        private static final long serialVersionUID = -6754627999115496384L;

        public ExemptionCategoryIdNotFound(String id)
        {
            super("Could not find classification reason with id " + id);
        }
    }

    public static class InvalidNode extends ClassificationException
    {
        /** serial version uid */
        private static final long serialVersionUID = -4485335425932302477L;

        public InvalidNode(NodeRef nodeRef, String message)
        {
            super("Operation not permitted on node " + nodeRef + ", error message: " + message);
        }
    }
}
