package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.emulated.rev161228;

import opticaltranslator.mock.OpticalTranslatorMockProvider;

public class OpticalTranslatorMockModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.emulated.rev161228.AbstractOpticalTranslatorMockModule {
    public OpticalTranslatorMockModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OpticalTranslatorMockModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.emulated.rev161228.OpticalTranslatorMockModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        OpticalTranslatorMockProvider provider = new OpticalTranslatorMockProvider();
        getBrokerDependency().registerProvider(provider);
        return provider;
    }
}
