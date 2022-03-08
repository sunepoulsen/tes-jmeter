package dk.sunepoulsen.tes.jmeter;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.trilead.ssh2.StreamGobbler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;

@Slf4j
public class JMeterExecutor {
    public static final File WORKING_DIR = new File("build/test-results/jmeter");
    public static final File STATISTIC_RESULT_FILE = new File("build/test-results/jmeter/report-html/statistics.json");

    Integer containerPort;

    public void prepareExecutor(GenericContainer container) throws IOException {
        findExternalPorts(container);
        ensureWorkingDirectory();
        createPropertyFile();
    }

    public boolean runTests() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("jmeter", "-n", "-t", "../../../src/test/resources/stress-test.jmx", "-p", "stress-test.properties", "-l", "results.jtl", "-e", "-o", "report-html");
        processBuilder.directory(WORKING_DIR);

        log.info( "Executing {}", String.join(" ", processBuilder.command()) );
        Process process = processBuilder.start();

        process.waitFor();
        String output = new String(process.getInputStream().readAllBytes());

        log.info( "Standard output:\n{}", output);
        log.info( "Exit code: {}", process.exitValue());

        return process.exitValue() == 0;

    }

    private void findExternalPorts(GenericContainer container) {
        containerPort = container.getMappedPort(8080);
        log.info("Container {} is accessible on port ${}", container.getDockerImageName(), containerPort);
    }

    private void ensureWorkingDirectory() throws IOException {
        if (WORKING_DIR.exists()) {
            Files.walk(WORKING_DIR.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }

        WORKING_DIR.mkdirs();
    }

    private void createPropertyFile() throws IOException {
        String propertyFile = "/user.properties";
        String profile = System.getProperty("stress.test.profile");
        if (profile != null && !profile.isEmpty()) {
            propertyFile = String.format("/user-%s.properties", profile);
        }

        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream(propertyFile));

        properties.put("service.port", containerPort.toString());

        log.info("Using {} property file with the stress test", propertyFile);
        properties.store(new FileOutputStream(WORKING_DIR.getAbsolutePath() + "/stress-test.properties"), "");
    }
}
