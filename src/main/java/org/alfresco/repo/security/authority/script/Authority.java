/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.authority.script;

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.surf.util.I18NUtil;

public interface Authority 
{
    public enum ScriptAuthorityType { GROUP, USER }; 
    public ScriptAuthorityType getAuthorityType();
    public String getShortName();
    public String getFullName();
    public String getDisplayName();
    public Set<String> getZones();

    
    /**
     * Does case insensitive sorting of ScriptGroups and ScriptUsers.
     */
    public static class AuthorityComparator implements Comparator<Authority>
    {
        private Map<Authority,String> nameCache;
        private String sortBy;
        private Collator col;
        private int orderMultiplicator = 1;
        
        public AuthorityComparator(String sortBy)
        {
            this.col = Collator.getInstance(I18NUtil.getLocale());
            this.sortBy = sortBy;
            this.nameCache = new HashMap<Authority, String>();
        }

        public AuthorityComparator(String sortBy, boolean sortAsc)
        {
            col = Collator.getInstance(I18NUtil.getLocale());
            this.sortBy = sortBy;
            this.nameCache = new HashMap<Authority, String>();
            if (!sortAsc)
            {
                orderMultiplicator = -1;
            }
        }        

        @Override
        public int compare(Authority g1, Authority g2)
        {
            return col.compare(get(g1), get(g2)) * orderMultiplicator;
        }
        
        private String get(Authority g)
        {
            String v = nameCache.get(g);
            if(v == null)
            {
                // Get the value from the group
                if("displayName".equals(sortBy))
                {
                    v = g.getDisplayName(); 
                }
                else if("shortName".equals(sortBy))
                {
                    v = g.getShortName();
                }
                else
                {
                    v = g.getFullName(); 
                }
                // Lower case it for case insensitive search
                v = v.toLowerCase();
                // Cache it
                nameCache.put(g, v);
            }
            return v;
        }
    }
}
