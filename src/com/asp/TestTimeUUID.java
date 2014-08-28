package com.asp;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.utils.UUIDs;

/**
 * Testing TimeUUID generator from the below sources;
 * https://wiki.apache.org/cassandra/FAQ#working_with_timeuuid_in_java
 * http://grepcode
 * .com/file_/repo1.maven.org/maven2/com.carmatechnologies.cassandra
 * /cassandra-utils/0.1/com/carmatech/cassandra/TimeUUID.java/?v=source
 */
public class TestTimeUUID {

//    private static final String FMT = "%s: %s Time UUID 1 for \"%s\" is %s and converted back it is %s";
    private static final String FMT = "%s, %s, %s, %s, %s";

    public static void main(String[] args) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

        List<String> sources = new ArrayList<String>();
        sources.add("2014-08-04 12:00:00-0400");
        sources.add("2014-08-04 10:00:00-0400");
        sources.add("2014-08-03 09:00:00-0400");
        sources.add("2014-08-02 08:00:00-0400");

        for (String source : sources) {
            Date date = sdf.parse(source);

            UUID tuid1 = uuidForDate(date);
            UUID tuid2 = uuidForDate(date);

            Date uiddate1 = new Date(UUIDs.unixTimestamp(tuid1));
            Date uiddate2 = new Date(UUIDs.unixTimestamp(tuid2));
//            System.out.println(String.format(FMT, getHostname(), "", date, tuid1, uiddate1));
//            System.out.println(String.format(FMT + "\n", getHostname(), "", date, tuid2, uiddate2));
//            System.out.println("Test status(F)==== " + (!tuid1.equals(tuid2) && date.equals(uiddate1) && date.equals(uiddate2)));

            tuid1 = TimeUUID.forDate(date);
            tuid2 = TimeUUID.forDate(date);

            uiddate1 = new Date(UUIDs.unixTimestamp(tuid1));
            uiddate2 = new Date(UUIDs.unixTimestamp(tuid2));
//            System.out.println(String.format(FMT, getHostname(), "Cassandra", date, tuid1, new Date(UUIDs.unixTimestamp(tuid1))));
//            System.out.println(String.format(FMT + "\n", getHostname(), "Cassandra", date, tuid2, new Date(UUIDs.unixTimestamp(tuid2))));
//            System.out.println("Test status(F)==== " + (!tuid1.equals(tuid2) && date.equals(uiddate1) && date.equals(uiddate2)));

            tuid1 = TimeUUID.forDate(date, true);
            tuid2 = TimeUUID.forDate(date, true);

            uiddate1 = new Date(UUIDs.unixTimestamp(tuid1));
            uiddate2 = new Date(UUIDs.unixTimestamp(tuid2));
            System.out.println(String.format(FMT, getHostname(), "Random", date, tuid1, new Date(UUIDs.unixTimestamp(tuid1))));
            System.out.println(String.format(FMT + "\n", getHostname(), "Random", date, tuid2, new Date(UUIDs.unixTimestamp(tuid2))));
            System.out.println("Test status(T)==== " + (!tuid1.equals(tuid2) && date.equals(uiddate1) && date.equals(uiddate2)));
        }
    }

    /**
     * Generate a new, unique UUID based on the provided date.
     *
     * @param date
     *            date used for the "time" component of the UUID.
     */
    public static UUID uuidForDate(Date date) {
        /*
         * Magic number obtained from #cassandra's thobbs, who claims to have
         * stolen it from a Python library.
         */
        final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

        long origTime = date.getTime();
        long time = origTime * 10000 + NUM_100NS_INTERVALS_SINCE_UUID_EPOCH;

        // Example:
        // Lowest 16 bits and version 1: 0123 4567 89AB CDEF -> 89AB CDEF 0000
        // 0000 -> 89AB CDEF 0000 1000
        // Middle 32 bits: 0123 4567 89AB CDEF -> 0000 4567 0000 0000 -> 0000
        // 0000 4567 0000 -> 89AB CDEF 4567 1000
        // Highest 16 bits: 0123 4567 89AB CDEF -> 0123 0000 0000 0000 -> 0000
        // 0000 0000 0123 -> 89AB CDEF 4567 1123

        long timeLow = time & 0xffffffffL;
        long timeMid = time & 0xffff00000000L;
        long timeHi = time & 0xfff000000000000L;
        long upperLong = (timeLow << 32) | (timeMid >> 16) | (1 << 12) | (timeHi >> 48);
        return new UUID(upperLong, 0xC000000000000000L);
    }

    private static String getHostname() {
        try {
            InetAddress inetAddr = InetAddress.getLocalHost();

            byte[] addr = inetAddr.getAddress();

            // Convert to dot representation
            @SuppressWarnings("unused")
            String ipAddr = "";
            for (int i = 0; i < addr.length; i++) {
                if (i > 0) {
                    ipAddr += ".";
                }
                ipAddr += addr[i] & 0xFF;
            }

            String hostname = inetAddr.getHostName();

//            System.out.println("IP Address: " + ipAddr);
//            System.out.println("Hostname: " + hostname);
            return hostname;

        } catch (UnknownHostException e) {
            System.out.println("Host not found: " + e.getMessage());
        }
        return null;
    }

}
