package com.example.exper.Contacts;
import android.provider.ContactsContract;

public class ContactMethod<T> {
    private String value;
    private T type;

    public ContactMethod(String value, T type){
        this.value = value;
        this.type = type;
    }

    public T getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setNumber(String number) {
        this.value = number;
    }

    public void setValue(T type) {
        this.type = type;
    }
}
