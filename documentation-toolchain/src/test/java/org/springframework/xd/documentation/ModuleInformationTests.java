/*
 * Copyright 2015 the original author or authors.
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
 *
 *
 */

package org.springframework.xd.documentation;

import org.junit.Test;
import org.springframework.xd.dirt.module.ModuleRegistry;
import org.springframework.xd.dirt.module.ResourceModuleRegistry;
import org.springframework.xd.module.ModuleDefinition;
import org.springframework.xd.module.info.DefaultSimpleModuleInformationResolver;
import org.springframework.xd.module.info.ModuleInformation;
import org.springframework.xd.module.info.ModuleInformationResolver;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

/**
 * Checks that all out of the box modules have module information, and that it jsr303 validates.
 *
 * @author Eric Bottard
 */
public class ModuleInformationTests {

    private ModuleRegistry moduleRegistry = new ResourceModuleRegistry("file:../modules");

    private ModuleInformationResolver moduleInformationResolver = new DefaultSimpleModuleInformationResolver();

    @Test
    public void validate() {
        for (ModuleDefinition definition : moduleRegistry.findDefinitions()) {
            ModuleInformation info = moduleInformationResolver.resolve(definition);
            assertThat(definition.toString(), info, not(nullValue()));
            assertThat(definition.toString(), info.getShortDescription(), not(nullValue()));
        }
    }

}
