package com.example.exper.Contacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class Contacts {
    private static Contact setContact(ContentResolver cr, Cursor cursor){
        if (cursor != null) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            Contact contact = new Contact(id, name);
            setContactsPhoneNumbers(cr, cursor, contact);
            return contact;
        }
        return null;
    }

    private static void setContactsPhoneNumbers(ContentResolver cr, Cursor cursor, Contact contact){
        int phoneNumbers = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
        if (phoneNumbers > 0){
            Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contact.getId()},
                    null);
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    String phoneNUmber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String type = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                    contact.addPhoneNumber(type, phoneNUmber);
                }
                phoneCursor.close();
            }
        }
    }

    public static ArrayList<Contact> getContactsList(ContentResolver cr){
        ArrayList<Contact> contacts = new ArrayList<>();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0){
            while (cursor.moveToNext()){
                setContact(cr, cursor);
            }
            cursor.close();
        }
        return contacts;
    }

    private static Contact setContactFromSelection(ContentResolver cr, String selection){
        Contact contact = null;
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, selection, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            contact = setContact(cr, cursor);
            cursor.close();
        }
        return contact;
    }

    public static Contact getContactByName(ContentResolver cr, String name){
        String selection = String.format("%s = '%s'", ContactsContract.Data.DISPLAY_NAME, name);
        return setContactFromSelection(cr, selection);
    }

    public static Contact getContactByPhoneNumber(ContentResolver cr, String number){
        String selection = String.format("%s = '%s'", ContactsContract.Data.DATA1, number);
        return setContactFromSelection(cr, selection);
    };

    public static void deleteContact(ContentResolver cr, String id)
            throws OperationApplicationException, RemoteException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ContentProviderOperation.Builder builder =
                ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI);
        String[] args = {id};
        builder.withSelection(ContactsContract.RawContacts.CONTACT_ID + "=?", args);
        ops.add(builder.build());
        cr.applyBatch(ContactsContract.AUTHORITY, ops);
    }


    private static ContentProviderOperation.Builder getBuilder(){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        return builder;
    }

    public static void addNewContact(ContentResolver cr, Contact contact)
            throws OperationApplicationException, RemoteException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ContentProviderOperation op = ContentProviderOperation.newInsert(
                                ContactsContract.RawContacts.CONTENT_URI)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build();
        ops.add(op);
        ContentProviderOperation.Builder builder = getBuilder();

        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getName());
        ops.add(builder.build());

        if (contact.getNumbers() != null) {
            for (ContactMethod<String> cm : contact.getNumbers()) {
                builder = getBuilder();
                builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, cm.getValue());
                builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, cm.getType());
                ops.add(builder.build());
            }
        }
        if (contact.getEmails() != null) {
            for (ContactMethod<String> cm : contact.getEmails()) {
                builder = getBuilder();
                builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, cm.getValue());
                builder.withValue(ContactsContract.CommonDataKinds.Email.TYPE, cm.getType());
                ops.add(builder.build());
            }
        }
        cr.applyBatch(ContactsContract.AUTHORITY, ops);
    }

    public static void updateContactName(ContentResolver cr, String id, String newName)
            throws OperationApplicationException, RemoteException {
        String where = String.format(
                "%s = '%s' AND %s = ?",
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                ContactsContract.Data.CONTACT_ID);

        String[] args = {id};
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(where, args)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, newName)
                .build()
        );
        cr.applyBatch(ContactsContract.AUTHORITY, ops);
    }

    public static void updateContactNumbers(ContentResolver cr, ArrayList<ContactMethodUpdate> numbers)
            throws OperationApplicationException, RemoteException {
        String where = String.format(
                "%s = '%s' AND %s = ?",
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                ContactsContract.Data.DATA1);

        if (numbers != null) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            for (ContactMethodUpdate update : numbers){
                String[] args = {update.getOldValue()};
                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(where, args)
                        .withValue(ContactsContract.Data.DATA1, update.getNewValue())
                        .build()
                );
            }
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        }
    }
}
