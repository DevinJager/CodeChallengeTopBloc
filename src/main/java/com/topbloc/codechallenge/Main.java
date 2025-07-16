package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.connect();
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

        // Routes for the Distributors

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

        // Explore any table given table name (CSV format)
        // Example cURL: "curl http://localhost:4567/"table name""
        get("/", (req, res) -> {
            int id = Integer.parseInt(req.params(":table"));
            return DatabaseManager.getItemDynamic(id);
        });


        // POST/PUT/DELETE routes
        post("/items", (req, res) -> { /* add new item */ });
        post("/inventory", (req, res) -> { /* add to inventory */ });
        put("/inventory/:id", (req, res) -> { /* modify inventory */ });
        post("/distributors", (req, res) -> { /* add distributor */ });
        post("/distributor/:id/items", (req, res) -> { /* add item to distributor */ });
        put("/distributor/:id/item/:itemId", (req, res) -> { /* modify price */ });
        get("/cheapest/:id/:quantity", (req, res) -> { /* find cheapest */ });
        delete("/inventory/:id", (req, res) -> { /* delete from inventory */ });
        delete("/distributor/:id", (req, res) -> { /* delete distributor */ });

    }
}