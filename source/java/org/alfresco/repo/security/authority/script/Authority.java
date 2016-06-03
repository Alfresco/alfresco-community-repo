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
