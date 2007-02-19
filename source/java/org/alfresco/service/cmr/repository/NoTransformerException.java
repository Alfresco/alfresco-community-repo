/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.repository;

import java.text.MessageFormat;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when a transformation request cannot be honoured due to
 * no transformers being present for the requested transformation.  
 * 
 * @author Derek Hulley
 */
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
