package com.marc.onnet;

import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class InMemoryLdapClientConnectionTest {
    public static final String LDPA_LOCAL_PORT = "42085";
    private LdapClient ldapClient;
    private InMemoryLdapServer ldapServer;


    @Before
    public void setUp() throws Exception {
        ldapServer = new InMemoryLdapServer(LDPA_LOCAL_PORT);
        ldapServer.startServer();

        ldapClient = LdapClient.init("127.0.0.1", LDPA_LOCAL_PORT);
    }


    @After
    public void tearDown() throws Exception {
        ldapServer.stopServer();
    }


    @Test
    public void test_ladpConnectWithCompleteDn() throws Exception {
        ldapClient.withLogin("cn=Directory Manager").withPassword("password").connect();
        ldapClient.withLogin("uid=user_tr,ou=people,dc=example,dc=com").withPassword("hQOSq4VD").connect();
        ldapClient.withLogin("user_tr").withPassword("hQOSq4VD").connect();
    }


    @Test
    public void test_ladpConnectWithOnlyLogin() throws Exception {
        ldapClient.withLogin("user_tr").withPassword("hQOSq4VD").connect();
    }


    @Test
    public void test_ladpConnectionWithBadLogin() throws Exception {
        try {
            ldapClient.withLogin("badUSer").withPassword("AzhR1OMe").connect();
            fail();
        }
        catch (CommunicationException ex) {
            ; // Ok
        }
    }


    @Test
    public void test_ladpBadConfiguration() throws Exception {
        try {
            ldapClient.connect();
            fail();
        }
        catch (IllegalArgumentException ex) {
            ; // Ok
        }
    }

    private static class LdapClient {
        private String providerUrl;
        private String login;
        private String password;


        private LdapClient(String providerUrl) {
            this.providerUrl = providerUrl;
        }


        public static LdapClient init(String url, String port) {
            return new LdapClient("ldap://" + url + ":" + port);
        }


        public LdapClient withLogin(String login) {
            this.login = login;
            return this;
        }


        public LdapClient withPassword(String password) {
            this.password = password;
            return this;
        }


        public LdapClient connect() throws CommunicationException {
            if (providerUrl == null || login == null || password == null) {
                throw new IllegalArgumentException("Configuration pourrie !");
            }
            tryLoginOnLdap(login, password, providerUrl);
            return this;
        }


        private void tryLoginOnLdap(String userDn, String password, String providerUrl) throws CommunicationException {
            Hashtable<String, Object> env = new Hashtable<String, Object>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.PROVIDER_URL, providerUrl);
            env.put(Context.SECURITY_PRINCIPAL, userDn);
            env.put(Context.SECURITY_CREDENTIALS, password);

            try {
                new InitialLdapContext(env, null).close();
            }
            catch (AuthenticationException e) {
                e.printStackTrace();
                throw new CommunicationException();
            }
            catch (NamingException e) {
                e.printStackTrace();
                throw new CommunicationException();
            }
        }
    }
}

