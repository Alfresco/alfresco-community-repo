package org.example.alfresco.rule;

import org.apache.maven.model.Dependency;
import org.example.alfresco.rule.mojo.DependencyVersions;

import java.util.List;
import java.util.function.Predicate;

public class DependencyUtils
{
    public boolean isBanned(Dependency dependency, List<DependencyVersions> bannedDependenciesList)
    {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = dependency.getVersion();

        return bannedDependenciesList.stream()
                .filter(isGroupIdTheSame(groupId))
                .filter(isArtifactIdTheSame(artifactId))
                .anyMatch(isInVersionRange(version));
    }

    public Predicate<DependencyVersions> isGroupIdTheSame(String groupId) {
        return dv -> dv.getGroupId().equals(groupId);
    }

    public Predicate<DependencyVersions> isArtifactIdTheSame(String artifactId) {
        return dv -> dv.getArtifactId().equals(artifactId);
    }

    public Predicate<DependencyVersions> isInVersionRange(String version) {
        return dv -> dv.getMinVersion().compareTo(version) >= 0 && dv.getMaxVersion().compareTo(version) <= 0;
    }
}
