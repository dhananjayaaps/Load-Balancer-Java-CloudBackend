package com.cloudbackend.ssh;

import com.cloudbackend.entity.User;
import com.cloudbackend.service.TerminalService;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SshTerminal implements Command, Runnable {

    private final TerminalService terminalService;
    private final User user;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private Thread thread;

    public SshTerminal(TerminalService terminalService, User user) {
        this.terminalService = terminalService;
        this.user = user;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void start(ChannelSession channel, Environment environment) throws IOException {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            out.write("Welcome to the SSH terminal!\n".getBytes());
            out.write("Available commands: mv, cp, ls, mkdir, ps, whoami, tree, nano\n".getBytes());
            out.flush();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                String command = new String(buffer, 0, len).trim();
                String[] parts = command.split(" ");
                String cmd = parts[0];
                String output;

                try {
                    switch (cmd) {
                        case "mv":
                            output = terminalService.moveFileOrDirectory(parts[1], parts[2], user);
                            break;
                        case "cp":
                            output = terminalService.copyFileOrDirectory(parts[1], parts[2], user);
                            break;
                        case "ls":
                            output = String.join("\n", terminalService.listFiles(parts.length > 1 ? parts[1] : "/", user));
                            break;
                        case "mkdir":
                            output = terminalService.createDirectory(parts.length > 2 ? parts[1] : "/", parts[parts.length - 1], user);
                            break;
                        case "ps":
                            output = terminalService.listProcesses();
                            break;
                        case "whoami":
                            output = terminalService.whoami(user);
                            break;
                        case "tree":
                            output = terminalService.listDirectoryTree(parts.length > 1 ? parts[1] : "/", user);
                            break;
                        case "nano":
                            output = terminalService.editFile(parts[1], parts.length > 2 ? parts[2] : "", user);
                            break;
                        default:
                            output = "Unknown command: " + cmd;
                            break;
                    }
                } catch (Exception e) {
                    output = "Error: " + e.getMessage();
                }

                out.write((output + "\n").getBytes());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {

    }

    @Override
    public void destroy(ChannelSession channel) throws Exception {
        thread.interrupt();
    }

    @Override
    public void setExitCallback(ExitCallback callback) {

    }
}