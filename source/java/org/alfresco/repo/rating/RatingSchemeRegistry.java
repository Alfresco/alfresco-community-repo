
package org.alfresco.repo.rating;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.service.cmr.rating.RatingScheme;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class maintains a registry of all known {@link RatingScheme rating schemes} in the system.
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingSchemeRegistry
{
    private static final Log log = LogFactory.getLog(RatingSchemeRegistry.class);
    
    Map<String, RatingScheme> ratingSchemes = new TreeMap<String, RatingScheme>();

    public void register(String name, RatingScheme ratingScheme)
    {
        ratingSchemes.put(name, ratingScheme);
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Registering ")
               .append(ratingScheme);

            log.debug(msg.toString());
        }
    }

    /**
     * This method returns an unmodifiable map of the registered rating schemes.
     * @return Map
     */
    public Map<String, RatingScheme> getRatingSchemes()
    {
        return Collections.unmodifiableMap(ratingSchemes);
    }
}
