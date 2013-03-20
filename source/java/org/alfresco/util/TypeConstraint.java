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
package org.alfresco.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Stores a set of expected and excluded types, by full type name. The localName can be a wildcard (*) to indicate that the
 * whole namespace should be expected/excluded.
 * 
 * A node is tested to ensure that its type is in the expected list and not in the excluded list. Its aspects are also
 * tested to ensure that they are in the expected list and not in the excluded list.
 * 
 * Adapted some code from QNameFilter.
 * 
 * @author steveglover
 *
 */
public class TypeConstraint
{
    public static final String WILDCARD = "*";

	private Set<String> expectedModels;
	private Set<QName> expectedQNames;
	private List<String> expectedTypes;

    private Set<QName> excludedQNames;
    private Set<String> excludedModels;
    private List<String> excludedTypes;

    private NodeService nodeService;

    public void setExpectedTypes(List<String> expectedTypes)
    {
//        if(expectedTypes != null && expectedTypes.size() > 0)
//        {
//        	this.expectedTypes = new ArrayList<QName>(expectedTypes.size());
//
//	    	for(String type : expectedTypes)
//	    	{
//	            final QName typeDef = QName.createQName(type);
//	    		this.expectedTypes.add(typeDef);
//	    	}
//        }
    	this.expectedTypes = expectedTypes;
	}

	public void setNodeService(NodeService nodeService)
    {
		this.nodeService = nodeService;
	}

    public void setExcludedTypes(List<String> excludedTypes)
    {
        this.excludedTypes = excludedTypes;
    }

    public void init()
    {
        if(expectedTypes != null && !expectedTypes.isEmpty())
        {
        	preprocessExpectedTypes(expectedTypes);
        }
        if(excludedTypes != null && !excludedTypes.isEmpty())
        {
        	preprocessExcludedTypes(excludedTypes);
        }
    }

    /**
     * Processes the user-defined list of types into valid QNames & models, it validates them
     * against the dictionary and also supports wildcards
     * @param excludeTypeNames
     * @return Set<QName> Valid type QNames
     */
    protected void preprocessExcludedTypes(List<String> typeNames)
    {
        Set<QName> qNamesToExclude = new HashSet<QName>(typeNames.size());
        Set<String> modelsToExclude = new HashSet<String>();
        
        for (String typeDefinition : typeNames)
        {
            final QName typeDef = QName.createQName(typeDefinition);
            if (WILDCARD.equals(typeDef.getLocalName()))
            {
            	modelsToExclude.add(typeDef.getNamespaceURI());
            }
            else
            {
            	qNamesToExclude.add(typeDef); // valid so add it to the list
            }
        }

        this.excludedModels = modelsToExclude;
        this.excludedQNames = qNamesToExclude;
    }

    /**
     * Processes the user-defined list of types into valid QNames & models, it validates them
     * against the dictionary and also supports wildcards
     * @param excludeTypeNames
     * @return Set<QName> Valid type QNames
     */
    protected void preprocessExpectedTypes(List<String> typeNames)
    {
        Set<QName> qNames = new HashSet<QName>(typeNames.size());
        Set<String> models = new HashSet<String>();
        
        for (String typeDefinition : typeNames)
        {
            final QName typeDef = QName.createQName(typeDefinition);
            if (WILDCARD.equals(typeDef.getLocalName()))
            {
            	models.add(typeDef.getNamespaceURI());
            }
            else
            {
            	qNames.add(typeDef); // valid so add it to the list
            }
        }

        this.expectedModels = models;
        this.expectedQNames = qNames;
    }
    
    private boolean isExcluded(QName typeQName)
    {
        return excludedQNames != null && excludedQNames.contains(typeQName) || excludedModels != null && excludedModels.contains(typeQName.getNamespaceURI());
    }
    
    private boolean matchesExpected(QName typeQName)
    {
    	boolean ret = false;

    	if(expectedQNames == null || expectedQNames.contains(typeQName) || expectedModels == null || expectedModels.contains(typeQName.getNamespaceURI()))
    	{
    		ret = true;
    	}

        return ret;
    }

    /**
     * Returns true if the nodeRef matches the constraints, false otherwise.
     * 
     * @param nodeRef
     * @return returns true if the nodeRef matches the constraints, false otherwise.
     */
	public boolean matches(final NodeRef nodeRef)
	{
		// need to run as system - caller may not be able to read the node's aspects
		// but we need to know what they are in order to determine exclusion.
		boolean matches = TenantUtil.runAsSystemTenant(new TenantRunAsWork<Boolean>()
		{
			@Override
			public Boolean doWork() throws Exception
			{
				boolean matches = true;

				QName nodeType = nodeService.getType(nodeRef);
				boolean typeExcluded = isExcluded(nodeType);

				if(typeExcluded)
				{
					// if the type is excluded, bail out
					matches = false;
				}
				else
				{
					boolean isExpected = matchesExpected(nodeType);
					if(isExpected)
					{
						// type is expected and not excluded, check the aspects for exclusion
						Set<QName> aspects = nodeService.getAspects(nodeRef);
						for(QName aspect : aspects)
						{
							if(isExcluded(aspect))
							{
								matches = false;
								break;
							}
						}
					}
					else
					{
						// type not in expected list, check aspects for exclusion and in the expected list

						// check aspects
						Set<QName> aspects = nodeService.getAspects(nodeRef);
						for(QName aspect : aspects)
						{
							if(isExcluded(aspect))
							{
								// aspect is excluded, bail out
								matches = false;
								break;
							}

							if(matchesExpected(aspect))
							{
								// aspect matches an expected type
								isExpected = true;
							}
						}
						
						if(!isExpected)
						{
							// neither the type nor any of the aspects in the expected list, no match
							matches = false;
						}
					}
				}

				return matches;
			}
		}, TenantUtil.getCurrentDomain());

		return matches;
	}
}
