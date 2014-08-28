/*
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.asp;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import com.google.common.base.Charsets;

/**
 * An utility class to generate TimeUUID based on a given time,<br>
 * which is compatible with Cassandra's time UUID,<br>
 * and can be parsed back to date instance/timestamp using {@link com.datastax.driver.core.utils.UUIDs#unixTimestamp(UUID)}
 *
 * @see http://www.datastax.com/doc-source/developer/java-apidocs/com/datastax/driver/core/utils/UUIDs.html
 */
public class TimeUUID {
    private static final long CLOCK_SEQ_AND_NODE = makeClockSeqAndNode(true);
    private static final long NODE = makeNode();

    // http://www.ietf.org/rfc/rfc4122.txt
    private static final long START_EPOCH = makeEpoch();

    /**
     * Generate a unique Time based UUID based on the provided date.
     *
     * @param date
     *            date used for the "time" component of the UUID.
     */
    public static UUID forDate(Date date) {
        return forDate(date, true);
    }

    /**
     * Generate a Time based UUID based on the provided date.
     *
     * @param date
     *            date used for the "time" component of the UUID.
     * @param randomize
     *            Should we randomize the time UUID.
     */
    public static UUID forDate(Date date, Boolean randomize) {
        long clockSeqAndNode = CLOCK_SEQ_AND_NODE;
        if (randomize) {
            // Generate the clock sequence again to make this UUID unique/random
            clockSeqAndNode = makeClockSeqAndNode(false);
        }

        return new UUID(makeMSB(fromUnixTimestamp(date.getTime())), clockSeqAndNode);
    }

    /* Clones of methods from {@link com.datastax.driver.core.utils.UUIDs} */
    /**
     * Create timeuuid timestamp from unix timestamp.
     *
     * @param tstamp
     * @return
     */
    private static long fromUnixTimestamp(long tstamp) {
        return (tstamp - START_EPOCH) * 10000;
    }

    private static long makeMSB(long timestamp) {
        long msb = 0L;
        msb |= (0x00000000ffffffffL & timestamp) << 32;
        msb |= (0x0000ffff00000000L & timestamp) >>> 16;
        msb |= (0x0fff000000000000L & timestamp) >>> 48;
        msb |= 0x0000000000001000L; // sets the version to 1.
        return msb;
    }

    /**
     * Updated version of {@link com.datastax.driver.core.utils.UUIDs#makeClockSeqAndNode()}<br>
     * which allows us to generate a Random clock that
     * does not use the current time as the seed to generate truly random clock.<br>
     * Node will not be generated again.
     *
     * @param useSeed
     * @return
     */
    private static long makeClockSeqAndNode(boolean useSeed) {
        long clock;
        if (useSeed) {
            clock = new Random(System.currentTimeMillis()).nextLong();
        } else {
            clock = new Random().nextLong();
        }
        long node = NODE;

        long lsb = 0;
        lsb |= (clock & 0x0000000000003FFFL) << 48;
        lsb |= 0x8000000000000000L;
        lsb |= node;
        return lsb;
    }

    private static long makeNode() {
        /*
         * We don't have access to the MAC address (in pure JAVA at least) but
         * need to generate a node part that identify this host as uniquely as
         * possible. The spec says that one option is to take as many source
         * that identify this node as possible and hash them together. That's
         * what we do here by gathering all the ip of this host as well as a few
         * other sources.
         */
        try {

            MessageDigest digest = MessageDigest.getInstance("MD5");
            for (String address : getAllLocalAddresses())
                update(digest, address);

            Properties props = System.getProperties();
            update(digest, props.getProperty("java.vendor"));
            update(digest, props.getProperty("java.vendor.url"));
            update(digest, props.getProperty("java.version"));
            update(digest, props.getProperty("os.arch"));
            update(digest, props.getProperty("os.name"));
            update(digest, props.getProperty("os.version"));

            byte[] hash = digest.digest();

            long node = 0;
            for (int i = 0; i < 6; i++)
                node |= (0x00000000000000ffL & hash[i]) << (i * 8);
            // Since we don't use the mac address, the spec says that multicast
            // bit (least significant bit of the first byte of the node ID) must be 1.
            return node | 0x0000010000000000L;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static void update(MessageDigest digest, String value) {
        if (value != null)
            digest.update(value.getBytes(Charsets.UTF_8));
    }

    private static Set<String> getAllLocalAddresses() {
        Set<String> allIps = new HashSet<String>();
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            allIps.add(localhost.toString());
            // Also return the hostname if available, it won't hurt (this does a
            // dns lookup, it's only done once at startup)
            allIps.add(localhost.getCanonicalHostName());
            InetAddress[] allMyIps = InetAddress.getAllByName(localhost.getCanonicalHostName());
            if (allMyIps != null) {
                for (int i = 0; i < allMyIps.length; i++)
                    allIps.add(allMyIps[i].toString());
            }
        } catch (UnknownHostException e) {
            // Ignore, we'll try the network interfaces anyway
        }

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en != null) {
                while (en.hasMoreElements()) {
                    Enumeration<InetAddress> enumIpAddr = en.nextElement().getInetAddresses();
                    while (enumIpAddr.hasMoreElements())
                        allIps.add(enumIpAddr.nextElement().toString());
                }
            }
        } catch (SocketException e) {
            // Ignore, if we've really got nothing so far, we'll throw an exception
        }

        return allIps;
    }

    private static long makeEpoch() {
        // UUID v1 timestamp must be in 100-nanoseconds interval since 00:00:00.000 15 Oct 1582.
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT-0"));
        c.set(Calendar.YEAR, 1582);
        c.set(Calendar.MONTH, Calendar.OCTOBER);
        c.set(Calendar.DAY_OF_MONTH, 15);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

}
