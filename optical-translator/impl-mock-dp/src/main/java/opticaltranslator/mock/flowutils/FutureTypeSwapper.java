/*
 * Nextworks S.r.l. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package opticaltranslator.mock.flowutils;

import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class FutureTypeSwapper {

    FutureTypeSwapper() {}

    public <T, S> Future<RpcResult<S>> swapFuture(Future<RpcResult<T>> inFuture, Function<T,S> converter) {
        return new Future<RpcResult<S>>() {
            @Override
            public boolean cancel(boolean b) {
                return inFuture.cancel(b);
            }

            @Override
            public boolean isCancelled() {
                return inFuture.isCancelled();
            }

            @Override
            public boolean isDone() {
                return inFuture.isDone();
            }

            @Override
            public RpcResult<S> get()
                    throws InterruptedException, ExecutionException {
                inFuture.get();
                return RpcResultBuilder.success(converter.apply(inFuture.get().getResult())).build();
            }

            @Override
            public RpcResult<S> get(long l, @Nonnull TimeUnit timeUnit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                inFuture.get(l, timeUnit);
                return RpcResultBuilder.success(converter.apply(inFuture.get().getResult())).build();
            }
        };
    }

    //Probably could just call above method with converter = (t) -> null, but meh.
    public <T> Future<RpcResult<Void>> generalizeFuture(Future<RpcResult<T>> inFuture) {

        return new Future<RpcResult<Void>>() {
            @Override
            public boolean cancel(boolean b) {
                return inFuture.cancel(b);
            }

            @Override
            public boolean isCancelled() {
                return inFuture.isCancelled();
            }

            @Override
            public boolean isDone() {
                return inFuture.isDone();
            }

            @Override
            public RpcResult<Void> get() {
                try {
                    inFuture.get();
                } catch (InterruptedException | ExecutionException exc) {
                    return RpcResultBuilder.<Void>failed().withError(
                            RpcError.ErrorType.APPLICATION,
                            exc.getMessage(),
                            exc
                    ).build();
                }
                return RpcResultBuilder.<Void>success().build();
            }

            @Override
            public RpcResult<Void> get(long l, @Nonnull TimeUnit timeUnit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                try {
                    inFuture.get(l, timeUnit);
                } catch (InterruptedException | ExecutionException exc) {
                    return RpcResultBuilder.<Void>failed().withError(
                            RpcError.ErrorType.APPLICATION,
                            exc.getMessage(),
                            exc
                    ).build();
                }
                return RpcResultBuilder.<Void>success().build();
            }
        };
    }
}
