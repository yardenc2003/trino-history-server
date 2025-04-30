package io.trino.historyserver.service.session;

import io.trino.historyserver.exception.TrinoAuthFailed;
import org.springframework.stereotype.Service;

@Service
public interface TrinoSessionManager
{
    String getSessionCookie(String coordinatorUrl)
            throws TrinoAuthFailed;

    void refreshSessionCookie(String coordinatorUrl)
            throws TrinoAuthFailed;
}