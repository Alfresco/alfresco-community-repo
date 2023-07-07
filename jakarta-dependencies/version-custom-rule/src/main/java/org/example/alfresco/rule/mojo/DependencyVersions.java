package org.example.alfresco.rule.mojo;

import org.apache.maven.plugins.annotations.Parameter;

public class DependencyVersions {

    @Parameter
    private String name;

    @Parameter
    private String minVersion;

    @Parameter
    private String maxVersion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }

    public String getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }
}
