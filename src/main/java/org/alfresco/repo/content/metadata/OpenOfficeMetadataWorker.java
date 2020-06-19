/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentReader;

/**
 * @deprecated OOTB extractors are being moved to T-Engines.
 *
 * An interface that allows separation between the metadata extractor registry and the third party subsystem owning the
 * open office connection.
 * 
 * @author dward
 */
@Deprecated
public interface OpenOfficeMetadataWorker
{
    /**
     * @return Returns true if a connection to the Uno server could be established
     */
    public boolean isConnected();

    /**
     * @see AbstractMappingMetadataExtracter#extractRaw(ContentReader)
     */
    public Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable;
}