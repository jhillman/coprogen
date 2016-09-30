package com.example.model;   
 
import android.content.ContentValues;
import android.database.Cursor;
  
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import com.google.gson.GsonBuilder;
import com.chatbooks.model.ModelAdapterFactory;
   
import java.util.ArrayList;
import java.util.List;
  
import com.example.database.table.PlaceTable; 
 
import java.util.Set;
  
public class Place {
    private transient long mRowId;
 
    @SerializedName("address") 
    private String mAddress;  
  
    private transient ContentValues mValues = new ContentValues(); 
    public Place() {}  
  
    public Place(final Cursor cursor) {
        this(cursor, false, null);
    }
 
    public Place(final Cursor cursor, Set<String> fields) {
        this(cursor, false, fields);
    }
 
    public Place(final Cursor cursor, boolean prependTableName, Set<String> fields) {
        String prefix = prependTableName ? PlaceTable.TABLE_NAME.replace("`", "") + "_" : "";
 
        if (shouldSet(cursor, fields, prefix + PlaceTable._ID)) { 
            setRowId(cursor.getLong(cursor.getColumnIndex(prefix + PlaceTable._ID)));
        } 
        if (shouldSet(cursor, fields, prefix + PlaceTable.ADDRESS)) {
            setAddress(cursor.getString(cursor.getColumnIndex(prefix + PlaceTable.ADDRESS))); 
        } 
    }
    
    private boolean shouldSet(Cursor cursor, Set<String> fields, String field) {
        return cursor.getColumnIndex(field) != -1 && !cursor.isNull(cursor.getColumnIndex(field)) && (fields == null || fields.contains(field));
    }
  
    public final void setRowId(long _id) {
        mRowId = _id;
        mValues.put(PlaceTable._ID, _id);
    }
 
    public final void setAddress(String address) {
        mAddress = address;
        mValues.put(PlaceTable.ADDRESS, address); 
    }
  
    public long getRowId() {
        return mRowId;
    }
 
    public String getAddress() {
        return mAddress;
    }
  
    public ContentValues getContentValues() {
        return mValues;
    }
  
    public static List<Place> listFromCursor(Cursor cursor, Set<String> fields) {
        List<Place> list = new ArrayList<Place>();
 
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(new Place(cursor, fields));
            } while (cursor.moveToNext());
        }
 
        return list;
    }
  
    public static List<Place> listFromCursor(Cursor cursor) {
        return listFromCursor(cursor, null);
    }
 
    // BEGIN PERSISTED SECTION - put custom methods here

    // END PERSISTED SECTION
 
    public static final class GsonTypeAdapter extends TypeAdapter<Place> {
        private final TypeAdapter<String> mStringAdapter; 
  
        public GsonTypeAdapter(Gson gson) {
            mStringAdapter = gson.getAdapter(String.class);
        }
 
        @Override
        public Place read(JsonReader jsonReader) throws IOException {
            Place jsonPlace = new Place();
            jsonReader.beginObject();
 
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
 
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.skipValue();
                    continue;
                }
                
                switch (name) {
                    case "address":
                        jsonPlace.setAddress(mStringAdapter.read(jsonReader));
                        break;
                     default: {
                        jsonReader.skipValue();
                    }
                }
            }
 
            jsonReader.endObject();
 
            return jsonPlace;
        }
 
        @Override
        public void write(JsonWriter jsonWriter, Place object) throws IOException {
            jsonWriter.beginObject();
 
            if (object.getAddress() != null) {
                jsonWriter.name("address");
                mStringAdapter.write(jsonWriter, object.getAddress());
            }
  
            jsonWriter.endObject();
        }
    }
  
}