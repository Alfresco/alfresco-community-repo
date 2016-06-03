
package org.alfresco.service.cmr.rendition;

import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * This interface defines a callback object which can be used to handle the ultimate
 * result of asynchronous renditions.
 * 
 * @author Neil McErlean
 * @see RenditionService#render(org.alfresco.service.cmr.repository.NodeRef, RenditionDefinition, RenderCallback)
 */
public interface RenderCallback
{
    /**
     * This callback method will be called upon successful completion of an asynchronous
     * rendition.
     * @param primaryParentOfNewRendition a ChildAssociationRef linking the new rendition
     *            object to its primary parent.
     */
    void handleSuccessfulRendition(ChildAssociationRef primaryParentOfNewRendition);
    
    /**
     * This callback method will be called upon unsuccessful completion of an
     * asynchronous rendition.
     * @param t the Throwable giving the cause of the rendition failure.
     */
    void handleFailedRendition(Throwable t);
}
