package io.mopesbox.Utils;

import io.mopesbox.Client.GameClient;
import io.mopesbox.Objects.GameObject;
import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.Point;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;



public class Utils {
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public static boolean isValidDouble(double d) {
        if (!Double.isNaN(d) && !Double.isInfinite(d))
            return true;
        return false;
    }
    public static Point rotate(double cx, double cy, double x, double y, double angle, boolean anticlockwise) {
        if (angle == 0) {
        return new Point(Utils.toInt(String.valueOf(x)), Utils.toInt(String.valueOf(y)));
        }
        double radians = anticlockwise ? Math.toRadians(angle) : -Math.toRadians(angle);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double nx = (cos * (x - cx)) + (sin * (y - cy)) + cx;
        double ny = (cos * (y - cy)) - (sin * (x - cx)) + cy;
        return new Point(Utils.toInt(String.valueOf(nx)), Utils.toInt(String.valueOf(ny)));
    }
    public static void doTaskTimer(GameClient client){

        for(Map.Entry<String, String> entry : client.logsArray.entrySet()){
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                client.room.accountConnector.sendLog(entry.getKey(), 0, 0, entry.getValue());

          }
        };
        timer.schedule(task, 5000);

            }
        

            
    }
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static boolean randomBoolean() {
        return Math.random() < 0.5;
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static int randomInt(int min, int max) {
        if (max <= min)
            return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static String encode_utf8(final String string) {
        return encodeURIComponent(string);
    }

    public static boolean toBool(String a) {
        return Boolean.parseBoolean(a);
    }

    public static boolean toBool(int a) {
        return a == 1;
    }

    public static int toInt(String a) {
        int val;
        try {
            val = Integer.parseInt(a);
        } catch (NumberFormatException e) {
            val = 0;
        }
        return val;
    }

    public static double seedAngle(long seed) {
        return (double) (seed % 360);
    }
    public static double toDouble(String a) {
        double val;
        try {
            val = Double.parseDouble(a);
        } catch (NumberFormatException e) {
            val = 0;
        }
        return val;
    }

    public static String decode_utf8(final String string) {
        return decodeURIComponent(string);
    }

    public static String decodeURIComponent(final String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getReadableTime(Long nanos) {
        long tempSec = nanos / (1000 * 1000 * 1000);
        long sec = tempSec % 60;
        long min = (tempSec / 60) % 60;
        long hour = (tempSec / (60 * 60)) % 24;
        long day = (tempSec / (24 * 60 * 60)) % 24;

        return String.format("%dd %dh %dm %ds", day, hour, min, sec);
    }

    public static String encodeURIComponent(final String string) {
        try {
            final String encoded = URLEncoder.encode(string, "UTF-8").replaceAll("\\+", "%20").replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'").replaceAll("\\%28", "(").replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
            return encoded;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String escape(final String string) {
        return StringEscapeUtils.escapeEcmaScript(string);
    }

    public static String unescape(final String string) {
        return StringEscapeUtils.unescapeEcmaScript(string);
    }

    public static double distance(double x1, double x2, double y1, double y2) {
        double a = x1 - x2;
        double b = y1 - y2;
        return Math.sqrt(a * a + b * b);
    }
    public static double getDistance2D(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static boolean TestAABBOverlap(double amaxx, double bmaxx, double aminx, double bminx, double amaxy,
            double bmaxy, double aminy, double bminy) {
        float d1x = (float) (bminx - amaxx);
        float d1y = (float) (bminy - amaxy);
        float d2x = (float) (aminx - bmaxx);
        float d2y = (float) (aminy - bmaxy);

        if (d1x > 0.0f || d1y > 0.0f)
            return false;

        if (d2x > 0.0f || d2y > 0.0f)
            return false;

        return true;
    }

    public static double lerp(double start, double end, double t) {
        return start * (1 - t) + end * t;
    }

    public static double constrain(double n, double low, double high) {
        return Math.max(Math.min(n, high), low);
    }

    public static double normalize(double n, double min, double max) {
        return (n - min) / (max - min);
    }

    public static boolean testCollision(GameObject obj1, GameObject obj2) {
        return testCollision(obj1, obj2, 0);
    }

    public static boolean testCollision(GameObject obj1, GameObject obj2, double modifier) {
        return Utils.distance(obj1.getX(), obj2.getX(), obj1.getY(),
        obj2.getY()) < obj1.getRadius() + obj2.getRadius() + modifier;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
            if(d == 0){
                
            }
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static double smooth(double t) {
        return 1f - Math.cos(t * Math.PI * 0.5f);

    }

    public static byte set_bit(byte num, int bit, boolean value) {
        if (value)
            return (byte) (num | 1 << bit);
        else
            return (byte) (num & ~(1 << bit));

    }

    public static boolean get_bit(byte num, int bit) {
        return ((num >> bit) % 2 != 0);
    }
}
