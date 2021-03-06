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
 */

package org.gradle.test.fixtures

import org.gradle.model.internal.core.*
import org.gradle.model.internal.core.rule.describe.SimpleModelRuleDescriptor
import org.gradle.model.internal.fixture.ModelRegistryHelper
import org.gradle.model.internal.fixture.TestNodeInitializerRegistry
import org.gradle.model.internal.manage.instance.ManagedProxyFactory
import org.gradle.model.internal.manage.projection.ManagedModelProjection
import org.gradle.model.internal.manage.schema.StructSchema
import org.gradle.model.internal.manage.schema.extract.DefaultModelSchemaStore
import org.gradle.model.internal.type.ModelType

public class BaseInstanceFixtureSupport {
    static <B, T extends B, I extends B> T create(Class<T> publicType, Class<? extends B> internalView, Class<I> implType,
                                                  String name, Closure<I> createUnmanagedInstance) {
        def node = createNode(publicType, internalView, implType, name, createUnmanagedInstance)
        return node.asMutable(ModelType.of(publicType), new SimpleModelRuleDescriptor("<get $name>")).instance
    }

    static <B, T extends B, I extends B> MutableModelNode createNode(Class<T> publicType, Class<? extends B> internalView, Class<I> implType,
                                                        String name, Closure<I> createUnmanagedInstance) {
        def modelRegistry = new ModelRegistryHelper()
        modelRegistry.registerInstance("nodeInitializerRegistry", TestNodeInitializerRegistry.INSTANCE)

        def publicTypeSchema = (StructSchema<T>) DefaultModelSchemaStore.instance.getSchema(publicType)
        def internalViewSchema = (StructSchema<? extends T>) DefaultModelSchemaStore.instance.getSchema(internalView)
        def delegateSchema = (StructSchema<? extends T>) DefaultModelSchemaStore.instance.getSchema(implType)

        def registration = ModelRegistrations.of(ModelPath.path(name))
            .action(ModelActionRole.Create) { MutableModelNode node ->
                def privateData = createUnmanagedInstance(node)
                node.setPrivateData(implType, privateData)
            }
            .withProjection(new ManagedModelProjection<T>(publicTypeSchema, delegateSchema, ManagedProxyFactory.INSTANCE, null))
            .withProjection(new ManagedModelProjection<T>(internalViewSchema, delegateSchema, ManagedProxyFactory.INSTANCE, null))
            .descriptor(new SimpleModelRuleDescriptor("<create $name>"))
        modelRegistry.register(registration.build())

        return modelRegistry.atState(name, ModelNode.State.Initialized)
    }
}
