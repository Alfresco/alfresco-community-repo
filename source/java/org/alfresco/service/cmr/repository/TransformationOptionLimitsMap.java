/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to create an immutable
 * {@code Map<String, Map<String, TransformationOptionLimits>>}, keyed on source and target mimetypes. Used to
 * define transformation limits, without having to specify lots of spring XML. For example:
 * 
 * <pre>
 * &ltbean id="mimetypeLimits.OpenOffice" class="org.alfresco.service.cmr.repository.TransformationOptionLimitsMap"&gt;
 *    &ltconstructor-arg&gt;
 *       &ltvalue&gt;
 *          *   txt maxSourceSizeKBytes ${content.transformer.OpenOffice.mimeTypeLimits.txt.pdf.maxSourceSizeKBytes} ;
 *          doc pdf maxSourceSizeKBytes ${content.transformer.OpenOffice.mimeTypeLimits.doc.pdf.maxSourceSizeKBytes}
 *       &lt/value&gt;
 *    &lt/constructor-arg&gt;
 *    &ltconstructor-arg&gt;
 *       &ltref bean="mimetypeService" /&gt;
 *    &lt/constructor-arg&gt;
 * &lt/bean&gt;
 * <pre>
 * The first constructor argument is a space separated list of values:
 * <pre>
 * configuration ::= [ &ltsource extension&gt; &lttarget extension&gt; &ltproperty name&gt; &lt;value&gt; ]* ";" ]*
 * property name ::= "maxSourceSizeKBytes" | "readLimitKBytes" | "readLimitTimeMs" | "timeoutMs" | "maxPages" | "pageLimit"
 * </pre>
 * 
 * @author Alan Davis
 */
public class TransformationOptionLimitsMap extends
        AbstractMap<String, Map<String, TransformationOptionLimits>>
{
    private static final Log logger = LogFactory.getLog(TransformationOptionLimitsMap.class);

    private Map<String, Map<String, TransformationOptionLimits>> map = new HashMap<String, Map<String, TransformationOptionLimits>>();

    private enum State
    {
        SOURCE, TARGET, NAME, VALUE
    }

    private List<String> propertyNames = Arrays.asList("maxsourcesizekbytes", "readlimitkbytes",
            "readlimittimems", "timeoutms", "maxpages", "pagelimit");

    /**
     * Constructor. The {@code value} parameter takes a space separated list of
     * values that define the map. See the class descriptor.
     * 
     * @param values
     *            the configuration.
     * @param mimetypeService
     *            used to lookup mimetypes.
     */
    public TransformationOptionLimitsMap(String values, MimetypeService mimetypeService)
    {
        String sourceMimetype = null;
        String targetMimetype = null;
        TransformationOptionLimits limits = null;
        String name = null;
        long value;
        int entry = 1;
        State state = State.SOURCE;
        
        
        String[] bits = values.split("  *");
        mimetypeService.getMimetypesByExtension();
        for (String bit: bits)
        {
            // ignore white space such as new lines
            bit = bit.trim();
            if (bit.length() == 0)
            {
                continue;
            }            
            
            switch(state)
            {
            case SOURCE:
                sourceMimetype = bit.equals("*") ? bit : mimetypeService.getMimetype(bit);
                if (sourceMimetype == null)
                {
                    logger.error("Entry "+entry+" invalid source extension '"+bit+"' - ignored");
                }
                else
                {
                    state = State.TARGET;
                }
                break;
            case TARGET:
                targetMimetype = bit.equals("*") ? bit : mimetypeService.getMimetype(bit);
                if (targetMimetype == null)
                {
                    logger.error("Entry "+entry+" invalid target extension '"+bit+"' - ignored");
                }
                else
                {
                    limits = new TransformationOptionLimits();
                    state = State.NAME;
                }
                break;
            case NAME:
                if (";".equals(bit))
                {
                    add(sourceMimetype, targetMimetype, limits);
                    entry++;
                    state = State.SOURCE;
                }
                else
                {
                    if (propertyNames.contains(bit.toLowerCase()))
                    {
                        name = bit.toLowerCase();
                        state = State.VALUE;
                    }
                    else
                    {
                        logger.error("Entry " + entry + " invalid property name '" + bit
                                + "' - ignored");
                    }
                }
                break;
            case VALUE:
                try
                {
                    value = Long.parseLong(bit);
                    if (name.equalsIgnoreCase("maxSourceSizeKBytes"))
                    {
                        limits.setMaxSourceSizeKBytes(value);
                    }
                    else if (name.equalsIgnoreCase("readLimitKBytes"))
                    {
                        limits.setReadLimitKBytes(value);
                    }
                    else if (name.equalsIgnoreCase("readLimitTimeMs"))
                    {
                        limits.setReadLimitTimeMs(value);
                    }
                    else if (name.equalsIgnoreCase("timeoutMs"))
                    {
                        limits.setTimeoutMs(value);
                    }
                    else if (value > Integer.MAX_VALUE)
                    {
                        logger.error("Entry "+entry+" invalid property name '"+bit+"' - skipping property and value");
                    }
                    else if (name.equalsIgnoreCase("maxPages"))
                    {
                        limits.setMaxPages((int)value);
                    }
                    else if (name.equalsIgnoreCase("pageLimit"))
                    {
                        limits.setPageLimit((int)value);
                    }
                }
                catch (NumberFormatException e)
                {
                    logger.error("Entry " + entry + " invalid value '" + bit + "' - skipping property and value");
                }
                state = State.NAME;
                break;
            }
        }
        // Optional last ;
        if (state == State.NAME)
        {
            add(sourceMimetype, targetMimetype, limits);
        }
        else if (state != State.SOURCE)
        {
            logger.error("Entry " + entry + " is incomplete.");
        }
    }

    private void add(String sourceMimetype, String targetMimetype, TransformationOptionLimits limits)
    {
        Map<String, TransformationOptionLimits> map2 = map.get(sourceMimetype);
        if (map2 == null)
        {
            map2 = new HashMap<String, TransformationOptionLimits>();
            map.put(sourceMimetype, map2);
        }
        map2.put(targetMimetype, limits);
    }

    @Override
    public Set<Map.Entry<String, Map<String, TransformationOptionLimits>>> entrySet()
    {
        return map.entrySet();
    }
}
