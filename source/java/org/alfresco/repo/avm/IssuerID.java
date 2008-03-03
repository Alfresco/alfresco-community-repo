/**
 *
 */
package org.alfresco.repo.avm;

/**
 * Trivial interface for accessing issuer ids.
 * @author britt
 */
public interface IssuerID
{
    /**
     * Get the name of the issuer.
     * @return
     */
    public String getIssuer();

    /**
     * Get the highest id.
     * @return
     */
    public long getNext();

    /**
     * Set the next id to be issued.
     * @param next The next id to be issued.
     */
    public void setNext(long next);
}
