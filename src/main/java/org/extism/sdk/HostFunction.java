package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.LibExtism.ExtismVal;
import org.extism.sdk.LibExtism.InternalExtismFunction;

import java.util.Arrays;
import java.util.Optional;

public class HostFunction<T extends HostUserData> {

    private final Pointer pointer;

    private final String name;

    private final LibExtism.ExtismValType[] params;

    private final LibExtism.ExtismValType[] returns;

    private final T userData;

    public HostFunction(String name, LibExtism.ExtismValType[] params, LibExtism.ExtismValType[] returns, ExtismFunction<T> func) {
        this(name, params, returns, func, null);
    }

    public HostFunction(String name, LibExtism.ExtismValType[] params, LibExtism.ExtismValType[] returns, ExtismFunction<T> func, T userData) {

        this.name = name;
        this.params = params;
        this.returns = returns;
        this.userData = userData;

        int[] inputTypeValues = Arrays.stream(this.params).mapToInt(r -> r.v).toArray();
        int[] outputTypeValues = Arrays.stream(this.returns).mapToInt(r -> r.v).toArray();
        InternalExtismFunction callback = createCallbackFunction(func, userData);
        Pointer userDataPointer = userData != null ? userData.getPointer() : null;

        this.pointer = LibExtism.INSTANCE.extism_function_new(
                name,
                inputTypeValues,
                inputTypeValues.length,
                outputTypeValues,
                outputTypeValues.length,
                callback,
                userDataPointer,
                null
        );
    }

    private InternalExtismFunction createCallbackFunction(ExtismFunction<T> func, T userData) {
        return (Pointer pluginPointer, ExtismVal inputs, int nInputs, ExtismVal outputs, int nOutputs, Pointer data) -> {

            var outputValues = (ExtismVal[]) outputs.toArray(nOutputs);
            var inputValues = (ExtismVal[]) inputs.toArray(nInputs);
            var currentPlugin = new ExtismCurrentPlugin(pluginPointer);

            func.invoke(currentPlugin, inputValues, outputValues, Optional.ofNullable(userData));

            for (ExtismVal output : outputValues) {
                convertOutput(output, output);
            }
        };
    }

    void convertOutput(ExtismVal original, ExtismVal fromHostFunction) {

        if (fromHostFunction.t != original.t) {
            throw new ExtismException(String.format("Output type mismatch, got %d but expected %d", fromHostFunction.t, original.t));
        } else if (fromHostFunction.t == LibExtism.ExtismValType.I32.v) {
            original.v.setType(Integer.TYPE);
            original.v.i32 = fromHostFunction.v.i32;
        } else if (fromHostFunction.t == LibExtism.ExtismValType.I64.v) {
            original.v.setType(Long.TYPE);
            original.v.i64 = fromHostFunction.v.i64;
        } else if (fromHostFunction.t == LibExtism.ExtismValType.F32.v) {
            original.v.setType(Float.TYPE);
            original.v.f32 = fromHostFunction.v.f32;
        } else if (fromHostFunction.t == LibExtism.ExtismValType.F64.v) {
            original.v.setType(Double.TYPE);
            original.v.f64 = fromHostFunction.v.f64;
        } else {
            throw new ExtismException(String.format("Unsupported return type: %s", original.t));
        }
    }

    public void setNamespace(String name) {
        if (this.pointer != null) {
            LibExtism.INSTANCE.extism_function_set_namespace(this.pointer, name);
        }
    }

    public HostFunction<T> withNamespace(String name) {
        this.setNamespace(name);
        return this;
    }

    public Optional<LibExtism.ExtismValType[]> params() {
        return Optional.ofNullable(params).map(LibExtism.ExtismValType[]::clone);
    }

    public Optional<LibExtism.ExtismValType[]> returns() {
        return Optional.ofNullable(returns).map(LibExtism.ExtismValType[]::clone);
    }

    public Optional<T> userData() {
        return Optional.ofNullable(userData);
    }

    public String name() {
        return name;
    }

    /* package scoped */ Pointer getPointer() {
        return pointer;
    }
}
