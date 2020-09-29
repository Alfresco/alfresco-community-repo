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
package org.alfresco.repo.search.impl;

import java.util.List;
import java.util.Map;

/**
 * JSON returned from SOLR API
 *
 * @author aborroy
 * @since 6.2
 */
public interface JSONAPIResult
{
    
    /**
     * Time to perform the requested action or command in SOLR
     * @return Number of milliseconds
     */
    public Long getQueryTime();
    
    /**
     * HTTP Response code
     * But for 200, that is being returned as 0
     * @return Number representing an HTTP Status
     */
    public Long getStatus();

    /**
     * Name of the cores managed by SOLR
     * @return A list with the names of the cores
     */
    public List<String> getCores();

    /**
     * Information from the cores to be exposed in JMX Beans
     * The names and the structure of the properties depend on the type of the Action
     * @return Core information by core name
     */
    public Map<String, Map<String, Object>> getCoresInfo();

}