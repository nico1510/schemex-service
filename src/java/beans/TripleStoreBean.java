/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.tomcat.jdbc.pool.DataSource;
import virtuoso.jdbc3.VirtuosoExtendedString;
import virtuoso.jdbc3.VirtuosoRdfBox;

/**
 *
 * @author nico
 */
public class TripleStoreBean {

    public static Connection getTripleStoreConnection() {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/virtuoso");
            Connection conn = ds.getConnection();

            return conn;
        } catch (NamingException | SQLException ex) {
            Logger.getLogger(TripleStoreBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String answerLiteqQuery(String query, boolean useCache) {
        JsonObject response = new JsonObject();
        Gson gson = new Gson();

        if (useCache) {
            String cachedResult = CacheBean.getCachedLiteqQueryResult(query.hashCode());
            if (cachedResult != null) {
                Logger.getLogger(TripleStoreBean.class.getName()).log(Level.INFO, "returning cached result");
                return cachedResult;
            }
        }
        JsonElement result = null;
        JsonArray values;

        ResultSet rs;
        Connection conn = null;
        try {
            conn = getTripleStoreConnection();
            Statement stmt = conn.createStatement();

            boolean more = stmt.execute(query);
            ResultSetMetaData data = stmt.getResultSet().getMetaData();

            if (data.getColumnCount() == 1) {
                result = new JsonArray();
            } else {
                result = new JsonObject();
            }

            while (more) {
                rs = stmt.getResultSet();
                while (rs.next()) {
                    if (data.getColumnCount() > 1) {
                        String key = convertToIRI(rs.getObject(1));
                        if (key == null) {
                            key = rs.getString(1);
                        }
                        String value = convertToIRI(rs.getObject(2));
                        if (value == null) {
                            value = rs.getString(2);
                        }
                        if (((JsonObject) result).has(key)) {
                            values = ((JsonObject) result).get(key).getAsJsonArray();
                            values.add(new JsonPrimitive(value));
                        } else {
                            values = new JsonArray();
                            values.add(new JsonPrimitive(value));
                            ((JsonObject) result).add(key, values);
                        }
                    } else if (data.getColumnCount() == 1) {
                        String key = convertToIRI(rs.getObject(1));
                        if (key == null) {
                            key = rs.getString(1);
                        }
                        ((JsonArray) result).add(new JsonPrimitive(key));
                    }
                }
                more = stmt.getMoreResults();
            }
            response.add("response", result);

            if (useCache) {
                CacheBean.storeResponseInCache(gson.toJson(response), query.hashCode());
            }
        } catch (SQLException ex) {
            Logger.getLogger(TripleStoreBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(TripleStoreBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return gson.toJson(response);
    }

    private static String convertToIRI(Object o) {
        String iri = null;
        if (o instanceof VirtuosoExtendedString) {
            VirtuosoExtendedString vs = (VirtuosoExtendedString) o;
            if (vs.iriType == VirtuosoExtendedString.IRI && (vs.strType & 0x01) == 0x01) {
                iri = "<".concat(vs.str).concat(">");
            } else if (vs.iriType == VirtuosoExtendedString.BNODE) {
                iri = "<".concat(vs.str).concat(">");
            } else {
                iri = "\"".concat(vs.str).concat("\"");
            }
        } else if (o instanceof VirtuosoRdfBox) {
            VirtuosoRdfBox rb = (VirtuosoRdfBox) o;
            iri = rb.rb_box.toString().concat(" lang=").concat(rb.getLang()).concat(" type=").concat(rb.getType()).concat(" ");
        }
        return iri;
    }

//
//    public void moveEntitiesToRepo(String nodeID) {
//        String namedGraphID = nodeID.replaceAll("/", "");
//        String entityQuery = "sparql define input:default-graph-uri <" + namedGraphID + ">"
//                + " SELECT ?s, ?example WHERE { ?s a <http://schemex.west.uni-koblenz.de/EquivalenceClass> ."
//                + " ?s <http://schemex.west.uni-koblenz.de/hasDataset> ?dataset ."
//                + " ?dataset <http://schemex.west.uni-koblenz.de/exampleResource> ?example . }";
//
//        Logger.getLogger(TripleStoreBean.class.getName()).log(Level.INFO, entityQuery);
//        String entityResponse = answerLiteqQuery(entityQuery, false);
//
//        String metaPath = localRepoBean.persistMeta(new ByteArrayInputStream(entityResponse.getBytes(StandardCharsets.UTF_8)), "/liteq_entities", namedGraphID);
//
//        String removeQuery = "sparql define input:default-graph-uri <"
//                + namedGraphID + ">  DELETE { ?pred ?property ?value } WHERE "
//                + "{ ?pred ?property ?value ."
//                + " ?pred <http://schemex.west.uni-koblenz.de/exampleResource> ?value }";
//        removeFromTripleStore(removeQuery);
//    }
//    
//        private void removeFromTripleStore(String removeQuery) {
//        Connection conn = null;
//        try {
//            conn = getTripleStoreConnection();
//            Statement stmt = conn.createStatement();
//            stmt.execute(removeQuery);
//
//        } catch (SQLException ex) {
//            Logger.getLogger(TripleStoreBean.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                conn.close();
//            } catch (SQLException ex) {
//                Logger.getLogger(TripleStoreBean.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//
//    @Override
//    public String getLiteqEntityQueryResult(String eqClassURI) {
//        Session session = localRepoBean.createSession(false);
//        JsonObject nullResponse = new JsonObject();
//        nullResponse.add("response", new JsonArray());
//        Gson gson = new Gson();
//        String entities = gson.toJson(nullResponse);
//        String decodedUri = null;
//        try {
//            decodedUri = URLDecoder.decode(eqClassURI, "UTF-8");
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(RepositoryBean.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        String getGraphQuery = "sparql select distinct ?g"
//                + " where { GRAPH ?g { <" + decodedUri + "> ?p ?o } "
//                + "}";
//        JsonObject graphQueryResponse = gson.fromJson(answerLiteqQuery(getGraphQuery, false), JsonObject.class);
//        String graph = graphQueryResponse.get("response").getAsJsonArray().get(0).getAsString().replace("<", "").replace(">", "");
//        Logger.getLogger(RepositoryBean.class.getName()).log(Level.INFO, "GRAPH : " + graph);
//
//        try {
//            Node entityNode = session.getRootNode().getNode("liteq_entities");
//
//            if (entityNode.hasProperty(graph)) {
//                StringWriter writer = new StringWriter();
//                IOUtils.copy(entityNode.getProperty(graph).getBinary().getStream(), writer, StandardCharsets.UTF_8.name());
//                String allEntities = writer.toString();
//                JsonObject responseMap = gson.fromJson(allEntities, JsonObject.class);
//                JsonObject entityMap = responseMap.get("response").getAsJsonObject();
//                entities = gson.toJson(entityMap.get("<" + decodedUri + ">"));
//            }
//        } catch (RepositoryException ex) {
//            Logger.getLogger(LocalRepoAccessBean.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(LocalRepoAccessBean.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            session.logout();
//        }
//
//        return entities;
//    }
}
