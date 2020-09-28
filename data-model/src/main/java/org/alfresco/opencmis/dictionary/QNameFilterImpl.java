/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.opencmis.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * Filters a QName and excludes any
 * that are defined using the excludedTypes parameter.
 * The list of types can either be defined using a property name such as "cm:name" or with a wildcard: "cm:*"
 * It validates the definitions against the DataDictionary.
 *
 * @author steveglover
 * @author Gethin James
 */
public class QNameFilterImpl implements QNameFilter
{
    private Map<QName, Boolean> excludedQNames;
    private Set<String> excludedModels;
    
    private List<String> excludedTypes;
    
    /**
     * Filters out any QName defined in the "excludedModels" property
     * 
     * @param typesToFilter - original list
     * @return the filtered list
     */
    public Collection<QName> filterQName(Collection<QName> typesToFilter)
    {
        Collection<QName> filteredTypes = new ArrayList<QName>();
        if (!excludedQNames.isEmpty() || !excludedModels.isEmpty())
        {
            //If we have a exclusion list then loop through and exclude models /types
            //that are in this list.
            for (QName classQName : typesToFilter)
            {
            	if(!isExcluded(classQName))
//                if (!excludedQNames.contains(classQName) && !excludedModels.contains(classQName.getNamespaceURI()))
                {    
                  //Not excluded so add it
                  filteredTypes.add(classQName); 
                }
            }
        }
        else
        {
            filteredTypes = typesToFilter;
        }
        return filteredTypes;
    }
    
    
    /**
     * Processes the user-defined list of types into valid QNames and models, it validates them
     * against the dictionary and also supports wildcards
     */
    protected void preprocessExcludedTypes(List<String> excludeTypeNames)
    {
        if (excludeTypeNames == null || excludeTypeNames.isEmpty()) return;

        Map<QName, Boolean> qNamesToExclude = new HashMap<QName, Boolean>();
        Set<String> modelsToExclude = new HashSet<String>();

        for (String typeDefinition : excludeTypeNames)
        {
            final QName typeDef = QName.createQName(typeDefinition);
            if (WILDCARD.equals(typeDef.getLocalName()))
            {
            	modelsToExclude.add(typeDef.getNamespaceURI());
            }
            else
            {
            	qNamesToExclude.put(typeDef, Boolean.TRUE); // valid so add it to the list
            }
        }

        this.excludedModels = modelsToExclude;
        this.excludedQNames = qNamesToExclude;
    }

    /**
     * Indicates that this QName should be excluded.
     * @param typeQName QName
     * @return boolean true if it is excluded
     */
    public boolean isExcluded(QName typeQName)
    {
    	Boolean isExcluded = excludedQNames.get(typeQName);
        return (isExcluded != null && isExcluded.booleanValue() || excludedModels.contains(typeQName.getNamespaceURI()));
    }
    
    @Override
    public void initFilter()
    {
        if (excludedTypes == null || excludedTypes.isEmpty())
        {
            excludedTypes = listOfHardCodedExcludedTypes();
        }
        preprocessExcludedTypes(excludedTypes);
    }

    public void setExcludedTypes(List<String> excludedTypes)
    {
        this.excludedTypes = excludedTypes;
    }

    /**
     * I don't like hard code values, but have been persuaded its less pain
     * than having to keep a config file in sync with both sides of SOLR.
     * 
     */
    public static List<String> listOfHardCodedExcludedTypes() {
        List<String> hardCodeListOfTypes = new ArrayList<String>();
        
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/imap/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/publishing/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/publishingworkflow/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/publishing/twitter/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/publishing/slideshare/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/publishing/facebook/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/publishing/youtube/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/publishing/linkedin/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/publishing/flickr/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/transfer/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/emailserver/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/calendar}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/blogintegration/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/linksmodel/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/datalist/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/forum/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/cloud/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/bpm/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/workflow/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/workflow/invite/moderated/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/workflow/invite/nominated/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/workflow/cloud/resetpassword/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/workflow/cloud/siteinvitation/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/workflow/signup/selfsignup/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/versionstore/2.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/versionstore/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/action/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/application/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/rule/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/rendition/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/qshare/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/sync/1.0}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/content/1.0}thumbnailed");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/content/1.0}failedThumbnailSource");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/cmis/custom}*");
        hardCodeListOfTypes.add("{http://www.alfresco.org/model/hybridworkflow/1.0}*");
        return hardCodeListOfTypes;
    }

	@Override
	public void setExcluded(QName typeQName, boolean excluded)
	{
		excludedQNames.put(typeQName, Boolean.valueOf(excluded));
	}

}
