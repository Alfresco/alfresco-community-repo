
package org.alfresco.service.cmr.rendition;

import org.alfresco.service.cmr.action.ActionList;

/**
 * This is a special {@link RenditionDefinition} which allows sequential
 * execution of a list of other {@link RenditionDefinition}s. For example, it
 * might be used to transform a PDF file to a JPEG imaged and then resize that
 * image. This would be achieved by creating a
 * {@link CompositeRenditionDefinition} that has two sub-definitions, one to
 * reformat the PDF to a JPEG image and the second to resize the JPEG image.
 * 
 * @author Nick Smith
 */
public interface CompositeRenditionDefinition extends RenditionDefinition, ActionList<RenditionDefinition>
{
    // Intentionally empty!
}
