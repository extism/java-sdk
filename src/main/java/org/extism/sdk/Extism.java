package org.extism.sdk;

import org.extism.sdk.manifest.Manifest;

/**
 * Extism convenience functions.
 */
public class Extism {

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
