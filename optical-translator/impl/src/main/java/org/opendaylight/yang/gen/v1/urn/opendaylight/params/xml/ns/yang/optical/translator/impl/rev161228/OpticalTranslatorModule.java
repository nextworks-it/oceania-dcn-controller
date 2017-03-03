package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.rev161228;

import opticaltranslator.core.OpticalTranslatorProvider;

public class OpticalTranslatorModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.rev161228.AbstractOpticalTranslatorModule {
    public OpticalTranslatorModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OpticalTranslatorModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.rev161228.OpticalTranslatorModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        OpticalTranslatorProvider provider = new OpticalTranslatorProvider();
        getBrokerDependency().registerProvider(provider);
        return provider;
    }
}
