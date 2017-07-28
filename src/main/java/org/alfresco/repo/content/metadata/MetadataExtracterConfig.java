/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.content.metadata;

import org.apache.tika.metadata.Metadata;

/**
 * Interface for allowing Alfresco to provide configuration properties to metadata extracters like
 * Tika parser;
 * 
 * @author Andrei Rebegea
 */
public interface MetadataExtracterConfig
{

    // Users can specify override values in alfreso-global.properties
    public static final boolean TIKA_PARSER_PARSE_SHAPES_DEFAULT_VALUE = false;

    /**
     * This method adds appropriate configuration values in the metadata map that will be used in
     * Tika to control some of the features Tika has;
     * 
     * @param metadata
     *            input/output map used by Tika to pass information about the file
     */
    void prepareMetadataWithConfigParams(Metadata metadata);
}
