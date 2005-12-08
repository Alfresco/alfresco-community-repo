/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search.impl.lucene;

import org.alfresco.repo.search.AbstractResultSetRowIterator;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * Iterate over the rows in a LuceneResultSet
 * 
 * @author andyh
 * 
 */
public class LuceneResultSetRowIterator extends AbstractResultSetRowIterator
{
    /**
     * Create an iterator over the result set. Follows standard ListIterator
     * conventions
     * 
     * @param resultSet
     */
    public LuceneResultSetRowIterator(LuceneResultSet resultSet)
    {
        super(resultSet);
    }

    public ResultSetRow next()
    {
        return new LuceneResultSetRow((LuceneResultSet)getResultSet(), moveToNextPosition());
    }

    public ResultSetRow previous()
    {
        return new LuceneResultSetRow((LuceneResultSet)getResultSet(), moveToPreviousPosition());
    }
}
