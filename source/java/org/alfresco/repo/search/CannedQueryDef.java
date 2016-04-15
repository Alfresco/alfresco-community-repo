package org.alfresco.repo.search;

import java.util.Collection;
import java.util.Map;

import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * The definition of a canned query
 * 
 * @author andyh
 * 
 */
public interface CannedQueryDef
{
    /**
     * Get the unique name for the query
     * 
     * @return QName
     */
    public QName getQname();

    /**
     * Get the language in which the query is defined.
     * 
     * @return String
     */
    public String getLanguage();

    /**
     * Get the definitions for any query parameters.
     * 
     * @return Collection<QueryParameterDefinition>
     */
    public Collection<QueryParameterDefinition> getQueryParameterDefs();

    /**
     * Get the query string.
     * 
     * @return String
     */
    public String getQuery();

    /**
     * Return the mechanism that this query definition uses to map namespace
     * prefixes to URIs. A query may use a predefined set of prefixes for known
     * URIs. I would be unwise to rely on the defaults.
     * 
     * @return NamespacePrefixResolver
     */
    public NamespacePrefixResolver getNamespacePrefixResolver();

    /**
     * Get a map to look up definitions by Qname
     * 
     * @return Map
     */
    public Map<QName, QueryParameterDefinition> getQueryParameterMap();
}
