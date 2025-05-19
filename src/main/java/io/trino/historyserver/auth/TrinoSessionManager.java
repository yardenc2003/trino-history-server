package io.trino.historyserver.auth;

import io.trino.historyserver.exception.TrinoAuthException;
import org.springframework.stereotype.Service;

@Service
public interface TrinoSessionManager
{
    String getSessionCookie(String coordinatorUrl)
            throws TrinoAuthException;

    void refreshSessionCookie(String coordinatorUrl)
            throws TrinoAuthException;
}