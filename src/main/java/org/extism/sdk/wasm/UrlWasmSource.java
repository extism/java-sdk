package org.extism.sdk.wasm;

/**
 * WASM Source represented by a url.
 */
public class UrlWasmSource implements WasmSource {

    private final String name;

    private final String url;

    private final String hash;

    /**
     * Provides a quick way to instantiate with just a url
     *
     * @param url String url to the wasm file
     * @return
     */
    public static UrlWasmSource fromUrl(String url) {
        return new UrlWasmSource(null, url, null);
    }

    /**
     * Constructor
     * @param name
     * @param url
     * @param hash
     */
    public UrlWasmSource(String name, String url, String hash) {
        this.name = name;
        this.url = url;
        this.hash = hash;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String hash() {
        return hash;
    }

    public String url() {
        return url;
    }
}

