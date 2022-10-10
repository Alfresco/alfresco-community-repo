/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

import java.util.List;

import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Halts the bootstrap process if a deprecated module is present.
 *
 * @author Domenico Sibilio
 * @since 7.3.0
 */
public class DeprecatedModulesValidator
{
    private static final String ERROR_MSG = "module.err.deprecated_modules";
    private final ModuleService moduleService;
    private final List<String> deprecatedModules;

    public DeprecatedModulesValidator(final ModuleService moduleService, final List<String> deprecatedModules)
    {
        this.moduleService = moduleService;
        this.deprecatedModules = deprecatedModules;
    }

    public void onInit()
    {
        ofNullable(moduleService.getAllModules())
            .map(this::getDeprecatedModules)
            .filter(not(String::isBlank))
            .ifPresent(DeprecatedModulesValidator::throwException);
    }

    private String getDeprecatedModules(List<ModuleDetails> modules)
    {
        return modules.stream()
            .filter(module -> deprecatedModules.contains(module.getId()))
            .map(module -> module.getTitle() + " " + module.getModuleVersionNumber())
            .collect(joining(", "));
    }

    private static void throwException(String foundDeprecatedModules)
    {
        throw new IllegalStateException(I18NUtil.getMessage(ERROR_MSG, foundDeprecatedModules));
    }
}
