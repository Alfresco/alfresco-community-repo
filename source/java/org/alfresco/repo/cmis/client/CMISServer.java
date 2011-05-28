/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.client;

import java.util.Map;

/**
 * CMIS server definition.
 */
public interface CMISServer
{
    /**
     * Gets CMIS Server Name
     * 
     * @return server name
     */
    String getName();

    /**
     * Gets CMIS Server Description
     * 
     * @return server description
     */
    String getDescription();

    /**
     * Gets CMIS Server Parameters
     * 
     * @return server parameters
     */
    Map<String, String> getParameters();
}
