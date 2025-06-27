package com.deliverar.pagos.infrastructure.security;

import com.unboundid.ldap.sdk.*;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLSocketFactory;

@Slf4j
@Service
public class ActiveDirectoryService {

    @Value("${ad.domain:DELIVERAR}")
    private String domain;
    
    @Value("${ad.host:ad.deliver.ar}")
    private String ldapHost;
    
    @Value("${ad.port:389}")
    private int ldapPort;

    public boolean authenticate(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            log.warn("Authentication failed: username or password is null or empty");
            return false;
        }

        String fqdnUser;
        if (username.contains("@")) {
            fqdnUser = username;
        } else {
            fqdnUser = domain + "\\" + username;
        }
        
        log.debug("Attempting authentication for user: {}", fqdnUser);
        LDAPConnection connection = null;

        try {
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();

            connection = new LDAPConnection(ldapHost, ldapPort);
            
            connection.processExtendedOperation(
                    new StartTLSExtendedRequest(sslSocketFactory)
            );

            BindResult bindResult = connection.bind(fqdnUser, password);

            if (bindResult.getResultCode() == ResultCode.SUCCESS) {
                log.info("User '{}' authenticated successfully via Active Directory", username);
                return true;
            } else {
                log.warn("Authentication failed for user '{}': {}", username, bindResult.getDiagnosticMessage());
                return false;
            }

        } catch (LDAPException e) {
            log.error("LDAP error authenticating user '{}': {}", username, e.getDiagnosticMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("General error authenticating user '{}': {}", username, e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}