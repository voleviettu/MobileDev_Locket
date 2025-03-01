package com.example.locket.ui.settings;

public class SettingsItem {
    private int icon;
    private String title;

    public SettingsItem(int icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }
}