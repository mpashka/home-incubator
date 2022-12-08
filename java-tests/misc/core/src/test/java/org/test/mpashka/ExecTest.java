package org.test.mpashka;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Slf4j
public class ExecTest {
    @Test
    public void test() throws Exception {
        ProcessBuilder builder = new ProcessBuilder()
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.command("bash", "-c", "echo \"$PATH\"");
        Process process = builder.start();

        log.info("Stop process");
        process.destroy();
        process.waitFor();
    }
}
