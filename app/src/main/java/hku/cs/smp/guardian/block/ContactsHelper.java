package hku.cs.smp.guardian.block;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactsHelper {

    private ContentResolver resolver;

    private static ContactsHelper instance;

    public synchronized static void init(Context context) {
        if (instance == null)
            instance = new ContactsHelper(context);
    }

    public static ContactsHelper getInstance() {
        return instance;
    }

    private ContactsHelper(Context context) {
        this.resolver = context.getContentResolver();
    }

    public String findNameByNumber(String number) {

        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,

                },
                ContactsContract.Contacts.Data.MIMETYPE + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, number},
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " DESC"
        );

        String name;
        cursor.moveToFirst();
        if (cursor.isAfterLast())
            name = null;
        else
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        cursor.close();
        return name;
    }
}
