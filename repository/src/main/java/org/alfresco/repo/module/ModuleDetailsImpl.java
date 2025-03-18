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
package org.alfresco.repo.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.springframework.extensions.surf.util.ISO8601DateFormat;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.module.tool.LogOutput;
import org.alfresco.service.cmr.module.ModuleDependency;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleInstallState;
import org.alfresco.util.Pair;
import org.alfresco.util.VersionNumber;

/**
 * Module details implementation.
 * 
 * Loads details from the serialized properties file provided.
 * 
 * @author Roy Wetherall 
 */
/**
 * @author Derek Hulley
 */
public class ModuleDetailsImpl implements ModuleDetails
{
    private static final long serialVersionUID = 5782747774317351424L;

    private String id;
    private List<String> aliases;
    private ModuleVersionNumber version;
    private String title;
    private String description;
    private List<String> editions;
    private VersionNumber repoVersionMin;
    private VersionNumber repoVersionMax;
    private List<ModuleDependency> dependencies;
    private Date installDate;
    private ModuleInstallState installState;
    private LogOutput log;

    /**
     * Private constructor to set default values.
     */
    private ModuleDetailsImpl()
    {
        aliases = new ArrayList<String>(0);
        repoVersionMin = VersionNumber.VERSION_ZERO;
        repoVersionMax = VersionNumber.VERSION_BIG;
        dependencies = new ArrayList<ModuleDependency>(0);
        this.installState = ModuleInstallState.UNKNOWN;
    }

    /**
     * Creates the instance from a set of properties. All the property values are trimmed and empty string values are removed from the set. In other words, zero length or whitespace strings are not supported.
     * 
     * @param properties
     *            the set of properties
     */
    public ModuleDetailsImpl(Properties properties)
    {
        this(properties, null);
    }

    /**
     * Creates the instance from a set of properties. All the property values are trimmed and empty string values are removed from the set. In other words, zero length or whitespace strings are not supported.
     * 
     * @param properties
     *            the set of properties
     * @param log
     *            logger
     */
    public ModuleDetailsImpl(Properties properties, LogOutput log)
    {
        // Set defaults
        this();
        this.log = log;
        // Copy the properties so they don't get modified
        Properties trimmedProperties = new Properties();
        // Trim all the property values
        for (Map.Entry entry : properties.entrySet())
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (value == null)
            {
                // Don't copy nulls over
                continue;
            }
            String trimmedValue = value.trim();
            if (trimmedValue.length() == 0)
            {
                // Don't copy empty strings over
                continue;
            }
            // It is a real value
            trimmedProperties.setProperty(key, trimmedValue);
        }

        // Check that the required properties are present
        List<String> missingProperties = new ArrayList<String>(1);
        // ID
        id = trimmedProperties.getProperty(PROP_ID);
        if (id == null)
        {
            missingProperties.add(PROP_ID);
        }
        // ALIASES
        String aliasesStr = trimmedProperties.getProperty(PROP_ALIASES);
        if (aliasesStr != null)
        {
            StringTokenizer st = new StringTokenizer(aliasesStr, ",");
            while (st.hasMoreTokens())
            {
                String alias = st.nextToken().trim();
                if (alias.length() == 0)
                {
                    continue;
                }
                aliases.add(alias);
            }
        }
        // VERSION
        if (trimmedProperties.getProperty(PROP_VERSION) == null)
        {
            missingProperties.add(PROP_VERSION);
        }
        else
        {
            try
            {
                version = new ModuleVersionNumber(trimmedProperties.getProperty(PROP_VERSION));
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException("Unable to parse version information: " + PROP_VERSION + ", " + trimmedProperties.getProperty(PROP_VERSION), e);
            }
        }
        // TITLE
        title = trimmedProperties.getProperty(PROP_TITLE);
        if (title == null)
        {
            missingProperties.add(PROP_TITLE);
        }
        // DESCRIPTION
        description = trimmedProperties.getProperty(PROP_DESCRIPTION);
        if (description == null)
        {
            missingProperties.add(PROP_DESCRIPTION);
        }
        // REPO MIN
        if (trimmedProperties.getProperty(PROP_REPO_VERSION_MIN) != null)
        {
            try
            {
                repoVersionMin = new VersionNumber(trimmedProperties.getProperty(PROP_REPO_VERSION_MIN));
                int[] parts = repoVersionMin.getParts();
                if (parts.length > 3)
                {
                    repoVersionMin = new VersionNumber(parts[0] + "." + parts[1] + "." + parts[2]);
                    if (log != null)
                    {
                        log.info("WARNING: version.label from repoVersionMin is ignored.");
                    }
                }
            }
            catch (Throwable t)
            {
                throw new AlfrescoRuntimeException("Unable to parse repo version min: " + PROP_REPO_VERSION_MIN + ", " + repoVersionMin, t);
            }
        }
        // REPO MAX
        if (trimmedProperties.getProperty(PROP_REPO_VERSION_MAX) != null)
        {
            try
            {
                repoVersionMax = new VersionNumber(trimmedProperties.getProperty(PROP_REPO_VERSION_MAX));
                int[] parts = repoVersionMax.getParts();
                if (parts.length > 3)
                {
                    repoVersionMax = new VersionNumber(parts[0] + "." + parts[1] + "." + parts[2]);
                    if (log != null)
                    {
                        log.info("WARNING: version.label from repoVersionMax is ignored.");
                    }
                }
            }
            catch (Throwable t)
            {
                throw new AlfrescoRuntimeException("Unable to parse repo version max: " + PROP_REPO_VERSION_MAX + ", " + repoVersionMax, t);
            }
        }
        // DEPENDENCIES
        this.dependencies = extractDependencies(trimmedProperties);

        this.editions = extractEditions(trimmedProperties);

        // INSTALL DATE
        if (trimmedProperties.getProperty(PROP_INSTALL_DATE) != null)
        {
            String installDateStr = trimmedProperties.getProperty(PROP_INSTALL_DATE);
            try
            {
                installDate = ISO8601DateFormat.parse(installDateStr);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException("Unable to parse install date: " + PROP_INSTALL_DATE + ", " + installDateStr, e);
            }
        }
        // INSTALL STATE
        if (trimmedProperties.getProperty(PROP_INSTALL_STATE) != null)
        {
            String installStateStr = trimmedProperties.getProperty(PROP_INSTALL_STATE);
            try
            {
                installState = ModuleInstallState.valueOf(installStateStr);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException("Unable to parse install state: " + PROP_INSTALL_STATE + ", " + installStateStr, e);
            }
        }
        // Check
        if (missingProperties.size() > 0)
        {
            throw new AlfrescoRuntimeException("The following module properties need to be defined: " + missingProperties);
        }
        if (repoVersionMax.compareTo(repoVersionMin) < 0)
        {
            throw new AlfrescoRuntimeException("The max repo version must be greater than the min repo version:\n" +
                    "   ID:               " + id + "\n" +
                    "   Min repo version: " + repoVersionMin + "\n" +
                    "   Max repo version: " + repoVersionMax);
        }
        if (id.matches(INVALID_ID_REGEX))
        {
            throw new AlfrescoRuntimeException(
                    "The module ID '" + id + "' is invalid.  It may consist of valid characters, numbers, '.', '_' and '-'");
        }
    }

    /**
     * @param id
     *            module id
     * @param versionNumber
     *            version number
     * @param title
     *            title
     * @param description
     *            description
     */
    public ModuleDetailsImpl(String id, ModuleVersionNumber versionNumber, String title, String description)
    {
        // Set defaults
        this();

        this.id = id;
        this.version = versionNumber;
        this.title = title;
        this.description = description;
    }

    private static List<String> extractEditions(Properties trimmedProperties)
    {
        List<String> specifiedEditions = null;
        String editions = trimmedProperties.getProperty(PROP_EDITIONS);
        if (editions != null)
        {
            specifiedEditions = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(editions, ",");
            while (st.hasMoreTokens())
            {
                specifiedEditions.add(st.nextToken());
            }
        }
        return specifiedEditions;
    }

    private static List<ModuleDependency> extractDependencies(Properties properties)
    {
        int prefixLength = PROP_DEPENDS_PREFIX.length();

        List<ModuleDependency> dependencies = new ArrayList<ModuleDependency>(2);
        for (Map.Entry entry : properties.entrySet())
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (!key.startsWith(PROP_DEPENDS_PREFIX))
            {
                continue;
            }
            if (key.length() == prefixLength)
            {
                // Just ignore it
                continue;
            }
            String dependencyId = key.substring(prefixLength);
            // Build the dependency
            ModuleDependency dependency = new ModuleDependencyImpl(dependencyId, value);
            // Add it
            dependencies.add(dependency);
        }
        // Done
        return dependencies;
    }

    public Properties getProperties()
    {
        Properties properties = new Properties();
        // Mandatory properties
        properties.setProperty(PROP_ID, id);
        properties.setProperty(PROP_VERSION, version.toString());
        properties.setProperty(PROP_TITLE, title);
        properties.setProperty(PROP_DESCRIPTION, description);
        // Optional properites
        if (repoVersionMin != null)
        {
            properties.setProperty(PROP_REPO_VERSION_MIN, repoVersionMin.toString());
        }
        if (repoVersionMax != null)
        {
            properties.setProperty(PROP_REPO_VERSION_MAX, repoVersionMax.toString());
        }
        if (editions != null)
        {
            properties.setProperty(PROP_EDITIONS, join(editions.toArray(new String[editions.size()]), ','));
        }
        if (dependencies.size() > 0)
        {
            for (ModuleDependency dependency : dependencies)
            {
                String key = PROP_DEPENDS_PREFIX + dependency.getDependencyId();
                String value = dependency.getVersionString();
                properties.setProperty(key, value);
            }
        }
        if (installDate != null)
        {
            String installDateStr = ISO8601DateFormat.format(installDate);
            properties.setProperty(PROP_INSTALL_DATE, installDateStr);
        }
        if (installState != null)
        {
            String installStateStr = installState.toString();
            properties.setProperty(PROP_INSTALL_STATE, installStateStr);
        }
        if (aliases.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String oldId : aliases)
            {
                if (!first)
                {
                    sb.append(", ");
                }
                sb.append(oldId);
                first = false;
            }
            properties.setProperty(PROP_ALIASES, sb.toString());
        }
        // Done
        return properties;
    }

    @Override
    public String toString()
    {
        return "ModuleDetails[" + getProperties() + "]";
    }

    public String getId()
    {
        return id;
    }

    public List<String> getAliases()
    {
        return aliases;
    }

    public ModuleVersionNumber getModuleVersionNumber()
    {
        return version;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public VersionNumber getRepoVersionMin()
    {
        return repoVersionMin;
    }

    public void setRepoVersionMin(VersionNumber repoVersionMin)
    {
        this.repoVersionMin = repoVersionMin;
    }

    public VersionNumber getRepoVersionMax()
    {
        return repoVersionMax;
    }

    public void setRepoVersionMax(VersionNumber repoVersionMax)
    {
        this.repoVersionMax = repoVersionMax;
    }

    public List<ModuleDependency> getDependencies()
    {
        return dependencies;
    }

    public Date getInstallDate()
    {
        return installDate;
    }

    public void setInstallDate(Date installDate)
    {
        this.installDate = installDate;
    }

    public ModuleInstallState getInstallState()
    {
        return installState;
    }

    public void setInstallState(ModuleInstallState installState)
    {
        this.installState = installState;
    }

    public List<String> getEditions()
    {
        return editions;
    }

    public void setEditions(List<String> editions)
    {
        this.editions = editions;
    }

    /**
     * Grateful received from Apache Commons StringUtils class
     * 
     */
    private static String join(Object[] array, char separator)
    {
        if (array == null)
        {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * Grateful received from Apache Commons StringUtils class
     * 
     * @param array
     *            Object[]
     * @param separator
     *            char
     * @param startIndex
     *            int
     * @param endIndex
     *            int
     * @return String
     */
    private static String join(Object[] array, char separator, int startIndex, int endIndex)
    {
        if (array == null)
        {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0)
        {
            return "";
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1);
        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = startIndex; i < endIndex; i++)
        {
            if (i > startIndex)
            {
                buf.append(separator);
            }
            if (array[i] != null)
            {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * @author Derek Hulley
     */
    public static final class ModuleDependencyImpl implements ModuleDependency
    {
        private static final long serialVersionUID = -6850832632316987487L;

        private String dependencyId;
        private String versionStr;
        private List<Pair<ModuleVersionNumber, ModuleVersionNumber>> versionRanges;

        private ModuleDependencyImpl(String dependencyId, String versionStr)
        {
            this.dependencyId = dependencyId;
            this.versionStr = versionStr;
            try
            {
                versionRanges = buildVersionRanges(versionStr);
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException("Unable to interpret the module version ranges: " + versionStr, e);
            }
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(dependencyId).append(":").append(versionStr);
            return sb.toString();
        }

        private static List<Pair<ModuleVersionNumber, ModuleVersionNumber>> buildVersionRanges(String versionStr)
        {
            List<Pair<ModuleVersionNumber, ModuleVersionNumber>> versionRanges = new ArrayList<Pair<ModuleVersionNumber, ModuleVersionNumber>>(1);
            StringTokenizer rangesTokenizer = new StringTokenizer(versionStr, ",");
            while (rangesTokenizer.hasMoreTokens())
            {
                String range = rangesTokenizer.nextToken().trim();
                // Handle the * special case
                if (range.equals("*"))
                {
                    range = "*-*";
                }
                if (range.startsWith("-"))
                {
                    range = "*" + range;
                }
                if (range.endsWith("-"))
                {
                    range = range + "*";
                }
                // The range must have at least one version in it
                StringTokenizer rangeTokenizer = new StringTokenizer(range, "-", false);
                ModuleVersionNumber versionLower = null;
                ModuleVersionNumber versionUpper = null;
                while (rangeTokenizer.hasMoreTokens())
                {
                    String version = rangeTokenizer.nextToken();
                    version = version.trim();
                    if (versionLower == null)
                    {
                        if (version.equals("*"))
                        {
                            // Unbounded lower version
                            versionLower = ModuleVersionNumber.VERSION_ZERO;
                        }
                        else
                        {
                            // Explicit lower bound
                            versionLower = new ModuleVersionNumber(version);
                        }
                    }
                    else if (versionUpper == null)
                    {
                        if (version.equals("*"))
                        {
                            // Unbounded upper version
                            versionUpper = ModuleVersionNumber.VERSION_BIG;
                        }
                        else
                        {
                            // Explicit upper bound
                            versionUpper = new ModuleVersionNumber(version);
                        }
                    }
                }
                // Check
                if (versionUpper == null && versionLower == null)
                {
                    throw new AlfrescoRuntimeException(
                            "Valid dependency version ranges are: \n" +
                                    "   LOW  - HIGH \n" +
                                    "   *    - HIGH \n" +
                                    "   LOW  - *    \n" +
                                    "   *       ");
                }
                else if (versionUpper == null && versionLower != null)
                {
                    versionUpper = versionLower;
                }
                else if (versionLower == null && versionUpper != null)
                {
                    versionLower = versionUpper;
                }
                // Create the range pair
                Pair<ModuleVersionNumber, ModuleVersionNumber> rangePair = new Pair<ModuleVersionNumber, ModuleVersionNumber>(versionLower, versionUpper);
                versionRanges.add(rangePair);
            }
            return versionRanges;
        }

        public String getDependencyId()
        {
            return dependencyId;
        }

        public String getVersionString()
        {
            return versionStr;
        }

        public boolean isValidDependency(ModuleDetails moduleDetails)
        {
            // Nothing to compare to
            if (moduleDetails == null)
            {
                return false;
            }
            // Check the ID
            if (!moduleDetails.getId().equals(dependencyId))
            {
                return false;
            }
            // Check the version number
            ModuleVersionNumber checkVersion = moduleDetails.getModuleVersionNumber();
            boolean matched = false;
            for (Pair<ModuleVersionNumber, ModuleVersionNumber> versionRange : versionRanges)
            {
                ModuleVersionNumber versionLower = versionRange.getFirst();
                ModuleVersionNumber versionUpper = versionRange.getSecond();
                if (checkVersion.compareTo(versionLower) < 0)
                {
                    // The version is too low
                    continue;
                }
                if (checkVersion.compareTo(versionUpper) > 0)
                {
                    // The version is too high
                    continue;
                }
                // It is a match
                matched = true;
                break;
            }
            return matched;
        }
    }

    @Override
    public VersionNumber getVersion()
    {
        // lossy translation between maven version and old VersionNumber
        String mavenVersion = version.toString();
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < mavenVersion.length(); i++)
        {
            char c = mavenVersion.charAt(i);
            if (Character.isDigit(c) || c == '.')
            {
                b.append(c);
            }
        }

        return new VersionNumber(b.toString());
    }
}
