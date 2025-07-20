package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;

import static spark.Spark.*;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.connect();
        
        // Enable CORS
        options("/*", (req, res) -> {
            String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                res.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        before("/*", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
            if (req.requestMethod().equals("OPTIONS")) {
                res.status(200);
                res.body("");
                halt(200);
            }
        });
        // Don't change this - required for GET and POST requests with the header 'content-type'
        options("/*",
                (req, res) -> {
                    res.header("Access-Control-Allow-Headers", "content-type");
                    res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
                    return "OK";
                });

        // Don't change - if required you can reset your database by hitting this endpoint at localhost:4567/reset
        get("/reset", (req, res) -> {
            DatabaseManager.resetDatabase();
            return "OK";
        });

        // Routes for the Inventory and Items

        // All Items Route
        get("/items", (req, res) -> DatabaseManager.getItems());
        // Example Version Route
        get("/version", (req, res) -> "TopBloc Code Challenge v1.0");
        // All Inventory Route
        get("/inventory", (req, res) -> DatabaseManager.getInventory());
        // No Stock Route
        get("/nostock", (req, res) -> DatabaseManager.getInventoryEmpty());
        // Overstock Route
        get("/overstock", (req, res) -> DatabaseManager.getInventoryOverStock());
        // Low Stock Route
        get("/lowstock", (req, res) -> DatabaseManager.getInventoryLowStock());
        // Dynamic Route Using ID. Example cURL: "curl http://localhost:4567/inventory/id" 
        get("/inventory/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            return DatabaseManager.getInventoryDyanmic(id);
        });

        // Routes for Distributors table

        // All Distributors Route
        get("/distributors", (req, res) -> DatabaseManager.getDistributors());

        // Dynamic Distributor Route Using ID. Example cURL: "curl http://localhost:4567/distributors/id"
        get("/distributor/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            return DatabaseManager.getDistributorDyanmic(id);
        });

        // Dynamic item route showing all distributor options of given item.
        // Example cURL: "curl http://localhost:4567/item/id"
        get("/item/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            return DatabaseManager.getItemDynamic(id);
        });

     
        // POST/PUT/DELETE routes

        // Add Item route
        // Example usage: curl -X POST -H "Content-Type: application/json" -d '{"name":"New Candy","stock":10,"capacity":50}' http://localhost:4567/add_inventory
        post("/add_inventory", (req, res) -> {
            try {
                // Get JSON body from request
                String body = req.body();
                System.out.println("Received body: " + body);
                
                // Parse JSON
                JSONObject json = (JSONObject) new JSONParser().parse(body);
                
                // Extract values from JSON
                String name = (String) json.get("name");
                
                // Handle different number formats
                Object stockObj = json.get("stock");
                Object capacityObj = json.get("capacity");
                
                int stock = 0;
                int capacity = 0;
                
                if (stockObj instanceof Long) {
                    stock = ((Long) stockObj).intValue();
                } else if (stockObj instanceof Integer) {
                    stock = (Integer) stockObj;
                } else if (stockObj instanceof String) {
                    stock = Integer.parseInt((String) stockObj);
                } else if (stockObj instanceof Double) {
                    stock = ((Double) stockObj).intValue();
                }
                
                if (capacityObj instanceof Long) {
                    capacity = ((Long) capacityObj).intValue();
                } else if (capacityObj instanceof Integer) {
                    capacity = (Integer) capacityObj;
                } else if (capacityObj instanceof String) {
                    capacity = Integer.parseInt((String) capacityObj);
                } else if (capacityObj instanceof Double) {
                    capacity = ((Double) capacityObj).intValue();
                }
                
                System.out.println("Parsed values: name=" + name + ", stock=" + stock + ", capacity=" + capacity);
                
                boolean success = DatabaseManager.addItemAndInventory(name, stock, capacity);
                
                // Return appropriate response
                if (success) {
                    return "Item added to inventory successfully";
                } else {
                    res.status(400);
                    return "Failed to add item to inventory";
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.status(400);
                return "Error: " + e.getMessage();
            }
            });
        
        // Update item in inventory
        // Example usage: curl -X PUT -H "Content-Type: application/json" -d '{"id":19,"stock":20,"capacity":100}' http://localhost:4567/update_inventory 
        put("/update_inventory", (req, res) -> {
            try {
                // Get JSON body from request
                String body = req.body();
                
                // Parse JSON
                JSONObject json = (JSONObject) new JSONParser().parse(body);
                
                // Extract values from JSON
                int id = ((Long) json.get("id")).intValue();
                int stock = ((Long) json.get("stock")).intValue();
                int capacity = ((Long) json.get("capacity")).intValue();
                
                
                boolean success = DatabaseManager.modifyInventory(id, stock, capacity);
                
                // Return appropriate response
                if (success) {
                    return "Inventory item modified successfully";
                } else {
                    res.status(400);
                    return "Failed to modify inventory item";
                }
            } catch (Exception e) {
                res.status(400);
                return "Error: " + e.getMessage();
            }
            });

        // Add distributor 
        // Example usage: curl -X POST -H "Content-Type: application/json" -d '{"name":"New Distributor"}' http://localhost:4567/add_distributors
        post("/add_distributors", (req, res) -> {
            try {
                // Get JSON body from request
                String body = req.body();
                
                // Parse JSON
                JSONObject json = (JSONObject) new JSONParser().parse(body);
                
                // Extract values from JSON
                String name = (String) json.get("name");
                
                
                boolean success = DatabaseManager.addDistributors(name);
                
                // Return appropriate response
                if (success) {
                    return "Distributor added successfully";
                } else {
                    res.status(400);
                    return "Failed to add distributor";
                }
            } catch (Exception e) {
                res.status(400);
                return "Error: " + e.getMessage();
            }
            });

        // Add item to distributor
        // Example usage: curl -X POST -H "Content-Type: application/json" -d '{"distName":"Candy Corp","itemName":"Necco Wafers","cost":0.99}' http://localhost:4567/add_distributor_item
        post("/add_distributor_item", (req, res) -> {
            try {
                // Get JSON body from request
                String body = req.body();
                
                // Parse JSON
                JSONObject json = (JSONObject) new JSONParser().parse(body);
                
                // Extract values from JSON
                String distName = (String) json.get("distName");
                String itemName = (String) json.get("itemName");
                double cost = ((Number) json.get("cost")).doubleValue();
                
                
                boolean success = DatabaseManager.addDistributorItem(distName, itemName, cost);
                
                // Return appropriate response
                if (success) {
                    return "Item added to distributor catalog successfully";
                } else {
                    res.status(400);
                    return "Failed to add item to distributor catalog";
                }
            } catch (Exception e) {
                res.status(400);
                return "Error: " + e.getMessage();
            }
            });

        // Update distributor item
        // Example usage: curl -X PUT -H "Content-Type: application/json" -d '{"distName":"Candy Corp","itemName":"Necco Wafers","cost":1.29}' http://localhost:4567/update_distributor_item
        put("/update_distributor_item", (req, res) -> {
            try {
                // Get JSON body from request
                String body = req.body();
                
                // Parse JSON
                JSONObject json = (JSONObject) new JSONParser().parse(body);
                
                // Extract values from JSON
                String distName = (String) json.get("distName");
                String itemName = (String) json.get("itemName");
                double cost = ((Number) json.get("cost")).doubleValue();
                
                
                boolean success = DatabaseManager.modifyDistributorPrice(distName, itemName, cost);
                
                // Return appropriate response
                if (success) {
                    return "Item modified in distributor catalog successfully";
                } else {
                    res.status(400);
                    return "Failed to modify item in distributor catalog";
                }
            } catch (Exception e) {
                res.status(400);
                return "Error: " + e.getMessage();
            }
            });

        // Get cheapest restock option for an item
        // Example usage: curl "http://localhost:4567/cheapest_restock?itemName=Necco%20Wafers&quantity=10"
        get("/cheapest_restock", (req, res) -> {
            try {
                // Get query parameters
                String itemName = req.queryParams("itemName");
                int quantity = Integer.parseInt(req.queryParams("quantity"));
                
                if (itemName == null || itemName.isEmpty()) {
                    res.status(400);
                    return "Item name is required";
                }
                
                return DatabaseManager.getCheapestRestock(itemName, quantity);
            } catch (Exception e) {
                res.status(400);
                return "Error: " + e.getMessage();
            }
        });

        // Delete item from inventory by name
        // Example usage: curl -X DELETE "http://localhost:4567/delete_inventory?name=Test%20Candy"
        delete("/delete_inventory", (req, res) -> {
            try {
                String name = req.queryParams("name");
                
                if (name == null || name.isEmpty()) {
                    res.status(400);
                    return "Item name is required";
                }
                
                boolean success = DatabaseManager.deleteInventoryItemByName(name);
                
                if (success) {
                    return "Inventory item deleted successfully";
                } else {
                    res.status(400);
                    return "Failed to delete inventory item";
                }
            } catch (Exception e) {
                res.status(400);
                return "Error: " + e.getMessage();
            }
        });

        // Delete distributor by ID
        // Example usage: curl -X DELETE "http://localhost:4567/distributor/4"
        delete("/distributor/:id", (req, res) -> {
            try {
                int id = Integer.parseInt(req.params(":id"));
                boolean success = DatabaseManager.deleteDistributor(id);
                
                if (success) {
                    return "Distributor deleted successfully";
                } else {
                    res.status(400);
                    return "Failed to delete distributor";
                }
            } catch (Exception e) {
                res.status(400);
                return "Error: " + e.getMessage();
            }
        });

        // Stream database updates (Server-Sent Events)
        // Connect to endpoint using this js:
        //const eventSource = new EventSource('http://localhost:4567/stream');
        //eventSource.onmessage = function(event) {
            //const data = JSON.parse(event.data);
            //console.log('Received update:', data);
            //};
        // Example usage: curl -N http://localhost:4567/stream
        get("/stream", (req, res) -> {
            res.type("text/event-stream");
            res.header("Cache-Control", "no-cache");
            res.header("Connection", "keep-alive");
            res.header("Access-Control-Allow-Origin", "*"); // Allow cross-origin requests
            
            // Create a new thread to send updates
            new Thread(() -> {
                try {
                    // Get the output stream
                    OutputStream out = res.raw().getOutputStream();
                    PrintWriter writer = new PrintWriter(out);
                    
                    // Send an initial event to establish the connection
                    writer.write("event: connected\ndata: {\"status\":\"connected\"}\n\n");
                    writer.flush();
                    
                    // Keep the connection open and send updates
                    int count = 0;
                    while (!Thread.currentThread().isInterrupted() && count < 100) { // Limit to 100 updates
                        try {
                            // Get the latest data
                            JSONArray inventory = DatabaseManager.getInventory();
                            
                            // Format as SSE
                            writer.write("event: update\n");
                            writer.write("data: " + inventory.toJSONString() + "\n\n");
                            writer.flush();
                            
                            count++;
                            
                            // Wait before sending the next update
                            Thread.sleep(2000); // 2 seconds
                        } catch (Exception e) {
                            if (e instanceof InterruptedException) {
                                break;
                            }
                            writer.write("event: error\ndata: {\"error\":\"" + e.getMessage() + "\"}\n\n");
                            writer.flush();
                        }
                    }
                } catch (Exception e) {
                    // Connection closed or error occurred
                    System.out.println("SSE connection closed: " + (e.getMessage() != null ? e.getMessage() : "Client disconnected"));
                }
            }).start();
            
            return "";
        });

        // Export table to CSV format
        // Example usage: curl "http://localhost:4567/export/csv?table=items" > items.csv
        get("/export/csv", (req, res) -> {
            try {
                // Get table name from query parameter
                String tableName = req.queryParams("table");
                
                if (tableName == null || tableName.isEmpty()) {
                    res.status(400);
                    return "Table name is required as a query parameter";
                }
                
                // Export table to CSV
                String csvData = DatabaseManager.exportTableToCsv(tableName);
                
                // Check if there was an error
                if (csvData.startsWith("Error:")) {
                    res.status(400);
                    return csvData;
                }
                
                // Set content type and headers for CSV download
                res.type("text/csv");
                res.header("Content-Disposition", "attachment; filename=" + tableName + ".csv");
                
                return csvData;
            } catch (Exception e) {
                res.status(500);
                return "Error: " + e.getMessage();
            }
        });
    }
}