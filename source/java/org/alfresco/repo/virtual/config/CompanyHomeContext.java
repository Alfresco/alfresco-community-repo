
package org.alfresco.repo.virtual.config;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * A {@link NodeRefContext} that solves a name path relative to the Alfresco
 * company home node.
 */
public class CompanyHomeContext implements NodeRefContext
{

    public static final String COMPANY_HOME_CONTEXT_NAME = "CompanyHome";

    private static final String[] EMPTY_PATH = new String[0];

    private String companyHomeQName;

    public void setCompanyHomeQName(String companyHomeQName)
    {
        this.companyHomeQName = companyHomeQName;
    }

    @Override
    public NodeRef resolveNamePath(String[] namePath, NodeRefResolver resolver)
    {
        String[] companyHomeRealtiveRef = createRelativeNamePath(namePath);

        return resolver.resolvePathReference(companyHomeRealtiveRef);
    }

    private String[] createRelativeNamePath(String[] namePath)
    {
        if (namePath == null)
        {
            namePath = EMPTY_PATH;
        }
        String[] companyHomeRealtiveRef = new String[namePath.length + 3];
        companyHomeRealtiveRef[0] = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol();
        companyHomeRealtiveRef[1] = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getIdentifier();
        companyHomeRealtiveRef[2] = companyHomeQName;

        if (namePath.length > 0)
        {
            System.arraycopy(namePath,
                             0,
                             companyHomeRealtiveRef,
                             3,
                             namePath.length);
        }
        return companyHomeRealtiveRef;
    }

    @Override
    public NodeRef resolveQNamePath(String[] qNamePath, NodeRefResolver resolver)
    {
        String[] companyHomeRealtiveRef = createRelativeQNamePath(qNamePath);

        return resolver.resolveQNameReference(companyHomeRealtiveRef);
    }

    private String[] createRelativeQNamePath(String[] qNamePath)
    {
        if (qNamePath == null)
        {
            qNamePath = EMPTY_PATH;
        }
        String[] companyHomeRealtiveRef = new String[qNamePath.length + 1];
        companyHomeRealtiveRef[0] = companyHomeQName;

        if (qNamePath.length > 0)
        {
            System.arraycopy(qNamePath,
                             0,
                             companyHomeRealtiveRef,
                             1,
                             qNamePath.length);
        }
        return companyHomeRealtiveRef;
    }

    @Override
    public NodeRef createNamePath(String[] namePath, NodeRefResolver resolver)
    {
        String[] relativeNamePath = createRelativeNamePath(namePath);
        return resolver.createNamePath(relativeNamePath);
    }

    @Override
    public NodeRef createQNamePath(String[] qNamePath, String[] names, NodeRefResolver resolver)
    {
        String[] relativeQNamePath = createRelativeQNamePath(qNamePath);
        return resolver.createQNamePath(relativeQNamePath,
                                        names);
    }

    @Override
    public String getContextName()
    {
        return COMPANY_HOME_CONTEXT_NAME;
    }

}
