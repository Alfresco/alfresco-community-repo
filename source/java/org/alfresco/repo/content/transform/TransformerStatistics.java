/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;


/**
 * Interface to obtain the configuration and performance data for every
 * source, target and transformer combination.
 *  
 * @author Alan Davis
 */
public interface TransformerStatistics
{
    /**
     * @return the extension of the source mimetype of the transformer.
     */
    public String getSourceExt();

    /**
     * @return the extension of the target mimetype of the transformer.
     */
    public String getTargetExt();

    /**
     * @return the name of the parent transformer.
     */
    public String getTransformerName();

    /**
     * @return the number of time the transformer has been called.
     */
    public long getCount();

    /**
     * @param count overrides the number of time the transformer has been called.
     */
    public void setCount(long count);

    /**
     * @return the number of time the transformer has failed.
     */
    public long getErrorCount();

    /**
     * @param errorCount overrides the number of time the transformer has failed.
     */
    public void setErrorCount(long errorCount);

    /**
     * @return the average time taken by the the transformer.
     */
    public long getAverageTime();

    /**
     * @param averageTime overrides the average time taken by the the transformer.
     */
    public void setAverageTime(long averageTime);
    
    /**
     * @return <code>true</code> if this is the summary of all transformations done
     *         by a transformer rather than just between two specific mimetypes.
     */
    public boolean isSummary();

    /**
     * @param transformationTime to be added to this TransformationData and its parents.
     */
    public void recordTime(long transformationTime);

    /**
     * Adds 1 to the error count of this TransformationData and its parents.
     */
    public void recordError(long transformationTime);
}