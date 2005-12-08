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
package org.alfresco.repo.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

public class EmptyResultSet implements ResultSet
{

    public EmptyResultSet()
    {
        super();
    }

    public Path[] getPropertyPaths()
    {
       return new Path[]{};
    }

    public int length()
    {
        return 0;
    }

    public NodeRef getNodeRef(int n)
    {
        throw new UnsupportedOperationException();
    }

    public float getScore(int n)
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<ResultSetRow> iterator()
    {
        ArrayList<ResultSetRow> dummy = new ArrayList<ResultSetRow>(0);
        return dummy.iterator();
    }

    public void close()
    {

    }

    public ResultSetRow getRow(int i)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public List<NodeRef> getNodeRefs()
    {
        return Collections.<NodeRef>emptyList();
    }

    public List<ChildAssociationRef> getChildAssocRefs()
    {
        return Collections.<ChildAssociationRef>emptyList();
    }

    public ChildAssociationRef getChildAssocRef(int n)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }
}
