package org.minecrafttest.main.Database;

import java.util.List;

public class Database {
    private final ParkourDataLobby parkourDataLobby = new ParkourDataLobby();

    public List<String> getListInTo(int List_A, int List_B){
        return parkourDataLobby.getScoresInTo(List_A, List_B);
    }
}
