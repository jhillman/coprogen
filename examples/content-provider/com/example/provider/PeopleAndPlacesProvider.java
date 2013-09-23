package com.example.provider;
 
import com.example.database.PeopleAndPlacesDatabase;
 
import com.example.database.table.*;
 
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.content.ContentUris;
import android.database.sqlite.SQLiteQueryBuilder;
 
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
 
    public static final Uri PERSON_CONTENT_URI = Uri.withAppendedPath(PeopleAndPlacesProvider.AUTHORITY_URI, PersonContent.CONTENT_PATH);
 
    public static final Uri PLACE_CONTENT_URI = Uri.withAppendedPath(PeopleAndPlacesProvider.AUTHORITY_URI, PlaceContent.CONTENT_PATH);
   
    private static final UriMatcher URI_MATCHER;
    private PeopleAndPlacesDatabase mDatabase;
 
    private static final int PERSON_DIR = 0;
    private static final int PERSON_ID = 1;
 
    private static final int PLACE_DIR = 2;
    private static final int PLACE_ID = 3;
   
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
 
        URI_MATCHER.addURI(AUTHORITY, PersonContent.CONTENT_PATH, PERSON_DIR);
        URI_MATCHER.addURI(AUTHORITY, PersonContent.CONTENT_PATH + "/#",    PERSON_ID);
 
        URI_MATCHER.addURI(AUTHORITY, PlaceContent.CONTENT_PATH, PLACE_DIR);
        URI_MATCHER.addURI(AUTHORITY, PlaceContent.CONTENT_PATH + "/#",    PLACE_ID);
     }
 
    public static final class PersonContent implements BaseColumns {
        public static final String CONTENT_PATH = "person";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.people_and_places.person";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.people_and_places.person";
    }
 
    public static final class PlaceContent implements BaseColumns {
        public static final String CONTENT_PATH = "place";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.people_and_places.place";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.people_and_places.place";
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
            case PERSON_DIR:
                queryBuilder.setTables(PersonTable.TABLE_NAME);
                break;
 
            case PLACE_ID:
                queryBuilder.appendWhere(PlaceTable._ID + "=" + uri.getLastPathSegment());
            case PLACE_DIR:
                queryBuilder.setTables(PlaceTable.TABLE_NAME);
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
 
        try {
            dbConnection.beginTransaction();
 
            switch (URI_MATCHER.match(uri)) {
                case PERSON_DIR:
                case PERSON_ID:
                    final long personId = dbConnection.insertOrThrow(PersonTable.TABLE_NAME, null, values);
                    final Uri newPersonUri = ContentUris.withAppendedId(PERSON_CONTENT_URI, personId);
                    getContext().getContentResolver().notifyChange(newPersonUri, null); 
                    dbConnection.setTransactionSuccessful();
                    return newPersonUri;
 
                case PLACE_DIR:
                case PLACE_ID:
                    final long placeId = dbConnection.insertOrThrow(PlaceTable.TABLE_NAME, null, values);
                    final Uri newPlaceUri = ContentUris.withAppendedId(PLACE_CONTENT_URI, placeId);
                    getContext().getContentResolver().notifyChange(newPlaceUri, null); 
                    dbConnection.setTransactionSuccessful();
                    return newPlaceUri;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
  
                   dbConnection.setTransactionSuccessful();
                   break;
               case PERSON_ID :
                   final long personId = ContentUris.parseId(uri);
                   updateCount = dbConnection.update(PersonTable.TABLE_NAME, values, 
                       PersonTable._ID + "=" + personId + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")"), selectionArgs);
  
                   dbConnection.setTransactionSuccessful();
                   break;
 
               case PLACE_DIR :
                   updateCount = dbConnection.update(PlaceTable.TABLE_NAME, values, selection, selectionArgs);
  
                   dbConnection.setTransactionSuccessful();
                   break;
               case PLACE_ID :
                   final long placeId = ContentUris.parseId(uri);
                   updateCount = dbConnection.update(PlaceTable.TABLE_NAME, values, 
                       PlaceTable._ID + "=" + placeId + (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")"), selectionArgs);
  
                   dbConnection.setTransactionSuccessful();
                   break;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } finally {
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
  
                    dbConnection.setTransactionSuccessful();
                    break;
                case PERSON_ID :
                    deleteCount = dbConnection.delete(PersonTable.TABLE_NAME, PersonTable.WHERE_ID_EQUALS, new String[] { uri.getLastPathSegment() });
  
                    dbConnection.setTransactionSuccessful();
                    break;
 
                case PLACE_DIR :
                    deleteCount = dbConnection.delete(PlaceTable.TABLE_NAME, selection, selectionArgs);
  
                    dbConnection.setTransactionSuccessful();
                    break;
                case PLACE_ID :
                    deleteCount = dbConnection.delete(PlaceTable.TABLE_NAME, PlaceTable.WHERE_ID_EQUALS, new String[] { uri.getLastPathSegment() });
  
                    dbConnection.setTransactionSuccessful();
                    break;
  
                default :
                    throw new IllegalArgumentException("Unsupported URI:" + uri);
            }
        } finally {
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