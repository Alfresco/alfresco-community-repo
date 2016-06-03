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
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.jscript.ScriptableHashMap;

/**
 * FormData JavaScript Object.
 * 
 * @author Neil McErlean
 */
public class ScriptFormData implements Serializable
{
    private static final long serialVersionUID = 5663057820580831201L;
    private FormData formData;

    
    /* default */ScriptFormData(FormData formObject)
    {
        this.formData = formObject;
    }

    public ScriptableHashMap<String, ScriptFieldData> getData()
    {
        ScriptableHashMap<String, ScriptFieldData> result = new ScriptableHashMap<String, ScriptFieldData>();
        if (this.formData != null)
        {
            for (FieldData fieldData : formData)
            {
                ScriptFieldData wrappedFieldData = new ScriptFieldData(fieldData);
                result.put(fieldData.getName(), wrappedFieldData);
            }
        }
        return result;
    }
    
    public class ScriptFieldData
    {
        private final FieldData wrappedFieldData;
            
        public ScriptFieldData(FieldData fieldData)
        {
            this.wrappedFieldData = fieldData;
        }

        /**
         * Returns the name of the form field that data represents
         * 
         * @return The name
         */
        public String getName()
        {
            return this.wrappedFieldData.getName();
        }

        /**
         * Returns the value of the form field that data represents
         * 
         * @return The value
         */
        @SuppressWarnings("unchecked")
        public Object getValue()
        {
            Object rawResult = wrappedFieldData.getValue();

            // An implementation decision was taken in handling sequence values here.
            //
            // Background:
            // (1) if this method returns instances of java.util.List as is, the data
            // webscript that renders the REST JSON in the repo tier will render a
            // List.toString() for these field values, which works - but see below.
            //
            // However the JavaScript API will then not offer sequences as sequences.
            // Instead they will be Strings (e.g. field.value == "[foo, bar]")
            // and JavaScript client code will have to parse these strings.
            //
            // (2) if this method instead returns listObject.toArray(), then the
            // JavaScript API will see true sequence objects and can easily consume
            // them like so field.value[1] == "bar"
            //
            // However with arrays returned from this method, the webscript that renders
            // the REST JSON in the repo tier cannot handle the array type. (I should say
            // that I am unable at this point to make it handle it.)
            //
            //
            //
            // So should we return List instances or Object[] instances?
            // In order to allow the JSON to be easily rendered, and also to make it
            // easier for client JavaScript code to use sequence values, we are
            // returning our own toString implementation for List instances.
            //
            // So instead of "[foo, bar]" we'll have "foo,bar"
            // This should be very easy for the JavaScript to parse: value.split(",")
            // and it works for the webscript too.

            if (rawResult instanceof List)
            {
                List listValue = (List)rawResult;
                StringBuilder result = new StringBuilder();
                for (Iterator iter = listValue.iterator(); iter.hasNext(); )
                {
                    result.append(iter.next());
                    if (iter.hasNext())
                    {
                        result.append(",");
                    }
                }
                return result.toString();
            } else
            {
                return rawResult;
            }
        }

        /**
         * Determines whether the data represents a file
         * 
         * @return true if the data is a file
         */
        public boolean isFile()
        {
            return this.wrappedFieldData.isFile();
        }
    }
}
