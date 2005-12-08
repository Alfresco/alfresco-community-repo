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
package org.alfresco.repo.dictionary;

import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;


/**
 * Compiled Association Definition.
 * 
 * @author David Caruana
 */
/*package*/ class M2ChildAssociationDefinition extends M2AssociationDefinition
    implements ChildAssociationDefinition
{

    /**
     * Construct
     * @param classDef  class definition
     * @param assoc  child assocation
     * @param resolver  namespace resolver
     */
    /*package*/ M2ChildAssociationDefinition(ClassDefinition classDef, M2ChildAssociation assoc, NamespacePrefixResolver resolver)
    {
        super(classDef, assoc, resolver);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ChildAssociationDefinition#getRequiredChildName()
     */
    public String getRequiredChildName()
    {
        return ((M2ChildAssociation)getM2Association()).getRequiredChildName();
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ChildAssociationDefinition#getDuplicateChildNamesAllowed()
     */
    public boolean getDuplicateChildNamesAllowed()
    {
        return ((M2ChildAssociation)getM2Association()).allowDuplicateChildName();
    }
        
}
