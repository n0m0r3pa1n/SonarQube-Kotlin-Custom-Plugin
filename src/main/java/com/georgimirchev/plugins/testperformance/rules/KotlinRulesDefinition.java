/*
 * Example Plugin for SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.georgimirchev.plugins.testperformance.rules;

import com.georgimirchev.plugins.testperformance.Constants;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.server.rule.RulesDefinition;

public class KotlinRulesDefinition implements RulesDefinition {

    public static final RuleKey TEST_PERFORMANCE_RULE = RuleKey.of(Constants.REPOSITORY, "testPerformance");

    @Override
    public void define(Context context) {
        NewRepository repository = context
                .createRepository(Constants.REPOSITORY, Constants.KOTLIN_LANGUAGE)
                .setName("Test Performance Analyzer");

        NewRule x1Rule = repository.createRule(TEST_PERFORMANCE_RULE.rule())
                .setName("Test Performance Rule")
                .setHtmlDescription("Generates an issue for unit test execution above a given threshold")

                // optional tags
                .setTags("style", "performance")
                .setStatus(RuleStatus.READY)
                .setSeverity(Severity.MAJOR);

        x1Rule.setDebtRemediationFunction(x1Rule.debtRemediationFunctions().linearWithOffset("0h", "30min"));
        repository.done();
    }
}
