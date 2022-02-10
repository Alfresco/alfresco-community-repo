/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.record;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Record missing metadata exception class
 *
 * @author Sara Aspery
 * @since 2.6
 */
public class RecordMissingMetadataException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3437687270967611234L;

    public RecordMissingMetadataException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }

    public RecordMissingMetadataException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public RecordMissingMetadataException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public RecordMissingMetadataException(String msgId)
    {
        super(msgId);
    }
}
