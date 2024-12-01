package io.questdb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.questdb.cairo.TableToken;

public class BinarySerializer {
    private static final byte TYPE_NULL = 0;
    private static final byte TYPE_INTEGER = 1;
    private static final byte TYPE_LONG = 2;
    private static final byte TYPE_DOUBLE = 3;
    private static final byte TYPE_STRING = 4;
    private static final byte TYPE_BOOLEAN = 5;
    private static final byte TYPE_TABLE_TOKEN = 6;

    public static void serializeToBinary(Map<TableToken, List<Object>> data, String outputPath) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(outputPath)))) {
            
            // Write number of tables
            out.writeInt(data.size());
            
            // Write each table's data
            for (Map.Entry<TableToken, List<Object>> entry : data.entrySet()) {
                // Serialize TableToken
                writeTableToken(out, entry.getKey());
                
                List<Object> values = entry.getValue();
                // Write number of values
                out.writeInt(values.size());
                
                // Write each value
                for (Object value : values) {
                    writeValue(out, value);
                }
            }
        }
    }

    private static void writeTableToken(DataOutputStream out, TableToken token) throws IOException {
        out.writeByte(TYPE_TABLE_TOKEN);
        
        // Write tableName
        writeString(out, token.getTableName());
        
        // Write dirName
        writeString(out, token.getDirName());
        
        // Write tableId
        out.writeInt(token.getTableId());
        
        // Write boolean flags
        out.writeBoolean(token.isWal());
        out.writeBoolean(token.isSystem());
        out.writeBoolean(token.isProtected());
        out.writeBoolean(token.isPublic());
    }

    private static void writeString(DataOutputStream out, String str) throws IOException {
        byte[] bytes = str.getBytes("UTF-8");
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static void writeValue(DataOutputStream out, Object value) throws IOException {
        if (value == null) {
            out.writeByte(TYPE_NULL);
        } else if (value instanceof Integer) {
            out.writeByte(TYPE_INTEGER);
            out.writeInt((Integer) value);
        } else if (value instanceof Long) {
            out.writeByte(TYPE_LONG);
            out.writeLong((Long) value);
        } else if (value instanceof Double) {
            out.writeByte(TYPE_DOUBLE);
            out.writeDouble((Double) value);
        } else if (value instanceof String) {
            out.writeByte(TYPE_STRING);
            writeString(out, (String) value);
        } else if (value instanceof Boolean) {
            out.writeByte(TYPE_BOOLEAN);
            out.writeBoolean((Boolean) value);
        } else if (value instanceof TableToken) {
            writeTableToken(out, (TableToken) value);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + value.getClass());
        }
    }

    public static Map<TableToken, List<Object>> deserializeFromBinary(String inputPath) throws IOException {
        Map<TableToken, List<Object>> result = new HashMap<>();
        
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(
                new FileInputStream(inputPath)))) {
            
            // Read number of tables
            int numTables = in.readInt();
            
            // Read each table's data
            for (int i = 0; i < numTables; i++) {
                // Read TableToken
                TableToken tableToken = readTableToken(in);
                
                // Read number of values
                int numValues = in.readInt();
                List<Object> values = new ArrayList<>(numValues);
                
                // Read each value
                for (int j = 0; j < numValues; j++) {
                    values.add(readValue(in));
                }
                
                result.put(tableToken, values);
            }
            return result;
        }
    }

    private static TableToken readTableToken(DataInputStream in) throws IOException {
        byte type = in.readByte();
        if (type != TYPE_TABLE_TOKEN) {
            throw new IOException("Expected TABLE_TOKEN type but got: " + type);
        }
        
        String tableName = readString(in);
        String dirName = readString(in);
        int tableId = in.readInt();
        boolean isWal = in.readBoolean();
        boolean isSystem = in.readBoolean();
        boolean isProtected = in.readBoolean();
        boolean isPublic = in.readBoolean();
        
        return new TableToken(tableName, dirName, tableId, isWal, isSystem, isProtected, isPublic);
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, "UTF-8");
    }

    private static Object readValue(DataInputStream in) throws IOException {
        byte type = in.readByte();
        switch (type) {
            case TYPE_NULL:
                return null;
            case TYPE_INTEGER:
                return in.readInt();
            case TYPE_LONG:
                return in.readLong();
            case TYPE_DOUBLE:
                return in.readDouble();
            case TYPE_STRING:
                return readString(in);
            case TYPE_BOOLEAN:
                return in.readBoolean();
            case TYPE_TABLE_TOKEN:
                return readTableToken(in);
            default:
                throw new IOException("Unknown type: " + type);
        }
    }
}