package me.kyrobi.cynagenteams;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.HashMap;

public class Datastore {

    public static HashMap<String, ListingData> myDataStore = new HashMap<>();
    private static String DB_PATH;

    public static void initialize() {
        File file = new File("");
        DB_PATH = String.valueOf(new File(file.getAbsolutePath() + File.separator + "plugins" + File.separator + "CynagenTeams" + File.separator + "data.db"));

        File dbFile = new File(DB_PATH);

        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS listings (
                name TEXT PRIMARY KEY,
                creationDate INTEGER NOT NULL,
                description TEXT NOT NULL,
                motd TEXT NOT NULL
            );
            """;

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            if (dbFile.exists()) {
                System.out.println("Database already exists. No action taken.");
            } else {
                System.out.println("A new database has been created.");
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("Table 'listings' has been created (or already exists).");
            } catch (SQLException e) {
                System.out.println("Failed to create the table: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }

        loadAllData();
    }

    private static void loadAllData() {
        String selectSQL = "SELECT * FROM listings";
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             PreparedStatement pstmt = connection.prepareStatement(selectSQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                long creationDate = rs.getLong("creationDate");
                String description = rs.getString("description");
                String motd = rs.getString("motd");

                ListingData listingData = new ListingData(name, creationDate, description, motd);
                myDataStore.put(name, listingData);
            }

            System.out.println("All data has been loaded into myDataStore.");

        } catch (SQLException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    public static void uninitialize() {
        String insertOrReplaceSQL = "INSERT OR REPLACE INTO listings (name, creationDate, description, motd) VALUES (?, ?, ?, ?)";

        final int BATCH_SIZE = 1000;

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             PreparedStatement pstmt = connection.prepareStatement(insertOrReplaceSQL)) {

            connection.setAutoCommit(false);

            int count = 0;
            for (ListingData listingData : myDataStore.values()) {
                pstmt.setString(1, listingData.getPartyName());
                pstmt.setLong(2, listingData.getCreationDate());
                pstmt.setString(3, listingData.getDescription());
                pstmt.setString(4, listingData.getMotd());
                pstmt.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                }
            }

            pstmt.executeBatch();
            connection.commit();
            Bukkit.getLogger().info("[CynagenPwarp] All data has been saved to the database.");

        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error saving data: " + e.getMessage());
        }

        myDataStore.clear();
    }

    public static void removePartyListing(String name) {
        myDataStore.remove(name);

        String deleteSQL = "DELETE FROM listings WHERE name = ?";

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
             PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {

            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                myDataStore.remove(name);
                Bukkit.getLogger().info("[CynagenPwarp] Warp with name " + name + " has been removed from the database.");
            } else {
                Bukkit.getLogger().info("[CynagenPwarp] No warp found with name " + name + ".");
            }

        } catch (SQLException e) {
            Bukkit.getLogger().severe("Error removing warp with name " + name + ": " + e.getMessage());
        }
    }
}
