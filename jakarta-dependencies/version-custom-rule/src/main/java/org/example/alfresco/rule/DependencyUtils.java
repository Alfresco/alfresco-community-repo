package org.example.alfresco.rule;

import org.apache.maven.enforcer.rule.api.EnforcerLogger;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.example.alfresco.rule.mojo.DependencyVersions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DependencyUtils
{
    public static Map<String, Set<Dependency>> detectBannedDependenciesInProjects(MavenProject project, List<DependencyVersions> bannedDependencies) {
        Map<String, Set<Dependency>> result = new HashMap<>();
        Set<Dependency> detectedDependenciesInDependencyManagement = detectBannedDependency(project.getDependencyManagement().getDependencies(), bannedDependencies);
        Set<Dependency> detectedDependencies = detectBannedDependency(project.getDependencies(), bannedDependencies);

        Set<Dependency> combined = new HashSet<>();
        combined.addAll(detectedDependenciesInDependencyManagement);
        combined.addAll(detectedDependencies);

        if(!combined.isEmpty()) {
            result.put(project.getName() + " - " +project.getId(), combined);
        }

        if(!project.getCollectedProjects().isEmpty()) {
            for(MavenProject mavenProject : project.getCollectedProjects()){
                result.putAll(detectBannedDependenciesInProjects(mavenProject, bannedDependencies));
            }
        }

        return result;
    }

    public static Set<Dependency> detectBannedDependency(List<Dependency> dependenciesToCheck, List<DependencyVersions> bannedDependencies)
    {
        return dependenciesToCheck.stream()
                .filter(dependency -> isBanned(dependency, bannedDependencies))
                .collect(Collectors.toSet());
    }

    public static boolean isBanned(Dependency dependency, List<DependencyVersions> bannedDependenciesList)
    {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = dependency.getVersion();

        return bannedDependenciesList.stream()
                .filter(isGroupIdTheSame(groupId))
                .filter(isArtifactIdTheSame(artifactId))
                .anyMatch(isInVersionRange(version));
    }

    public static Predicate<DependencyVersions> isGroupIdTheSame(String groupId) {
        return dv -> dv.getGroupId().equals(groupId);
    }

    public static Predicate<DependencyVersions> isArtifactIdTheSame(String artifactId) {
        return dv -> dv.getArtifactId().equals(artifactId);
    }

    public static Predicate<DependencyVersions> isInVersionRange(String version) {
        return dv -> dv.getMinVersion().compareTo(version) >= 0 && dv.getMaxVersion().compareTo(version) <= 0;
    }

    public static void printLogs(EnforcerLogger enforcerLogger, Map<String, Set<Dependency>> toPrint) {
        for(Map.Entry<String, Set<Dependency>> entry : toPrint.entrySet()) {
            enforcerLogger.info("Banned dependencies detected in: " + entry.getKey());
            for(Dependency dependency : entry.getValue()) {
                enforcerLogger.info("Dependency: " + dependency);
            }
        }
    }
}
