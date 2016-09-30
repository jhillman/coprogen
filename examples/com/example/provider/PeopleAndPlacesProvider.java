package com.example.provider;
 
import com.example.database.PeopleAndPlacesDatabase;
 
import com.example.database.table.*;
 
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.content.ContentUris;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
 
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
 
import java.util.ArrayList;
import java.util.List;
 
public class PeopleAndPlacesProvider extends ContentProvider {
 
    public static final String AUTHORITY = "com.example.provider.people_and_places";
  
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final String TAG = "PeopleAndPlacesProvider";
    public static final String SQL_INSERT_OR_REPLACE = "__sql_insert_or_replace__";
 
    public static final Uri PERSON_CONTENT_URI = Uri.withAppendedPath(PeopleAndPlacesProvider.AUTHORITY_URI, PersonContent.CONTENT_PATH);
 
    public static final Uri PLACE_CONTENT_URI = Uri.withAppendedPath(PeopleAndPlacesProvider.AUTHORITY_URI, PlaceContent.CONTENT_PATH);
  
    public static final Uri PLACE_JOIN_PERSON_CONTENT_URI = Uri.withAppendedPath(PeopleAndPlacesProvider.AUTHORITY_URI, PlaceJoinPersonContent.CONTENT_PATH);
  
    private static final UriMatcher URI_MATCHER;
    private PeopleAndPlacesDatabase mDatabase;
 
    private static final int PERSON_DIR = 0;
    private static final int PERSON_ID = 1;
 
    private static final int PLACE_DIR = 2;
    private static final int PLACE_ID = 3;
  
    private static final int PLACE_JOIN_PERSON_DIR = 4;
  
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
 
        URI_MATCHER.addURI(AUTHORITY, PersonContent.CONTENT_PATH, PERSON_DIR);
        URI_MATCHER.addURI(AUTHORITY, PersonContent.CONTENT_PATH + "/#", PERSON_ID);
 
        URI_MATCHER.addURI(AUTHORITY, PlaceContent.CONTENT_PATH, PLACE_DIR);
        URI_MATCHER.addURI(AUTHORITY, PlaceContent.CONTENT_PATH + "/#", PLACE_ID);
  
        URI_MATCHER.addURI(AUTHORITY, PlaceJoinPersonContent.CONTENT_PATH, PLACE_JOIN_PERSON_DIR);
   }
 
    private static class PersonContent implements BaseColumns {
        private PersonContent() {}
 
        public static final String CONTENT_PATH = "`person`";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.people_and_places.person";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.people_and_places.person";
    }
 
    private static class PlaceContent implements BaseColumns {
        private PlaceContent() {}
 
        public static final String CONTENT_PATH = "`place`";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.people_and_places.place";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.people_and_places.place";
    }
  
    public static final class PlaceJoinPersonContent implements BaseColumns {
        public static final String CONTENT_PATH = "place_join_person";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.people_and_places.place_join_person";
    }
  
    @Override
    public final boolean onCreate() {
        mDatabase = new PeopleAndPlacesDatabase(getContext());
        return true;
    }
 
    @Override
    public final String getType(final Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case PERSON_DIR:
                return PersonContent.CONTENT_TYPE;
            case PERSON_ID:
                return PersonContent.CONTENT_ITEM_TYPE;
 
            case PLACE_DIR:
                return PlaceContent.CONTENT_TYPE;
            case PLACE_ID:
                return PlaceContent.CONTENT_ITEM_TYPE;
  
            case PLACE_JOIN_PERSON_DIR:
                return PlaceJoinPersonContent.CONTENT_TYPE;
  
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
 
    @Override
    public final Cursor query(final Uri uri, String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        final SQLiteDatabase dbConnection = mDatabase.getReadableDatabase();
 
        switch (URI_MATCHER.match(uri)) {
            case PERSON_ID:
                queryBuilder.appendWhere(PersonTable._ID + "=" + uri.getLastPathSegment());
                break;
            case PERSON_DIR:
                queryBuilder.setTables(PersonTable.TABLE_NAME);
                break;
 
            case PLACE_ID:
                queryBuilder.appendWhere(PlaceTable._ID + "=" + uri.getLastPathSegment());
                break;
            case PLACE_DIR:
                queryBuilder.setTables(PlaceTable.TABLE_NAME);
                break;
  
            case PLACE_JOIN_PERSON_DIR:
                queryBuilder.setTables(PlaceTable.TABLE_NAME + " LEFT OUTER JOIN " + PersonTable.TABLE_NAME + " ON (" + PlaceTable.TABLE_NAME + "." + PlaceTable._ID + "=" + PersonTable.TABLE_NAME + "." + PersonTable.ID_PLACE + ")");
 
                projection = new String[] {
                    PlaceTable.TABLE_NAME + "." + PlaceTable._ID + " || " + PersonTable.TABLE_NAME + "." + PersonTable._ID + " AS " + PlaceTable._ID,
 
                    PersonTable.TABLE_NAME + "._id AS " + PersonTable.TABLE_NAME.replace("`", "") + "__id",
 
                    PersonTable.TABLE_NAME.replace("`", "") + "." + PersonTable.NAME + " AS " + PersonTable.TABLE_NAME.replace("`", "") + "_" + PersonTable.NAME,
 
                    PersonTable.TABLE_NAME.replace("`", "") + "." + PersonTable.AGE + " AS " + PersonTable.TABLE_NAME.replace("`", "") + "_" + PersonTable.AGE,
 
                    PersonTable.TABLE_NAME.replace("`", "") + "." + PersonTable.ALIVE + " AS " + PersonTable.TABLE_NAME.replace("`", "") + "_" + PersonTable.ALIVE,
 
                    PersonTable.TABLE_NAME.replace("`", "") + "." + PersonTable.BODY_FAT + " AS " + PersonTable.TABLE_NAME.replace("`", "") + "_" + PersonTable.BODY_FAT,
 
                    PersonTable.TABLE_NAME.replace("`", "") + "." + PersonTable.ID_PLACE + " AS " + PersonTable.TABLE_NAME.replace("`", "") + "_" + PersonTable.ID_PLACE,
 
                    PlaceTable.TABLE_NAME + "._id AS " + PlaceTable.TABLE_NAME.replace("`", "") + "__id",
 
                    PlaceTable.TABLE_NAME.replace("`", "") + "." + PlaceTable.ADDRESS + " AS " + PlaceTable.TABLE_NAME.replace("`", "") + "_" + PlaceTable.ADDRESS,
                };
                break;
  
            default :
                throw new IllegalArgumentException("Unsupported URI:" + uri);
        }
 
        Cursor cursor = queryBuilder.query(dbConnection, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
 
        return cursor;
 
    }
 
    @Override
    public final Uri insert(final Uri uri, final ContentValues values) {
        final SQLiteDatabase dbConnection = mDatabase.getWritableDatabase();
        boolean replace = false;
 
        if (values.containsKey(SQL_INSERT_OR_REPLACE)) {
            replace = values.getAsBoolean(SQL_INSERT_OR_REPLACE);
            values.remove(SQL_INSERT_OR_REPLACE);
        }
 
        try {
            dbConnection.beginTransaction();
 
            switch (URI_MATCHER.match(uri)) {
                case PERSON_DIR:
                case PERSON_ID:
                    final long personId = replace ? dbConnection.replaceOrThrow(PersonTable.TABLE_NAME, null, values) : dbConnection.insertOrThrow(PersonTable.TABLE_NAME, null, values);
                    final Uri personUri = ContentUris.withAppendedId(PERSON_CONTENT_URI, personId);
                    getContext().getContentResolver().notifyChange(personUri, null);
                    getContext().getContentResolver().notifyChange(PLACE_JOIN_PERSON_CONTENT_URI, null);
  
                    return personUri;
 
                case PLACE_DIR:
                case PLACE_ID:
                    final long placeId = replace ? dbConnection.replaceOrThrow(PlaceTable.TABLE_NAME, null, values) : dbConnection.insertOrThrow(PlaceTable.TABLE_NAME, null, values);
                    final Uri placeUri = ContentUris.withAppendedId(PLACE_CONTENT_URI, placeId);
                    getContext().getContentResolver().notifyChange(placeUri, null);
                    getContext().getContentResolver().notifyChange(PLACE_JOIN_PERSON_CONTENT_URI, null);
  
                    return placeUri;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            dbConnection.setTransactionSuccessful();
            dbConnection.endTransaction();
        }
 
        return null;
    }
 
    @Override
    public final int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase dbConnection = mDatabase.getWritableDatabase();
        int updateCount = 0;
        List<Uri> joinUris = new ArrayList<Uri>();
 
        try {
            dbConnection.beginTransaction();
 
            switch (URI_MATCHER.match(uri)) {
               case PERSON_DIR :
                   updateCount = dbConnection.update(PersonTable.TABLE_NAME, values, selection, selectionArgs);
 
                   joinUris.add(PLACE_JOIN_PERSON_CONTENT_URI);
  
                   break;
               case PERSON_ID :
                   final long personId = ContentUris.parseId(uri);
                   updateCount = dbConnection.update(PersonTable.TABLE_NAME, values, 
                       PersonTable._ID + "=" + personId + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")"), selectionArgs);
 
                   joinUris.add(PLACE_JOIN_PERSON_CONTENT_URI);
  
                   break;
 
               case PLACE_DIR :
                   updateCount = dbConnection.update(PlaceTable.TABLE_NAME, values, selection, selectionArgs);
 
                   joinUris.add(PLACE_JOIN_PERSON_CONTENT_URI);
  
                   break;
               case PLACE_ID :
                   final long placeId = ContentUris.parseId(uri);
                   updateCount = dbConnection.update(PlaceTable.TABLE_NAME, values, 
                       PlaceTable._ID + "=" + placeId + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")"), selectionArgs);
 
                   joinUris.add(PLACE_JOIN_PERSON_CONTENT_URI);
  
                   break;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } finally {
            dbConnection.setTransactionSuccessful();
            dbConnection.endTransaction();
        }
 
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
 
            for (Uri joinUri : joinUris) {
                getContext().getContentResolver().notifyChange(joinUri, null);
            }
        }
 
        return updateCount;
 
    }
 
    @Override
    public final int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase dbConnection = mDatabase.getWritableDatabase();
        int deleteCount = 0;
        List<Uri> joinUris = new ArrayList<Uri>();
 
        try {
            dbConnection.beginTransaction();
 
            switch (URI_MATCHER.match(uri)) {
                case PERSON_DIR :
                    deleteCount = dbConnection.delete(PersonTable.TABLE_NAME, selection, selectionArgs);
 
                    joinUris.add(PLACE_JOIN_PERSON_CONTENT_URI);
  
                    break;
                case PERSON_ID :
                    deleteCount = dbConnection.delete(PersonTable.TABLE_NAME, PersonTable.WHERE_ID_EQUALS, new String[] { uri.getLastPathSegment() });
 
                    joinUris.add(PLACE_JOIN_PERSON_CONTENT_URI);
  
                    break;
 
                case PLACE_DIR :
                    deleteCount = dbConnection.delete(PlaceTable.TABLE_NAME, selection, selectionArgs);
 
                    joinUris.add(PLACE_JOIN_PERSON_CONTENT_URI);
  
                    break;
                case PLACE_ID :
                    deleteCount = dbConnection.delete(PlaceTable.TABLE_NAME, PlaceTable.WHERE_ID_EQUALS, new String[] { uri.getLastPathSegment() });
 
                    joinUris.add(PLACE_JOIN_PERSON_CONTENT_URI);
  
                    break;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } finally {
            dbConnection.setTransactionSuccessful();
            dbConnection.endTransaction();
        }
 
        if (deleteCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
 
            for (Uri joinUri : joinUris) {
                getContext().getContentResolver().notifyChange(joinUri, null);
            }
        }
 
        return deleteCount;
    }
}