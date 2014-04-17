/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gryf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Monika Waleczek
 */
public class JdbcTest {
    
    private String url, user, password;
    private Connection connDBase;
    private Statement st;
    private Boolean dbExist;
    private Boolean tableExist;
    

    public JdbcTest(String url, String user, String password) {
        this.user = user;
        this.url = url;
        this.password = password;
        try {
            connDBase = DriverManager.getConnection("jdbc:mysql://localhost", user, password);
            st = connDBase.createStatement();
        } catch (SQLException ex) {
            Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Błąd bazy danych  " + ex, "JdbcTest", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void createAccount(String user, String password) {

        // test if user exist
        String query = "SELECT COUNT(*) FROM mysql.user WHERE User='" + user + "'";
        boolean userExist = false;
        ResultSet rs;
        try {
            rs = st.executeQuery(query);
            rs.next();
            userExist = rs.getInt("COUNT(*)") > 0;
        } catch (SQLException ex) {
            Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!userExist) {
            query = "CREATE USER '" + user + "'@'localhost' IDENTIFIED BY '" + password + "';";
            try {
                st.executeUpdate(query);
                JOptionPane.showMessageDialog(null, "Utworzono klienta bazy " + user, "JdbcTest", JOptionPane.INFORMATION_MESSAGE);
                st.executeUpdate("grant all privileges on gryf.* to 'user_gryf'@'localhost' identified by 'gryf'");
            } catch (SQLException ex) {
                Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Nie utworzono klienta bazy " + ex, "JdbcTest", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    public Boolean isDbExist(String dataBaseName) {
        try {
            String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + dataBaseName + "' ";
            ResultSet rs = st.executeQuery(query);
            rs.next();
            System.out.println("isDbexist?=" + rs.getString("SCHEMA_NAME"));
            dbExist = (rs.getString("SCHEMA_NAME")).matches(dataBaseName);
// Close connection, statement, and result set.

        } catch (SQLException ex) {
            Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("błąd w isDbExist");
        }
        return dbExist;
    }

    public Boolean isTableExist(String dataBaseName, String tableBaseName) {
        try {
            String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '" + dataBaseName + "' AND table_name = '" + tableBaseName + "'";
            ResultSet rs = st.executeQuery(query);
            rs.next();
            tableExist = rs.getInt("COUNT(*)") > 0;
// Close connection, statement, and result set.

        } catch (SQLException ex) {
            Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tableExist;
    }

    
    public void createDB(String dbName){
        String query = "CREATE DATABASE IF NOT EXISTS " + dbName;
        try {
            st.executeUpdate(query);
        } catch (SQLException ex) {
            Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Nie utworzono bazy " + ex, "JdbcTest", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void createTable(String tableName, String dbStructure, String dbName){
        if(isDbExist(dbName)){
            String query = "CREATE TABLE IF NOT EXISTS " + tableName + " " + dbStructure;
            try {
                Connection connDBaseLocal = DriverManager.getConnection(url + "/" +dbName, user, password);
                Statement stLocal = connDBaseLocal.createStatement();
                stLocal.executeUpdate(query);
            } catch (SQLException ex) {
                Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "Nie utworzono tabeli " + ex, "JdbcTest", JOptionPane.ERROR_MESSAGE);
            }
        }else {
            JOptionPane.showMessageDialog(null, "Nie utworzono tabeli; brak bazy " + dbName, "JdbcTest", JOptionPane.ERROR_MESSAGE);
        }
    }

    void fillArmedTable() {
        String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'gryf' AND table_name = 'armdisarm'";
        try {
            ResultSet rs = st.executeQuery(query);
            rs.next();
            int lr = rs.getInt("COUNT(*)");
            System.out.println("liczba rekordów armdisarm=" + lr);
            if(lr!=2){
                connDBase = DriverManager.getConnection("jdbc:mysql://localhost/gryf?characterEncoding=utf8", "user_gryf", "gryf");
                Statement st = connDBase.createStatement();
                query = "INSERT INTO `gryf`.`armdisarm` (`id`, `description`, `code`) VALUES (NULL, 'uzbrojenie', 1);";
                st.execute(query);
                query = "INSERT INTO `gryf`.`armdisarm` (`id` ,`description` ,`code`)VALUES (NULL , 'rozbrojenie', 2);";
                st.execute(query);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(JdbcTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
