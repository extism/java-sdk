package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.LibExtism.ExtismVal;
import org.extism.sdk.LibExtism.ExtismValType;
import org.extism.sdk.LibExtism.InternalExtismFunction;

import java.util.Arrays;
import java.util.Optional;

public class HostFunction<T extends HostUserData> {

    private final Pointer pointer;

    private final String name;

    private final ExtismValType[] params;

    private final ExtismValType[] returns;

    private final T userData;

    public HostFunction(String name, ExtismValType[] params, ExtismValType[] returns, ExtismFunction<T> func) {
        this(name, params, returns, func, null);
    }

    public HostFunction(String name, ExtismValType[] params, ExtismValType[] returns, ExtismFunction<T> func, T userData) {

        this.name = name;
        this.params = params;
        this.returns = returns;
        this.userData = userData;

        int[] inputTypeValues = Arrays.stream(this.params).mapToInt(r -> r.v).toArray();
        int[] outputTypeValues = Arrays.stream(this.returns).mapToInt(r -> r.v).toArray();
        InternalExtismFunction callback = createCallbackFunction(func, userData);
        Pointer userDataPointer = userData != null ? userData.getPointer() : null;

        this.pointer = LibExtism.INSTANCE.extism_function_new( //
                name, //
                inputTypeValues, //
                inputTypeValues.length, //
                outputTypeValues, //
                outputTypeValues.length, //
                callback, //
                userDataPointer, //
                null //
        );
    }

    private InternalExtismFunction createCallbackFunction(ExtismFunction<T> func, T userData) {
        return (Pointer pluginPointer, ExtismVal inputs, int nInputs, ExtismVal outputs, int nOutputs, Pointer data) -> {

            var outputValues = (ExtismVal[]) outputs.toArray(nOutputs);
            var inputValues = (ExtismVal[]) inputs.toArray(nInputs);
            var currentPlugin = new ExtismCurrentPlugin(pluginPointer);

            func.invoke(currentPlugin, inputValues, outputValues, Optional.ofNullable(userData));

            for (ExtismVal output : outputValues) {
                coerceType(output);
            }
        };
    }

    void coerceType(ExtismVal value) {

        switch (value.t) {
            case ExtismValType.I32_KEY:
                value.v.setType(Integer.TYPE);
                break;
            case ExtismValType.I64_KEY:
                value.v.setType(Long.TYPE);
                break;
            case ExtismValType.F32_KEY:
                value.v.setType(Float.TYPE);
                break;
            case ExtismValType.F64_KEY:
                value.v.setType(Double.TYPE);
                break;
            default:
                throw new ExtismException(String.format("Unsupported return type: %s", value.t));
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

    public Optional<ExtismValType[]> params() {
        return Optional.ofNullable(params).map(ExtismValType[]::clone);
    }

    public Optional<ExtismValType[]> returns() {
        return Optional.ofNullable(returns).map(ExtismValType[]::clone);
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
