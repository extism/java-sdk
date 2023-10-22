package org.extism.sdk.support;

import org.extism.sdk.Extism;
import org.extism.sdk.ExtismException;
import org.extism.sdk.LibExtism;

import java.nio.file.Path;
import java.util.Objects;

public final class Logging {

    private Logging() {
        // prevent instantiation
    }

    /**
     * Configure a log file with the given {@link Path} and configure the given {@link LogLevel}.
     *
     * @param path
     * @param level
     *
     * @deprecated will be replaced with better logging API.
     */
    @Deprecated(forRemoval = true)
    public static void setLogFile(Path path, LogLevel level) {

        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(level, "level");

        var result = LibExtism.INSTANCE.extism_log_file(path.toString(), level.getLevel());
        if (!result) {
            var error = String.format("Could not set extism logger to %s with level %s", path, level);
            throw new ExtismException(error);
        }
    }

    /**
     * Error levels for the Extism logging facility.
     *
     * @see Logging#setLogFile(Path, LogLevel)
     */
    public enum LogLevel {

        INFO("info"), //

        DEBUG("debug"), //

        WARN("warn"), //

        TRACE("trace"), //

        ERROR("error"), //

        OFF("off"), //
        ;

        private final String level;

        LogLevel(String level) {
            this.level = level;
        }

        public String getLevel() {
            return level;
        }
    }
}
