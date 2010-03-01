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
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentReader;

/**
 * An interface that allows separation between the metadata extractor registry and the third party subsystem owning the
 * open office connection.
 * 
 * @author dward
 */
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