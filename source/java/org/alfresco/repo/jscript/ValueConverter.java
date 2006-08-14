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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;


/**
 * Value conversion allowing safe usage of values in Script and Java.
 */
public class ValueConverter
{

    /**
     * Convert an object from any repository serialized value to a valid script object.
     * This includes converting Collection multi-value properties into JavaScript Array objects.
     *
     * @param services  Repository Services Registry
     * @param scope     Scripting scope
     * @param qname     QName of the property value for conversion
     * @param value     Property value
     * 
     * @return Value safe for scripting usage
     */
    public Serializable convertValueForScript(ServiceRegistry services, Scriptable scope, QName qname, Serializable value)
    {
        // perform conversions from Java objects to JavaScript scriptable instances
        if (value == null)
        {
            return null;
        }
        else if (value instanceof NodeRef)
        {
            // NodeRef object properties are converted to new Node objects
            // so they can be used as objects within a template
            value = new Node(((NodeRef)value), services, null, scope);
        }
        else if (value instanceof Date)
        {
            // convert Date to JavaScript native Date object
            // call the "Date" constructor on the root scope object - passing in the millisecond
            // value from the Java date - this will construct a JavaScript Date with the same value
            Date date = (Date)value;
            Object val = ScriptRuntime.newObject(
                    Context.getCurrentContext(), scope, "Date", new Object[] {date.getTime()});
            value = (Serializable)val;
        }
        else if (value instanceof Collection)
        {
            // recursively convert each value in the collection
            Collection<Serializable> collection = (Collection<Serializable>)value;
            Serializable[] array = new Serializable[collection.size()];
            int index = 0;
            for (Serializable obj : collection)
            {
                array[index++] = convertValueForScript(services, scope, qname, obj);
            }
            value = array;
        }
        // simple numbers and strings are wrapped automatically by Rhino
        
        return value;
    }
    
    
    /**
     * Convert an object from any script wrapper value to a valid repository serializable value.
     * This includes converting JavaScript Array objects to Lists of valid objects.
     * 
     * @param value     Value to convert from script wrapper object to repo serializable value
     * 
     * @return valid repo value
     */
    public Serializable convertValueForRepo(Serializable value)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof Node)
        {
            // convert back to NodeRef
            value = ((Node)value).getNodeRef();
        }
        else if (value instanceof Wrapper)
        {
            // unwrap a Java object from a JavaScript wrapper
            // recursively call this method to convert the unwrapped value
            value = convertValueForRepo((Serializable)((Wrapper)value).unwrap());
        }
        else if (value instanceof ScriptableObject)
        {
            // a scriptable object will probably indicate a multi-value property
            // set using a JavaScript Array object
            ScriptableObject values = (ScriptableObject)value;
            
            if (value instanceof NativeArray)
            {
                // convert JavaScript array of values to a List of Serializable objects
                Object[] propIds = values.getIds();
                List<Serializable> propValues = new ArrayList<Serializable>(propIds.length);
                for (int i=0; i<propIds.length; i++)
                {
                    // work on each key in turn
                    Object propId = propIds[i];
                    
                    // we are only interested in keys that indicate a list of values
                    if (propId instanceof Integer)
                    {
                        // get the value out for the specified key
                        Serializable val = (Serializable)values.get((Integer)propId, values);
                        // recursively call this method to convert the value
                        propValues.add(convertValueForRepo(val));
                    }
                }
                value = (Serializable)propValues;
            }
            else
            {
                // TODO: add code here to use the dictionary and convert to correct value type
                Object javaObj = Context.jsToJava(value, Date.class);
                if (javaObj instanceof Serializable)
                {
                    value = (Serializable)javaObj;
                }
            }
        }
        else if (value instanceof Serializable[])
        {
            // convert back a list of Java values
            Serializable[] array = (Serializable[])value;
            ArrayList<Serializable> list = new ArrayList<Serializable>(array.length);
            for (int i=0; i<array.length; i++)
            {
                list.add(convertValueForRepo(array[i]));
            }
            value = list;
        }
        return value;
    }
    
}
