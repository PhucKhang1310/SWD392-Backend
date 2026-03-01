package swd392.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Order(2) // runs after CartStatusMigration (which has no @Order, defaulting to lowest priority)
@RequiredArgsConstructor
public class SqlFolderRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @SuppressWarnings("NullableProblems")
    public void run(ApplicationArguments args) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:sql/*.sql");

        if (resources.length == 0) {
            log.info("No SQL files found in classpath:sql/");
            return;
        }

        // Sort alphabetically so execution order is predictable (e.g. 01_users.sql, 02_products.sql)
        Arrays.sort(resources, java.util.Comparator.comparing(
                r -> r.getFilename() != null ? r.getFilename() : ""
        ));

        log.info("Found {} SQL file(s) in classpath:sql/ — executing in order:", resources.length);
        for (Resource r : resources) {
            log.info("  {}", r.getFilename());
        }

        for (Resource resource : resources) {
            log.info("Running: {}", resource.getFilename());
            executeScript(resource);
        }

        log.info("SQL folder execution complete.");
    }

    private void executeScript(Resource resource) throws Exception {
        // Detect encoding: read raw bytes to check for UTF-16 LE BOM (FF FE)
        byte[] raw = resource.getInputStream().readAllBytes();
        java.nio.charset.Charset charset = StandardCharsets.UTF_8;
        int byteOffset = 0;
        if (raw.length >= 2 && (raw[0] & 0xFF) == 0xFF && (raw[1] & 0xFF) == 0xFE) {
            charset = java.nio.charset.Charset.forName("UTF-16LE");
            byteOffset = 2; // skip the BOM bytes
        } else if (raw.length >= 3 && (raw[0] & 0xFF) == 0xEF && (raw[1] & 0xFF) == 0xBB && (raw[2] & 0xFF) == 0xBF) {
            byteOffset = 3; // skip UTF-8 BOM
        }
        String content = new String(raw, byteOffset, raw.length - byteOffset, charset);

        // Split on GO (on its own line, case-insensitive)
        List<String> batches = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : content.split("\\r?\\n")) {
            if (line.trim().equalsIgnoreCase("GO")) {
                String batch = current.toString().trim();
                if (!batch.isEmpty()) batches.add(batch);
                current.setLength(0);
            } else {
                current.append(line).append("\n");
            }
        }
        String last = current.toString().trim();
        if (!last.isEmpty()) batches.add(last);

        int executed = 0, skipped = 0;
        for (String batch : batches) {
            String trimmed = batch.trim();
            if (trimmed.isEmpty()) { skipped++; continue; }

            String upper = trimmed.toUpperCase();
            // Skip non-executable or redundant batches
            if (upper.startsWith("USE ")
                    || upper.startsWith("SET ANSI_NULLS")
                    || upper.startsWith("SET QUOTED_IDENTIFIER")
                    || upper.startsWith("SET ANSI_PADDING")
                    // "ALTER TABLE ... CHECK CONSTRAINT" just re-enables a constraint already
                    // enforced by the preceding "WITH CHECK ADD" — safe to skip
                    || (upper.startsWith("ALTER TABLE") && upper.contains("CHECK CONSTRAINT") && !upper.contains("ADD"))
                    || (upper.startsWith("/*") && !upper.contains("CREATE") && !upper.contains("INSERT")
                        && !upper.contains("ALTER") && !upper.contains("DROP") && !upper.contains("IF"))) {
                skipped++;
                continue;
            }

            try {
                jdbcTemplate.execute(trimmed);
                executed++;
            } catch (Exception e) {
                log.warn("  [{}] Batch failed (skipping): {}", resource.getFilename(),
                        e.getMessage().split("\n")[0]);
            }
        }
        log.info("  Done — {} batches executed, {} skipped.", executed, skipped);
    }
}
