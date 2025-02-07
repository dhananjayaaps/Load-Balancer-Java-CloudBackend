package com.cloudbackend.config;

import com.cloudbackend.entity.User;
import com.cloudbackend.repository.UserRepository;
import com.cloudbackend.service.CustomUserDetailsService;
import com.cloudbackend.service.TerminalService;
import com.cloudbackend.ssh.SshTerminal;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.apache.sshd.server.SshServer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

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

    private final AuthenticationManager authenticationManager;

    public sshServerConfig(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostConstruct
    public void startSshServer() throws IOException {
        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(sshPort);

        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")));

        sshServer.setPasswordAuthenticator((username, password, session) -> {
            try {
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, password)
                );
                if (authentication.isAuthenticated()){
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                    session.setUsername(userDetails.getUsername());
                }
                return authentication.isAuthenticated();
            } catch (Exception e) {
                return false;
            }
        });

        sshServer.setShellFactory(session -> {
            User user = userRepository.findByUsername(session.getSession().getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return new SshTerminal(terminalService, user);
        });

        sshServer.start();
        System.out.println("✅ SSH server started on port " + sshPort);
    }

    @PreDestroy
    public void stopSshServer() throws IOException {
        if (sshServer != null) {
            sshServer.stop();
            System.out.println("❌ SSH server stopped");
        }
    }
}