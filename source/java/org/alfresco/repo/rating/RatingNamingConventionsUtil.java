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

package org.alfresco.repo.rating;

import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * This class encapsulates the naming conventions used in the RatingService.
 * e.g. the naming conventions used for the aspect and for the properties in the content model.
 * <p/>
 * Each rating scheme which has one or more rating property rollups, will lead to the
 * addition of an aspect on to the rated node. This aspect is named:
 * <pre>
 * "cm:" + &lt;ratingSchemeName&gt; + "Rollups"
 * e.g. cm:likesRatingSchemeRollups
 * </pre>
 * Then within that aspect, any rolled up property values will be persisted in a property named:
 * <pre>
 * "cm:" + &lt;ratingSchemeName&gt; + &lt;rollupName&gt;
 * e.g. cm:likesRatingSchemeCount
 * </pre>
 * The ratingSchemeName is the spring bean name of the rating scheme and the rollupName is
 * the rollup name as defined in the algorithm class e.g. {@link RatingCountRollupAlgorithm#ROLLUP_NAME}.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public class RatingNamingConventionsUtil
{
    private static final String RATING_ASSOC_SEPARATOR = "__";
    private NamespaceService namespaceService;
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * This method returns the {@link QName association name} that will be used to link
     * a cm:rateable node to its cm:rating child for the specified username and ratingSchemeName.
     */
    public QName getRatingAssocNameFor(String username, String ratingSchemeName)
    {
        StringBuilder compoundString = new StringBuilder();
        compoundString.append("cm:").append(username).append(RATING_ASSOC_SEPARATOR).append(ratingSchemeName);
        QName result = QName.createQName(compoundString.toString(), namespaceService);
        
        return result;
    }
    
    /**
     * This method returns a QNamePattern for the specified username and ratingSchemeName.
     * This pattern can be used when navigating child-associations looking for cm:rating nodes.
     * 
     * @param username the username to match against or <code>null</code> for all usernames.
     * @param ratingSchemeName the ratingSchemeName to match against or <code>null</code> for all ratingSchemes.
     * @return
     */
    public QNamePattern getRatingAssocPatternForUser(String username, String ratingSchemeName)
    {
        if (username == null)
        {
            username = ".*";
        }
        if (ratingSchemeName == null)
        {
            ratingSchemeName = ".*";
        }
        return new RegexQNamePattern(NamespaceService.CONTENT_MODEL_1_0_URI, username + RATING_ASSOC_SEPARATOR + ratingSchemeName);
    }
    
    /**
     * Given a ratingSchemeName, this method returns the aspect name which would
     * by convention be used to store rating property rollups.
     * 
     * @param ratingSchemeName the ratingSchemeName, which is the spring bean name.
     * @return the aspect name used to store all property rollups for that scheme.
     */
    public QName getRollupAspectNameFor(String ratingSchemeName)
    {
        String result = "cm:" + ratingSchemeName + "Rollups";
        return QName.createQName(result, namespaceService);
    }
    
    /**
     * Given a ratingScheme, this method returns the aspect name which would
     * by convention be used to store rating property rollups.
     * 
     * @param ratingScheme the ratingScheme.
     * @return the aspect name used to store all property rollups for that scheme.
     */
    public QName getRollupAspectNameFor(RatingScheme ratingScheme)
    {
        return getRollupAspectNameFor(ratingScheme.getName());
    }
    
    /**
     * Given a ratingSchemeName and a rollup name, this method returns the property name
     * which would by convention be used to store the given rollup.
     * 
     * @param ratingSchemeName the ratingSchemeName, which is the spring bean name.
     * @param rollupName the name of the property rollup as given by {@link AbstractRatingRollupAlgorithm#getRollupName()}.
     * @return the property name used to persist the given rollup in the given scheme.
     */
    public QName getRollupPropertyNameFor(String ratingSchemeName, String rollupName)
    {
        String result = "cm:" + ratingSchemeName + rollupName;
        return QName.createQName(result, namespaceService);
    }

    /**
     * Given a ratingScheme and a rollup name, this method returns the property name
     * which would by convention be used to store the given rollup.
     * 
     * @param ratingScheme the ratingScheme.
     * @param rollupName the name of the property rollup as given by {@link AbstractRatingRollupAlgorithm#getRollupName()}.
     * @return the property name used to persist the given rollup in the given scheme.
     */
    public QName getRollupPropertyNameFor(RatingScheme ratingScheme, String rollupName)
    {
        return getRollupPropertyNameFor(ratingScheme.getName(), rollupName);
    }
}
