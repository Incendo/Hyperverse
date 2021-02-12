//
//
//  Hyperverse - A minecraft world management plugin
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package se.hyperver.hyperverse.database;

import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.Hyperverse;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public final class SQLiteDatabase extends HyperDatabase {

    private static final String TABLE_LOCATIONS = "CREATE TABLE IF NOT EXISTS locations ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "uuid VARCHAR, world VARCHAR,"
            + "x [DOUBLE PRECISION], y [DOUBLE PRECISION],"
            + "z [DOUBLE PRECISION], locationType VARCHAR,"
            + "UNIQUE (uuid, world, locationType));";

    private Connection connection;

    @Inject
    public SQLiteDatabase(final TaskChainFactory taskChainFactory, final Hyperverse hyperverse) {
        super(taskChainFactory, hyperverse);
    }

    @Override
    public boolean attemptConnect() {
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
                this.getHyperverse().getLogger().severe(
                        "No connection was established. The location table will not not be created.");
                return false;
            }

            this.getHyperverse().getLogger().info("The database has been completely setup.");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void attemptClose() {
        try {
            this.connection.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void storeLocation(
            final @NonNull PersistentLocation persistentLocation, final boolean updateTable,
            final boolean clear
    ) {
        if (updateTable) {
            this.getLocations().get(persistentLocation.getLocationType())
                    .put(UUID.fromString(persistentLocation.getUuid()), persistentLocation.getWorld(),
                            persistentLocation
                    );
        }

        this.getTaskChainFactory().newChain().async(() -> {
            try (final PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT OR REPLACE INTO `locations` (`uuid`, `world`, `x`, `y`, `z`, `locationType`) VALUES(?, ?, ?, ?, ?, ?)")) {
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

    @Override
    public @NonNull CompletableFuture<Collection<PersistentLocation>> getLocations(final @NonNull UUID uuid) {
        final CompletableFuture<Collection<PersistentLocation>> future = new CompletableFuture<>();
        this.getTaskChainFactory().newChain().async(() -> {
            try (final PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM `locations` WHERE `uuid` = ?")) {
                statement.setString(1, uuid.toString());
                final List<PersistentLocation> locationList = new ArrayList<>();
                try (final ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        final PersistentLocation persistentLocation = new PersistentLocation(
                                uuid.toString(),
                                resultSet.getString("world"),
                                resultSet.getDouble("x"),
                                resultSet.getDouble("y"),
                                resultSet.getDouble("z"),
                                LocationType.valueOf(resultSet.getString("locationType"))
                        );
                        locationList.add(persistentLocation);
                        this.getLocations().get(persistentLocation.getLocationType())
                                .put(uuid, persistentLocation.getWorld(), persistentLocation);
                    }
                }
                if (this.getHyperverse().getConfiguration().shouldPrintDebug()) {
                    this.getHyperverse().getLogger().info(String.format("(Debug) Loaded %s persistent locations for player %s",
                            locationList.size(), uuid
                    ));
                }
                future.complete(locationList);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        }).execute();
        return future;
    }

    @Override
    public void clearWorld(final @NonNull String worldName) {
        this.getTaskChainFactory().newChain().async(() -> {
            try (final PreparedStatement statement = this.connection.prepareStatement("DELETE FROM `locations` WHERE `world` = ?")) {
                statement.setString(1, worldName);
                statement.executeUpdate();
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }).execute();
    }

    private void executeUpdate(final @NonNull String sql) {
        try (final Statement statement = this.connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

}
