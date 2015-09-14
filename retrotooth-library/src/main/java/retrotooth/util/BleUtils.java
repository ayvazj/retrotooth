package retrotooth.util;

import android.util.SparseArray;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;


public class BleUtils {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static byte[] hexToBytes(String s) {
        int length = s.length();
        byte[] data = new byte[length / 2];

        for (int index = 0; index < length; index += 2) {
            data[index / 2] = (byte) ((Character.digit(s.charAt(index), 16) << 4) + Character.digit(s.charAt(index + 1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int index = 0; index < bytes.length; index++) {
            int var = bytes[index] & 0xFF;
            hexChars[index * 2] = hexArray[var >>> 4];
            hexChars[index * 2 + 1] = hexArray[var & 0x0F];
        }
        return "0x" + new String(hexChars);
    }

    public static int byteArrayToInt(byte[] bytes) {
        return new BigInteger(bytes).intValue();
    }


    /**
     * Returns a string composed from a {@link SparseArray}.
     */
    public static String toString(SparseArray<byte[]> array) {
        if (array == null) {
            return "null";
        }
        if (array.size() == 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        for (int i = 0; i < array.size(); ++i) {
            buffer.append(array.keyAt(i)).append("=").append(Arrays.toString(array.valueAt(i)));
        }
        buffer.append('}');
        return buffer.toString();
    }

    /**
     * Returns a string composed from a {@link Map}.
     */
    public static <T> String toString(Map<T, byte[]> map) {
        if (map == null) {
            return "null";
        }
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        Iterator<Map.Entry<T, byte[]>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<T, byte[]> entry = it.next();
            Object key = entry.getKey();
            buffer.append(key).append("=").append(Arrays.toString(map.get(key)));
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }

    private static final String BASE_UUID_FORMAT = "0000%s-0000-1000-8000-00805F9B34FB";

    public static UUID getUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException iae) {
            if (uuid.matches("[0-9a-fA-F]{4}")) {
                return UUID.fromString(String.format(BASE_UUID_FORMAT, uuid.toUpperCase()));
            }
        }
        return null;
    }
}
