package org.alfresco.repo.security.person;

import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * An object responsible for creating / validating the home folder for a given person node child association reference.
 */
public interface HomeFolderManager
{
    /**
     * Create / move the home folder if required.
     * 
     * @param childAssocRef
     *            the primary child association pointing to the person node.
     */
    public void makeHomeFolder(ChildAssociationRef childAssocRef);

}