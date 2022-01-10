/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

import net.sf.acegisecurity.ConfigAttribute;

/**
 * RM security configuration definition.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class ConfigAttributeDefinition
{
    /** allowable RM security configurations */
    public static final String RM = "RM";
    public static final String RM_ALLOW = "RM_ALLOW";
    public static final String RM_DENY = "RM_DENY";
    public static final String RM_CAP = "RM_CAP";
    public static final String RM_ABSTAIN = "RM_ABSTAIN";
    public static final String RM_QUERY = "RM_QUERY";

    /** security type */
    private String typeString;

    /** policy name */
    private String policyName;

    /** simple permission reference */
    private SimplePermissionReference required;

    /** parameter position map */
    private Map<Integer, Integer> parameters = new HashMap<>(2, 1.0f);

    /** is parent */
    private boolean parent = false;

    /**
     * Default constructor
     * 
     * @param attr                      configuration attribute instance 
     * @param namespacePrefixResolver   namespace prefix resolver
     */
    public ConfigAttributeDefinition(ConfigAttribute attr, NamespacePrefixResolver namespacePrefixResolver)
    {
        // tokenize configuration string
        StringTokenizer st = new StringTokenizer(attr.getAttribute(), ".", false);
        if (st.countTokens() < 1)
        {
            throw new ACLEntryVoterException("There must be at least one token in a config attribute");
        }
        typeString = st.nextToken();

        // check that the configuration is valid
        if (!(typeString.equals(RM) || 
              typeString.equals(RM_ALLOW) || 
              typeString.equals(RM_CAP) || 
              typeString.equals(RM_DENY) || 
              typeString.equals(RM_QUERY) || 
              typeString.equals(RM_ABSTAIN)))
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

    public Map<Integer, Integer> getParameters()
    {
        return parameters;
    }

    public boolean isParent()
    {
        return parent;
    }
}
