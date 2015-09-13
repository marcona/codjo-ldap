package com.marc.onnet;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSimpleBindRequest;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SimpleBindRequest;

public class InMemoryLdapServer {
    private InMemoryDirectoryServer ds;
    private final String ldpaLocalPort;


    public InMemoryLdapServer(String ldpaLocalPort) {
        this.ldpaLocalPort = ldpaLocalPort;
    }


    public void startServer() throws LDAPException {
        // Create the configuration to use for the server.
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
        config.addAdditionalBindCredentials("cn=Directory Manager", "password");
        config.addInMemoryOperationInterceptor(new InMemoryOperationInterceptor() {
            @Override
            public void processSimpleBindRequest(InMemoryInterceptedSimpleBindRequest request) throws LDAPException {
                String bindDN = request.getRequest().getBindDN();
                if (!bindDN.contains("=")) { //Gestion du cas ou on ne passe qu'un login
                    String bindPattern = "uid=" + bindDN + ",ou=people,dc=example,dc=com";
                    request.setRequest(new SimpleBindRequest(bindPattern,
                                                             request.getRequest().getPassword().stringValue()));
                }
                super.processSimpleBindRequest(request);
            }
        });

        InMemoryListenerConfig listenerConfig = new InMemoryListenerConfig("test", null, Integer.parseInt(ldpaLocalPort), null, null, null);
        config.setListenerConfigs(listenerConfig);
        config.setSchema(null);
        // do not check (attribute) schema
        // Create the directory server instance, populate it with data from the
        // "test-data.ldif" file, and start listening for client connections.                  s
        ds = new InMemoryDirectoryServer(config);
        ds.importFromLDIF(true, "/home/marcona/dev/java/codjo/codjo-ldap/test-data.ldif");
        ds.startListening();
    }


    public static void main(String[] args) {
        InMemoryLdapServer server = new InMemoryLdapServer("42085");
        try {
            server.startServer();
            while (true) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    server.stopServer();
                }
            }
        }
        catch (LDAPException e) {
            e.printStackTrace();
        }
    }


    public void stopServer() {
        if (ds!=null) {
            ds.shutDown(true);
        }
    }
}
