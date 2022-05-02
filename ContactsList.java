package com.example.exper.Contacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class ContactsList {
    private ArrayList<Contact> contacts;

    public ContactsList(){
        contacts = new ArrayList<>();
    }

    public ContactsList(ArrayList<Contact> contacts){
        this.contacts = contacts;
    }

    public ContactsList(ContentResolver cr){
        this.readContactsFromDevice(cr);
    }

    private void readContactsFromDevice(ContentResolver cr){
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0){
            while (cursor.moveToNext()){
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                int phoneNumbers = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (phoneNumbers > 0){
                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            String phoneNUmber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            System.out.println(id + "" + name + "" + phoneNUmber);
                        }
                        phoneCursor.close();
                    }
                }
            }
            cursor.close();
        }
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }
}
