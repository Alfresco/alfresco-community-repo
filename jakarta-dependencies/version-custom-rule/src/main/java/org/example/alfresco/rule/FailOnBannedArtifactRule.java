/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.example.alfresco.rule;


import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.example.alfresco.rule.mojo.BannedArtifact;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Fail On Wrong Dependency Version Enforcer Rule
 */
@Named("failOnBannedArtifactRule")
public class FailOnBannedArtifactRule extends AbstractEnforcerRule {

    private List<BannedArtifact> bannedArtifacts;

    private boolean shouldFail = false;

    @Inject
    private MavenProject project;

    public void execute() throws EnforcerRuleException {

        if(bannedArtifacts != null && bannedArtifacts.size() > 0)
        {
            getLog().info("Checking banned artifacts with specific versions. Banned artifacts to check number: "
                    + bannedArtifacts.size());
            Map<String, Set<Artifact>> result = DependencyUtils.detectBannedArtifactsInProjects(project, bannedArtifacts);

            DependencyUtils.printLogs(getLog(), result);
            if (this.shouldFail && !result.isEmpty()) {
                throw new EnforcerRuleException("Banned artifacts detected.");
            }
        } else {
            getLog().info("No banned artifacts specified - skipping check.");
        }
    }

    /**
     * If your rule is cacheable, you must return a unique id when parameters or conditions
     * change that would cause the result to be different. Multiple cached results are stored
     * based on their id.
     * <p>
     * The easiest way to do this is to return a hash computed from the values of your parameters.
     * <p>
     * If your rule is not cacheable, then you don't need to override this method or return null
     */
    @Override
    public String getCacheId() {
        //no hash on boolean...only parameter so no hash is needed.
        return Integer.toString(bannedArtifacts.hashCode());
    }
}
