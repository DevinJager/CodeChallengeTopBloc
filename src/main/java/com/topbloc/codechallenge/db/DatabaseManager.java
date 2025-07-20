package com.topbloc.codechallenge.db;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DatabaseManager {
    private static final String jdbcPrefix = "jdbc:sqlite:";
    private static final String dbName = "challenge.db";
    private static String connectionString;
    private static Connection conn;

    static {
        File dbFile = new File(dbName);
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
    }

    public static void connect() {
        try {
            Connection connection = DriverManager.getConnection(connectionString);
            System.out.println("Connection to SQLite has been established.");
            conn = connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    // Schema function to reset the database if needed - do not change
    public static void resetDatabase() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
        connect();
        applySchema();
        seedDatabase();
    }

    // Schema function to reset the database if needed - do not change
    private static void applySchema() {
        String itemsSql = "CREATE TABLE IF NOT EXISTS items (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String inventorySql = "CREATE TABLE IF NOT EXISTS inventory (\n"
                + "id integer PRIMARY KEY,\n"
                + "item integer NOT NULL UNIQUE references items(id) ON DELETE CASCADE,\n"
                + "stock integer NOT NULL,\n"
                + "capacity integer NOT NULL\n"
                + ");";
        String distributorSql = "CREATE TABLE IF NOT EXISTS distributors (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String distributorPricesSql = "CREATE TABLE IF NOT EXISTS distributor_prices (\n"
                + "id integer PRIMARY KEY,\n"
                + "distributor integer NOT NULL references distributors(id) ON DELETE CASCADE,\n"
                + "item integer NOT NULL references items(id) ON DELETE CASCADE,\n"
                + "cost float NOT NULL\n" +
                ");";

        try {
            System.out.println("Applying schema");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Schema applied");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Schema function to reset the database if needed - do not change
    private static void seedDatabase() {
        String itemsSql = "INSERT INTO items (id, name) VALUES (1, 'Licorice'), (2, 'Good & Plenty'),\n"
            + "(3, 'Smarties'), (4, 'Tootsie Rolls'), (5, 'Necco Wafers'), (6, 'Wax Cola Bottles'), (7, 'Circus Peanuts'), (8, 'Candy Corn'),\n"
            + "(9, 'Twix'), (10, 'Snickers'), (11, 'M&Ms'), (12, 'Skittles'), (13, 'Starburst'), (14, 'Butterfinger'), (15, 'Peach Rings'), (16, 'Gummy Bears'), (17, 'Sour Patch Kids')";
        String inventorySql = "INSERT INTO inventory (item, stock, capacity) VALUES\n"
                + "(1, 22, 25), (2, 4, 20), (3, 15, 25), (4, 30, 50), (5, 14, 15), (6, 8, 10), (7, 10, 10), (8, 30, 40), (9, 17, 70), (10, 43, 65),\n" +
                "(11, 32, 55), (12, 25, 45), (13, 8, 45), (14, 10, 60), (15, 20, 30), (16, 15, 35), (17, 14, 60)";
        String distributorSql = "INSERT INTO distributors (id, name) VALUES (1, 'Candy Corp'), (2, 'The Sweet Suite'), (3, 'Dentists Hate Us')";
        String distributorPricesSql = "INSERT INTO distributor_prices (distributor, item, cost) VALUES \n" +
                "(1, 1, 0.81), (1, 2, 0.46), (1, 3, 0.89), (1, 4, 0.45), (2, 2, 0.18), (2, 3, 0.54), (2, 4, 0.67), (2, 5, 0.25), (2, 6, 0.35), (2, 7, 0.23), (2, 8, 0.41), (2, 9, 0.54),\n" +
                "(2, 10, 0.25), (2, 11, 0.52), (2, 12, 0.07), (2, 13, 0.77), (2, 14, 0.93), (2, 15, 0.11), (2, 16, 0.42), (3, 10, 0.47), (3, 11, 0.84), (3, 12, 0.15), (3, 13, 0.07), (3, 14, 0.97),\n" +
                "(3, 15, 0.39), (3, 16, 0.91), (3, 17, 0.85)";

        try {
            System.out.println("Seeding database");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Database seeded");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Helper methods to convert ResultSet to JSON - change if desired, but should not be required
    private static JSONArray convertResultSetToJson(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<String> colNames = IntStream.range(0, columns)
                .mapToObj(i -> {
                    try {
                        return md.getColumnName(i + 1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());

        JSONArray jsonArray = new JSONArray();
        while (rs.next()) {
            jsonArray.add(convertRowToJson(rs, colNames));
        }
        return jsonArray;
    }

    private static JSONObject convertRowToJson(ResultSet rs, List<String> colNames) throws SQLException {
        JSONObject obj = new JSONObject();
        for (String colName : colNames) {
            obj.put(colName, rs.getObject(colName));
        }
        return obj;
    }

    // Export table to CSV format
    // Example usage: curl "http://localhost:4567/export/csv?table=items" > items.csv
    public static String exportTableToCsv(String tableName) {
        StringBuilder csv = new StringBuilder();
        
        try {
            // Validate table name
            if (!isValidTableName(tableName)) {
                return "Error: Invalid table name";
            }
            
            // Query to get all data from the table
            String sql = "SELECT * FROM " + tableName;
            ResultSet rs = conn.createStatement().executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Add column headers
            for (int i = 1; i <= columnCount; i++) {
                csv.append(metaData.getColumnName(i));
                if (i < columnCount) {
                    csv.append(",");
                }
            }
            csv.append("\n");
            
            // Add data rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    // Escape quotes and handle nulls
                    if (value == null) {
                        value = "";
                    } else if (value.contains("\"") || value.contains(",") || value.contains("\n")) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }
                    csv.append(value);
                    if (i < columnCount) {
                        csv.append(",");
                    }
                }
                csv.append("\n");
            }
            
            return csv.toString();
        } catch (SQLException e) {
            System.out.println("Error exporting table to CSV: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
    
    // Helper fn to validate table name preventing SQL injection
    private static boolean isValidTableName(String tableName) {
        try {
            String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If there's a result, the table exists
        } catch (SQLException e) {
            System.out.println("Error validating table name: " + e.getMessage());
            return false;
        }
    }

    // Controller functions - add your routes here. getItems is provided as an example

    // Inventory Routes
    public static JSONArray getItems() {
        String sql = "SELECT * FROM items";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Complete Inventory
    public static JSONArray getInventory() {
        String sql = "SELECT items.name, inventory.id, inventory.stock, inventory.capacity " +
             "FROM inventory " +
             "JOIN items ON inventory.item = items.id ";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // No Stock
    public static JSONArray getInventoryEmpty() {
        String sql = "SELECT items.name, inventory.id, inventory.stock, inventory.capacity " +
            "FROM inventory " +
            "JOIN items ON inventory.item = items.id " +
            "WHERE stock = 0";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Overstock
    public static JSONArray getInventoryOverStock() {
        String sql = "SELECT items.name, inventory.id, inventory.stock, inventory.capacity " +
            "FROM inventory " +
            "JOIN items ON inventory.item = items.id " +
            "WHERE stock > capacity";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Low Stock (< 35% of capacity)
    public static JSONArray getInventoryLowStock() {
        String sql = "SELECT items.name, inventory.id, inventory.stock, inventory.capacity " +
            "FROM inventory " +
            "JOIN items ON inventory.item = items.id " + 
            "WHERE stock < (capacity * .35)";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Dynamic Inventory Route Using ID
    public static JSONArray getInventoryDyanmic(int id) {
        String sql = "SELECT items.name, inventory.id, inventory.stock, inventory.capacity " +
            "FROM inventory " +
            "JOIN items ON inventory.item = items.id " +
            "WHERE inventory.id = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet set = pstmt.executeQuery();
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    // Distributor Routes

    // All Distributors
    public static JSONArray getDistributors() {
        String sql = "SELECT * FROM distributors";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Dynamic Distributor Route Using ID showing all for that distributor
    public static JSONArray getDistributorDyanmic(int id) {
        String sql = "SELECT items.name, items.id, distributor_prices.cost " +
            "FROM distributor_prices " +
            "JOIN items ON distributor_prices.item = items.id " +
            "WHERE distributor_prices.distributor = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet set = pstmt.executeQuery();
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Dynamic given Item ID return all distributors for item
    public static JSONArray getItemDynamic(int id) {
        String sql = "SELECT distributors.name, distributor_prices.id, distributor_prices.cost " +
            "FROM distributor_prices " +
            "JOIN distributors ON distributor_prices.distributor = distributors.id " +
            "WHERE distributor_prices.item = ?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet set = pstmt.executeQuery();
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Add Item to items table and inventory table
    public static boolean addItemAndInventory(String name, int stock, int capacity) {
        try {
            // Check if item already exists
            String checkSql = "SELECT COUNT(*) FROM items WHERE name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, name);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                return false; 
            }
            
            // Get the next available ID
            String maxIdSql = "SELECT MAX(id) as max_id FROM items";
            ResultSet rs = conn.createStatement().executeQuery(maxIdSql);
            int nextId = rs.next() ? rs.getInt("max_id") + 1 : 1;
            
            // Insert item with explicit ID
            String itemSql = "INSERT INTO items (id, name) VALUES (?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            itemStmt.setInt(1, nextId);
            itemStmt.setString(2, name);
            itemStmt.executeUpdate();
            
            // Insert inventory with same ID
            String invSql = "INSERT INTO inventory (id, item, stock, capacity) VALUES (?, ?, ?, ?)";
            PreparedStatement invStmt = conn.prepareStatement(invSql);
            invStmt.setInt(1, nextId);
            invStmt.setInt(2, nextId);
            invStmt.setInt(3, stock);
            invStmt.setInt(4, capacity);
            invStmt.executeUpdate();
            
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean modifyInventory(int id, int stock, int capacity) {
        try {
            // Check if inventory item exists
            String checkSql = "SELECT COUNT(*) FROM inventory WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, id);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) < 1) {
                return false; // Inventory item doesn't exist
            }
            
            // Update inventory with new stock and capacity
            String invSql = "UPDATE inventory SET stock = ?, capacity = ? WHERE id = ?";
            PreparedStatement invStmt = conn.prepareStatement(invSql);
            invStmt.setInt(1, stock);
            invStmt.setInt(2, capacity);
            invStmt.setInt(3, id);
            invStmt.executeUpdate();
            
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean addDistributors(String name) {
        try {
            // Check if distributor already exists
            String checkSql = "SELECT COUNT(*) FROM distributors WHERE name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, name);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                return false; 
            }
            
            // Get the next available ID for the distributor
            String maxIdSql = "SELECT MAX(id) as max_id FROM distributors";
            ResultSet rs = conn.createStatement().executeQuery(maxIdSql);
            int nextId = rs.next() ? rs.getInt("max_id") + 1 : 1;
            
            // Insert distributor with explicit ID
            String itemSql = "INSERT INTO distributors (id, name) VALUES (?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            itemStmt.setInt(1, nextId);
            itemStmt.setString(2, name);
            itemStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean addDistributorItem(String distName, String itemName, double cost) {
        try {
            // Check if distributor exists
            String checkSql = "SELECT COUNT(*) FROM distributors WHERE name = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, distName);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) < 1) {
                return false; 
            }
            
            // Get the next available ID for the item
            String maxIdSql = "SELECT MAX(id) as max_id FROM distributor_prices";
            ResultSet rs = conn.createStatement().executeQuery(maxIdSql);
            int nextId = rs.next() ? rs.getInt("max_id") + 1 : 1;

            // Get distributor ID from name
            String distIdSql = "SELECT id FROM distributors WHERE name = ?";
            PreparedStatement distIdStmt = conn.prepareStatement(distIdSql);
            distIdStmt.setString(1, distName);
            ResultSet distIdRs = distIdStmt.executeQuery();
            if (!distIdRs.next()) {
                return false; 
            }
            int distId = distIdRs.getInt("id");

            // Get item ID from name
            String itemIdSql = "SELECT id FROM items WHERE name = ?";
            PreparedStatement itemIdStmt = conn.prepareStatement(itemIdSql);
            itemIdStmt.setString(1, itemName);
            ResultSet itemIdRs = itemIdStmt.executeQuery();
            if (!itemIdRs.next()) {
                return false; // Item not found
            }
            int itemId = itemIdRs.getInt("id");

            // Insert into distributor_prices with explicit ID
            String priceSql = "INSERT INTO distributor_prices (id, distributor, item, cost) VALUES (?, ?, ?, ?)";
            PreparedStatement priceStmt = conn.prepareStatement(priceSql);
            priceStmt.setInt(1, nextId);
            priceStmt.setInt(2, distId);
            priceStmt.setInt(3, itemId);
            priceStmt.setDouble(4, cost);
            priceStmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    // Modify distributor catalog
    public static boolean modifyDistributorPrice(String distName, String itemName, double cost) {
        try {
            // Check if distributor item exists
            String checkSql = "SELECT COUNT(*) FROM distributor_prices WHERE distributor = ? AND item = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);

            // Get distributor ID from name
            String distIdSql = "SELECT id FROM distributors WHERE name = ?";
            PreparedStatement distIdStmt = conn.prepareStatement(distIdSql);
            distIdStmt.setString(1, distName);
            ResultSet distIdRs = distIdStmt.executeQuery();
            if (!distIdRs.next()) {
                return false; 
            }
            int distId = distIdRs.getInt("id");

            // Get item ID from name
            String itemIdSql = "SELECT id FROM items WHERE name = ?";
            PreparedStatement itemIdStmt = conn.prepareStatement(itemIdSql);
            itemIdStmt.setString(1, itemName);
            ResultSet itemIdRs = itemIdStmt.executeQuery();
            if (!itemIdRs.next()) {
                return false; // Item not found
            }
            int itemId = itemIdRs.getInt("id");

            // Check if this distributor has this item
            checkStmt.setInt(1, distId);
            checkStmt.setInt(2, itemId);
            ResultSet checkRs = checkStmt.executeQuery();
            if (!checkRs.next() || checkRs.getInt(1) == 0) {
                return false; // Distributor item doesn't exist
            }
            
            // Update distributor price with new cost
            String updateSql = "UPDATE distributor_prices SET cost = ? WHERE distributor = ? AND item = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setDouble(1, cost);
            updateStmt.setInt(2, distId);
            updateStmt.setInt(3, itemId);
            updateStmt.executeUpdate();
            
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static JSONObject getCheapestRestock(String itemName, int quantity) {
        try {
            // Get item ID from name
            String itemIdSql = "SELECT id FROM items WHERE name = ?";
            PreparedStatement itemIdStmt = conn.prepareStatement(itemIdSql);
            itemIdStmt.setString(1, itemName);
            ResultSet itemIdRs = itemIdStmt.executeQuery();
            if (!itemIdRs.next()) {
                return null; // Item not found
            }
            int itemId = itemIdRs.getInt("id");
            
            // Find cheapest distributor for this item
            String cheapestSql = 
                "SELECT d.name as distributor_name, dp.cost, (dp.cost * ?) as total_cost " +
                "FROM distributor_prices dp " +
                "JOIN distributors d ON dp.distributor = d.id " +
                "WHERE dp.item = ? " +
                "ORDER BY dp.cost ASC " +
                "LIMIT 1";
            
            PreparedStatement cheapestStmt = conn.prepareStatement(cheapestSql);
            cheapestStmt.setInt(1, quantity);
            cheapestStmt.setInt(2, itemId);
            ResultSet cheapestRs = cheapestStmt.executeQuery();
            
            if (!cheapestRs.next()) {
                return null; // No distributors found for this item
            }
            
            JSONObject result = new JSONObject();
            result.put("item", itemName);
            result.put("quantity", quantity);
            result.put("distributor", cheapestRs.getString("distributor_name"));
            result.put("unit_cost", cheapestRs.getDouble("cost"));
            result.put("total_cost", cheapestRs.getDouble("total_cost"));
            
            return result;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static boolean deleteInventoryItemByName(String name) {
        try {
            // Get item ID from name
            String itemIdSql = "SELECT id FROM items WHERE name = ?";
            PreparedStatement itemIdStmt = conn.prepareStatement(itemIdSql);
            itemIdStmt.setString(1, name);
            ResultSet itemIdRs = itemIdStmt.executeQuery();
            if (!itemIdRs.next()) {
                return false; // Item not found
            }
            int itemId = itemIdRs.getInt("id");
            
            // Delete the inventory item
            String deleteSql = "DELETE FROM inventory WHERE item = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, itemId);
            int rowsAffected = deleteStmt.executeUpdate();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    public static boolean deleteDistributor(int id) {
        try {
            // Check if distributor exists
            String checkSql = "SELECT COUNT(*) FROM distributors WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, id);
            ResultSet checkRs = checkStmt.executeQuery();
            if (!checkRs.next() || checkRs.getInt(1) == 0) {
                return false; // Distributor doesn't exist
            }
            
            // Delete the distributor
            String deleteSql = "DELETE FROM distributors WHERE id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, id);
            int rowsAffected = deleteStmt.executeUpdate();
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}