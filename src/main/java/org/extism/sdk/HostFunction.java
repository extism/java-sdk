package org.extism.sdk;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import java.util.Arrays;
import java.util.Optional;

public class HostFunction<T extends HostUserData> {

    private final LibExtism.InternalExtismFunction callback;

    private boolean freed;

    public final Pointer pointer;

    public final String name;

    public final LibExtism.ExtismValType[] params;

    public final LibExtism.ExtismValType[] returns;

    public HostFunction(String name, LibExtism.ExtismValType[] params, LibExtism.ExtismValType[] returns, ExtismFunction f, Optional<T> userData) {
        this.freed = false;
        this.name = name;
        this.params = params;
        this.returns = returns;
        this.callback = new Callback(f, userData);

        this.pointer = LibExtism.INSTANCE.extism_function_new(
                this.name,
                Arrays.stream(this.params).mapToInt(r -> r.v).toArray(),
                this.params.length,
                Arrays.stream(this.returns).mapToInt(r -> r.v).toArray(),
                this.returns.length,
                this.callback,
                userData.map(PointerType::getPointer).orElse(null),
                null
        );
    }

    static void convertOutput(LibExtism.ExtismVal original, LibExtism.ExtismVal fromHostFunction) {
        if (fromHostFunction.t != original.t)
            throw new ExtismException(String.format("Output type mismatch, got %d but expected %d", fromHostFunction.t, original.t));

        if (fromHostFunction.t == LibExtism.ExtismValType.I32.v) {
            original.v.setType(Integer.TYPE);
            original.v.i32 = fromHostFunction.v.i32;
        } else if (fromHostFunction.t == LibExtism.ExtismValType.I64.v) {
            original.v.setType(Long.TYPE);
            // PTR is an alias for I64
            if (fromHostFunction.v.i64 == 0 && fromHostFunction.v.ptr > 0) {
                original.v.i64 = fromHostFunction.v.ptr;
            } else {
                original.v.i64 = fromHostFunction.v.i64;
            }
        } else if (fromHostFunction.t == LibExtism.ExtismValType.F32.v) {
            original.v.setType(Float.TYPE);
            original.v.f32 = fromHostFunction.v.f32;
        } else if (fromHostFunction.t == LibExtism.ExtismValType.F64.v) {
            original.v.setType(Double.TYPE);
            original.v.f64 = fromHostFunction.v.f64;
        } else
            throw new ExtismException(String.format("Unsupported return type: %s", original.t));
    }

    public void setNamespace(String name) {
        if (this.pointer != null) {
            LibExtism.INSTANCE.extism_function_set_namespace(this.pointer, name);
        }
    }

    public HostFunction withNamespace(String name) {
        this.setNamespace(name);
        return this;
    }

    public void free() {
        if (!this.freed) {
            LibExtism.INSTANCE.extism_function_free(this.pointer);
            this.freed = true;
        }
    }

    static class Callback<T> implements LibExtism.InternalExtismFunction {
        private final ExtismFunction f;
        private final Optional<T> userData;

        public Callback(ExtismFunction f, Optional<T> userData) {
            this.f = f;
            this.userData = userData;
        }

        @Override
        public void invoke(Pointer currentPlugin, LibExtism.ExtismVal ins, int nInputs, LibExtism.ExtismVal outs, int nOutputs, Pointer data) {

            LibExtism.ExtismVal[] inputs;
            LibExtism.ExtismVal[] outputs;

            if (outs == null) {
                if (nOutputs > 0) {
                    throw new ExtismException("Output array is null but nOutputs is greater than 0");
                }
                outputs = new LibExtism.ExtismVal[0];
            } else {
                outputs = (LibExtism.ExtismVal[]) outs.toArray(nOutputs);
            }

            if (ins == null) {
                if (nInputs > 0) {
                    throw new ExtismException("Input array is null but nInputs is greater than 0");
                }
                inputs = new LibExtism.ExtismVal[0];
            } else {
                inputs = (LibExtism.ExtismVal[]) ins.toArray(nInputs);
            }

            f.invoke(new ExtismCurrentPlugin(currentPlugin), inputs, outputs, userData);

            for (LibExtism.ExtismVal output : outputs) {
                convertOutput(output, output);
            }
        }
    }
}
