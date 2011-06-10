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

package org.alfresco.repo.forms.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.Item;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for all FormProcessor implementations provides a regex
 * pattern match to test for processor applicability
 * 
 * @author Gavin Cornwell
 */
public abstract class AbstractFormProcessor implements FormProcessor
{
    public static final String DESTINATION = "alf_destination";
    
    private static final Log logger = LogFactory.getLog(AbstractFormProcessor.class);

    protected FormProcessorRegistry processorRegistry;

    protected String matchPattern;

    protected boolean active = true;

    protected Pattern patternMatcher;

    /**
     * Sets the form process registry
     * 
     * @param processorRegistry The FormProcessorRegistry instance
     */
    public void setProcessorRegistry(FormProcessorRegistry processorRegistry)
    {
        this.processorRegistry = processorRegistry;
    }

    /**
     * Sets the match pattern
     * 
     * @param pattern The regex pattern to use to determine if this processor is
     *            applicable
     */
    public void setMatchPattern(String pattern)
    {
        this.matchPattern = pattern;
    }

    /**
     * Sets whether this processor is active
     * 
     * @param active true if the processor should be active
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /**
     * Registers this processor with the processor registry
     */
    public void register()
    {
        if (this.processorRegistry == null)
        {
            if (logger.isWarnEnabled())
                logger.warn("Property 'processorRegistry' has not been set.  Ignoring auto-registration of processor: "
                            + this);

            return;
        }

        if (this.matchPattern == null)
        {
            if (logger.isWarnEnabled())
                logger.warn("Property 'matchPattern' has not been set.  Ignoring auto-registration of processor: "
                            + this);

            return;
        }
        else
        {
            // setup pattern matcher
            this.patternMatcher = Pattern.compile(this.matchPattern);
        }

        // register this instance
        this.processorRegistry.addProcessor(this);
    }

    /*
     * @see org.alfresco.repo.forms.processor.FormProcessor#isActive()
     */
    public boolean isActive()
    {
        return this.active;
    }

    /*
     * @see
     * org.alfresco.repo.forms.processor.FormProcessor#isApplicable(org.alfresco
     * .repo.forms.Item)
     */
    public boolean isApplicable(Item item)
    {
        // this form processor matches if the match pattern provided matches
        // the kind of the item provided

        Matcher matcher = patternMatcher.matcher(item.getKind());
        boolean matches = matcher.matches();

        if (logger.isDebugEnabled())
            logger.debug("Checking processor " + this + " for applicability for item '" + item + "', result = "
                        + matches);

        return matches;
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(super.toString());
        buffer.append(" (");
        buffer.append("active=").append(this.active);
        buffer.append(", matchPattern=").append(this.matchPattern);
        buffer.append(")");
        return buffer.toString();
    }

    /**
     * Gets the Item from the <code>form</code> parameter and sets its type
     * field to <code>type</code>.
     * 
     * @param form
     * @param type
     */
    protected void setFormItemType(Form form, String type)
    {
        form.getItem().setType(type);
    }

    /**
     * Gets the Item from the <code>form</code> parameter and sets its URL field
     * to <code>url</code>.
     * 
     * @param form
     * @param url
     */
    protected void setFormItemUrl(Form form, String url)
    {
        form.getItem().setUrl(url);
    }

}
