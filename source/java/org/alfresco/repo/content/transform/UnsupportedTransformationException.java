/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception indicates that a transformer is unable to transform a requested
 * transformation. Normally the transformer is a component of a complex (compound) transformer
 * and has been asked to transform a file that is too large (see transformation limits) as the
 * size of the intermediate file is unknown at the start.
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
public class UnsupportedTransformationException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 9039331287661301086L;

    public UnsupportedTransformationException(String msgId)
    {
        super(msgId);
    }
}
