package org.alfresco.repo.tenant;

/**
 * Network quota (Cloud only at present).
 * 
 * @author steveglover
 */
public class Quota
{
	private String name;
	private Long limit;
	private Long usage;
	
	public Quota()
	{
	}

	public Quota(String name, Long limit, Long usage)
	{
		this.name = name;
		this.limit = limit;
		this.usage = usage;
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}

	public Long getLimit()
	{
		return limit;
	}

	public void setLimit(Long limit)
	{
		this.limit = limit;
	}

	public Long getUsage()
	{
		return usage;
	}

	public void setUsage(Long usage)
	{
		this.usage = usage;
	}

	@Override
	public String toString()
	{
		return "Quota [name=" + name + ", limit=" + limit + ", usage=" + usage
				+ "]";
	}

}