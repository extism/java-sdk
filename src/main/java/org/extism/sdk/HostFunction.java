package org.extism.sdk;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.util.Arrays;
import java.util.Optional;

public class HostFunction<T extends HostUserData> {

    private final Pointer pointer;

    private final String name;

    private final LibExtism.ExtismValType[] params;

    private final LibExtism.ExtismValType[] returns;

    private final Optional<T> userData;

    public HostFunction(String name, LibExtism.ExtismValType[] params, LibExtism.ExtismValType[] returns, ExtismFunction<T> f, Optional<T> userData) {

        this.name = name;
        this.params = params;
        this.returns = returns;
        this.userData = userData;
        var callback = (LibExtism.InternalExtismFunction) (
                Pointer currentPluginPointer,
                LibExtism.ExtismVal ins,
                int nInputs,
                LibExtism.ExtismVal outs,
                int nOutputs,
                Pointer data) -> {

            var outputs = (LibExtism.ExtismVal[]) outs.toArray(nOutputs);
            var inputs = (LibExtism.ExtismVal[]) ins.toArray(nInputs);
            var currentPlugin = new ExtismCurrentPlugin(currentPluginPointer);

            f.invoke(currentPlugin, inputs, outputs, userData);

            for (LibExtism.ExtismVal output : outputs) {
                convertOutput(output, output);
            }
        };

        int[] inputTypeValues = Arrays.stream(this.params).mapToInt(r -> r.v).toArray();
        int[] outputTypeValues = Arrays.stream(this.returns).mapToInt(r -> r.v).toArray();
        Pointer userDataPointer = userData.map(PointerType::getPointer).orElse(null);
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

    void convertOutput(LibExtism.ExtismVal original, LibExtism.ExtismVal fromHostFunction) {
        if (fromHostFunction.t != original.t)
            throw new ExtismException(String.format("Output type mismatch, got %d but expected %d", fromHostFunction.t, original.t));

        if (fromHostFunction.t == LibExtism.ExtismValType.I32.v) {
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
        return userData;
    }

    public String name() {
        return name;
    }

    /* package scoped */ Pointer getPointer() {
        return pointer;
    }
}
