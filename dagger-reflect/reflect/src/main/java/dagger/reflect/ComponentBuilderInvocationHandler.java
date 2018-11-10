/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dagger.reflect;

import dagger.BindsInstance;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Set;

import static dagger.reflect.Reflection.findQualifier;

final class ComponentBuilderInvocationHandler implements InvocationHandler {
  static <T> T create(Class<?> componentClass, Class<T> builderClass,
      BindingGraph.Builder graphBuilder, Set<Class<?>> modules, Set<Class<?>> dependencies) {
    if ((componentClass.getModifiers() & Modifier.PUBLIC) == 0) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException("Component interface "
          + componentClass.getName()
          + " must be public in order to be reflectively created");
    }
    return builderClass.cast(
        Proxy.newProxyInstance(builderClass.getClassLoader(), new Class[] { builderClass },
            new ComponentBuilderInvocationHandler(componentClass, builderClass, graphBuilder,
                modules, dependencies)));
  }

  private final Class<?> componentClass;
  private final Class<?> builderClass;
  private final Set<Class<?>> missingModules;
  private final Set<Class<?>> missingDependencies;
  private final BindingGraph.Builder graphBuilder;

  private ComponentBuilderInvocationHandler(Class<?> componentClass, Class<?> builderClass,
      BindingGraph.Builder graphBuilder, Set<Class<?>> missingModules,
      Set<Class<?>> missingDependencies) {
    this.componentClass = componentClass;
    this.builderClass = builderClass;
    this.graphBuilder = graphBuilder;
    this.missingModules = missingModules;
    this.missingDependencies = missingDependencies;
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(proxy, args);
    }

    Class<?> returnType = method.getReturnType();
    Type[] parameterTypes = method.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();

    if (returnType.equals(componentClass)) {
      if (parameterTypes.length != 0) {
        throw new IllegalStateException(); // TODO must be no-arg
      }
      if (!missingDependencies.isEmpty()) {
        throw new IllegalStateException(); // TODO missingDependencies must have provided instances
      }
      for (Class<?> missingModule : missingModules) {
        ReflectiveModuleParser.parse(missingModule, null, graphBuilder);
      }

      return ComponentInvocationHandler.create(componentClass, graphBuilder.build());
    }

    if (returnType.equals(builderClass)) {
      if (method.getAnnotation(BindsInstance.class) != null) {
        if (parameterTypes.length != 1) {
          throw new IllegalStateException(); // TODO must be single arg
        }
        Key key = Key.of(findQualifier(parameterAnnotations[0]), parameterTypes[0]);
        Object instance = args[0];
        // TODO most nullable annotations don't have runtime retention. so maybe just always allow?
        //if (instance == null && !hasNullable(parameterAnnotations[0])) {
        //  throw new NullPointerException(); // TODO message
        //}
        graphBuilder.add(key, new Binding.Instance<>(instance));
      } else {
        throw new IllegalStateException(method.toString()); // TODO report unsupported builder shape
      }
      return proxy;
    }

    throw new IllegalStateException(method.toString()); // TODO report unsupported method shape
  }
}
