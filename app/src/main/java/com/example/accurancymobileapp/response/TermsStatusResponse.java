package com.example.accurancymobileapp.response;

import com.google.gson.annotations.SerializedName;

/** POST auth/termos: whether the user still has to accept the current Terms of Use. */
public class TermsStatusResponse extends RespostaSimples {

    @SerializedName("version") public String version;
    @SerializedName("accepted_at") public String acceptedAt;
    @SerializedName("pending") public boolean pending;
}
