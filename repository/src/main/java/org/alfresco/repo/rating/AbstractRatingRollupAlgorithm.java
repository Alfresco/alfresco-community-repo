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

package org.alfresco.repo.rating;

import java.io.Serializable;

import org.springframework.beans.factory.InitializingBean;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.rating.RatingService;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * This class provides the basic implementation of a rating property rollup. By providing an implementation of this class (or reusing an existing one), injecting the object into the {@link org.alfresco.service.cmr.rating.RatingScheme} and following the content model naming conventions, it should be possible to have new rating property rollups automatically calculated and persisted into the Alfresco content model, thereby enabling indexing, searching and sorting of rating-related properties.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
@AlfrescoPublicApi
public abstract class AbstractRatingRollupAlgorithm implements InitializingBean
{
    protected String ratingSchemeName;
    protected NamespaceService namespaceService;
    protected NodeService nodeService;
    protected RatingServiceImpl ratingServiceImpl;

    protected final String rollupName;

    public AbstractRatingRollupAlgorithm(String rollupName)
    {
        this.rollupName = rollupName;
    }

    public void setRatingSchemeName(String ratingScheme)
    {
        this.ratingSchemeName = ratingScheme;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setRatingService(RatingService ratingService)
    {
        this.ratingServiceImpl = (RatingServiceImpl) ratingService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (ratingSchemeName == null)
        {
            throw new RatingServiceException("Illegal null ratingSchemeName in " + this.getClass().getSimpleName());
        }
    }

    public abstract Serializable recalculate(NodeRef ratedNode);

    /**
     * This method returns the rollup name, for example "Total" or "Count". This rollup name is used as part of the convention-based naming of content model property names.
     * 
     * @return String
     */
    public String getRollupName()
    {
        return this.rollupName;
    }
}
