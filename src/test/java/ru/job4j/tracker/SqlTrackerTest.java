package ru.job4j.tracker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class SqlTrackerTest {

    private static Connection connection;

    @BeforeAll
    public static void initConnection() {
        try (InputStream in = SqlTrackerTest.class.getClassLoader().getResourceAsStream("test.properties")) {
            Properties config = new Properties();
            config.load(in);
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterEach
    public void wipeTable() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM items")) {
            statement.execute();
        }
    }

    @AfterAll
    public static void closeConnection() throws SQLException {
        connection.close();
    }

    @Test
    public void whenSaveItemAndFindByGeneratedIdThenMustBeTheSame() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        assertEquals(tracker.findById(item.getId()), item);
    }

    @Test
    public void whenReplaceItem() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        Item newItem = new Item("newItem");
        tracker.add(item);
        System.out.println(item.getId());
        newItem.setId(item.getId());
        tracker.replace(item.getId(), newItem);
        assertEquals(tracker.findById(newItem.getId()), newItem);
    }

    @Test
    public void whenDeleteItem() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item = new Item("item");
        tracker.add(item);
        tracker.delete(item.getId());
        assertNull(tracker.findById(item.getId()));
    }

    @Test
    public void whenFindAllItems() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = new Item("item");
        Item item2 = new Item("item");
        Item item3 = new Item("item");
        Item item4 = new Item("item");
        tracker.add(item1);
        tracker.add(item2);
        tracker.add(item3);
        tracker.add(item4);
        List<Item> expected = List.of(item1, item2, item3, item4);
        assertEquals(expected, tracker.findAll());
    }

    @Test
    public void whenFindByNameItems() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = new Item("item1");
        Item item2 = new Item("item1");
        Item item3 = new Item("item2");
        Item item4 = new Item("item2");
        tracker.add(item1);
        tracker.add(item2);
        tracker.add(item3);
        tracker.add(item4);
        List<Item> expected = List.of(item1, item2);
        assertEquals(expected, tracker.findByName("item1"));
    }

    @Test
    public void whenFindByIdItem() {
        SqlTracker tracker = new SqlTracker(connection);
        Item item1 = new Item("item1");
        Item item2 = new Item("item1");
        tracker.add(item1);
        tracker.add(item2);
        assertEquals(item1, tracker.findById(item1.getId()));
    }
}