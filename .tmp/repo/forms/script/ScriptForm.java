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
package org.alfresco.repo.forms.script;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;

/**
 * Form JavaScript Object.
 * 
 * @author Neil Mc Erlean
 */
public class ScriptForm implements Serializable
{
    private static final long serialVersionUID = 579853076546002023L;

    private Form form;
    private Map<String, FieldDefinition> fieldDefinitionData;
    //TODO Consider caching

    /* default */ScriptForm(Form formObject)
    {
        this.form = formObject;
        
        fieldDefinitionData = new HashMap<String, FieldDefinition>();
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        if (fieldDefs != null)
        {
            for (FieldDefinition fd : fieldDefs) 
            {
                fieldDefinitionData.put(fd.getName(), fd);
            }
        }
    }

    public String getItemKind()
    {
        return form.getItem().getKind();
    }
    
    public String getItemId()
    {
        return form.getItem().getId();
    }

    public String getItemType()
    {
        return form.getItem().getType();
    }
    
    public String getItemUrl()
    {
        return form.getItem().getUrl();
    }
    
    public String getSubmissionUrl()
    {
        return form.getSubmissionUrl();
    }

    public FieldDefinition[] getFieldDefinitions()
    {
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        if (fieldDefs == null)
        {
            fieldDefs = Collections.emptyList();
        }
        return fieldDefs.toArray(new FieldDefinition[fieldDefs.size()]);
    }

    public ScriptFormData getFormData()
    {
        return new ScriptFormData(form.getFormData());
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ScriptForm:").append(form.getItem());
        return builder.toString();
    }
}
