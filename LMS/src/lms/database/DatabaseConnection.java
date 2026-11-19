/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Mary
 */

public final class DatabaseConnection {
    public static final String DATABASE = "library_management_system";
    
    private DatabaseConnection() {}
    
    public static Connection getConnection() throws SQLException{
            String host = System.getenv("LMS_DB_HOST");
            String port = System.getenv("LMS_DB_PORT");
            String user = System.getenv("LMS_DB_USER");
            String password = System.getenv("LMS_DB_PASSWORD");
            
            if(host == null || host.isBlank()) {
                host = "localhost";
            }
            if(port == null || port.isBlank()) {
                port = "3306";
            }
            if(user == null || user.isBlank() || password == null || password.isBlank()) {
                throw new SQLException("Database credentials are not configured");
            }
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + DATABASE;
            
            return DriverManager.getConnection(url, user, password);
        }
}
