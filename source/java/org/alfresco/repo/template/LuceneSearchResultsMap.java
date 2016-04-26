package org.alfresco.repo.template;

import java.io.StringReader;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Provides functionality to execute a Lucene search string and return TemplateNode objects.
 * 
 * @author Kevin Roast
 */
public class LuceneSearchResultsMap extends BaseSearchResultsMap
{
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public LuceneSearchResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }

    /**
     * @see org.alfresco.repo.template.BaseTemplateMap#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        // execute the search
        return query(key.toString());
    }
}
