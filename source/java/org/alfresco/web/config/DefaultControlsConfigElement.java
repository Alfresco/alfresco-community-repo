/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.web.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.config.ConfigElement;
import org.alfresco.config.ConfigException;
import org.alfresco.config.element.ConfigElementAdapter;

/**
 * Custom config element that represents &lt;default-controls&gt; values for the
 * client.
 * 
 * @author Neil McErlean.
 */
public class DefaultControlsConfigElement extends ConfigElementAdapter
{
    public static final String CONFIG_ELEMENT_ID = "default-controls";

    private final Map<String, DefaultControl> datatypeDefCtrlMappings = new HashMap<String, DefaultControl>();
    private static final long serialVersionUID = -6758804774427314050L;

    /**
     * This constructor creates an instance with the default name.
     */
    public DefaultControlsConfigElement()
    {
        super(CONFIG_ELEMENT_ID);
    }

    /**
     * This constructor creates an instance with the specified name.
     * 
     * @param name the name for the ConfigElement.
     */
    public DefaultControlsConfigElement(String name)
    {
        super(name);
    }

    /**
     * @see org.alfresco.config.ConfigElement#getChildren()
     */
    public List<ConfigElement> getChildren()
    {
        throw new ConfigException(
                "Reading the default-controls config via the generic interfaces is not supported");
    }

    /**
     * @see org.alfresco.config.ConfigElement#combine(org.alfresco.config.ConfigElement)
     */
    public ConfigElement combine(ConfigElement configElement)
    {
        // There is an assumption here that it is only like-with-like
        // combinations
        // that are allowed. i.e. Only an instance of a
        // DefaultControlsConfigElement
        // can be combined with this.
        DefaultControlsConfigElement otherDCCElement = (DefaultControlsConfigElement) configElement;

        DefaultControlsConfigElement result = new DefaultControlsConfigElement();

        for (String nextDataType : datatypeDefCtrlMappings.keySet())
        {
            String nextTemplate = getTemplateFor(nextDataType);
            DefaultControl nextDefaultControls = otherDCCElement.datatypeDefCtrlMappings
                    .get(nextDataType);
            List<ControlParam> nextControlParams = nextDefaultControls
                    .getControlParams();

            result.addDataMapping(nextDataType, nextTemplate,
                            nextControlParams);
        }

        for (String nextDataType : otherDCCElement.datatypeDefCtrlMappings.keySet())
        {
            String nextTemplate = otherDCCElement.getTemplateFor(nextDataType);
            DefaultControl nextDefaultControls = otherDCCElement.datatypeDefCtrlMappings
                    .get(nextDataType);
            List<ControlParam> nextControlParams = nextDefaultControls
                    .getControlParams();

            result.addDataMapping(nextDataType, nextTemplate, nextControlParams);
        }

        return result;
    }

    /* package */void addDataMapping(String dataType, String template,
            List<ControlParam> parameters)
    {
        if (parameters == null)
        {
            parameters = Collections.emptyList();
        }
        DefaultControl newControl = new DefaultControl(dataType, template);
        for (ControlParam p : parameters)
        {
            newControl.addControlParam(p);
        }
        this.datatypeDefCtrlMappings.put(dataType, newControl);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return datatypeDefCtrlMappings.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object otherObj)
    {
        if (otherObj == null || !otherObj.getClass().equals(this.getClass()))
        {
            return false;
        }
        DefaultControlsConfigElement otherDCCE = (DefaultControlsConfigElement) otherObj;
        return this.datatypeDefCtrlMappings
                .equals(otherDCCE.datatypeDefCtrlMappings);
    }

    /**
     * This method returns an unmodifiable Set of the names of the default-control items.
     * @return Set of names.
     */
    public Set<String> getNames()
    {
        Set<String> result = new HashSet<String>(datatypeDefCtrlMappings.size());
        for (String dataType : datatypeDefCtrlMappings.keySet())
        {
            result.add(dataType);
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * This method returns a String representing the path of the template associated
     * with the given dataType.
     * 
     * @param dataType the dataType for which a template is required.
     * @return the path of the associated template. <code>null</code> if the specified
     *         dataType is <code>null</code> or if there is no registered template.
     */
    public String getTemplateFor(String dataType)
    {
        DefaultControl ctrl = datatypeDefCtrlMappings.get(dataType);
        if (ctrl == null)
        {
            return null;
        }
        else
        {
            return ctrl.getTemplate();
        }
    }

    /**
     * This method returns an unmodifiable List of <code>ControlParam</code> objects
     * associated with the specified dataType.
     * 
     * @param dataType the dataType for which control-params are required.
     * @return an unmodifiable List of the associated <code>ControlParam</code> objects.
     * 
     * @see org.alfresco.web.config.DefaultControlsConfigElement.ControlParam
     */
    public List<ControlParam> getControlParamsFor(String dataType)
    {
        return Collections.unmodifiableList(datatypeDefCtrlMappings.get(
                dataType).getControlParams());
    }

    /**
     * This class represents a single default-control configuration item within a
     * group of &lt;default-controls&gt;.
     * 
     * @author Neil McErlean.
     */
    public static class DefaultControl
    {
        private final String name;
        private final String template;
        private final List<ControlParam> controlParams = new ArrayList<ControlParam>();

        /**
         * Constructs a DefaultControl object with the specified name and template.
         * 
         * @param name the name of the type.
         * @param template the template associated with that name.
         */
        public DefaultControl(String name, String template)
        {
            this.name = name;
            this.template = template;
        }

        void addControlParam(ControlParam param)
        {
            controlParams.add(param);
        }

        /**
         * This method returns the name of the type of this DefaultControl.
         * @return the name of the type.
         */
        public String getName()
        {
            return name;
        }

        /**
         * This method returns the template path of this DefaultControl.
         * @return the template path.
         */
        public String getTemplate()
        {
            return template;
        }

        /**
         * This method returns an unmodifiable List of <code>ControlParam</code>
         * objects that are associated with this DefaultControl.
         * @return an unmodifiable List of ControlParam references.
         */
        public List<ControlParam> getControlParams()
        {
            return Collections.unmodifiableList(controlParams);
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return name.hashCode() + 7 * template.hashCode() + 13
                    * controlParams.size();
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object otherObj)
        {
            if (otherObj == this)
            {
                return true;
            }
            else if (otherObj == null
                    || !otherObj.getClass().equals(this.getClass()))
            {
                return false;
            }
            DefaultControl otherDC = (DefaultControl) otherObj;
            return otherDC.name.equals(this.name)
                    && otherDC.template.equals(this.template)
                    && otherDC.controlParams.equals(this.controlParams);
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            StringBuilder result = new StringBuilder();
            result.append(name).append(":").append(template);
            result.append(controlParams);
            return result.toString();
        }
    }

    /**
     * This class represents a single control-param configuration item.
     * 
     * @author Neil McErlean.
     */
    public static class ControlParam
    {
        private final String name;
        private final String value;

        /**
         * Constructs a ControlParam object with the specified name and value.
         * 
         * @param name the name of the param.
         * @param value the value associated with that name.
         */
        public ControlParam(String name, String value)
        {
            this.name = name;
            this.value = value;
        }

        /**
         * Gets the name of this ControlParam.
         * @return the param name.
         */
        public String getName()
        {
            return name;
        }

        /**
         * Gets the value of this ControlParam.
         * @return the value.
         */
        public String getValue()
        {
            return value;
        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            StringBuilder result = new StringBuilder();
            result.append(name).append(":").append(value);
            return result.toString();
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return name.hashCode() + 7 * value.hashCode();
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object otherObj)
        {
            if (otherObj == this)
            {
                return true;
            }
            else if (otherObj == null
                    || !otherObj.getClass().equals(this.getClass()))
            {
                return false;
            }
            ControlParam otherCP = (ControlParam) otherObj;
            return otherCP.name.equals(this.name)
                    && otherCP.value.equals(this.value);
        }
    }
}
