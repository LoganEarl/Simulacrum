package database;

import client.AccountTable;
import world.entity.EntityTable;
import world.item.*;
import world.room.Room;
import world.room.RoomTable;
import world.story.StoryArcTable;

import java.util.LinkedList;
import java.util.List;

public class DatabaseTester {
    private static final String DB_NAME = "testSim.db";

    public static void main(String[] args){
        List<DatabaseManager.DatabaseTable> tables = new LinkedList<>();
        tables.add(new AccountTable());
        tables.add(new ItemStatTable());
        tables.add(new ContainerStatTable());
        tables.add(new RoomTable());
        tables.add(new EntityTable());
        tables.add(new StoryArcTable());

        tables.add(new ItemInstanceTable());
        tables.add(new ContainerInstanceTable());

        DatabaseManager.createDirectories();
        DatabaseManager.createNewDatabase(DB_NAME);
        DatabaseManager.createTables(DB_NAME, tables);

        Room r = Room.getRoomByRoomName("The Origin", DB_NAME);
        if(r != null) {
            List<Item> inRoom = Item.getItemsInRoom(r.getRoomName(), DB_NAME);

        }
    }
}
