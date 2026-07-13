package dev.evvie.waylandcraft.desktop;

public record RawDesktopEntry(String appId, String name, String genericName, String exec, boolean execTerminal,
                              String comment, String[] keywords, String[] categories, boolean visible,
                              String iconPath) {

}
