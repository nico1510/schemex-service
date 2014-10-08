/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listeners;

import beans.CacheBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author nico
 */
public class CreateTableListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Connection conn = null;
        try {
            String createTableString = "CREATE TABLE IF NOT EXISTS virt_cache"
                    + " ( id integer, response text )";
            conn = CacheBean.getCacheDBConnection();
            Statement createTableStatement = conn.createStatement();
            createTableStatement.execute(createTableString);
            Logger.getLogger(CreateTableListener.class.getName()).log(Level.INFO, "Cache table created");
        } catch (SQLException ex) {
            Logger.getLogger(CreateTableListener.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(CreateTableListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
