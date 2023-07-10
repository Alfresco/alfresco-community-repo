package org.example.alfresco.rule.mojo;

import org.apache.maven.plugins.annotations.Parameter;

public class BannedArtifact {

    @Parameter
    private String groupId;

    @Parameter
    private String artifactId;

    @Parameter
    private String minVersion;

    @Parameter
    private String maxVersion;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
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
