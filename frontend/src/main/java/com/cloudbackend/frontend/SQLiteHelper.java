package com.cloudbackend.frontend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper {
    private static final String DB_URL = "jdbc:sqlite:files.db";

    static {
        try {
            // Create a connection to the database
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            String createTableQuery = "CREATE TABLE IF NOT EXISTS files (" +
                    "path TEXT PRIMARY KEY, " +
                    "size LONG, " +
                    "canRead BOOLEAN, " +
                    "canWrite BOOLEAN, " +
                    "othersCanRead BOOLEAN, " +
                    "othersCanWrite BOOLEAN, " +
                    "isDirectory BOOLEAN)";
            stmt.execute(createTableQuery);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
        }
    }

    public static void saveFile(FileDTO file) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String insertQuery = "INSERT OR REPLACE INTO files (path, size, canRead, canWrite, othersCanRead, othersCanWrite, isDirectory) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, file.getPath());
            pstmt.setLong(2, file.getSize());
            pstmt.setBoolean(3, file.isCanRead());
            pstmt.setBoolean(4, file.isCanWrite());
            pstmt.setBoolean(5, file.isOthersCanRead());
            pstmt.setBoolean(6, file.isOthersCanWrite());
            pstmt.setBoolean(7, file.isDirectory());
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error saving file to database: " + e.getMessage());
        }
    }

    public static FileDTO getFile(String path) {
        FileDTO file = null;
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String selectQuery = "SELECT * FROM files WHERE path = ?";
            PreparedStatement pstmt = conn.prepareStatement(selectQuery);
            pstmt.setString(1, path);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                file = new FileDTO();
                file.setPath(rs.getString("path"));
                file.setSize(rs.getLong("size"));
                file.setCanRead(rs.getBoolean("canRead"));
                file.setCanWrite(rs.getBoolean("canWrite"));
                file.setOthersCanRead(rs.getBoolean("othersCanRead"));
                file.setOthersCanWrite(rs.getBoolean("othersCanWrite"));
                file.setIsDirectory(rs.getBoolean("isDirectory"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error fetching file from database: " + e.getMessage());
        }
        return file;
    }

    public static List<FileDTO> getAllFiles() {
        List<FileDTO> files = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String selectQuery = "SELECT * FROM files";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectQuery);

            while (rs.next()) {
                FileDTO file = new FileDTO();
                file.setPath(rs.getString("path"));
                file.setSize(rs.getLong("size"));
                file.setCanRead(rs.getBoolean("canRead"));
                file.setCanWrite(rs.getBoolean("canWrite"));
                file.setOthersCanRead(rs.getBoolean("othersCanRead"));
                file.setOthersCanWrite(rs.getBoolean("othersCanWrite"));
                file.setIsDirectory(rs.getBoolean("isDirectory"));
                files.add(file);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Error fetching files from database: " + e.getMessage());
        }
        return files;
    }

    public static void deleteFile(String path) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String deleteQuery = "DELETE FROM files WHERE path = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteQuery);
            pstmt.setString(1, path);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Error deleting file from database: " + e.getMessage());
        }
    }
}
