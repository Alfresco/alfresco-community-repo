package org.alfresco.repo.search.impl.querymodel.impl.db;

import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.JoinType;
import org.alfresco.repo.search.impl.querymodel.Source;
import org.alfresco.repo.search.impl.querymodel.impl.BaseJoin;

/**
 * @author Andy
 *
 */
public class DBJoin extends BaseJoin
{

    /**
     * @param left Source
     * @param right Source
     * @param joinType JoinType
     * @param joinConstraint Constraint
     */
    public DBJoin(Source left, Source right, JoinType joinType, Constraint joinConstraint)
    {
        super(left, right, joinType, joinConstraint);
    }

}
