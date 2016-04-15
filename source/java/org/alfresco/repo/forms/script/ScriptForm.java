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
