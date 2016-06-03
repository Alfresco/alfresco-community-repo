package org.alfresco.repo.transfer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.util.ISO9075;

public class PathHelper
{
    /**
     * Converts a String representation of a path to a Path
     * 
     * e.g "/{http://www.alfresco.org/model/application/1.0}company_home/{http://www.alfresco.org/model/application/1.0}dictionary/{http://www.alfresco.org/model/application/1.0}transfers/{http://www.alfresco.org/model/content/1.0}default/{http://www.alfresco.org/model/transfer/1.0}snapshotMe";
     * @param value the string representation of the path.
     * @return Path 
     */
    public static Path stringToPath(String value)
    {
        Path path = new Path();
        
        // pattern for QName e.g. /{stuff}stuff
        
        Pattern pattern = Pattern.compile("/\\{[a-zA-Z:./0-9]*\\}[^/]*");
        Matcher matcher = pattern.matcher(value);
        
        // This is the root node
        path.append(new SimplePathElement("/"));
               
        while ( matcher.find() )
        {
            String group = matcher.group();
            final String val = ISO9075.decode(group.substring(1));
            path.append(new SimplePathElement(val));
        }
        
        return path;
    }
    
    
    private static class SimplePathElement extends Path.Element {
        private static final long serialVersionUID = -5243552616345217924L;
        private String elementString;
        
        public SimplePathElement(String elementString) 
        {
            this.elementString = elementString;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.Path.Element#getElementString()
         */
        @Override
        public String getElementString()
        {
            return elementString;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.repository.Path.Element#getBaseNameElement(org.alfresco.repo.tenant.TenantService)
         */
        @Override
        public Element getBaseNameElement(TenantService tenantService)
        {
           return new SimplePathElement(elementString);
        }
        
    }

}
