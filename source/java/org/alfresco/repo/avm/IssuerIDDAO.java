/**
 *
 */
package org.alfresco.repo.avm;

/**
 * DAO interface for IssuerIDs.
 * @author britt
 */
public interface IssuerIDDAO
{
    /**
     * Get one by name (primary key).
     * @param name
     * @return
     */
    public IssuerID get(String name);

    /**
     * Save one.
     * @param issuerID
     */
    public void save(IssuerID issuerID);
}
