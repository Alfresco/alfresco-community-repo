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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Generic class for any runtime exception thrown within the {@link BasePostMethodInvocationProcessor} and subclasses.
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ClassificationPostMethodInvocationException extends AlfrescoRuntimeException
{
    /** Serial version UID */
    private static final long serialVersionUID = 2614182915625548638L;

    /**
     * Base classification post method invocation exception
     *
     * @param msgId The text which will be shown in the exception
     */
    public ClassificationPostMethodInvocationException(String msgId)
    {
        super(msgId);
    }

    /**
     * Base classification post method invocation exception
     *
     * @param msgId The text which will be shown in the exception
     * @param cause The cause of the exception
     */
    public ClassificationPostMethodInvocationException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Represents a fatal error due to a wrong class type
     */
    public static class NotSupportedClassTypeException extends ClassificationPostMethodInvocationException
    {
        /** Serial version UID */
        private static final long serialVersionUID = 7614080640030648878L;

        /**
         * @param msgId The text which will be shown in the exception
         */
        public NotSupportedClassTypeException(String msgId)
        {
            super(msgId);
        }

        /**
         * @param msgId The text which will be shown in the exception
         * @param cause The cause of the exception
         */
        public NotSupportedClassTypeException(String msgId, Throwable cause)
        {
            super(msgId, cause);
        }
    }
}
