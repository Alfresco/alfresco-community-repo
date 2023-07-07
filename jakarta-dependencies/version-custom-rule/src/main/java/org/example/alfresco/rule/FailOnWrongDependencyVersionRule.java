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


import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Fail On Wrong Dependency Version Enforcer Rule
 */
@Named("failOnWrongDependencyVersionRule")
public class FailOnWrongDependencyVersionRule extends AbstractEnforcerRule {

    /**
     * Simple param. This rule fails if the value is true.
     */
    private boolean shouldFail = false;

    /**
     * Rule parameter as list of items.
     */
    private List<String> listParameters;

    // Inject needed Maven components

    @Inject
    private MavenProject project;

    @Inject
    private MavenSession session;

    @Inject
    private RuntimeInformation runtimeInformation;

    public void execute() throws EnforcerRuleException {

        getLog().info("Retrieved Target Folder: " + project.getBuild().getDirectory());
        getLog().info("Retrieved ArtifactId: " + project.getArtifactId());
        getLog().info("Retrieved Project: " + project);
        getLog().info("Retrieved Maven version: " + runtimeInformation.getMavenVersion());
        getLog().info("Retrieved Session: " + session);
        getLog().warnOrError("Parameter shouldIfail: " + shouldFail);
        getLog().info(() -> "Parameter listParameters: " + listParameters);



        if (this.shouldFail) {
            throw new EnforcerRuleException("Failing because my param said so.");
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
        return Boolean.toString(shouldFail);
    }

    //TODO: IMPLEMENT TOSTRING METHOD!

}
