package io.osvaldas.backoffice.acceptance.config

import org.reflections.Reflections

import com.google.common.base.Preconditions
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector

class StepdefDependencyInjector {

    static final List<Object> INJECTABLE_OBJECTS = []

    static final String CUSTOM_PACKAGE_URI = System.getProperty('customPackageUri', 'io.osvaldas.backoffice.acceptance')

    static final ThreadLocal<Injector> INJECTOR_HOLDER = new ThreadLocal<Injector>() {

        @Override
        protected Injector initialValue() {
            Reflections reflections = new Reflections(CUSTOM_PACKAGE_URI)
            Class<? extends AbstractModule> guiceModule = reflections
                .getSubTypesOf(AbstractModule)
                .sort { a, b -> a.isAssignableFrom(b) ? 1 : -1 }
                .first()
            Preconditions.checkNotNull guiceModule, 'Guice module not found! You should implement AbstractModule!'
            Guice.createInjector(guiceModule.getDeclaredConstructor().newInstance())
        }

    }

    static void injectMembersTo(Object injectable) {
        Injector injector = INJECTOR_HOLDER.get()
        injector.injectMembers injectable
        INJECTABLE_OBJECTS.add(injectable)
    }

}
