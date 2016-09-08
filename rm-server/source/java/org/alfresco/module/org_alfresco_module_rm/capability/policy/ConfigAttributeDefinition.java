/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import java.util.HashMap;
import java.util.StringTokenizer;

import net.sf.acegisecurity.ConfigAttribute;

import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 * @since 2.1
 */
public class ConfigAttributeDefinition
{
    public static final String RM = "RM";
    public static final String RM_ALLOW = "RM_ALLOW";
    public static final String RM_DENY = "RM_DENY";
    public static final String RM_CAP = "RM_CAP";
    public static final String RM_ABSTAIN = "RM_ABSTAIN";
    public static final String RM_QUERY = "RM_QUERY";
    
    private String typeString;

    private String policyName;

    private SimplePermissionReference required;

    private HashMap<Integer, Integer> parameters = new HashMap<Integer, Integer>(2, 1.0f);

    private boolean parent = false;
    
    public ConfigAttributeDefinition(ConfigAttribute attr, NamespacePrefixResolver namespacePrefixResolver)
    {        
        StringTokenizer st = new StringTokenizer(attr.getAttribute(), ".", false);
        if (st.countTokens() < 1)
        {
            throw new ACLEntryVoterException("There must be at least one token in a config attribute");
        }
        typeString = st.nextToken();

        if (!(typeString.equals(RM) || typeString.equals(RM_ALLOW) || typeString.equals(RM_CAP) || typeString.equals(RM_DENY) || typeString.equals(RM_QUERY) || typeString
                .equals(RM_ABSTAIN)))
        {
            throw new ACLEntryVoterException("Invalid type: must be ACL_NODE, ACL_PARENT or ACL_ALLOW");
        }

        if (typeString.equals(RM))
        {
            policyName = st.nextToken();
            int position = 0;
            while (st.hasMoreElements())
            {
                String numberString = st.nextToken();
                Integer value = Integer.parseInt(numberString);
                parameters.put(position, value);
                position++;
            }
        }
        else if (typeString.equals(RM_CAP))
        {
            String numberString = st.nextToken();
            String qNameString = st.nextToken();
            String permissionString = st.nextToken();

            Integer value = Integer.parseInt(numberString);
            parameters.put(0, value);

            QName qName = QName.createQName(qNameString, namespacePrefixResolver);

            required = SimplePermissionReference.getPermissionReference(qName, permissionString);

            if (st.hasMoreElements())
            {
                parent = true;
            }
        }
    }
    
    public String getTypeString()
    {
        return typeString;
    }
    
    public String getPolicyName()
    {
        return policyName;
    }
    
    public SimplePermissionReference getRequired()
    {
        return required;
    }
    
    public HashMap<Integer, Integer> getParameters()
    {
        return parameters;
    }
    
    public boolean isParent()
    {
        return parent;
    }
}
