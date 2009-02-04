/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms.script;

import java.io.Serializable;
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
        for (String k : formData.getData().keySet())
        {
            ScriptFieldData wrappedFieldData = new ScriptFieldData(formData.getData().get(k));
            result.put(k, wrappedFieldData);
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
            if (rawResult instanceof List)
            {
                return ((List) rawResult).toArray();
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
