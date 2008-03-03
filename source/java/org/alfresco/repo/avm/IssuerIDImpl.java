/**
 *
 */
package org.alfresco.repo.avm;

/**
 * Bean for issuer id tracking.
 * @author britt
 */
public class IssuerIDImpl implements IssuerID
{
    private String fIssuer;

    private long fNext;

    private long fVersion;

    public IssuerIDImpl()
    {
    }

    public IssuerIDImpl(String issuer, long next)
    {
        fIssuer = issuer;
        fNext = next;
    }

    public void setVersion(long version)
    {
        fVersion = version;
    }

    public long getVersion()
    {
        return fVersion;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.IssuerID#getIssuer()
     */
    public String getIssuer()
    {
        return fIssuer;
    }

    public void setIssuer(String issuer)
    {
        fIssuer = issuer;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.IssuerID#getNext()
     */
    public long getNext()
    {
        return fNext;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.IssuerID#setNext(long)
     */
    public void setNext(long next)
    {
        fNext = next;
    }
}
