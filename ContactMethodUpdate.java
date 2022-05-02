package com.example.exper.Contacts;

public class ContactMethodUpdate {
    private final String oldValue;
    private final String newValue;

    public ContactMethodUpdate(String oldValue, String newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getOldValue() {
        return oldValue;
    }
}
