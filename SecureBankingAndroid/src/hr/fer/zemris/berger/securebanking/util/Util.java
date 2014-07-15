package hr.fer.zemris.berger.securebanking.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.provider.Settings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dalvik.system.DexFile;

/**
 * Utility class. Contains methods for calculating application hash, fetching application signature,
 * and fetching device ID. 
 * @author Viktor Berger
 * @version 1.0
 */
public class Util {

	/**
	 * Calculates hash code of this application (DEX file).
	 * @param algorithm hashing algorithm
	 * @param activity context
	 * @return hash code of the DEX file
	 * @throws IOException if problems occur during the file reading
	 * @throws NoSuchAlgorithmException if provided algorithm doesn't exist
	 */
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

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    /**
     * Converts byte array to HEX string.
     * @param bytes byte array
     * @return string in HEX representation
     */
    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Returns the signature of this application.	
     * @param activity context
     * @return
     * @throws PackageManager.NameNotFoundException if the package can't be found.
     */
    public static String signature(Activity activity) throws PackageManager.NameNotFoundException {
        PackageManager pm = activity.getPackageManager();
        Signature sig = pm.getPackageInfo(activity.getPackageName(), PackageManager.GET_SIGNATURES).signatures[0];
        return toHexString(sig.toByteArray());
    }

    /**
     * Returns unique device identifier.
     * @param activity context
     * @return device ID
     */
    public static String getDeviceID(Activity activity) {
        return Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

}
