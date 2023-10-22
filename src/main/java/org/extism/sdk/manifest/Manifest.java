package org.extism.sdk.manifest;

import com.google.gson.annotations.SerializedName;
import org.extism.sdk.wasm.WasmSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The `Manifest` type is used to configure the runtime and specify how to load modules.
 */
public class Manifest {

    /**
     * Holds the WebAssembly modules, the `main` module should be named `main` or listed last.
     */
    @SerializedName("wasm")
    private final List<WasmSource> sources;

    /**
     * Holds the memory options
     */
    @SerializedName("memory")
    private final MemoryOptions memoryOptions;

    /**
     * Specifies which hosts may be accessed via HTTP, if this is empty then no hosts may be accessed. Wildcards may be used.
     */
    // FIXME remove this and related stuff if not supported in java-sdk
    @SerializedName("allowed_hosts")
    private final List<String> allowedHosts;

    /**
     *  Specifies which paths should be made available on disk when using WASI.
     *  This is a mapping from the path on disk to the path it should be available inside the plugin.
     *  For example, `".": "/tmp"` would mount the current directory as `/tmp` inside the module
     */
    @SerializedName("allowed_paths")
    private final Map<String, String> allowedPaths;

    /**
     * Config values are made accessible using the PDK `extism_config_get` function
     */
    @SerializedName("config")
    private final Map<String, String> config;

    /**
     * Creates a new Manifest with an empty list of wasm sources.
     */
    public Manifest() {
        this(new ArrayList<>(), null, null, null, null);
    }

    public Manifest(WasmSource source) {
        this(List.of(source));
    }

    public Manifest(List<WasmSource> sources) {
        this(sources, null, null, null, null);
    }

    public Manifest(List<WasmSource> sources, MemoryOptions memoryOptions) {
        this(sources, memoryOptions, null, null, null);
    }

    public Manifest(List<WasmSource> sources, MemoryOptions memoryOptions, Map<String, String> config) {
        this(sources, memoryOptions, config, null, null);
    }

    public Manifest(List<WasmSource> sources, MemoryOptions memoryOptions, Map<String, String> config, List<String> allowedHosts) {
        this(sources, memoryOptions, config, allowedHosts, null);
    }

    public Manifest(List<WasmSource> sources, MemoryOptions memoryOptions, Map<String, String> config, List<String> allowedHosts, Map<String, String> allowedPaths) {
        this.sources = sources;
        this.memoryOptions = memoryOptions;
        this.config = config;
        this.allowedHosts = allowedHosts;
        this.allowedPaths = allowedPaths;
    }

    /**
     * Returns a new {@link Manifest} with the {@link WasmSource}.
     * @param source
     * @return
     */
    public Manifest withSource(WasmSource source) {
        return new Manifest(List.of(source), memoryOptions, config, allowedHosts, allowedPaths);
    }

    /**
     * Returns a new {@link Manifest} with the {@link WasmSource} added to the end of the current wasm sources.
     * @param source
     * @return
     */
    public Manifest withAdditionalSource(WasmSource source) {

        var sources = new ArrayList<>(this.sources);
        sources.add(source);

        return new Manifest(sources, memoryOptions, config, allowedHosts, allowedPaths);
    }

    /**
     * Returns a new {@link Manifest} with the given {@link MemoryOptions}.
     * @param memoryOptions
     * @return
     */
    public Manifest withMemoryOptions(MemoryOptions memoryOptions) {
        return new Manifest(sources, memoryOptions, config, allowedHosts, allowedPaths);
    }

    /**
     * Returns a new {@link Manifest} with the given config.
     * @param config
     * @return
     */
    public Manifest withConfig(Map<String, String> config) {
        return new Manifest(sources, memoryOptions, config, allowedHosts, allowedPaths);
    }

    /**
     * Returns a new {@link Manifest} with the given allowed hosts.
     * @param allowedHosts
     * @return
     */
    public Manifest withAllowedHosts(List<String> allowedHosts) {
        return new Manifest(sources, memoryOptions, config, allowedHosts, allowedPaths);
    }

    /**
     * Returns a new {@link Manifest} with the given allowed paths.
     *
     * @param allowedPaths
     * @return
     */
    public Manifest withAllowedPaths(Map<String, String> allowedPaths) {
        return new Manifest(sources, memoryOptions, config, allowedHosts, allowedPaths);
    }

    public List<WasmSource> getSources() {
        return Collections.unmodifiableList(sources);
    }

    public MemoryOptions getMemoryOptions() {
        return memoryOptions;
    }

    public Map<String, String> getConfig() {
        if (config == null || config.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(config);
    }

    public List<String> getAllowedHosts() {
        if (allowedHosts == null || allowedHosts.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(allowedHosts);
    }

    public Map<String, String> getAllowedPaths() {
        if (allowedPaths == null || allowedPaths.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(allowedPaths);
    }
}
