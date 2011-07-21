package org.alfresco.repo.solr;

import org.springframework.context.ApplicationEvent;

public class SolrActiveEvent extends ApplicationEvent
{
	private static final long serialVersionUID = -7361024456694701653L;

	public SolrActiveEvent(Object source) {
        super(source);
    }

}
