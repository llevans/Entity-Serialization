package eaf.core.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eaf.core.security.application_stig.StigAPP6240;

import org.hibernate.cfg.Configuration;
import org.jboss.logging.Logger;

/**
 * Static class defining common resources for EAF Java application.
 *
 */
public final class EafCommon {

    /**
     * APPID constant. Necessary to open proper DB PostgreSQL schema.
     */

    public static final int     EA_APPID            = 0;
    /**
     * APPID constant. Necessary to open proper DB PostgreSQL schema.
     */

    public static final int     MAAD_APPID          = 1;
    /**
     * APPID constant. Necessary to open proper DB PostgreSQL schema.
     */

    public static final int     JMCMS_APPID         = 2;

    /**
     * APPID constant. Necessary to open proper DB PostgreSQL schema.
     */

    public static final int     IRMS_APPID          = 3;

    /**
     * APPID constant. Necessary to open proper DB PostgreSQL schema.
     */

    public static final int     PUB_APPID           = 4;

    /**
     * APPID constant. Necessary to open proper DB PostgreSQL schema.
     */

    public static final int     SECURITY_APPID      = 5;
    
    /**
     * APPID constant.  Necessary to open proper DB PostgresSQL schema.
     */
    
    public static final int PMIS_APPID = 6;

    public static final Integer JAVA_TLOG_START_SEQ = 599999;

    /**
     * Users disposition enumeration.
     *
     */
    public static enum USERS_DISP {
        QUEUED(1), ACTIVE(2), INACTIVE(3), DL_USER(3), DISTRIBUTION_LIST(5);

        private final int id;

        USERS_DISP(final int disp_id) {
            this.id = disp_id;
        }

        public int getId() {
            return id;
        }

        public static int get(final String s) {
            if (s.toUpperCase().equals("QUEUED"))
                return 1;
            if (s.toUpperCase().equals("ACTIVE"))
                return 2;
            if (s.toUpperCase().equals("INACTIVE"))
                return 3;
            if (s.toUpperCase().equals("DL_USER"))
                return 4;
            if (s.toUpperCase().equals("DISTRIBUTION_LIST"))
                return 5;
            else
                return -1;
        }
    };

    /**
     * APPS enumeration.
     *
     */
    public static enum APPS {
        EA(0, "ea"), IRMS(3, "irms"), JMCMS(2, "jmcms"), MAAD(1, "maad"), PUBLIC(
                4, "public"), SECURITY(5, "security"), PMIS (6, "pmis");
        /**
         * Get APPS enumeration value based on integer key.
         *
         * @param i
         *            integer equal to an APPID constant
         * @return APPS enumeration object
         *
         */
        public static APPS getApp(final int i) {
            switch (i) {
                case EA_APPID:
                    return APPS.EA;
                case MAAD_APPID:
                    return APPS.MAAD;
                case JMCMS_APPID:
                    return APPS.JMCMS;
                case IRMS_APPID:
                    return APPS.IRMS;
                case PUB_APPID:
                    return APPS.PUBLIC;
                case SECURITY_APPID:
                    return APPS.SECURITY;
                case PMIS_APPID :
                    return APPS.PMIS;
                default:
                    return APPS.PUBLIC;
            }
        }

        /**
         * Get APPS enumeration value for a given database schema name.
         *
         * @param t
         *            String EAF application name
         * @return APPS enumerated value
         */
        public static APPS getApp(final String t) {
            try {
                return APPS.getApp(Arrays.asList(EafCommon.DB_SCHEMAS).indexOf(t.toLowerCase()));
            } catch (final Exception e) {
                return APPS.PUBLIC;
            }
        }

        /**
         * APPS enumeration object id data member.
         */
        private final int    id;

        /**
         * APPS enumeration object schema data member.
         */
        private final String schema;

        /**
         * APPS constructor.
         *
         * @param aid
         *            APP ID integer constant
         */
        APPS(final int aid) {
            this.id = aid;
            this.schema = EafCommon.DB_SCHEMAS[aid];
        }

        /**
         * APPS constructor.
         *
         * @param aid
         *            APP ID integer constant
         * @param aschema
         *            DB schema name
         */
        APPS(final int aid, final String aschema) {
            this.id = aid;
            this.schema = aschema;
        }

        /**
         * return APPS enumeration object integer id.
         *
         * @return integer id
         */
        public int getId() {
            return this.id;
        }

        /**
         * return APPS enumeration object schema name.
         *
         * @return schema name
         */
        public String getSchema() {
            return this.schema;
        }
    }

    /**
     * JMCMS Hibernate configuration object.
     */
    private static org.hibernate.cfg.Configuration daConfiguration;

    /**
     * JMCMS Hibernate Session Factory that manages db connection pool.
     */

    private static org.hibernate.SessionFactory    daSessionFactory;

    /**
     * String array of DB schema key names.
     */
    public static final String[] DB_SCHEMAS = {"ea", "maad", "jmcms", "irms",
        "public", "security", "pmis"};

    /**
     * EA Hibernate configuration object.
     */

    private static org.hibernate.cfg.Configuration eaConfiguration;

    /**
     * EA Hibernate Session Factory that manages db connection pool.
     */

    private static org.hibernate.SessionFactory    eaSessionFactory;

    /**
     * IRMS Hibernate configuration object.
     */

    private static org.hibernate.cfg.Configuration irmsConfiguration;

    /**
     * IRMS Hibernate Session Factory that manages db connection pool.
     */

    private static org.hibernate.SessionFactory    irmsSessionFactory;

    /**
     * Security Hibernate configuration object.
     */
    private static org.hibernate.cfg.Configuration securityConfiguration;

    /**
     * Security Hibernate Session Factory that manages db connection pool.
     */

    private static org.hibernate.SessionFactory    securitySessionFactory;

    /**
     * Log4J logger object.
     */

    private static Logger                          log         = Logger.getLogger(EafCommon.class.getName());

    /**
     * MAAD Hibernate Session Factory that manages db connection pool.
     */

    private static org.hibernate.cfg.Configuration maadConfiguration;

    /**
     * MAAD Hibernate configuration object.
     */

    private static org.hibernate.SessionFactory    maadSessionFactory;

    /**
     * PUBLIC Hibernate configuration object.
     */

    private static org.hibernate.cfg.Configuration publicConfiguration;

    /**
     * PUBLIC Hibernate Session Factory that manages db connection pool.
     */
    private static org.hibernate.SessionFactory    publicSessionFactory;
    
    /**
     * PMIS Hibernate configuration object.
     */
//PMISFLAG
//    private static org.hibernate.cfg.Configuration pmisConfiguration;

    /**
     * PUBLIC Hibernate Session Factory that manages db connection pool.
     */
  //PMISFLAG
//    private static org.hibernate.SessionFactory pmisSessionFactory;

    /**
     * PYTHON_LIBS constant - location of external Python classes.
     */
    // public static final String PYTHON_LIBS = "/export/code/"
    // + "webInfrastructure/trunk/jython/";
    public static final String                     PYTHON_LIBS = StaticVars.getJbossRoot() + StaticVars.getOsSep()
                                                                       + "server" + StaticVars.getOsSep()
                                                                       + "cnmoc_it_default" + StaticVars.getOsSep()
                                                                       + "lib" + StaticVars.getOsSep();

    static {

        EafCommon.eaConfiguration = new Configuration().configure("ea.hibernate.cfg.xml");
        EafCommon.eaSessionFactory = EafCommon.eaConfiguration.buildSessionFactory();

        EafCommon.daConfiguration = new Configuration().configure("da.hibernate.cfg.xml");
        EafCommon.daSessionFactory = EafCommon.daConfiguration.buildSessionFactory();

        // irmsConfiguration = new Configuration()
        // .configure("irms.hibernate.cfg.xml");
        // irmsSessionFactory = irmsConfiguration.buildSessionFactory();

        maadConfiguration = new Configuration().configure("maad.hibernate.cfg.xml");
        maadSessionFactory = maadConfiguration.buildSessionFactory();

        EafCommon.publicConfiguration = new Configuration().configure("public.hibernate.cfg.xml");
        EafCommon.publicSessionFactory = EafCommon.publicConfiguration.buildSessionFactory();

        EafCommon.securityConfiguration = new Configuration().configure("security.hibernate.cfg.xml");
        EafCommon.securitySessionFactory = EafCommon.securityConfiguration.buildSessionFactory();
//PMISFLAG
//        EafCommon.pmisConfiguration = new Configuration().configure("pmis.hibernate.cfg.xml");        
//        EafCommon.pmisSessionFactory = EafCommon.pmisConfiguration.buildSessionFactory();

        //
        // At startup, perform inactive EAF accounts security check.
        //
        StigAPP6240.getInstance();

    }

    /**
     * Implement EJB 3.0 standard for camel-case entity attribute names.
     *
     * Called by readFormByXML() in Transformer to verify and/or convert HTML
     * form field names to match the POJO data field names.
     *
     * @param aStr
     *            String incoming string
     * @return newStr String camel cased
     */

    public static String camelCase(final String aStr) {

        final int asciiOffset = 32;
        String incStr = aStr;

        if (incStr.indexOf("_") < 0) {
            return incStr;

        } else {
            incStr = incStr.toLowerCase();

            String newStr = incStr.substring(0, 1);

            char c;

            for (int i = 1; i < incStr.length(); i++) {
                c = incStr.charAt(i);
                if ((incStr.charAt(i - 1) == '_') && !incStr.substring(i, i + 1).matches("-?\\d+(.\\d+)?")) {
                    c = (char) (c - asciiOffset);
                }
                newStr += c;
            }

            newStr = newStr.replace("_", "");

            return newStr;

        }
    }

    /**
     * Implement a capitalize string function.
     *
     * @param aStr
     *            String incoming string
     * @param unCamelCase
     *            Boolean
     * @return newStr String capitalized
     */

    public static String capitalize(final String aStr, final Boolean... unCamelCase) {

        String newStr = aStr;

        if (unCamelCase.length == 0 || (unCamelCase.length > 0 && !unCamelCase[0]))
            return newStr.substring(0, 1).toUpperCase() + newStr.substring(1).toLowerCase();

        else {

            String words = "";

            for (int i = 0; i < aStr.length(); i++) {

                if (i == 0)
                    words += aStr.substring(0, 1).toUpperCase();

                else if (aStr.charAt(i) < 'A' || aStr.charAt(i) > 'Z')
                    words += aStr.substring(i, i + 1);

                else
                    words += " " + aStr.substring(i, i + 1);

            }
            return words;
        }

    }

    /**
     * Print information message to the console and server.log using Log4J
     * logger.
     *
     * @param incStr
     *            String
     *
     */
    public static void debug(final Object incStr) {
        // TODO this method will be deprecated and replaced w/ one that includes
        // the first parameter: incClass
        //

        EafCommon.log.debug(incStr.toString());
    }

    /**
     * Print information message to the console and server.log using Log4J
     * logger.
     *
     * @param incStr
     *            String
     *
     */
    public static void info(final Object incStr) {
        // TODO this method will be deprecated and replaced w/ one that includes
        // the first parameter: incClass
        //

        EafCommon.log.info(incStr.toString());
    }

    /**
     * Print information message to the console and server.log using Log4J
     * logger.
     *
     * Log levels ascending Less information printed as move up list Wrappers to
     * call log4j methods can be added to this class. EafCommon.log.fatal(""); ^
     * EafCommon.log.error(""); | EafCommon.log.warn(""); |
     * EafCommon.log.info(""); | EafCommon.log.debug(""); |
     *
     * ~cnmoc_it_default/conf/jboss-log4j-development.xml set to INFO and above
     * ~cnmoc_it_default/conf/jboss-log4j-operational.xml set to WARN and above
     *
     * @param incClass
     *            class
     * @param incStr
     *            String
     * @param level
     *            - org.apache.log4j.Level
     *
     */
    public static void log(final Class<?> incClass, final Object incStr, final org.apache.log4j.Level... level) {
        if (Arrays.asList(level).size() > 0) {
            if (level[0].equals(org.apache.log4j.Level.DEBUG)) {
                log.debug("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
            } else if (level[0].equals(org.apache.log4j.Level.INFO)) {
                log.info("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
            } else if (level[0].equals(org.apache.log4j.Level.WARN)) {
                log.warn("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
            } else if (level[0].equals(org.apache.log4j.Level.ERROR)) {
                log.error("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
            } else if (level[0].equals(org.apache.log4j.Level.FATAL)) {
                log.fatal("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
            }
        } else {
            log.debug("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
        }

    }

    /***/
    public static void debug(final Class<?> incClass, final Object incStr) {
        EafCommon.log.debug("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
    }

    /***/
    public static void info(final Class<?> incClass, final Object incStr) {
        EafCommon.log.info("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
    }

    /***/
    public static void warn(final Class<?> incClass, final Object incStr) {
        EafCommon.log.warn("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
    }

    /***/
    public static void error(final Class<?> incClass, final Object incStr) {
        EafCommon.log.error("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
    }

    /***/
    public static void fatal(final Class<?> incClass, final Object incStr) {
        EafCommon.log.fatal("--> [" + incClass.getName() + "]\n\t" + incStr.toString());
    }

    /**
     * Request database connection to a specific schema.
     *
     * @param appId
     *            int Application ID
     * @return Hibernate DB Session
     *
     */
    public static org.hibernate.Session getDBSession(final int appId) {

        switch (appId) {
            case (EA_APPID):
                return EafCommon.eaSessionFactory.openSession();
            case (MAAD_APPID):
                return EafCommon.maadSessionFactory.openSession();
            case (JMCMS_APPID):
                return EafCommon.daSessionFactory.openSession();
            case (IRMS_APPID):
                return EafCommon.irmsSessionFactory.openSession();
            case (PUB_APPID):
                return EafCommon.publicSessionFactory.openSession();
            case (SECURITY_APPID):
                return EafCommon.securitySessionFactory.openSession();
//PMISFLAG
//            case (PMIS_APPID) :
//                return EafCommon.pmisSessionFactory.openSession();          
            default:
                return EafCommon.publicSessionFactory.openSession();
        }
    }

    /**
     * Return database connection pool attributes.
     *
     * @param appId
     *            int enumerated value
     * @return Hibernate Configuration
     *
     */
    public static org.hibernate.cfg.Configuration getConfiguration(final int appId) {

        switch (appId) {
            case (EA_APPID):
                return EafCommon.eaConfiguration;
            case (MAAD_APPID):
                return EafCommon.maadConfiguration;
            case (JMCMS_APPID):
                return EafCommon.daConfiguration;
            case (IRMS_APPID):
                return EafCommon.irmsConfiguration;
            case (PUB_APPID):
                return EafCommon.publicConfiguration;
            case (SECURITY_APPID):
                return EafCommon.securityConfiguration;
//PMISFLAG
//            case (PMIS_APPID) :
//                return EafCommon.pmisConfiguration;
            default:
                return EafCommon.publicConfiguration;
        }
    }

    /**
     * Initialize database connection pools.
     *
     * @param appId
     *            int enumerated value
     * @return Hibernate Session Factory
     *
     */
    public static org.hibernate.SessionFactory getSessionFactory(final int appId) {

        switch (appId) {
            case (EA_APPID):
                return EafCommon.eaSessionFactory;
            case (MAAD_APPID):
                return EafCommon.maadSessionFactory;
            case (JMCMS_APPID):
                return EafCommon.daSessionFactory;
            case (IRMS_APPID):
                return EafCommon.irmsSessionFactory;
            case (PUB_APPID):
                return EafCommon.publicSessionFactory;
            case (SECURITY_APPID):
                return EafCommon.securitySessionFactory;
//PMISFLAG
//            case (PMIS_APPID) :
//                return EafCommon.pmisSessionFactory;
            default:
                return EafCommon.publicSessionFactory;
        }
    }
}
