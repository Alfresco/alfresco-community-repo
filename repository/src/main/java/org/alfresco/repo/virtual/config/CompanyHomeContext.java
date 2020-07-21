/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
