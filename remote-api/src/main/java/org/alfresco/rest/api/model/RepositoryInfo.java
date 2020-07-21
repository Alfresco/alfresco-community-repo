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
package org.alfresco.rest.api.model;

import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.license.LicenseDescriptor;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Representation of the repository information.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class RepositoryInfo
{
    private String id;
    private String edition;
    private VersionInfo version;
    private LicenseInfo license;
    private StatusInfo status;
    private List<ModulePackage> modules;

    public String getId()
    {
        return id;
    }

    public RepositoryInfo setId(String id)
    {
        this.id = id;
        return this;
    }

    public String getEdition()
    {
        return edition;
    }

    public RepositoryInfo setEdition(String edition)
    {
        this.edition = edition;
        return this;
    }

    public VersionInfo getVersion()
    {
        return version;
    }

    public RepositoryInfo setVersion(VersionInfo version)
    {
        this.version = version;
        return this;
    }

    public LicenseInfo getLicense()
    {
        return license;
    }

    public RepositoryInfo setLicense(LicenseInfo license)
    {
        this.license = license;
        return this;
    }

    public StatusInfo getStatus()
    {
        return status;
    }

    public RepositoryInfo setStatus(StatusInfo status)
    {
        this.status = status;
        return this;
    }

    public List<ModulePackage> getModules()
    {
        return modules;
    }

    public RepositoryInfo setModules(List<ModulePackage> modules)
    {
        this.modules = modules;
        return this;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(450);
        sb.append("RepositoryInfo [edition=").append(edition)
                    .append(", version=").append(version)
                    .append(", license=").append(license)
                    .append(", status=").append(status)
                    .append(", modules=").append(modules)
                    .append(']');
        return sb.toString();
    }

    /**
     * Representation of the repository version information.
     *
     * @author Jamal Kaabi-Mofrad
     */
    public static class VersionInfo
    {
        private static final Pattern HOTFIX_PATTERN = Pattern.compile("^[0-9]+$");
        private String major;
        private String minor;
        private String patch;
        private String hotfix;
        private int schema;
        private String label;
        private String display;

        // Default constructor required for test purposes
        public VersionInfo()
        {
        }

        public VersionInfo(Descriptor descriptor)
        {
            this.major = descriptor.getVersionMajor();
            this.minor = descriptor.getVersionMinor();
            this.patch = descriptor.getVersionRevision();
            this.hotfix = getHotfix(descriptor.getVersionLabel());
            this.schema = descriptor.getSchema();
            this.label = descriptor.getVersionBuild();
            this.display = getVersionDisplay();
        }

        public String getMajor()
        {
            return major;
        }

        public String getMinor()
        {
            return minor;
        }

        public String getPatch()
        {
            return patch;
        }

        public String getHotfix()
        {
            return hotfix;
        }

        public int getSchema()
        {
            return schema;
        }

        public String getLabel()
        {
            return label;
        }

        public String getDisplay()
        {
            return display;
        }

        private String getHotfix(String versionLabel)
        {
            /*
             * if the label starts with a dot, then digit(s), or just digit(s), we return the number only.
             * for anything else zero will be returned.
             */
            if (StringUtils.isNotEmpty(versionLabel))
            {
                if (versionLabel.startsWith("."))
                {
                    versionLabel = versionLabel.substring(1);
                }
                Matcher matcher = HOTFIX_PATTERN.matcher(versionLabel);
                if (matcher.find())
                {
                    return versionLabel;
                }
            }
            return Integer.toString(0);
        }

        private String getVersionDisplay()
        {
            StringBuilder version = new StringBuilder(major);
            version.append('.')
                        .append(minor)
                        .append('.')
                        .append(patch)
                        .append('.')
                        .append(getHotfix());

            if (StringUtils.isNotEmpty(label))
            {
                version.append(" (").append(label).append(") ");
            }
            version.append("schema ").append(schema);

            // Display example: "5.2.0.1 (r123456-b0) schema 10001"
            return version.toString();
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("VersionInfo [major=").append(major)
                        .append(", minor=").append(minor)
                        .append(", patch=").append(patch)
                        .append(", hotfix=").append(hotfix)
                        .append(", schema=").append(schema)
                        .append(", label=").append(label)
                        .append(", display=").append(display)
                        .append(']');
            return sb.toString();
        }
    }

    /**
     * Representation of the license information.
     *
     * @author Jamal Kaabi-Mofrad
     */
    public static class LicenseInfo
    {
        private Date issuedAt;
        private Date expiresAt;
        private Integer remainingDays;
        private String holder;
        private String mode;
        private LicenseEntitlement entitlements;

        // Default constructor required for test purposes
        public LicenseInfo()
        {
        }

        public LicenseInfo(LicenseDescriptor licenseDescriptor)
        {
            this.issuedAt = licenseDescriptor.getIssued();
            this.expiresAt = licenseDescriptor.getValidUntil();
            this.remainingDays = licenseDescriptor.getRemainingDays();
            this.holder = licenseDescriptor.getHolderOrganisation();
            this.mode = licenseDescriptor.getLicenseMode().name();
            this.entitlements = new LicenseEntitlement()
                        .setMaxDocs(licenseDescriptor.getMaxDocs())
                        .setMaxUsers(licenseDescriptor.getMaxUsers())
                        .setClusterEnabled(licenseDescriptor.isClusterEnabled())
                        .setCryptodocEnabled(licenseDescriptor.isCryptodocEnabled());
        }

        public Date getIssuedAt()
        {
            return issuedAt;
        }

        public Date getExpiresAt()
        {
            return expiresAt;
        }

        public Integer getRemainingDays()
        {
            return remainingDays;
        }

        public String getHolder()
        {
            return holder;
        }

        public String getMode()
        {
            return mode;
        }

        public LicenseEntitlement getEntitlements()
        {
            return entitlements;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("LicenseInfo [issuedAt=").append(issuedAt)
                        .append(", expiresAt=").append(expiresAt)
                        .append(", remainingDays=").append(remainingDays)
                        .append(", holder=").append(holder)
                        .append(", mode=").append(mode)
                        .append(", entitlements=").append(entitlements)
                        .append(']');
            return sb.toString();
        }
    }

    /**
     * Representation of the license's entitlement.
     *
     * @author Jamal Kaabi-Mofrad
     */
    public static class LicenseEntitlement
    {
        private Long maxUsers;
        private Long maxDocs;
        private boolean isClusterEnabled;
        private boolean isCryptodocEnabled;

        public LicenseEntitlement()
        {
        }

        public Long getMaxUsers()
        {
            return maxUsers;
        }

        public LicenseEntitlement setMaxUsers(Long maxUsers)
        {
            this.maxUsers = maxUsers;
            return this;
        }

        public Long getMaxDocs()
        {
            return maxDocs;
        }

        public LicenseEntitlement setMaxDocs(Long maxDocs)
        {
            this.maxDocs = maxDocs;
            return this;
        }

        public boolean getIsClusterEnabled()
        {
            return isClusterEnabled;
        }

        public LicenseEntitlement setClusterEnabled(boolean clusterEnabled)
        {
            isClusterEnabled = clusterEnabled;
            return this;
        }

        public boolean getIsCryptodocEnabled()
        {
            return isCryptodocEnabled;
        }

        public LicenseEntitlement setCryptodocEnabled(boolean cryptodocEnabled)
        {
            isCryptodocEnabled = cryptodocEnabled;
            return this;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(100);
            sb.append("LicenseEntitlement [maxUsers=").append(maxUsers)
                        .append(", maxDocs=").append(maxDocs)
                        .append(", isClusterEnabled=").append(isClusterEnabled)
                        .append(", isCryptodocEnabled=").append(isCryptodocEnabled)
                        .append(']');
            return sb.toString();
        }
    }

    /**
     * Representation of the repository status information.
     *
     * @author Jamal Kaabi-Mofrad
     */
    public static class StatusInfo
    {
        private boolean isReadOnly;
        private boolean isAuditEnabled;
        private boolean isQuickShareEnabled;
        private boolean isThumbnailGenerationEnabled;

        public StatusInfo()
        {
        }

        public boolean getIsReadOnly()
        {
            return isReadOnly;
        }

        public StatusInfo setReadOnly(boolean readOnly)
        {
            isReadOnly = readOnly;
            return this;
        }

        public boolean getIsAuditEnabled()
        {
            return isAuditEnabled;
        }

        public StatusInfo setAuditEnabled(boolean auditEnabled)
        {
            isAuditEnabled = auditEnabled;
            return this;
        }

        public boolean getIsQuickShareEnabled()
        {
            return isQuickShareEnabled;
        }

        public StatusInfo setQuickShareEnabled(boolean quickShareEnabled)
        {
            isQuickShareEnabled = quickShareEnabled;
            return this;
        }

        public boolean getIsThumbnailGenerationEnabled()
        {
            return isThumbnailGenerationEnabled;
        }

        public StatusInfo setThumbnailGenerationEnabled(boolean isThumbnailGenerationEnabled)
        {
            this.isThumbnailGenerationEnabled = isThumbnailGenerationEnabled;
            return this;
        }

        @Override
        public String toString()
        {
            final StringBuilder sb = new StringBuilder(250);
            sb.append("StatusInfo [isReadOnly=").append(isReadOnly)
                        .append(", isAuditEnabled=").append(isAuditEnabled)
                        .append(", isQuickShareEnabled=").append(isQuickShareEnabled)
                        .append(", isThumbnailGenerationEnabled=").append(isThumbnailGenerationEnabled)
                        .append(']');
            return sb.toString();
        }
    }
}
