package com.cloudbackend.ssh;

import com.cloudbackend.entity.User;
import com.cloudbackend.service.TerminalService;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import java.io.*;

public class SshTerminal implements Command, Runnable {

    private final TerminalService terminalService;
    private final User user;

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback exitCallback;
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
    public void setExitCallback(ExitCallback callback) {
        this.exitCallback = callback;
    }

    @Override
    public void start(ChannelSession channel, Environment environment) throws IOException {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             OutputStreamWriter writer = new OutputStreamWriter(out)) {

            writer.write("Welcome to the SSH terminal!\r\n");
            writer.write("Available commands: mv, cp, ls, mkdir, ps, whoami, tree, nano\r\n");
            writer.flush();

            StringBuilder commandBuffer = new StringBuilder();  // Buffer to hold user input

            int character;
            while ((character = reader.read()) != -1) {  // Read character by character
                if (character == 10 || character == 13) {  // Enter key (line break)
                    String command = commandBuffer.toString().trim();
                    if (!command.isEmpty()) {
                        processCommand(command, writer);
                    }
                    commandBuffer.setLength(0);  // Clear the buffer for the next command
                } else if (character == 127) {  // Handle backspace
                    if (commandBuffer.length() > 0) {
                        commandBuffer.deleteCharAt(commandBuffer.length() - 1);
                        writer.write("\b \b");  // Erase the last character
                        writer.flush();
                    }
                } else {  // Handle regular characters
                    commandBuffer.append((char) character);
                    writer.write((char) character);  // Echo the character
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (exitCallback != null) {
                exitCallback.onExit(0);
            }
        }
    }

    // Method to process the command when the user presses Enter
    private void processCommand(String command, OutputStreamWriter writer) throws IOException {
        String[] parts = command.split(" ");
        String cmd = parts[0];
        String output = "\r\n";

        try {
            switch (cmd) {
                case "mv":
                    output += (parts.length < 3) ? "Usage: mv <source> <destination>"
                            : terminalService.moveFileOrDirectory(parts[1], parts[2], user);
                    break;
                case "cp":
                    output += (parts.length < 3) ? "Usage: cp <source> <destination>"
                            : terminalService.copyFileOrDirectory(parts[1], parts[2], user);
                    break;
                case "ls":
                    output += String.join("\r\n", terminalService.listFiles(parts.length > 1 ? parts[1] : "/", user));
                    break;
                case "mkdir":
                    output += (parts.length < 2) ? "Usage: mkdir <directory-name>"
                            : terminalService.createDirectory(parts.length > 2 ? parts[1] : "/", parts[parts.length - 1], user);
                    break;
                case "ps":
                    output += terminalService.listProcesses();
                    break;
                case "whoami":
                    output += terminalService.whoami(user);
                    break;
                case "tree":
                    output += terminalService.listDirectoryTree(parts.length > 1 ? parts[1] : "/", user);
                    break;
                case "nano":
                    output += (parts.length < 2) ? "Usage: nano <file-path> [content]"
                            : terminalService.editFile(parts[1], parts.length > 2 ? parts[2] : "", user);
                    break;
                default:
                    output += "Unknown command: " + cmd;
                    break;
            }
        } catch (Exception e) {
            output = "Error: " + e.getMessage();
        }

        writer.write(output + "\r\n");
        writer.flush();
    }


    @Override
    public void destroy(ChannelSession channel) throws Exception {
        if (thread != null) {
            thread.interrupt();
        }
    }
}