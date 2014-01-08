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
package org.alfresco.module.org_alfresco_module_rm.search;


/**
 * Report details.
 *            
 * @author Roy Wetherall
 */
public class ReportDetails 
{
    /** Name */
	protected String name;
	
	/** Description */
	protected String description;
	
	/** Search */
	protected String search;
	
	/** Search parameters */
	protected RecordsManagementSearchParameters searchParameters;

	/**
	 * 
	 * @param name
	 * @param description
	 * @param search
	 * @param searchParameters
	 */
	public ReportDetails(String name, String description, String search, RecordsManagementSearchParameters searchParameters) 
	{
		this.name = name;
		this.description = description;
		this.search = search;
		this.searchParameters = searchParameters;
	}
	
	/**
	 * @return {@link String}  name
	 */
	public String getName() 
	{
		return name;
	}
	
	/**
	 * @return {@link String}  description
	 */
	public String getDescription() 
	{
		return description;
	}

	/**
	 * @param description  description
	 */
	public void setDescription(String description) 
	{
		this.description = description;
	}

	/**
	 * @return {@link String}  search string
	 */
	public String getSearch()
    {
        return search;
    }
	
	/**
	 * @param query query string
	 */
	public void setSearch(String search)
    {
        this.search = search;
    }	
	
	/**
	 * @return
	 */
	public RecordsManagementSearchParameters getSearchParameters()
    {
        return searchParameters;
    }
	
	/**
	 * @param searchParameters
	 */
	public void setSearchParameters(RecordsManagementSearchParameters searchParameters)
    {
        this.searchParameters = searchParameters;
    }
}
