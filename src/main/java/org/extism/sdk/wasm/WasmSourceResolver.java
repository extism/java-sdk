package org.extism.sdk.wasm;

import org.extism.sdk.ExtismException;
import org.extism.sdk.support.Hashing;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

/**
 * Resolves {@link WasmSource} from {@link Path Path's} or raw bytes.
 */
public class WasmSourceResolver {

    public PathWasmSource resolve(Path path) {
        return resolve(null, path);
    }

    public PathWasmSource resolve(String name, Path path) {
        Objects.requireNonNull(path, "path");

        var wasmFile = path.toFile();
        var hash = hash(path);
        var wasmName = name == null ? wasmFile.getName() : name;

        return new PathWasmSource(wasmName, wasmFile.getAbsolutePath(), hash);
    }

    public ByteArrayWasmSource resolve(String name, byte[] bytes) {
        return new ByteArrayWasmSource(name, bytes, hash(bytes));
    }

    public ByteArrayWasmSource resolve(byte[] bytes) {
        return resolve("wasm@" + Arrays.hashCode(bytes), bytes);
    }

    public UrlWasmSource resolve(URI uri) {
        return UrlWasmSource.fromUrl(uri);
    }

    protected String hash(Path wasmFile) {
        try {
            return hash(Files.readAllBytes(wasmFile));
        } catch (IOException ioe) {
            throw new ExtismException("Could not compute hash from path: " + wasmFile, ioe);
        }
    }

    protected String hash(byte[] bytes) {
        return Hashing.sha256HexDigest(bytes);
    }
}
