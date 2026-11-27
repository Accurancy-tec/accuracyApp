package com.example.accurancymobileapp.model;

import com.google.gson.annotations.SerializedName;

/** A device (or browser) where the account is open, as returned by auth/sessoes. */
public class DeviceSession {

    @SerializedName("id_sessao")
    private int id;

    @SerializedName("dispositivo")
    private String deviceName;

    /** "Android", "Site" or null for old logins. */
    @SerializedName("plataforma")
    private String platform;

    @SerializedName("criado_em")
    private String createdAt;

    @SerializedName("ultimo_uso_em")
    private String lastUsedAt;

    /** The device that made the request. */
    @SerializedName("atual")
    private boolean current;

    public int getId() { return id; }
    public String getDeviceName() { return deviceName == null || deviceName.trim().isEmpty() ? "Aparelho" : deviceName; }
    public String getPlatform() { return platform; }
    public String getCreatedAt() { return createdAt; }
    public String getLastUsedAt() { return lastUsedAt; }
    public boolean isCurrent() { return current; }

    public boolean isSite() {
        return "Site".equals(platform);
    }
}
