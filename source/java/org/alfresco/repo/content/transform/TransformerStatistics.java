package org.alfresco.repo.content.transform;

import org.alfresco.api.AlfrescoPublicApi;  

/**
 * Interface to obtain the configuration and performance data for every
 * source, target and transformer combination.
 *  
 * @author Alan Davis
 */
@AlfrescoPublicApi
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