package org.extism.sdk;

import org.extism.sdk.manifest.Manifest;
import org.extism.sdk.wasm.WasmSourceResolver;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Extism convenience functions.
 */
public class Extism {

    private static final WasmSourceResolver DEFAULT_RESOLVER = new WasmSourceResolver();

    /**
     * Creates a {@link Manifest} from the given {@code path}.
     * @param path
     * @return
     */
    public static Manifest manifestFromPath(Path path) {
        Objects.requireNonNull(path, "path");
        return new Manifest(DEFAULT_RESOLVER.resolve(path));
    }

    /**
     * Creates a {@link Manifest} from the given {@code name} and {@code bytes}.
     * @param name
     * @param bytes
     * @return
     */
    public static Manifest manifestFromBytes(String name, byte[] bytes) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(bytes, "bytes");
        return new Manifest(DEFAULT_RESOLVER.resolve(name, bytes));
    }

    /**
     * Creates a {@link Manifest} from the given {@code url}.
     * @param url
     * @return
     */
    public static Manifest manifestFromUrl(String url) {
        Objects.requireNonNull(url, "url");
        return new Manifest(DEFAULT_RESOLVER.resolve(URI.create(url)));
    }

    /**
     * Invokes the named {@code function} from the {@link Manifest} with the given {@code input}.
     * This is a convenience method. Prefer initializing and using a {@link Plugin} where possible.
     *
     * @param manifest the manifest containing the function
     * @param function the name of the function to call
     * @param input    the input as string
     * @return the output as string
     * @throws ExtismException if the call fails
     */
    public static String invokeFunction(Manifest manifest, String function, String input) throws ExtismException {
        try (var plugin = new Plugin(manifest, false, null)) {
            return plugin.call(function, input);
        }
    }
}
