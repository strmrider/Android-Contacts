package com.example.exper.Contacts;

import android.provider.ContactsContract.CommonDataKinds;

import java.util.ArrayList;

public class Contact {
    private final String id;
    private final String name;
    private ArrayList<ContactMethod<String>> numbers = new ArrayList<>();
    private ArrayList<ContactMethod<String>> emails = new ArrayList<>();

    public Contact(String id, String name){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ArrayList<ContactMethod<String>> getNumbers() {
        return numbers;
    }

    public ArrayList<ContactMethod<String>> getEmails() {
        return emails;
    }

    public void addPhoneNumber(String type, String number){
        this.numbers.add(new ContactMethod<>(number, type));
    }
}
