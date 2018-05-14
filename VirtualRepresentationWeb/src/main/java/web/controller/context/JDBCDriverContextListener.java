/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.controller.context;

import com.mysql.jdbc.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 *
 * @author Jan-Peter.Schmidt
 */
@WebListener("Registers JDBC Connector for MySQL")
public class JDBCDriverContextListener implements ServletContextListener {
    
    Driver mysqlDriver;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            //https://stackoverflow.com/questions/22384710/java-sql-sqlexception-no-suitable-driver-found-for-jdbcmysql-localhost3306
            //https://stackoverflow.com/a/3320554
            mysqlDriver = new Driver();
            DriverManager.registerDriver(mysqlDriver);        
        } catch (SQLException ex) {
            Logger.getLogger(JDBCDriverContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            System.out.println("Deregister MysqlDriver");
            DriverManager.deregisterDriver(mysqlDriver);
            System.out.println("MysqLDriver deregistered");
        } catch (SQLException ex) {
            Logger.getLogger(JDBCDriverContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
