package hr.fer.zemris.berger.securebanking.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dalvik.system.DexFile;


public class Util {

    public static byte[] footprint(String algorithm, Activity activity) throws IOException,
            NoSuchAlgorithmException {

        String name = null;
        // load file input stream of this class
        String sourceDir = activity.getApplicationInfo().sourceDir;
        DexFile dexFile = new DexFile(sourceDir);
        name = dexFile.getName();

        File file = new File(name);
        byte[] buffer = new byte[4096];

        MessageDigest md = MessageDigest.getInstance(algorithm);

        BufferedInputStream stream = new BufferedInputStream(
                new FileInputStream(file));

        int len = 0;

        while ((len = stream.read(buffer)) != -1) {
            md.update(buffer, 0, len);
        }

        byte[] finalDigest = md.digest(buffer);

        stream.close();

        return finalDigest;
    }

    public static byte[] toByteArray(InputStream stream) throws IOException {

        // create new byte array output stream
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int nextByte;

        // read bytes from the input stream and store them to the
        // byte array output stream
        while ((nextByte = stream.read()) != -1) {
            output.write(nextByte);
        }

        return output.toByteArray();
    }

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String signature(Activity activity) throws PackageManager.NameNotFoundException {
        PackageManager pm = activity.getPackageManager();
        Signature sig = pm.getPackageInfo(activity.getPackageName(), PackageManager.GET_SIGNATURES).signatures[0];
        return String.valueOf(sig.hashCode());
    }

    public static String getDeviceID(Activity activity) {
        return Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

}
