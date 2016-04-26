package org.alfresco.repo.domain.locks;

/**
 * Entity bean for <b>alf_lock_resource</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class LockResourceEntity
{
    private Long id;
    private Long version;
    private Long qnameNamespaceId;
    private String qnameLocalName;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    /**
     * @return                  Returns the ID of the namespace that the lock belongs to
     */
    public Long getQnameNamespaceId()
    {
        return qnameNamespaceId;
    }

    /**
     * @param namespaceId       the ID of the namespace that the lock belongs to
     */
    public void setQnameNamespaceId(Long namespaceId)
    {
        this.qnameNamespaceId = namespaceId;
    }

    /**
     * @return                  Returns the lock qualified name localname
     */
    public String getQnameLocalName()
    {
        return qnameLocalName;
    }

    /**
     * @param qnameLocalName    the lock qualified name localname
     */
    public void setQnameLocalName(String qnameLocalName)
    {
        this.qnameLocalName = qnameLocalName;
    }
}
