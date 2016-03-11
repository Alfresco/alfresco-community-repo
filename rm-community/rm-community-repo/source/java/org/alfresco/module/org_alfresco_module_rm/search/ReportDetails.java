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
