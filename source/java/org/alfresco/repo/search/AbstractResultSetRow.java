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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;

public abstract class AbstractResultSetRow implements ResultSetRow
{

    /**
     * The containing result set
     */
    private ResultSet resultSet;

    /**
     * The current position in the containing result set
     */
    private int index;

    /**
     * The direct properties of the current node
     * Used by those implementations that can cache the whole set.
     */

    private Map<Path, Serializable> properties;

    public AbstractResultSetRow(ResultSet resultSet, int index)
    {
        super();
        this.resultSet = resultSet;
        this.index = index;
    }

    public ResultSet getResultSet()
    {
        return resultSet;
    }

    public int getIndex()
    {
        return index;
    }

    public NodeRef getNodeRef()
    {
        return getResultSet().getNodeRef(getIndex());
    }

    public float getScore()
    {
        return getResultSet().getScore(getIndex());
    }

    public Map<Path, Serializable> getValues()
    {
        if (properties == null)
        {
            properties = new HashMap<Path, Serializable>();
            setProperties(getDirectProperties());
        }
        return Collections.unmodifiableMap(properties);
    }

    protected Map<QName, Serializable> getDirectProperties()
    {
        return Collections.<QName, Serializable>emptyMap();
    }
    
    protected void setProperties(Map<QName, Serializable> byQname)
    {
        for (QName qname : byQname.keySet())
        {
            Serializable value = byQname.get(qname);
            Path path = new Path();
            path.append(new Path.SelfElement());
            path.append(new Path.AttributeElement(qname));
            properties.put(path, value);
        }
    }
    
    public Serializable getValue(QName qname)
    {
        Path path = new Path();
        path.append(new Path.SelfElement());
        path.append(new Path.AttributeElement(qname));
        return getValues().get(path);
    }
    
}
