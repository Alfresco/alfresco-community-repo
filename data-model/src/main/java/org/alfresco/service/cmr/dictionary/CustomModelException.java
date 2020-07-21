/*
 * #%L
 * Alfresco Data model classes
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

package org.alfresco.service.cmr.dictionary;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 2868108814940403250L;

    /**
     * Constructor
     * 
     * @param msgId message id
     */
    public CustomModelException(String msgId)
    {
        super(msgId);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param msgParams message params
     */
    public CustomModelException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param cause causing exception
     */
    public CustomModelException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    /**
     * Constructor
     * 
     * @param msgId message id
     * @param msgParams message params
     * @param cause causing exception
     */
    public CustomModelException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public static class ModelExistsException extends CustomModelException
    {
        private static final long serialVersionUID = -6092936121754376435L;

        public ModelExistsException(String msgId)
        {
            super(msgId);
        }

        public ModelExistsException(String msgId, Object[] msgParams)
        {
            super(msgId, msgParams);
        }
    }

    public static class ModelDoesNotExistException extends CustomModelException
    {
        private static final long serialVersionUID = 6053078524717867514L;

        public ModelDoesNotExistException(String msgId)
        {
            super(msgId);
        }

        public ModelDoesNotExistException(String msgId, Object[] msgParams)
        {
            super(msgId, msgParams);
        }
    }

    public static class InvalidNamespaceException extends CustomModelException
    {
        private static final long serialVersionUID = 1405352431422776830L;

        public InvalidNamespaceException(String msgId)
        {
            super(msgId);
        }

        public InvalidNamespaceException(String msgId, Object[] msgParams)
        {
            super(msgId, msgParams);
        }
    }

    public static class InvalidCustomModelException extends CustomModelException
    {
        private static final long serialVersionUID = -2450003245810515336L;

        public InvalidCustomModelException(String msgId)
        {
            super(msgId);
        }

        public InvalidCustomModelException(String msgId, Object[] msgParams)
        {
            super(msgId, msgParams);
        }

        public InvalidCustomModelException(String msgId, Throwable cause)
        {
            super(msgId, cause);
        }

        public InvalidCustomModelException(String msgId, Object[] msgParams, Throwable cause)
        {
            super(msgId, msgParams, cause);
        }
    }

    public static class CustomModelConstraintException extends CustomModelException
    {
        private static final long serialVersionUID = 5993485961751086115L;

        public CustomModelConstraintException(String msgId)
        {
            super(msgId);
        }

        public CustomModelConstraintException(String msgId, Object[] msgParams)
        {
            super(msgId, msgParams);
        }
    }

    public static class NamespaceConstraintException extends CustomModelConstraintException
    {
        private static final long serialVersionUID = 9050315122661406808L;

        public NamespaceConstraintException(String msgId)
        {
            super(msgId);
        }

        public NamespaceConstraintException(String msgId, Object[] msgParams)
        {
            super(msgId, msgParams);
        }
    }

    public static class ActiveModelConstraintException extends CustomModelConstraintException
    {
        private static final long serialVersionUID = -6740246156929572802L;

        public ActiveModelConstraintException(String msgId)
        {
            super(msgId);
        }

        public ActiveModelConstraintException(String msgId, Object[] msgParams)
        {
            super(msgId, msgParams);
        }
    }
}
