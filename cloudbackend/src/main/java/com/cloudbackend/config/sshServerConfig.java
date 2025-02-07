package com.cloudbackend.config;

import com.cloudbackend.entity.User;
import com.cloudbackend.repository.UserRepository;
import com.cloudbackend.service.CustomUserDetailsService;
import com.cloudbackend.service.TerminalService;
import com.cloudbackend.ssh.SshTerminal;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.sshd.server.SshServer;

import java.io.IOException;

@Component
public class sshServerConfig {

    @Value("${ssh.port:2222}")
    private int sshPort;

    private SshServer sshServer;

    @Autowired
    private TerminalService terminalService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void startSshServer() throws IOException {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(sshPort);

        // Set up password authentication
        sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                try {
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    return userDetailsService.getPasswordEncoder().matches(password, user.getPassword());
                } catch (Exception e) {
                    return false;
                }
            }
        });

        // Set up a shell for executing commands
        sshServer.setShellFactory(new InteractiveProcessShellFactory() {
            public SshTerminal createShell(ServerSession session) {
                User user = userRepository.findByUsername(session.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                return new SshTerminal(terminalService, user);
            }
        });

        sshServer.start();
        System.out.println("SSH server started on port " + sshPort);
    }

    @PreDestroy
    public void stopSshServer() throws IOException {
        if (sshServer != null) {
            sshServer.stop();
            System.out.println("SSH server stopped");
        }
    }
}