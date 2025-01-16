package de.MoritzMCC.database;

import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MySQLHandler {
    private Connection connection;
    private final Gson gson = new Gson();

    public MySQLHandler(Connection connection) {
        this.connection = connection;
    }

    public void executeQuery(String query, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            setParameters(stmt, params);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Void> executeQueryAsync(String query, Object... params) {
        return CompletableFuture.runAsync(() -> executeQuery(query, params));
    }

    public HashMap<String, Object> getRow(String query, Object... params) {
        HashMap<String, Object> row = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            setParameters(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    row = parseResultSetRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row;
    }

    public CompletableFuture<HashMap<String, Object>> getRowAsync(String query, Object... params) {
        return CompletableFuture.supplyAsync(() -> getRow(query, params));
    }

    public List<HashMap<String, Object>> getRows(String query, Object... params) {
        List<HashMap<String, Object>> rows = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            setParameters(stmt, params);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(parseResultSetRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    public CompletableFuture<List<HashMap<String, Object>>> getRowsAsync(String query, Object... params) {
        return CompletableFuture.supplyAsync(() -> getRows(query, params));
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    private HashMap<String, Object> parseResultSetRow(ResultSet rs) throws SQLException {
        HashMap<String, Object> row = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object columnValue = rs.getObject(i);
            row.put(columnName, columnValue);
        }
        return row;
    }

    public Connection getConnection() {
        return connection;
    }
}
