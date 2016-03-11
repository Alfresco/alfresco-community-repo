package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* package scope */ final class PivotUtil
{
    private PivotUtil()
    {
        // Will not be called
    }

    static Map<String, List<String>> getPivot(Map<String, List<String>> source)
    {

        Map<String, List<String>> pivot = new HashMap<String, List<String>>();

        for (Map.Entry<String, List<String>> entry : source.entrySet())
        {
            List<String>values = entry.getValue();
            for (String value : values)
            {
                String authority = entry.getKey();
                if (pivot.containsKey(value))
                {
                    // already exists
                    List<String> list = pivot.get(value);
                    list.add(authority );
                }
                else
                {
                    // New value
                    List<String> list = new ArrayList<String>();
                    list.add(authority);
                    pivot.put(value, list);
                }
            }
        }

        return pivot;
    }
}
