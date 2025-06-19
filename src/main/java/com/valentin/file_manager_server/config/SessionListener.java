package com.valentin.file_manager_server.config;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

@Slf4j
@Component
public class SessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        log.info("Session created: ID={}, CreationTime={}, Timeout={} sec",
                session.getId(),
                new java.util.Date(session.getCreationTime()),
                session.getMaxInactiveInterval());

        Enumeration<String> attrs = session.getAttributeNames();
        if (attrs.hasMoreElements()) {
            log.info("Session {} already has attributes:", session.getId());
            while (attrs.hasMoreElements()) {
                String name = attrs.nextElement();
                log.info("     {} = {}", name, session.getAttribute(name));
            }
        } else {
            log.info("Session {} has no attributes at creation.", session.getId());
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        log.info("Session destroyed: ID={}, LastAccessedTime={}",
                session.getId(),
                new java.util.Date(session.getLastAccessedTime()));

        Enumeration<String> attrs = session.getAttributeNames();
        if (attrs.hasMoreElements()) {
            log.info("Attributes at session {} destruction:", session.getId());
            while (attrs.hasMoreElements()) {
                String name = attrs.nextElement();
                log.info("     {} = {}", name, session.getAttribute(name));
            }
        } else {
            log.info("Session {} had no attributes at destruction.", session.getId());
        }
    }
}

