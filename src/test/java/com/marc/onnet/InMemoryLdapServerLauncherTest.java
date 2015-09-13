package com.marc.onnet;

import org.junit.Test;
/**
 * Unit test for simple InMemoryLdapServer.
 */

public class InMemoryLdapServerLauncherTest {

    @Test
    public void test_launchServer() throws Exception {
        InMemoryLdapServer server = new InMemoryLdapServer("42085");
        server.startServer();
    }
}
