/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

import java.text.MessageFormat;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when a transformation request cannot be honoured due to
 * no transformers being present for the requested transformation.  
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class NoTransformerException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3689067335554183222L;

    private static final MessageFormat MSG =
        new MessageFormat("No transformation exists between mimetypes {0} and {1}");

    private String sourceMimetype;
    private String targetMimetype;
    
    /**
     * @param sourceMimetype the attempted source mimetype
     * @param targetMimetype the attempted target mimetype
     */
    public NoTransformerException(String sourceMimetype, String targetMimetype)
    {
        super(MSG.format(new Object[] {sourceMimetype, targetMimetype}));
        this.sourceMimetype = sourceMimetype;
        this.targetMimetype = targetMimetype;
    }

    public String getSourceMimetype()
    {
        return sourceMimetype;
    }
    
    public String getTargetMimetype()
    {
        return targetMimetype;
    }
}
