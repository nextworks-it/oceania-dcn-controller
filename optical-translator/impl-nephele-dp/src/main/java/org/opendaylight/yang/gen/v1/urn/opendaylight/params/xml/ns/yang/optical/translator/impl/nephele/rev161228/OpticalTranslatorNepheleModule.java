package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.nephele.rev161228;

import opticaltranslator.nephele.OpticalTranslatorNepheleProvider;

public class OpticalTranslatorNepheleModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.nephele.rev161228.AbstractOpticalTranslatorNepheleModule {
    public OpticalTranslatorNepheleModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OpticalTranslatorNepheleModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.optical.translator.impl.nephele.rev161228.OpticalTranslatorNepheleModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        OpticalTranslatorNepheleProvider provider = new OpticalTranslatorNepheleProvider();
        getBrokerDependency().registerProvider(provider);
        return provider;
    }
}
