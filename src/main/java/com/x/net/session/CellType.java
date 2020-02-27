package com.x.net.session;

public enum CellType {

    SANGO, // 本地服
    CROSS, // 国战
    COLISEUM_MANAGER, // 竞技场Manager
    DUNGEON, // 副本(国战)
    REGISTRY;// gvg manager

    public boolean isLocal() {
        return this == SANGO;
    }

    public boolean isCross() {
        return this == CROSS;
    }
    
    public boolean isDungeon() {
        return this == DUNGEON;
    }
}
