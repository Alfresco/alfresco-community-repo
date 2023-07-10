package org.example.alfresco.rule;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerLogger;
import org.apache.maven.project.MavenProject;
import org.example.alfresco.rule.mojo.BannedArtifact;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DependencyUtils
{
    public static Map<String, Set<Artifact>> detectBannedArtifactsInProjects(MavenProject project, List<BannedArtifact> bannedDependencies) {
        Map<String, Set<Artifact>> result = new HashMap<>();
        Set<Artifact> detectedArtifacts = detectBannedArtifacts(project.getArtifacts(), bannedDependencies);

        if(!detectedArtifacts.isEmpty()) {
            result.put(project.getName() + " - " +project.getId(), detectedArtifacts);
        }

        return result;
    }

    public static Set<Artifact> detectBannedArtifacts(Set<Artifact> dependenciesToCheck, List<BannedArtifact> bannedDependencies)
    {
        return dependenciesToCheck.stream()
                .filter(artifact -> isBanned(artifact, bannedDependencies))
                .collect(Collectors.toSet());
    }

    public static boolean isBanned(Artifact artifact, List<BannedArtifact> bannedDependenciesList)
    {
        String groupId = artifact.getGroupId();
        String artifactId = artifact.getArtifactId();
        String version = artifact.getVersion();

        return bannedDependenciesList.stream()
                .filter(isGroupIdTheSame(groupId))
                .filter(isArtifactIdTheSame(artifactId))
                .anyMatch(isInVersionRange(version));
    }

    public static Predicate<BannedArtifact> isGroupIdTheSame(String groupId) {
        return dv -> dv.getGroupId().equals(groupId);
    }

    public static Predicate<BannedArtifact> isArtifactIdTheSame(String artifactId) {
        return dv -> dv.getArtifactId().equals(artifactId);
    }

    public static Predicate<BannedArtifact> isInVersionRange(String version) {
        return dv -> version.compareTo(dv.getMinVersion()) >= 0 && version.compareTo(dv.getMaxVersion()) <= 0;
    }

    public static void printLogs(EnforcerLogger enforcerLogger, Map<String, Set<Artifact>> toPrint) {
        for(Map.Entry<String, Set<Artifact>> entry : toPrint.entrySet()) {
            enforcerLogger.info("Banned artifacts detected in: " + entry.getKey());
            for(Artifact artifact : entry.getValue()) {
                enforcerLogger.info("Artifact: " + artifact);
            }
        }
    }
}
