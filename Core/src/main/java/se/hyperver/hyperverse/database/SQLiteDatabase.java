package se.hyperver.hyperverse.database;

import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
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

            this.connection = DriverManager.getConnection("jdbc:sqlite:./plugins/Hyperverse/storage.db");

            if (this.connection == null) {
                this.executeUpdate(TABLE_LOCATIONS);
            }
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

    @Override public CompletableFuture<Collection<PersistentLocation>> getLocations(@NotNull final UUID uuid) {
        final CompletableFuture<Collection<PersistentLocation>> future = new CompletableFuture<>();
        this.getTaskChainFactory().newChain().async(() -> {
            try (final PreparedStatement statement = this.connection.prepareStatement("SELECT * FROM `location` WHERE `uuid` = ?")) {
                statement.setString(1, uuid.toString());
                final List<PersistentLocation> locationList = new ArrayList<>();
                try (final ResultSet resultSet = statement.executeQuery()) {
                    locationList.add(new PersistentLocation(uuid.toString(), resultSet.getString("world"),
                        resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"),
                        LocationType.valueOf(resultSet.getString("locationType"))));
                }
                for (final PersistentLocation persistentLocation : locationList) {
                    this.getLocations().get(persistentLocation.getLocationType())
                        .put(uuid, persistentLocation.getWorld(), persistentLocation);
                }
                future.complete(locationList);
            } catch (final Exception e) {
                future.completeExceptionally(e);
            }
        });
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
