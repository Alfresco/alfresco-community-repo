
package org.alfresco.repo.virtual.template;

import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A rule for filing (storing in a physical location) content that is created in
 * a virtual folder.
 *
 * @author Bogdan Horje
 */
public interface FilingRule
{
    FilingData createFilingData(FilingParameters parameters) throws VirtualizationException;

    NodeRef filingNodeRefFor(FilingParameters parameters) throws VirtualizationException;

    boolean isNullFilingRule();
}
