package org.extism.sdk;

import com.sun.jna.Pointer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class HostFunctionTests {
    @Test
    public void callbackShouldAcceptNullParameters() {
        var callback = new HostFunction.Callback<>(
                (plugin, params, returns, userData) -> {/* NOOP */}, null);
        callback.invoke(Pointer.NULL, null, 0, null, 0, Pointer.NULL);
    }

    @Test
    public void callbackShouldThrowOnNullParametersAndNonzeroCounts() {
        var callback = new HostFunction.Callback<>(
                (plugin, params, returns, userData) -> {/* NOOP */}, null);
        assertThrows(ExtismException.class, () ->
                callback.invoke(Pointer.NULL, null, 1, null, 0, Pointer.NULL));
        assertThrows(ExtismException.class, () ->
                callback.invoke(Pointer.NULL, null, 0, null, 1, Pointer.NULL));
    }
}
