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
package org.alfresco.repo.search.results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

public class DetachedResultSet extends AbstractResultSet
{
    List<ResultSetRow> rows = null;
    
    public DetachedResultSet(ResultSet resultSet, Path[] propertyPaths)
    {
        super(propertyPaths);
        rows = new ArrayList<ResultSetRow>(resultSet.length());
        for (ResultSetRow row : resultSet)
        {
            rows.add(new DetachedResultSetRow(this, row));
        }
    }

    public int length()
    {
        return rows.size();
    }

    public NodeRef getNodeRef(int n)
    {
        return rows.get(n).getNodeRef();
    }

    public ResultSetRow getRow(int i)
    {
        return rows.get(i);
    }

    public Iterator<ResultSetRow> iterator()
    {
       return rows.iterator();
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        return rows.get(n).getChildAssocRef();
    }

}
