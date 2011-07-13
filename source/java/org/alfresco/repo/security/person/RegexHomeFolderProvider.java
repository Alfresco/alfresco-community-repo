/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileNameValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation that returns a tree structure for a home folder based on a property (typically userName)
 * from the supplied person. The parent folder names are derived from regular expression groups matched
 * against the property value. The final folder name is the full property value.<p>
 * 
 * For example, given the value "adavis" and the regular expression <tt>"^(..)"</tt> the
 * resulting home folder path would be {@code "/ad/adavis"}. However with the regular expression
 * <tt>"^(.)(.?)"</tt> the home folder path would be {@code "/a/d/adavis"}. If any group matches a zero
 * length string, it is just ignored.<p>
 * 
 * Note: In order to choose an efficient distribution scheme, be aware that, when m users are
 * distributed into n leaf folders, when m >> n log n the statistical maximum load is
 * m/n + O( sqrt((m log n)/n)), w.h.p
 * 
 * @author Romain Guinot, Alan Davis
 */
public class RegexHomeFolderProvider extends UsernameHomeFolderProvider
{
    private static Log logger = LogFactory.getLog(RegexHomeFolderProvider.class);
    
    private QName propertyName;
    private Pattern pattern;
    private List<Integer> groupOrder;
    
    /**
     * @param propertyName String the cm:person property used as the key, such as userName
     *        or organizationId.
     */
    public void setPropertyName(String propertyName)
    {
        this.propertyName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, propertyName);
    }

    /**
     * @param patternString the regex pattern against the cm:person property value. Regex
     *        groups define the parent folder structure.
     */
    public void setPattern(String patternString)
    {
        pattern = getPattern(patternString);
    }

    /**
     * @param groupOrderString String the order (as a comma separated list) in which the
     *        regex pattern groups should be assembled into folders (such as {@code 2,1}).
     *        The default ordering is as they appear.
     */
    public void setGroupOrder(String groupOrderString)
    {
        groupOrder = getGroupOrder(groupOrderString);
    }

    private Pattern getPattern(String patternString)
    {
        if (patternString == null || patternString.trim().length() == 0)
            return null;
        
        Pattern pattern;
        try
        {
            pattern = Pattern.compile(patternString);
            logger.debug("Successfully compiled patternString : " + patternString);
        } catch (PatternSyntaxException pse)
        {
            throw new PersonException("Pattern string :" + patternString + " does not compile", pse);
        }
        return pattern;
    }

    private List<Integer> getGroupOrder(String groupOrderString)
    {
        if (groupOrderString == null || groupOrderString.trim().length() == 0)
            return Collections.emptyList();
        
        String[] groupOrderStrings = groupOrderString.split(",");
        List<Integer>groupOrder = new ArrayList<Integer>(groupOrderStrings.length);
        for (String group : groupOrderStrings)
        {       
            Integer i;
            try
            {
                i = Integer.valueOf(group);
            }
            catch (NumberFormatException nfe)
            {
                throw new PersonException("groupOrdering value " + groupOrderString + " is invalid.", nfe);
            }
            if (groupOrder.contains(i) || i < 0)
            {
                throw new PersonException("groupOrdering value " + groupOrderString + " is invalid.");
            }
            groupOrder.add(i);
        }
        return groupOrder;
    }
    
    @Override
    public List<String> getHomeFolderPath(NodeRef person)
    {
        List<String> path = new ArrayList<String>();
        String key = FileNameValidator.getValidFileName(
                getHomeFolderManager().getPersonProperty(person, propertyName));
        if (pattern != null)
        {
            Matcher matcher = pattern.matcher(key);

            if (matcher.find())
            {
                int groupCount = matcher.groupCount();
                if (!groupOrder.isEmpty())
                {
                    for (int group : groupOrder)
                    {
                        if (group > groupCount)
                        {
                            throw new PersonException("groupOrdering value "
                                    + group + " is out of range.");
                        }
                        addFolderToPath(path, matcher, group);
                    }
                }
                else // "natural" group ordering, i.e as they appear in the regex
                {
                    for (int group = 1; group <= groupCount; group++)
                    {
                        addFolderToPath(path, matcher, group);
                    }
                }
            }
        }
        path.add(key);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("returning "+path+" for key: "+key);
        }
        
        return path;
    }

    private void addFolderToPath(List<String> path, Matcher matcher, int group)
    {
        String folder = matcher.group(group);
        if (folder.length() > 0)
        {
            path.add(folder);
        }
    }
}
