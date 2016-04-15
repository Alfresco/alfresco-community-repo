package org.alfresco.repo.solr;

import org.springframework.context.ApplicationEvent;

/**
 * 
 * @since 4.0
 *
 */
public abstract class SolrEvent extends ApplicationEvent
{
	private static final long serialVersionUID = 1L;

	public SolrEvent(Object source)
	{
		super(source);
	}
}
