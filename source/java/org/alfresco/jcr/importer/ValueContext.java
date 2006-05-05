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
package org.alfresco.jcr.importer;

import org.alfresco.repo.importer.view.ElementContext;
import org.alfresco.service.namespace.QName;

/**
 * Maintains state about currently imported value
 * 
 * @author David Caruana
 */
public class ValueContext extends ElementContext
{
    private PropertyContext property;

    
    /**
     * Construct
     * 
     * @param elementName
     * @param property
     */
    public ValueContext(QName elementName, PropertyContext property)
    {
        super(elementName, property.getDictionaryService(), property.getImporter());
        this.property = property;
    }

    /**
     * Get property
     * 
     * @return  property holding value
     */
    public PropertyContext getProperty()
    {
        return property;
    }
    
}
