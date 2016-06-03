
package org.alfresco.util.schemacomp;

import java.util.Collection;
import java.util.List;

import org.alfresco.util.schemacomp.model.DbObject;

import com.google.gdata.data.extensions.Where;

/**
 * Utilities for comparing data structures in the context of comparing two database schemas.
 * 
 * @author Matt Ward
 */
public interface ComparisonUtils
{
    List<DbObject> findEquivalentObjects(DbObject rootObject, DbObject objToMatch);
    
    
    /**
     * Compare two {@link List}s of 'simple' (i.e. non-{@link DbObject}) objects. Ordering
     * is significant - if an element E appears in both collections but at different indexes
     * then it is not considered to be the same item.
     * 
     * @param leftProperty DbProperty
     * @param rightProperty DbProperty
     * @param ctx DiffContext
     */
    void compareSimpleOrderedLists(DbProperty leftProperty, DbProperty rightProperty, DiffContext ctx);

    /**
     * Compare two collections. Similar to {@link #compareSimpleOrderedLists(DbProperty, DbProperty, DiffContext)}
     * except that this method operates on {@link Collection}s and order (and cardinality) is not important. If
     * an element E from the reference collection appears one or more times at any position in the target collection
     * then that element is said to be {@link org.alfresco.util.schemacomp.Difference.Where#IN_BOTH_NO_DIFFERENCE in both with no difference}.
     * 
     * @param leftProperty DbProperty
     * @param rightProperty DbProperty
     * @param ctx - context
     */
    void compareSimpleCollections(DbProperty leftProperty, DbProperty rightProperty, DiffContext ctx);
    
    /**
     * Compare collections of {@link DbObject}s using their {@link DbObject#diff(DbObject, DiffContext)} method.
     * 
     * @param leftCollection Collection<? extends DbObject>
     * @param rightCollection Collection<? extends DbObject>
     * @param ctx - context
     */
    void compareCollections(Collection<? extends DbObject> leftCollection,
                Collection<? extends DbObject> rightCollection, DiffContext ctx);


    /**
     * Compare two 'simple' (i.e. non-{@link DbObject}) objects using their {@link Object#equals(Object)} method
     * to decide if there is a difference.
     * 
     * @param left DbProperty
     * @param right DbProperty
     * @param ctx - context
     */
    void compareSimple(DbProperty left, DbProperty right, DiffContext ctx);

}