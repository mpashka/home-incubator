package org.mpashka.test.java8.jfr;

import java.util.EnumMap;
import java.util.Map;

import jnr.ffi.LibraryLoader;
import jnr.ffi.LibraryOption;
import jnr.ffi.Platform;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestNuma {

    @Test
    public void testNumaNodesCount() {
        // Add library options to customize library behavior
        Map<LibraryOption, Object> libraryOptions = new EnumMap<>(LibraryOption.class);
        libraryOptions.put(LibraryOption.LoadNow, true); // load immediately instead of lazily (ie on first use)
        libraryOptions.put(LibraryOption.IgnoreError, true); // calls shouldn't save last errno after call
        log.info("Libc :{}", Platform.getNativePlatform().getStandardCLibraryName()); // platform specific name for libC
        String libName = "libnuma.so";

        LibNuma libNuma = LibraryLoader.loadLibrary(
                LibNuma.class,
                libraryOptions,
                libName
        );

        log.info("Nodes: {}", libNuma.numa_num_configured_nodes());
    }
}
