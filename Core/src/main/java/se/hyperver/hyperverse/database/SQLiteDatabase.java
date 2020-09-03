package se.hyperver.hyperverse.database;

import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.Hyperverse;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class SQLiteDatabase extends HyperDatabase {

    private static final String TABLE_LOCATIONS = "CREATE TABLE IF NOT EXISTS locations ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "uuid VARCHAR, world VARCHAR,"
        + "x [DOUBLE PRECISION], y [DOUBLE PRECISION],"
        + "z [DOUBLE PRECISION], locationType VARCHAR,"
        + "UNIQUE (uuid, world, locationType));";

    private Connection connection;

    @Inject public SQLiteDatabase(final TaskChainFactory taskChainFactory, final Hyperverse hyperverse) {
        super(taskChainFactory, hyperverse);
    }

    @Override public boolean attemptConnect() {
        try {
            Class.forName("org.sqlite.JDBC");

            final File file = new File(this.getHyperverse().getDataFolder(), "storage.db");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    new RuntimeException("Could not create storage.db").printStackTrace();
                    return false;
                }
            }

            final String url = String.format("jdbc:sqlite:%s", file.getAbsolutePath());
            this.getHyperverse().getLogger().info(String.format("Connecting to SQLite database: %s", url));
            this.connection = DriverManager.getConnection(url);

            if (this.connection != null) {
                this.executeUpdate(TABLE_LOCATIONS);
            } else {
                this.getHyperverse().getLogger().severe("No connection was established. The location table will not not be created.");
                return false;
            }

            this.getHyperverse().getLogger().info("The database has been completely setup.");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override public void attemptClose() {
        try {
            this.connection.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void storeLocation(@NotNull final PersistentLocation persistentLocation, final boolean updateTable,
        final boolean clear) {
        if (updateTable) {
            this.getLocations().get(persistentLocation.getLocationType())
                    .put(UUID.fromString(persistentLocation.getUuid()), persistentLocation.getWorld(),
                            persistentLocation);
        }

        this.getTaskChainFactory().newChain().async(() -> {
            try (final PreparedStatement statement = this.connection.prepareStatement("INSERT OR REPLACE INTO `locations` (`uuid`, `world`, `x`, `y`, `z`, `locationType`) VALUES(?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, persistentLocation.getUuid());
                statement.setString(2, persistentLocation.getWorld());
                statement.setDouble(3, persistentLocation.getX());
                statement.setDouble(4, persistentLocation.getY());
                statement.setDouble(5, persistentLocation.getZ());
                statement.setString(6, persistentLocation.getLocationType().name());
                statement.executeUpdate();
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }).syncLast(in -> {
            if (clear) {
                this.clearLocations(UUID.fromString(persistentLocation.getUuid()));
            }
        }).execute();
    }

    @Override public CompletableFuture<Collection<PersistentLocation>> getLocations(@NotNull final UUID uuid) {
        final CompletableFuture<Collection<PersistentLocation>> future = new CompletableFuture<>();
        this.getTaskChainFactory().newChain().async(() -> {
            try (final PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM `locations` WHERE `uuid` = ?")) {
                statement.setString(1, uuid.toString());
                final List<PersistentLocation> locationList = new ArrayList<>();
                try (final ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final PersistentLocation persistentLocation = new PersistentLocation(uuid.toString(), resultSet.getString("world"),
                                resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"),
                                LocationType.valueOf(resultSet.getString("locationType")));
                        locationList.add(persistentLocation);
                        this.getLocations().get(persistentLocation.getLocationType())
                                .put(uuid, persistentLocation.getWorld(), persistentLocation);
                    }
                }
                if (this.getHyperverse().getConfiguration().shouldPrintDebug()) {
                    this.getHyperverse().getLogger().info(String.format("(Debug) Loaded %s persistent locations for player %s",
                            locationList.size(), uuid));
                }
                future.complete(locationList);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        }).execute();
        return future;
    }

    @Override public void clearWorld(@NotNull String worldName) {

    }

    private void executeUpdate(@NonNull final String sql) {
        try (final Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

}
