/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.querymodel.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.search.impl.querymodel.Selector;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 */
public class BaseSelector implements Selector
{
    private QName type;

    private String alias;

    public BaseSelector(QName type, String alias)
    {
        this.type = type;
        this.alias = alias;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Selector#getAlias()
     */
    public String getAlias()
    {
        return alias;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Selector#getType()
     */
    public QName getType()
    {
        return type;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseSelector[");
        builder.append("alias=").append(getAlias()).append(", ");
        builder.append("type=").append(getType());
        builder.append("]");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Source#getSelectorNames()
     */
    public Map<String, Selector> getSelectors()
    {
        HashMap<String, Selector> answer = new HashMap<String, Selector>();
        answer.put(getAlias(), this);
        return answer;
    }

}
