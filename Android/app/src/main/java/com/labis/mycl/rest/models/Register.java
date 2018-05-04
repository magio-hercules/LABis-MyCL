package com.labis.mycl.rest.models;

public class Register {
    public final String result;
    public final String reason;
    public final String id;

    public Register(String result, String reason, String id) {
        this.result = result;
        this.reason = reason;
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public String getReason() {
        return reason;
    }

    public String getId() {
        return id;
    }
}
