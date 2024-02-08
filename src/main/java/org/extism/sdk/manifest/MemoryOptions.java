package org.extism.sdk.manifest;

import com.google.gson.annotations.SerializedName;

/**
 * Configures memory for the Wasm runtime.
 * Memory is described in units of pages (64KB) and represent contiguous chunks of addressable memory.
 *
 * @param maxPages Max number of pages.
 * @param httpMax Max number of bytes returned by HTTP requests using extism_http_request
 */
public class MemoryOptions {
    @SerializedName("max_pages")
    private final Integer maxPages;

    @SerializedName("max_http_response_bytes")
    private final Integer maxHttpResponseBytes;

    public MemoryOptions(Integer maxPages, Integer httpMax) {
        this.maxPages = maxPages;
        this.maxHttpResponseBytes = httpMax;
    }
}
