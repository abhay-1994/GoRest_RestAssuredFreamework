package com.gorest.base;

import com.gorest.config.ConfigManager;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;

/**
 * Parent for tests that write data (POST / PUT / PATCH / DELETE) and therefore need
 * a GoREST bearer token.
 *
 * <p>Without a token those tests are skipped with a clear reason instead of failing
 * with a confusing 401.</p>
 */
public class AuthenticatedTest extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void verifyTokenIsConfigured() {
        if (!ConfigManager.isTokenConfigured()) {
            throw new SkipException("No GoREST token configured. Run with -Dtoken=<your-token> "
                    + "or set it in src/main/resources/config.properties");
        }
    }
}
