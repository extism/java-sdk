package org.extism.sdk;

import com.sun.jna.Pointer;
import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.manifest.MemoryOptions;
import org.extism.sdk.wasm.UrlWasmSource;
import org.extism.sdk.wasm.WasmSourceResolver;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.extism.sdk.TestWasmSources.CODE;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PluginTests {

    // static {
    //     Extism.setLogFile(Paths.get("/tmp/extism.log"), Extism.LogLevel.TRACE);
    // }

    @Test
    public void shouldInvokeFunctionWithMemoryOptions() {
        var manifest = new Manifest(List.of(CODE.pathWasmSource()), new MemoryOptions(0));
        assertThrows(ExtismException.class, () -> {
            Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        });
    }

    @Test
    public void shouldInvokeFunctionWithConfig() {
        //FIXME check if config options are available in wasm call
        var config = Map.of("key1", "value1");
        var manifest = new Manifest(List.of(CODE.pathWasmSource()), null, config);
        var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");
    }

    @Test
    public void shouldInvokeFunctionFromFileWasmSource() {
        var manifest = new Manifest(CODE.pathWasmSource());
        var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
        assertThat(output).isEqualTo("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");
    }

    @Test
    public void shouldInvokeFunctionFromUrlWasmSource() {
        var url = "https://github.com/extism/plugins/releases/latest/download/count_vowels.wasm";
        var config = Map.of("vowels", "aeiouyAEIOUY");
        var manifest = new Manifest(List.of(UrlWasmSource.fromUrl(url)), null, config);
        var plugin = new Plugin(manifest, false, null);
        var output = plugin.call("count_vowels", "Yellow, World!");
        assertThat(output).isEqualTo("{\"count\":4,\"total\":4,\"vowels\":\"aeiouyAEIOUY\"}");
    }

//    @Test
//    public void shouldInvokeFunctionFromUrlWasmSourceHostFuncs() {
//        var url = "https://github.com/extism/plugins/releases/latest/download/count_vowels_kvstore.wasm";
//        var manifest = new Manifest(List.of(UrlWasmSource.fromUrl(url)));
//
//        // Our application KV store
//        // Pretend this is redis or a database :)
//        var kvStore = new HashMap<String, byte[]>();
//
//        ExtismFunction kvWrite = (plugin, params, returns, data) -> {
//            System.out.println("Hello from Java Host Function!");
//            var key = plugin.inputString(params[0]);
//            var value = plugin.inputBytes(params[1]);
//            System.out.println("Writing to key " +  key);
//            kvStore.put(key, value);
//        };
//
//        ExtismFunction kvRead = (plugin, params, returns, data) -> {
//            System.out.println("Hello from Java Host Function!");
//            var key = plugin.inputString(params[0]);
//            System.out.println("Reading from key " +  key);
//            var value = kvStore.get(key);
//            if (value == null) {
//                // default to zeroed bytes
//                var zero = new byte[]{0,0,0,0};
//                plugin.returnBytes(returns[0], zero);
//            } else {
//                plugin.returnBytes(returns[0], value);
//            }
//        };
//
//        HostFunction kvWriteHostFn = new HostFunction<>(
//                "kv_write",
//                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64, LibExtism.ExtismValType.I64},
//                new LibExtism.ExtismValType[0],
//                kvWrite,
//                Optional.empty()
//        );
//
//        HostFunction kvReadHostFn = new HostFunction<>(
//                "kv_read",
//                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
//                new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64},
//                kvRead,
//                Optional.empty()
//        );
//
//        HostFunction[] functions = {kvWriteHostFn, kvReadHostFn};
//        var plugin = new Plugin(manifest, false, functions);
//        var output = plugin.call("count_vowels", "Hello, World!");
//    }

     @Test
     public void shouldInvokeFunctionFromByteArrayWasmSource() {
         var manifest = new Manifest(CODE.byteArrayWasmSource());
         var output = Extism.invokeFunction(manifest, "count_vowels", "Hello World");
         assertThat(output).isEqualTo("{\"count\":3,\"total\":3,\"vowels\":\"aeiouAEIOU\"}");
     }

    @Test
    public void shouldFailToInvokeUnknownFunction() {
        assertThrows(ExtismException.class, () -> {
            var manifest = new Manifest(CODE.pathWasmSource());
            Extism.invokeFunction(manifest, "unknown", "dummy");
        }, "Function not found: unknown");
    }

    @Test
    public void shouldAllowInvokeFunctionFromFileWasmSourceApiUsageExample() {

        var wasmSourceResolver = new WasmSourceResolver();
        var manifest = new Manifest(wasmSourceResolver.resolve(CODE.getWasmFilePath()));

        var functionName = "count_vowels";
        var input = "Hello World";

        try (var plugin = new Plugin(manifest, false, null)) {
            var output = plugin.call(functionName, input);
            assertThat(output).contains("\"count\":3");
        }
    }

    @Test
    public void shouldAllowInvokeFunctionFromFileWasmSourceMultipleTimes() {
        var manifest = new Manifest(CODE.pathWasmSource());
        var functionName = "count_vowels";
        var input = "Hello World";

        try (var plugin = new Plugin(manifest, false, null)) {
            var output = plugin.call(functionName, input);
            assertThat(output).contains("\"count\":3");

            output = plugin.call(functionName, input);
            assertThat(output).contains("\"count\":3");
        }
    }

    @Test
    public void shouldAllowInvokeHostFunctionFromPDK() {
        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        class MyUserData extends HostUserData {
            private String data1;
            private int data2;

            public MyUserData(String data1, int data2) {
                super();
                this.data1 = data1;
                this.data2 = data2;
            }
        }

        ExtismFunction helloWorldFunction = (ExtismFunction<MyUserData>) (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
            System.out.println(String.format("Input string received from plugin, %s", plugin.inputString(params[0])));

            int offs = plugin.alloc(4);
            Pointer mem = plugin.memory();
            mem.write(offs, "test".getBytes(), 0, 4);
            returns[0].v.i64 = offs;

            data.ifPresent(d -> System.out.println(String.format("Host user data, %s, %d", d.data1, d.data2)));
        };

        HostFunction helloWorld = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.of(new MyUserData("test", 2))
        );

        HostFunction[] functions = {helloWorld};

        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
        String functionName = "count_vowels";

        try (var plugin = new Plugin(manifest, true, functions)) {
            var output = plugin.call(functionName, "this is a test");
            assertThat(output).isEqualTo("test");
        }
    }

    @Test
    public void shouldAllowInvokeHostFunctionWithoutUserData() {

        var parametersTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};
        var resultsTypes = new LibExtism.ExtismValType[]{LibExtism.ExtismValType.I64};

        ExtismFunction helloWorldFunction = (plugin, params, returns, data) -> {
            System.out.println("Hello from Java Host Function!");
            System.out.println(String.format("Input string received from plugin, %s", plugin.inputString(params[0])));

            int offs = plugin.alloc(4);
            Pointer mem = plugin.memory();
            mem.write(offs, "test".getBytes(), 0, 4);
            returns[0].v.i64 = offs;

            assertThat(data.isEmpty());
        };

        HostFunction f = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        )
                .withNamespace("extism:host/user");

        HostFunction g = new HostFunction<>(
                "hello_world",
                parametersTypes,
                resultsTypes,
                helloWorldFunction,
                Optional.empty()
        )
                .withNamespace("test");

        HostFunction[] functions = {f,g};

        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
        String functionName = "count_vowels";

        try (var plugin = new Plugin(manifest, true, functions)) {
            var output = plugin.call(functionName, "this is a test");
            assertThat(output).isEqualTo("test");
        }
    }


    @Test
    public void shouldFailToInvokeUnknownHostFunction() {
        Manifest manifest = new Manifest(Arrays.asList(CODE.pathWasmFunctionsSource()));
        String functionName = "count_vowels";

        try {
            var plugin = new Plugin(manifest, true, null);
            plugin.call(functionName, "this is a test");
        }  catch (ExtismException e) {
            assertThat(e.getMessage()).contains("unknown import: `extism:host/user::hello_world` has not been defined");
        }
    }

}
