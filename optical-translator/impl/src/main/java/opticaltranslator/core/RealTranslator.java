/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.core;

import com.google.common.util.concurrent.Futures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.GetTranslatorTypeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.GetTranslatorTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateAddOpticalFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslateRemoveOpticalFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.translator.api.rev161228.TranslatorApiService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import java.util.concurrent.Future;

class RealTranslator implements TranslatorApiService {

    private static final String NAME = "Real data plane translator";

    @Override
    public Future<RpcResult<GetTranslatorTypeOutput>> getTranslatorType() {
        return Futures.immediateFuture(
                RpcResultBuilder.success(
                        new GetTranslatorTypeOutputBuilder().setTranslatorType(NAME)
                ).build()
        );
    }

    @Override
    public Future<RpcResult<Void>> translateAddOpticalFlow(TranslateAddOpticalFlowInput input) {
        throw new UnsupportedOperationException("Not implemented yet."); //TODO when and if I will receive the API
    }

    @Override
    public Future<RpcResult<Void>> translateRemoveOpticalFlow(TranslateRemoveOpticalFlowInput input) {
        throw new UnsupportedOperationException("Not implemented yet."); //TODO when and if I will receive the API
    }
}
