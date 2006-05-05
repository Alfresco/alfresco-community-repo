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
package org.alfresco.jcr.item;

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.service.cmr.repository.ContentReader;


/**
 * Alfresco implementation of JCR Value
 * 
 * @author David Caruana
 */
public class ValueImpl implements Value
{
    private enum ValueState {Stream, Value, None};
    private ValueState state = ValueState.None;
    
    private SessionImpl session;
    private int datatype;
    private Object value;
    private InputStream stream = null;
    
    private Value proxy;
    

    /**
     * Constuct
     * 
     * @param value  value to wrap
     */
    public ValueImpl(SessionImpl session, int datatype, Object value)
    {
        this.session = session;
        this.datatype = datatype;
        this.value = value;
    }
    
    /**
     * Create a proxied JCR Value
     * 
     * @return  the proxied value
     */
    public Value getProxy()
    {
        if (proxy == null)
        {
            proxy = (Value)JCRProxyFactory.create(this, Value.class, session); 
        }
        return proxy;
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.Value#getString()
     */
    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException
    {
        isValidState(ValueState.Value);
        String typedValue = session.getTypeConverter().stringValue(getInternalValue());
        value = typedValue;
        enterState(ValueState.Value);
        return typedValue;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Value#getStream()
     */
    public InputStream getStream() throws IllegalStateException, RepositoryException
    {
        isValidState(ValueState.Stream);
        if (stream == null)
        {
            stream = session.getTypeConverter().streamValue(value);
        }
        enterState(ValueState.Stream);
        return stream;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Value#getLong()
     */
    public long getLong() throws ValueFormatException, IllegalStateException, RepositoryException
    {
        isValidState(ValueState.Value);
        long typedValue = session.getTypeConverter().longValue(getInternalValue());
        value = typedValue;
        enterState(ValueState.Value);
        return typedValue;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Value#getDouble()
     */
    public double getDouble() throws ValueFormatException, IllegalStateException, RepositoryException
    {
        isValidState(ValueState.Value);
        double typedValue = session.getTypeConverter().doubleValue(getInternalValue());
        value = typedValue;
        enterState(ValueState.Value);
        return typedValue;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Value#getDate()
     */
    public Calendar getDate() throws ValueFormatException, IllegalStateException, RepositoryException
    {
        isValidState(ValueState.Value);
        Calendar typedValue = session.getTypeConverter().dateValue(getInternalValue());
        value = typedValue.getTime();
        enterState(ValueState.Value);
        return typedValue;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Value#getBoolean()
     */
    public boolean getBoolean() throws ValueFormatException, IllegalStateException, RepositoryException
    {
        isValidState(ValueState.Value);
        boolean typedValue = session.getTypeConverter().booleanValue(getInternalValue());
        value = typedValue;
        enterState(ValueState.Value);
        return typedValue;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Value#getType()
     */
    public int getType()
    {
        return datatype;
    }
    
    /**
     * Get value
     * 
     * @param value  the value wrapper to extract from
     * @return  the value
     */
    public static Object getValue(Value value) throws RepositoryException
    {
        Object objValue = null;
        int valueType = value.getType();
        
        switch(valueType)
        {
            case PropertyType.STRING:
            case PropertyType.NAME:
            case PropertyType.PATH:
                objValue = value.getString();
                break;
            case PropertyType.LONG:
                objValue = value.getLong();
                break;
            case PropertyType.DOUBLE:
                objValue = value.getDouble();
                break;
            case PropertyType.BOOLEAN:
                objValue = value.getBoolean();
                break;
            case PropertyType.DATE:
                objValue = value.getDate();
                break;
            case PropertyType.BINARY:
                objValue = value.getStream();
                break;
            default:
                // Note: just take the internal value
                objValue = ((ValueImpl)value).value;
                break;
        }
        
        return objValue;
    }
    
    /**
     * Get typed value 
     * 
     * @param value  the value to extract from
     * @return  the wrapped object
     */
    public static Object getValue(JCRTypeConverter typeConverter, int requiredType, Value value) throws RepositoryException
    {
        Object objValue = null;
        
        switch(requiredType)
        {
            case PropertyType.STRING:
                objValue = value.getString();
                break;
            case PropertyType.LONG:
                objValue = value.getLong();
                break;
            case PropertyType.DOUBLE:
                objValue = value.getDouble();
                break;
            case PropertyType.BOOLEAN:
                objValue = value.getBoolean();
                break;
            case PropertyType.DATE:
                objValue = value.getDate();
                break;
            case PropertyType.BINARY:
                objValue = value.getStream();
                break;
            case PropertyType.NAME:
                objValue = typeConverter.nameValue(ValueImpl.getValue(value));
                break;
            case PropertyType.PATH:
                objValue = typeConverter.pathValue(ValueImpl.getValue(value));
                break;
            default:
                throw new ValueFormatException("Unsupported Value Type " + requiredType);
        }
        
        return objValue;
    }
    
    /**
     * Retrieve Value
     * 
     * Note: When retrieving non stream values against a backed stream, the content reader
     *       has to be re-created.
     * 
     * @return  the value
     */
    private Object getInternalValue()
    {
        if (value instanceof ContentReader && state == ValueState.Value)
        {
            value = ((ContentReader)value).getReader();
        }
        return value;
    }
    
    /**
     * Check for valid state
     * 
     * @param state  the state to check
     * @throws IllegalStateException  state is not valid
     */
    private void isValidState(ValueState state)
    {
        if (this.state != ValueState.None && this.state != state)
        {
            throw new IllegalStateException("This value has already been retrieved as a " + state + " and cannot be retrieved as a " + ValueState.Stream + ".");
        }
    }
    
    /**
     * Enter state
     * 
     * @param state  the state to enter
     */
    private void enterState(ValueState state)
    {
        this.state = state;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof ValueImpl))
        {
            return false;
        }
        ValueImpl other = (ValueImpl)obj;

        // check data type first
        if (datatype != other.datatype)
        {
            return false;
        }
        
        // handle case where values are content streams
        if (value instanceof ContentReader)
        {
            String thisUrl = ((ContentReader)value).getContentUrl();
            String otherUrl = ((ContentReader)other).getContentUrl();
            return thisUrl.equals(otherUrl);
        }

        // handle other value types
        return value.equals(other.value);
    }

    @Override
    public int hashCode()
    {
        return value.hashCode() * 32 + datatype;
    }

}
