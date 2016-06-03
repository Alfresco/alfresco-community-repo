package org.alfresco.repo.solr;

/**
 * Interface for SOLR changeset objects.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface AclChangeSet
{
    Long getId();
    Long getCommitTimeMs();
    int getAclCount();
}
