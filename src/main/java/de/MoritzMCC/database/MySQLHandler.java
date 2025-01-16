package de.MoritzMCC.database;

import com.google.gson.Gson;
import java.sql.*;
import java.util.HashMap;


public class MySQLHandler {
    private Connection connection;
    private final Gson gson = new Gson();

    public MySQLHandler(Connection connection) {
        this.connection = connection;
    }

    public void executeQuery(String query, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Object> getRow(String query, Object... params) {
        HashMap<String, Object> row = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object columnValue = rs.getObject(i);

                        row.put(columnName, columnValue);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    public Connection getConnection() {
        return connection;
    }
}
