package com.cloudbackend;

import com.jcraft.jsch.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class SSHClient {
    private static String prompt = "root@";  // Dynamically detected prompt
    private static volatile boolean commandCompleted = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter SSH port (e.g., 2201/2202/2203): ");
        int port = Integer.parseInt(scanner.nextLine());

        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession("root", "localhost", port);
            session.setPassword("Rootpassword@23");
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelShell channel = (ChannelShell) session.openChannel("shell");
            channel.setPty(true);

            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();

            Thread outputThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    StringBuilder response = new StringBuilder();

                    while ((bytesRead = in.read(buffer)) != -1) {
                        String data = new String(buffer, 0, bytesRead);
                        System.out.print(data);
                        response.append(data);

                        // Dynamically detect the prompt after login
                        if (prompt.equals("root@") && data.contains("\nroot@")) {
                            prompt = data.trim().split("\n")[data.trim().split("\n").length - 1]; // Extract last line
                        }

                        // Check if the prompt appears
                        if (data.contains(prompt)) {
                            commandCompleted = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            channel.connect();
            outputThread.start();

            System.out.println("✅ Connected to SSH server. Type commands below:");

            while (true) {
                String command = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }

                out.write((command + "\n").getBytes());
                out.flush();

                // Wait for command completion
                while (!commandCompleted) {
                    Thread.sleep(100);
                }
                commandCompleted = false;
            }

            // Cleanup
            channel.disconnect();
            session.disconnect();
            System.out.println("✅ Disconnected from SSH server.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
