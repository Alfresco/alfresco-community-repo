/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.jacksonextensions;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import java.io.IOException;

/**
 * Rest api string deserializer that ensures that empty strings are treated as null strings.
 * This is a copy of {@link StringDeserializer} with null handling
 *
 * @author steveglover
 * @author amukha
 *
 */
public class RestApiStringDeserializer extends StdScalarDeserializer<String>
{
    private static final long serialVersionUID = 1L;

    // @since 2.8.8
    protected final static int FEATURES_ACCEPT_ARRAYS =
            DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS.getMask() |
                    DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT.getMask();

    /**
     * @since 2.2
     */
    public final static StringDeserializer instance = new StringDeserializer();

    public RestApiStringDeserializer() { super(String.class); }

    // since 2.6, slightly faster lookups for this very common type
    @Override
    public boolean isCachable() { return true; }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        String ret = deserializeImpl(p, ctxt);
        // Return null for empty string
        if(ret != null && ret.length() == 0)
        {
            ret = null;
        }
        return ret;
    }

    private String deserializeImpl(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        if (p.hasToken(JsonToken.VALUE_STRING))
        {
            return p.getText();
        }
        JsonToken t = p.getCurrentToken();
        // [databind#381]
        if (t == JsonToken.START_ARRAY) {
            return _deserializeFromArray(p, ctxt);
        }
        // need to gracefully handle byte[] data, as base64
        if (t == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = p.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (ob instanceof byte[]) {
                return ctxt.getBase64Variant().encode((byte[]) ob, false);
            }
            // otherwise, try conversion using toString()...
            return ob.toString();
        }
        // allow coercions for other scalar types
        String text = p.getValueAsString();
        if (text != null) {
            return text;
        }
        return (String) ctxt.handleUnexpectedToken(_valueClass, p);
    }

    // Since we can never have type info ("natural type"; String, Boolean, Integer, Double):
    // (is it an error to even call this version?)
    @Override
    public String deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(p, ctxt);
    }

    // @since 2.8.8
    protected String _deserializeFromArray(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        JsonToken t;
        if (ctxt.hasSomeOfFeatures(FEATURES_ACCEPT_ARRAYS)) {
            t = p.nextToken();
            if (t == JsonToken.END_ARRAY) {
                if (ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
                    return getNullValue(ctxt);
                }
            }
            if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
                final String parsed = _parseString(p, ctxt);
                if (p.nextToken() != JsonToken.END_ARRAY) {
                    handleMissingEndArrayForSingle(p, ctxt);
                }
                return parsed;
            }
        } else {
            t = p.getCurrentToken();
        }
        return (String) ctxt.handleUnexpectedToken(_valueClass, t, p, null);
    }
}