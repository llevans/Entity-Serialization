package eaf.core.entities;

import java.util.ArrayList;
import java.util.Date;

import eaf.core.common.EafCommon;
import eaf.core.common.GlobalVars;
import eaf.core.entities.pub.EafTransaction;
import eaf.core.entities.pub.Poc;
import eaf.core.entities.pub.PocOrg;

/**
 * Parent class representing a base EAF data entity. The core attributes of an
 * entity, {@code owner, datetime, archive, origOwnSys}, are defined within this
 * class.
 *
 * @author lyn.evans
 *
 */
public abstract class EafEntity {

    /**
     * Identify the standard Java package in which the EAF data access object
     * (identified by incEntityNm) is defined.
     *
     * @param incEntityNm
     *            fully qualified entity name
     *            i.e.main.java.mil.navy.cnmoc.eaf.jmcms.MplParam
     *
     * @return Object - instantiated data object
     *
     *
     */

    public static Object findPOJO(final String incEntityNm) {

        String tmpNm = "", pkgNm = "", module = "";

        String entityNm = incEntityNm;

        PersistentEntity entity = null;

        EafCommon.debug("Find POJO " + entityNm);

        try {
            tmpNm = entityNm.split("\\.")[entityNm.split("\\.").length - 1];
            pkgNm = entityNm.substring(0, entityNm.indexOf(tmpNm)).toLowerCase();
            if (pkgNm.equals("")) {
                pkgNm = "pub";
            }
            module = pkgNm.split("\\.")[pkgNm.split("\\.").length - 1];

        } catch (final Exception e) {
            EafCommon.debug("findPOJO: could not split incoming EntityNm");
        }

        try {
            entity = (PersistentEntity) Class.forName(entityNm).newInstance();
        } catch (final ClassNotFoundException e) {
            EafCommon.debug("findPOJO: try " + entityNm);
        } catch (final Exception e) {
            EafCommon.debug("findPOJO: try " + entityNm);
        }

        // If entity POJO not found, try POJO name all caps
        if (entity == null) {
            try {
                entityNm = pkgNm + tmpNm.toUpperCase();
                entity = (PersistentEntity) Class.forName(entityNm).newInstance();
            } catch (final Exception e) {
                EafCommon.debug("findPOJO: try " + entityNm);
            }
        }

        // If entity POJO not found, try POJO name capitalized
        if (entity == null) {
            try {
                entityNm = pkgNm + tmpNm.substring(0, 1).toUpperCase() + tmpNm.substring(1).toLowerCase();
                entity = (PersistentEntity) Class.forName(entityNm).newInstance();
            } catch (final Exception e) {
                EafCommon.debug("findPOJO: try " + entityNm);
            }
        }

        // If entity POJO not found, try POJO in core.entities package
        if (entity == null) {
            try {
                entityNm = "eaf.core.entities." + module + "." + tmpNm;
                entity = (PersistentEntity) Class.forName(entityNm).newInstance();
            } catch (final Exception e) {
                EafCommon.debug("findPOJO: try " + entityNm);
            }
        }

        // Try POJO in core.entities package w/ all caps
        if (entity == null) {
            try {
                entityNm = "eaf.core.entities." + module + "." + tmpNm.toUpperCase();
                entity = (PersistentEntity) Class.forName(entityNm).newInstance();
            } catch (final Exception e) {
                EafCommon.debug("findPOJO: try " + entityNm);
            }
        }

        // Try POJO in core.entities package w/ capitalized
        if (entity == null) {
            try {
                entityNm = "eaf.core.entities." + module + "."
                        + tmpNm.substring(0, 1).toUpperCase() + tmpNm.substring(1).toLowerCase();
                entity = (PersistentEntity) Class.forName(entityNm).newInstance();
            } catch (final Exception e) {
                EafCommon.debug("findPOJO: try " + entityNm);
            }
        }

        if (entity == null) {
            EafCommon.debug("Can not instantiate " + entityNm);
        } else {
            EafCommon.debug("Instantiated " + entityNm);
        }

        return entity;

    }

    /**
     * getEntity - return an instantiated EAF POJO based on the entity path
     * parameter.
     *
     * @param incEntityPath
     *            qualified class name
     * @return Hibernate Object
     */
    public static Object getEntity(final String incEntityPath) {

        PersistentEntity entity = null;
        final String entityPath = "eaf.core.entities." + incEntityPath;
        EafCommon.debug("Find POJO " + entityPath);

        try {
            entity = (PersistentEntity) Class.forName(entityPath).newInstance();
        } catch (final ClassNotFoundException e) {
            EafCommon.debug("findPOJO: try " + entityPath);

        } catch (final Exception e) {
            EafCommon.debug("findPOJO: try " + entityPath);

        }

        if (entity == null) {
            EafCommon.debug("Can not instantiate " + entityPath);

        } else {
            EafCommon.debug("Instantiated " + entityPath);

        }

        return entity;

    }

    /**
     * Wrapper for getEntity() w/ globalVars argument.
     *
     * @param incEntityPath
     *            qualified class name
     * @return Hibernate Object
     */
    public static Object getEntity(final String incEntityPath, final GlobalVars gVars) {
        PersistentEntity entity = (PersistentEntity) getEntity(incEntityPath);

        entity.currentUserCACId = gVars.getPoc().getPocUsersLoginId();

        entity.setOwner(entity.currentUserCACId);

        entity.currentPOCAccessibleOrgs = new ArrayList<Integer>();

        gVars.getPoc().loadSet("orgAssocs");

        for (PocOrg o : gVars.getPoc().getOrgAssocs()) {

            entity.currentPOCAccessibleOrgs.add(o.getOrgId());
        }

        return entity;

    }

    /** APPS enumerated object. */
    private EafCommon.APPS       eafAPP           = EafCommon.APPS.PUBLIC;
    /** EAF application ID. */
    private int                  appID            = this.eafAPP.getId();
    /** archive bit. */
    protected Integer            archive;
    /** current datetime. */
    protected Date               datetime;

    // TODO Evaluate appID and its dependence on eafAPP already being defined.

    //
    // Set the application ID to EA, JMCMS, MAAD, IRMS, PUBLIC to query proper
    // DB schema
    // Default schema is set to PUBLIC
    //

    /** orig owning Sys. */
    protected String             origOwnSys;

    /** owner. */
    protected String             owner;

    /** transaction. */
    protected EafTransaction     xaction          = null;

    /** Current CAC of logged in user. */
    protected String             currentUserCACId;

    /** Current accessible Orgs for the currently logged in user. */
    protected ArrayList<Integer> currentPOCAccessibleOrgs;

    /** save transactions flag, default is False. */
    private Boolean              saveTransactions = Boolean.TRUE;
    private String viewUrl = "";

    /**
     * Eaf Entity default constructor.
     */
    public EafEntity() {
        this.owner = "ITAS";
        this.origOwnSys = "ITAS";
        this.datetime = new Date();
        this.archive = 0;

        String whichModule = this.getClass().getName().split("eaf.core.entities")[1]
                .split("\\.")[1];

        setApp(EafCommon.APPS.getApp(whichModule));

        setOrigOwnSys(whichModule.toUpperCase());
    }

    /**
     * Eaf Entity constructor w/ access to global vars POC.
     */
    public EafEntity(GlobalVars gVars) {

        // Get default values for common fields
        //
        this();

        currentUserCACId = gVars.getPoc().getPocUsersLoginId();

        owner = currentUserCACId;

        currentPOCAccessibleOrgs = new ArrayList<Integer>();

        gVars.getPoc().loadSet("orgAssocs");

        for (PocOrg o : gVars.getPoc().getOrgAssocs()) {

            currentPOCAccessibleOrgs.add(o.getOrgId());
        }

    }

    /**
     * Eaf Entity constructor w/ access to currently logged in POC.
     */

    public EafEntity(final Poc currPoc) {

        this();

        setCurrentUserCACId(currPoc.getPocUsersLoginId());

        currentPOCAccessibleOrgs = new ArrayList<Integer>();

        currPoc.loadSet("orgAssocs");

        for (PocOrg o : currPoc.getOrgAssocs()) {

            currentPOCAccessibleOrgs.add(o.getOrgId());
        }
    }

    /**
     * Initialization of CAC ID and accessible organization attributes of the
     * POC within whose session this entity was instantiated.
     */

    public void init(final GlobalVars globalVars) {

        setCurrentUserCACId(globalVars.getPoc().getPocUsersLoginId());

        currentPOCAccessibleOrgs = new ArrayList<Integer>();

        globalVars.getPoc().loadSet("orgAssocs");

        for (PocOrg o : globalVars.getPoc().getOrgAssocs()) {

            currentPOCAccessibleOrgs.add(o.getOrgId());
        }
    }

    /**
     * Return APPS enumeration object assigned for a data access object.
     *
     * @return eafCommon.APPS enumerated element
     *
     * @see EafCommon
     */

    public final EafCommon.APPS getApp() {
        return this.eafAPP;
    }

    /**
     * Return application ID for a data access object.
     *
     * @return int application ID
     *
     * @see EafCommon
     *
     */
    public final int getAppID() {
        return this.appID;
    }

    /**
     * getArchive.
     *
     * @return archive bit
     */
    public int getArchive() {
        return this.archive;
    }

    /**
     * getDateTime.
     *
     * @return datetime
     */
    public Date getDatetime() {
        return this.datetime;
    }

    /**
     * Abstract method - Must be defined to return a data access object/entity
     * id.
     *
     * @return int
     */

    public abstract Object getId();

    public String getThisLink() {
        return getName();
    }
    public String getViewUrl() {
        return viewUrl;
    }

    /**
     * Abstract method - Must be defined to return a data access object text
     * name.
     *
     * @return String
     */

    public abstract String getName();

    /**
     * getOrigOwnSys.
     *
     * @return incOwnSys String
     */

    public String getOrigOwnSys() {
        return this.origOwnSys;
    }

    /**
     * getOwner.
     *
     * @return Owner String
     */
    public String getOwner() {
        return this.owner;
    }

    public void setViewUrl(String incUrl){
        viewUrl = incUrl;
    }
    /**
     * getCurrentUserCACId The CAC id of the currently logged in POC in which
     * whose session this EAF POJO was instantiated.
     *
     * @return Owner String
     */
    public final String getCurrentUserCACId() {
        return this.currentUserCACId;
    }

    /**
     * setCurrentUserCACId().
     *
     * @return Owner String
     */
    public final void setCurrentUserCACId(final String incCACId) {
        this.currentUserCACId = incCACId;
    }

    /**
     * To ensure that a instantiated data access object has an "id" field set.
     *
     * @return Boolean - Check that getId() does not return null.
     *
     */
    public abstract Boolean hasId();

    /**
     * Print object member data to stdout or console.
     *
     */

    public final void printObject() {

        final org.hibernate.mapping.PersistentClass persistMap = EafCommon.getConfiguration(getAppID())
                .getClassMapping(this.getClass().getName());

        java.lang.reflect.Method getter;
        java.lang.reflect.Method setter;

        final Object[] args = new Object[1];

        for (java.lang.reflect.Field f : this.getClass().getDeclaredFields()) {

            try {

                if (persistMap.getProperty(f.getName()) == null) {
                    continue;
                }

                Object val;

                getter = this.getClass().getMethod(
                        "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1));

                val = getter.invoke(this);

                if (val != null) {
                    if (val.getClass().toString().indexOf("main.java.mil.navy") < 0) {
                    } else {
                        PersistentEntity bb = (PersistentEntity) Class.forName(val.getClass().getName()).newInstance();
                        bb = (PersistentEntity) val;
                    }
                }

            } catch (Exception e1) {
                // e1.printStackTrace();
            }
        }

        // Load default data members - archive, origOwningSys, datetime
        for (java.lang.reflect.Field f : this.getClass().getSuperclass().getSuperclass().getDeclaredFields()) {

            try {

                if (persistMap.getProperty(f.getName()) == null) {
                    continue;
                }

                Object val;

                getter = this.getClass().getMethod(
                        "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1));

                val = getter.invoke(this);

                if (val != null) {
                    if (val.getClass().toString().indexOf("main.java.mil.navy") < 0) {
                    } else {
                        PersistentEntity bb = (PersistentEntity) Class.forName(val.getClass().getName()).newInstance();
                        bb = (PersistentEntity) val;
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }

    /**
     * Return the value of the {@code saveTransactions} boolean flag.
     *
     * @return boolean
     */
    public final Boolean getSaveTransactions() {
        return saveTransactions;
    }

    /**
     * Set the APPS enumeration object to access the proper DB schema.
     *
     * @param app
     *            eafCommon.APPS enumerated value
     *
     * @see EafCommon
     *
     */
    public void setApp(final EafCommon.APPS app) {
        this.eafAPP = app;
        this.appID = eafAPP.getId();
    }

    /**
     * Set the APPS enumeration object by the incoming integer id to access the
     * proper DB schema.
     *
     * @param appId
     *            application id
     * @see EafCommon
     */
    public final void setApp(final int appId) {
        this.eafAPP = EafCommon.APPS.getApp(appId);
        this.appID = appId;
    }

    /**
     * Set APPS enumeration object by the incoming string.
     *
     * @param incNm
     *            application name {ea, jmcms, maad, irms}
     *
     * @see EafCommon
     */
    public final void setApp(final String incNm) {
        this.eafAPP = EafCommon.APPS.getApp(incNm);
        this.appID = eafAPP.getId();
    }

    /**
     * Set the data object's application ID to access the proper DB schema. An
     * application ID references the integer key 0-4 from APPS enumeration: EA,
     * JMCMS, MAAD, IRMS, PUBLIC. The default application id is set to Public.
     *
     * @param incId
     *            application id
     *
     * @see EafCommon
     *
     */
    public final void setAppID(final int incId) {
        this.eafAPP = EafCommon.APPS.getApp(incId);
    }

    /**
     * setArchive().
     *
     * @param incArchive
     *            archive bit
     */
    public void setArchive(final int incArchive) {
        this.archive = incArchive;
    }

    /**
     * setDateTime.
     *
     * @param incDatetime
     *            datetime
     */
    public void setDatetime(final Date incDatetime) {
        this.datetime = incDatetime;
    }

    /**
     * Abstract method - Must be defined to set a data object/entity id.
     *
     * @param incEntityId
     *            integer ID
     */
    public abstract void setId(Object incEntityId);

    /**
     * Abstract method - Must be defined to set a data object/entity text name.
     *
     * @param incName
     *            String
     */
    public abstract void setName(String incName);

    /**
     * setOrigOwnSys.
     *
     * @param ownsys
     *            orig owning sys
     */
    public void setOrigOwnSys(final String ownsys) {
        this.origOwnSys = ownsys;
    }

    /**
     * setOwner.
     *
     * @param incOwner
     *            owner
     */
    public void setOwner(final String incOwner) {
        this.owner = incOwner;
    }

    /**
     * getTransaction.
     *
     * @return EafTransaction
     */
    public final EafTransaction getTransaction() {
        if (this.xaction == null) {
            xaction = new EafTransaction();
        }
        return this.xaction;
    }

    /**
     * Set a flag so that entities (the extend eafEntity-->persistentEntity)
     * action (views/adds/updates/deletes) are added to TLOG.
     *
     * @param incSetTrans
     *            boolean
     */

    public final void setSaveTransactions(final Boolean incSetTrans) {
        this.saveTransactions = incSetTrans;
    }

    /**
     * Set saveTransactions flag to on.
     *
     * Flag can be checked after a successful Hibernate/DB transaction to
     * proceed saving EA transaction in TLOG.
     *
     */
    public final void xactionsOff() {
        this.saveTransactions = Boolean.FALSE;
    }

    /**
     * Set saveTransactions flag to off.
     *
     */
    public final void xactionsOn() {
        this.saveTransactions = Boolean.TRUE;
    }

    /*
     * Test for object being an EAF object
     */
    public static Boolean isEafDatatype(final String incDatatype) {

        if (incDatatype != null) {
            if (incDatatype.startsWith("main.java.mil.navy.cnmoc") || incDatatype.contains("main.java.mil.navy.cnmoc")) {

                return true;
            }
        }

        return false;
    }

    /*
     * Test for mapped tabled being an "assoc" table
     */
    public Boolean isAssociationTable() {

        final org.hibernate.mapping.PersistentClass persistMap = EafCommon.getConfiguration(getAppID())
                .getClassMapping(this.getClass().getName());

        if (persistMap.getTable().getName().contains("_assoc"))
            return true;

        return false;
    }
}
