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
package org.alfresco.repo.importer;

import java.io.Reader;

/**
 * This interface represents the contract between the importer service and a parser (which is responsible for parsing the input stream and extracting node descriptions).
 * 
 * The parser interacts with the passed importer to import nodes into the Repository.
 * 
 * @author David Caruana
 */
public interface Parser
{
    /**
     * Parse nodes from specified input stream and import via the provided importer
     * 
     * @param viewReader
     *            Reader
     * @param importer
     *            Importer
     */
    public void parse(Reader viewReader, Importer importer);

}
