/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.tomcat.jdbc.pool.DataSource;

/**
 *
 * @author nico
 */
public class CacheBean {

    public static Connection getCacheDBConnection() {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/cache");
            Connection conn = ds.getConnection();

            return conn;
        } catch (NamingException | SQLException ex) {
            Logger.getLogger(TripleStoreBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    static void storeResponseInCache(String reponse, int queryHash) {

        Connection conn = null;
        try {
            conn = getCacheDBConnection();
            Statement st = conn.createStatement();
            String insertQuery = "INSERT INTO virt_cache VALUES ( " + queryHash + ", '" + reponse + "')";
            st.executeUpdate(insertQuery);

        } catch (SQLException ex) {
            Logger.getLogger(CacheBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(CacheBean.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    public static void resetCache() {

        Connection conn = null;
        try {
            conn = getCacheDBConnection();
            Statement st = conn.createStatement();
            String deleteQuery = "DELETE FROM virt_cache";
            st.executeUpdate(deleteQuery);

        } catch (SQLException ex) {
            Logger.getLogger(CacheBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(CacheBean.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    static String getCachedLiteqQueryResult(int queryHash) {
        Connection conn = null;
        try {
            conn = getCacheDBConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT response FROM virt_cache "
                    + "WHERE id = " + queryHash);
            String result = null;
            while (rs.next()) {
                result = rs.getString(1);
            }
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(CacheBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(CacheBean.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        return null;
    }
}
