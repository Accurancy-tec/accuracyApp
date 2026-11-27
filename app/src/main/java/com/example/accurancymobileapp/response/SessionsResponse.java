package com.example.accurancymobileapp.response;

import com.example.accurancymobileapp.model.DeviceSession;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Answer of auth/sessoes, auth/sessaoEncerrar and auth/sessoesEncerrar. */
public class SessionsResponse extends RespostaSimples {

    @SerializedName("sessoes")
    private List<DeviceSession> sessions;

    /** The session of this device was ended too: the app has to go back to the login. */
    @SerializedName("encerrou_atual")
    private boolean currentSessionEnded;

    @SerializedName("encerradas")
    private int endedCount;

    public List<DeviceSession> getSessions() {
        return sessions == null ? new ArrayList<>() : sessions;
    }

    public boolean isCurrentSessionEnded() {
        return currentSessionEnded;
    }

    public int getEndedCount() {
        return endedCount;
    }
}
