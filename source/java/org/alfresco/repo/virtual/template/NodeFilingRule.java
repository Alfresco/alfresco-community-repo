
package org.alfresco.repo.virtual.template;

import java.util.Collections;

import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class NodeFilingRule implements FilingRule
{

    private ActualEnvironment environment;

    public NodeFilingRule(ActualEnvironment environment)
    {
        super();
        this.environment = environment;
    }

    @Override
    public FilingData createFilingData(FilingParameters parameters) throws VirtualizationException
    {
        NodeRef filingNodeRef = filingNodeRefFor(parameters); 
        return new FilingData(filingNodeRef,
                              parameters.getAssocTypeQName(),
                              parameters.getAssocQName(),
                              parameters.getNodeTypeQName(),
                              Collections.<QName> emptySet(),
                              parameters.getProperties());
    }

    @Override
    public NodeRef filingNodeRefFor(FilingParameters parameters) throws VirtualizationException
    {
        return parameters.getParentRef().execute(new GetActualNodeRefMethod(environment));
    }

    @Override
    public boolean isNullFilingRule()
    {
        return false;
    }

}
