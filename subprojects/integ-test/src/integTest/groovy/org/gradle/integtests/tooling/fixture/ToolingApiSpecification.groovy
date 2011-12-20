/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.integtests.tooling.fixture

import org.gradle.integtests.fixtures.BasicGradleDistribution
import org.gradle.integtests.fixtures.GradleDistribution
import org.gradle.util.GradleVersion
import org.gradle.util.SetSystemProperties
import org.junit.Rule
import org.junit.internal.AssumptionViolatedException
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

@RunWith(ToolingApiCompatibilitySuiteRunner)
abstract class ToolingApiSpecification extends Specification {
    static final Logger LOGGER = LoggerFactory.getLogger(ToolingApiSpecification)
    @Rule public final SetSystemProperties sysProperties = new SetSystemProperties()
    @Rule public final GradleDistribution dist = new GradleDistribution()
    final ToolingApi toolingApi = new ToolingApi(dist)
    private static final ThreadLocal<BasicGradleDistribution> VERSION = new ThreadLocal<BasicGradleDistribution>()

    static void selectTargetDist(BasicGradleDistribution version) {
        this.VERSION.set(version)
    }

    void setup() {
        def consumerGradle = GradleVersion.current()
        def target = GradleVersion.version(VERSION.get().version)
        LOGGER.info(" Using Tooling API consumer ${consumerGradle}, provider ${target}")
        boolean accept = accept(consumerGradle, target)
        if (!accept) {
            throw new AssumptionViolatedException("Test class ${getClass().name} does not work with tooling API ${consumerGradle} and Gradle ${target}.")
        }
        this.toolingApi.withConnector {
            if (consumerGradle.version != target.version) {
                LOGGER.info("Overriding daemon tooling API provider to use installation: " + target);
                def targetGradle = dist.previousVersion(target.version)
                it.useInstallation(new File(targetGradle.gradleHomeDir.absolutePath))
                it.embedded = false
            }
        }
    }

    /**
     * Returns true if this test class works with the given combination of tooling API consumer and provider.
     */
    protected boolean accept(GradleVersion toolingApi, GradleVersion targetGradle) {
        return true
    }

    def withConnection(Closure cl) {
        toolingApi.withConnection(cl)
    }

    def maybeFailWithConnection(Closure cl) {
        toolingApi.maybeFailWithConnection(cl)
    }
}
