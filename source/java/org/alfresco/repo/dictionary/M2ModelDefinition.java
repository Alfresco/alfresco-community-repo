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

import java.util.Date;

import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Compiled Model Definition
 * 
 * @author David Caruana
 *
 */
public class M2ModelDefinition implements ModelDefinition
{
    private QName name;
    private M2Model model;
    
    
    /*package*/ M2ModelDefinition(M2Model model, NamespacePrefixResolver resolver)
    {
        this.name = QName.createQName(model.getName(), resolver);
        this.model = model;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getName()
     */
    public QName getName()
    {
        return name;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getDescription()
     */
    public String getDescription()
    {
        String value = M2Label.getLabel(this, null, null, "description"); 
        if (value == null)
        {
            value = model.getDescription();
        }
        return value;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getAuthor()
     */
    public String getAuthor()
    {
        return model.getAuthor();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getPublishedDate()
     */
    public Date getPublishedDate()
    {
        return model.getPublishedDate();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getVersion()
     */
    public String getVersion()
    {
        return model.getVersion();
    }
    
}
