package dev.minceraft.sonus.common.rooms;

public enum RoomAudioType {

    OPEN, // Players who are not in your room can hear you, and you can hear them
    NORMAL, // Players who are not in your room cannot hear you, but you can hear them
    ISOLATED, // Players who are not in your room cannot hear you, and you cannot hear them either

}
