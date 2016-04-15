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
package org.alfresco.repo.solr;

import org.alfresco.service.namespace.QName;

/**
 * Represents a diff between the set of current repository Alfresco models and the set maintained in SOLR.
 * The diff can represent a new, changed or removed Alfresco model. For a new model the newChecksum is
 * populated; for a changed model both checksums are populated; for a removed model neither checksum is populated.
 * 
 * @since 4.0
 */
public class AlfrescoModelDiff
{
    public static enum TYPE
    {
        NEW, CHANGED, REMOVED;
    };
    
    private String modelName;
    private TYPE type;
    private Long oldChecksum;
    private Long newChecksum;

    /**
     * use full model name or it will be converted to the prefix form - as we are requesting the model it may not be on the other side - so the namespace is unknown.
     * @param modelName String
     * @param type TYPE
     * @param oldChecksum Long
     * @param newChecksum Long
     */
    public AlfrescoModelDiff(String modelName, TYPE type, Long oldChecksum, Long newChecksum)
    {
        super();
        this.modelName = modelName;
        this.type = type;
        this.oldChecksum = oldChecksum;
        this.newChecksum = newChecksum;
    }
    
    public AlfrescoModelDiff(QName modelName, TYPE type, Long oldChecksum, Long newChecksum)
    {
       this(modelName.toString(), type, oldChecksum, newChecksum);
    }

    public String getModelName()
    {
        return modelName;
    }

    public TYPE getType()
    {
        return type;
    }

    public Long getOldChecksum()
    {
        return oldChecksum;
    }

    public Long getNewChecksum()
    {
        return newChecksum;
    }
}
