package org.alfresco.repo.solr;

/**
 * Interface for SOLR ACL objects.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface Acl extends Comparable<Acl>
{
    Long getId();
    Long getInheritedId();
    Long getAclChangeSetId();
}
