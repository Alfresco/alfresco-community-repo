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
