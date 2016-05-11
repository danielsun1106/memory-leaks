package org.apache.markt.leaks.ldap;

import org.apache.markt.leaks.LeakBase;

/**
 * Java 5
 *  - leaks
 * Java 6
 *  - leaks
 * Java 7
 *  - leaks
 * Java 8
 *  - leaks
 */
public class PoolManagerLeak extends LeakBase {

    public static void main(String[] args) {
        PoolManagerLeak poolManagerLeak = new PoolManagerLeak();
        poolManagerLeak.doLeakTest();
    }


    @Override
    protected void createLeakingObjects() {
        System.setProperty("com.sun.jndi.ldap.connect.pool.timeout", "1");
        try {
            Class.forName("com.sun.jndi.ldap.LdapPoolManager");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void cleanUpLeakingObjects() {
        // None
    }
}