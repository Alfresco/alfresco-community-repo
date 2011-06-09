/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;


/**
 * Value conversion allowing safe usage of values in Script and Java.
 */
public class ValueConverter
{
    private static final String TYPE_DATE = "Date";
    
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
    @SuppressWarnings("unchecked")
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
            value = new ScriptNode(((NodeRef)value), services, scope);
        }
        else if (value instanceof QName || value instanceof StoreRef)
        {
        	   value = value.toString();
        }
        else if (value instanceof ChildAssociationRef)
        {
        	   value = new ChildAssociation(services, (ChildAssociationRef)value, scope);
        }
        else if (value instanceof AssociationRef)
        {
        	   value = new Association(services, (AssociationRef)value, scope);
        }
        else if (value instanceof Date)
        {
            // convert Date to JavaScript native Date object
            // call the "Date" constructor on the root scope object - passing in the millisecond
            // value from the Java date - this will construct a JavaScript Date with the same value
            Date date = (Date)value;
            
            try
            {
            	Context.enter();           
            	Object val = ScriptRuntime.newObject(
                    Context.getCurrentContext(), scope, TYPE_DATE, new Object[] {date.getTime()});
            	value = (Serializable)val;
            }
            finally
            {
            	Context.exit();
            }
        }
        else if (value instanceof Collection)
        {
            // recursively convert each value in the collection
            Collection<Serializable> collection = (Collection<Serializable>)value;
            Object[] array = new Object[collection.size()];
            int index = 0;
            for (Serializable obj : collection)
            {
                array[index++] = convertValueForScript(services, scope, qname, obj);
            }
            try
            {
            	Context.enter();
            	// Convert array to a native JavaScript Array
            	// Note - a scope is usually required for this to work
            	value = (Serializable)Context.getCurrentContext().newArray(scope, array);
            }
            finally
            {
            	Context.exit();
            }
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
        Object converted = convertValueForJava((Object)value);
        return converted instanceof Serializable ? (Serializable)converted : value;
    }
        
    public final Object convertValueForJava(Object value)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof ScriptNode)
        {
            // convert back to NodeRef
            value = ((ScriptNode)value).getNodeRef();
        }
        else if (value instanceof ChildAssociation)
        {
        	   value = ((ChildAssociation)value).getChildAssociationRef();
        }
        else if (value instanceof Association)
        {
        	   value = ((Association)value).getAssociationRef();
        }
        else if (value instanceof Wrapper)
        {
            // unwrap a Java object from a JavaScript wrapper
            // recursively call this method to convert the unwrapped value
            value = convertValueForJava(((Wrapper)value).unwrap());
        }
        else if (value instanceof Scriptable)
        {
            // a scriptable object will probably indicate a multi-value property
            // set using a JavaScript Array object
            Scriptable values = (Scriptable)value;
            
            if (value instanceof IdScriptableObject)
            {
                // TODO: add code here to use the dictionary and convert to correct value type
                if (TYPE_DATE.equals(((IdScriptableObject)value).getClassName()))
                {
                    value = Context.jsToJava(value, Date.class);
                }
                else if (value instanceof NativeArray)
                {
                    // convert JavaScript array of values to a List of objects
                    Object[] propIds = values.getIds();
                    if (isArray(propIds) == true)
                    {                    
                        List<Object> propValues = new ArrayList<Object>(propIds.length);
                        for (int i=0; i<propIds.length; i++)
                        {
                            // work on each key in turn
                            Object propId = propIds[i];
                            
                            // we are only interested in keys that indicate a list of values
                            if (propId instanceof Integer)
                            {
                                // get the value out for the specified key
                                Object val = values.get((Integer)propId, values);
                                // recursively call this method to convert the value
                                propValues.add(convertValueForJava(val));
                            }
                        }

                        value = propValues;
                    }
                    else
                    {
                        Map<Object, Object> propValues = new HashMap<Object, Object>(propIds.length);
                        for (Object propId : propIds)
                        {
                            // Get the value and add to the map
                            Object val = values.get(propId.toString(), values);
                            propValues.put(convertValueForJava(propId), convertValueForJava(val));
                        }
                        
                        value = propValues;
                    }
                }
                else
                {
                    // convert Scriptable object of values to a Map of objects
                    Object[] propIds = values.getIds();
                    Map<String, Object> propValues = new HashMap<String, Object>(propIds.length);
                    for (int i=0; i<propIds.length; i++)
                    {
                        // work on each key in turn
                        Object propId = propIds[i];

                        // we are only interested in keys that indicate a list of values
                        if (propId instanceof String)
                        {
                            // get the value out for the specified key
                            Object val = values.get((String)propId, values);
                            // recursively call this method to convert the value
                            propValues.put((String)propId, convertValueForJava(val));
                        }
                    }
                    value = propValues;
                }
            }
            else
            {
                // convert Scriptable object of values to a Map of objects
                Object[] propIds = values.getIds();
                Map<String, Object> propValues = new HashMap<String, Object>(propIds.length);
                for (int i=0; i<propIds.length; i++)
                {
                    // work on each key in turn
                    Object propId = propIds[i];

                    // we are only interested in keys that indicate a list of values
                    if (propId instanceof String)
                    {
                        // get the value out for the specified key
                        Object val = values.get((String)propId, values);
                        // recursively call this method to convert the value
                        propValues.put((String)propId, convertValueForJava(val));
                    }
                }
                value = propValues;
            }
        }
        else if (value.getClass().isArray())
        {
            // convert back a list of Java values
            int length = Array.getLength(value);
            ArrayList<Object> list = new ArrayList<Object>(length);
            for (int i=0; i<length; i++)
            {
                list.add(convertValueForJava(Array.get(value, i)));
            }
            value = list;
        }
        return value;
    }
    
    /**
     * Look at the id's of a native array and try to determine whether it's actually an Array or a Hashmap
     * 
     * @param ids       id's of the native array
     * @return boolean  true if it's an array, false otherwise (ie it's a map)
     */
    private boolean isArray(Object[] ids)
    {
        boolean result = true;
        for (Object id : ids)
        {
            if (id instanceof Integer == false)
            {
               result = false;
               break;
            }
        }
        return result;
    }
    
}
