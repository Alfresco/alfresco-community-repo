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
