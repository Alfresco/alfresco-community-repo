
package org.alfresco.repo.virtual.template;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A {@link FilingRule} created in the absence of filing criteria in the virtual
 * folder template.
 *
 * @author Bogdan Horje
 */
public class NullFilingRule implements FilingRule
{

    public NullFilingRule(ActualEnvironment environment)
    {
        super();
    }

    @Override
    public FilingData createFilingData(FilingParameters parameters) throws VirtualizationException
    {
        throw new VirtualizationException("Can not create filing data for readonly filing rule.");
    }

    @Override
    public NodeRef filingNodeRefFor(FilingParameters parameters) throws VirtualizationException
    {
        throw new VirtualizationException("Can not create parent readonly filing rule.");
    }

    @Override
    public boolean isNullFilingRule()
    {
        return true;
    }

}
