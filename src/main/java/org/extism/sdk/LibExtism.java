package org.extism.sdk;

import com.sun.jna.*;

/**
 * Wrapper around the Extism library.
 */
public interface LibExtism extends Library {

    /**
     * Holds the extism library instance.
     * Resolves the extism library based on the resolution algorithm defined in {@link com.sun.jna.NativeLibrary}.
     */
    LibExtism INSTANCE = Native.load("extism", LibExtism.class);

    /**
     * Host function callback
     */
    interface InternalExtismFunction extends Callback {

        /**
         * Host function implementation.
         *
         * @param plugin
         * @param inputs
         * @param nInputs
         * @param outputs
         * @param nOutputs
         * @param data
         */
        void invoke(Pointer plugin, ExtismVal inputs, int nInputs, ExtismVal outputs, int nOutputs, Pointer data);
    }

    /**
     * `Holds the type and value of a function argument/return.
     */
    @Structure.FieldOrder({"t", "v"})
    class ExtismVal extends Structure {
        public int t;
        public ExtismValUnion v;
    }

    /**
     * A union type for host function argument/return values.
     */
    class ExtismValUnion extends Union {
        public int i32;
        public long i64;
        public float f32;
        public double f64;
    }

    /**
     * An enumeration of all possible value types in WebAssembly.
     */
    enum ExtismValType {

        /**
         * Signed 32 bit integer.
         */
        I32(0),

        /**
         * Signed 64 bit integer.
         */
        I64(1),

        /**
         * Floating point 32 bit.
         */
        F32(2),

        /**
         * Floating point 64 bit.
         */
        F64(3),

        /**
         * A 128 bit number
         */
        V128(4),

        /**
         * A reference to a Wasm function.
         */
        FuncRef(5),

        /**
         * A reference to opaque data in the Wasm instance.
         */
        ExternRef(6);

        public final int v;

        ExtismValType(int value) {
            this.v = value;
        }
    }

    /**
     * Create a new host function.
     *
     * @param name function name
     * @param inputs argument types
     * @param nInputs number of argument types
     * @param outputs return types
     * @param nOutputs number of return types
     * @param func the function to call
     * @param userData a pointer that will be passed to the function when it's called
     * @param freeUserData a callback to release the `user_data` value when the resulting ExtismFunction is freed.
     * @return Returns a pointer to a new ExtismFunction or {@literal  null} if the {@code name} argument is invalid.
     */
    Pointer extism_function_new(String name,
                                int[] inputs,
                                int nInputs,
                                int[] outputs,
                                int nOutputs,
                                InternalExtismFunction func,
                                Pointer userData,
                                Pointer freeUserData);

    /**
     * Get the length of an allocated block
     * NOTE: this should only be called from host functions.
     */
    int extism_current_plugin_memory_length(Pointer plugin, long n);

    /**
     * Returns a pointer to the memory of the currently running plugin
     * NOTE: this should only be called from host functions.
     */
    Pointer extism_current_plugin_memory(Pointer plugin);

    /**
     * Allocate a memory block in the currently running plugin
     * NOTE: this should only be called from host functions.
     */
    int extism_current_plugin_memory_alloc(Pointer plugin, long n);

    /**
     * Free an allocated memory block
     * NOTE: this should only be called from host functions.
     */
    void extism_current_plugin_memory_free(Pointer plugin, long ptr);

    /**
     * Sets the logger to the given path with the given level of verbosity
     *
     * @param path     The file path of the logger
     * @param logLevel The level of the logger
     * @return true if successful
     */
    boolean extism_log_file(String path, String logLevel);

    /**
     * Returns the error associated with a @{@link Plugin}
     *
     * @param pluginPointer
     * @return
     */
    String extism_plugin_error(Pointer pluginPointer);

    /**
     * Create a new plugin.
     *
     * @param wasm           is a WASM module (wat or wasm) or a JSON encoded manifest
     * @param wasmSize       the length of the `wasm` parameter
     * @param functions      host functions
     * @param nFunctions     the number of host functions
     * @param withWASI       enables/disables WASI
     * @param errmsg         get the error message if the return value is null
     * @return id of the plugin or {@literal -1} in case of error
     */
    Pointer extism_plugin_new(byte[] wasm, long wasmSize, Pointer[] functions, int nFunctions, boolean withWASI, Pointer[] errmsg);

    /**
     * Free error message from `extism_plugin_new`
     */
    void extism_plugin_new_error_free(Pointer errmsg);

    /**
     * Returns the Extism version string
     */
    String extism_version();


    /**
     * Calls a function from the @{@link Plugin} at the given {@code pluginIndex}.
     *
     * @param pluginPointer
     * @param function_name  is the function to call
     * @param data           is the data input data
     * @param dataLength     is the data input data length
     * @return the result code of the plugin call. {@literal -1} in case of error, {@literal 0} otherwise.
     */
    int extism_plugin_call(Pointer pluginPointer, String function_name, byte[] data, int dataLength);

    /**
     * Returns 
     * @return the length of the output data in bytes.
     */
    int extism_plugin_output_length(Pointer pluginPointer);

    /**
   
     * @return
     */
    Pointer extism_plugin_output_data(Pointer pluginPointer);

    /**
     * Remove a plugin from the
     */
    void extism_plugin_free(Pointer pluginPointer);

    /**
     * Update plugin config values, this
     * @param json
     * @param jsonLength
     * @return {@literal true} if update was successful
     */
    boolean extism_plugin_config(Pointer pluginPointer, byte[] json, int jsonLength);

    /**
     * Get a handle for plugin cancellation
     * @param pluginPointer
     * @return a Pointer to a cancellation handle
     */
    Pointer extism_plugin_cancel_handle(Pointer pluginPointer);

    /**
     * Cancel a running plugin.
     * @param cancelHandle
     * @return {@literal true} if cancellation was successful
     */
    boolean extism_plugin_cancel(Pointer cancelHandle);

    /**
     * Set the namespace of an `ExtismFunction`
     * @param pluginPointer
     * @param namespace
     */
    void extism_function_set_namespace(Pointer pluginPointer, String namespace);

    /**
     * Helper function to get the length of a string represented by the pointer s.
     * @param stringPointer
     * @return
     */
    int strlen(Pointer stringPointer);
}
