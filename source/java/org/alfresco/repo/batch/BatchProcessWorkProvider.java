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
package org.alfresco.repo.batch;

import java.util.Collection;

/**
 * An interface that provides work loads to the {@link BatchProcessor}.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public interface BatchProcessWorkProvider<T>
{
    /**
     * Get an estimate of the total number of objects that will be provided by this instance.
     * Instances can provide accurate answers on each call, but only if the answer can be
     * provided quickly and efficiently; usually it is enough to to cache the result after
     * providing an initial estimate.
     * 
     * @return                  a total work size estimate
     */
    int getTotalEstimatedWorkSize();
    
    /**
     * Get the next lot of work for the batch processor.  Implementations should return
     * the largest number of entries possible; the {@link BatchProcessor} will keep calling
     * this method until it has enough work for the individual worker threads to process
     * or until the work load is empty.
     * 
     * @return                  the next set of work object to process or an empty collection
     *                          if there is no more work remaining.
     */
    Collection<T> getNextWork();
}
