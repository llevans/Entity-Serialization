package eaf.core.entities.pub;

// Generated Jul 25, 2010 1:46:12 AM by Hibernate Tools 3.2.4.GA

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import eaf.core.common.EafCommon;
import eaf.core.common.EafException;
import eaf.core.common.GlobalVars;
import eaf.core.common.utilityClasses.EaRequest;
import eaf.core.entities.EafEntity;
import eaf.core.entities.EntityDiffMap;
import eaf.core.entities.PersistentEntity;
import eaf.core.entities.EntityDiffMap.OPTIONS;

import org.hibernate.HibernateException;
import org.hibernate.property.Getter;

/**
 * EafTransaction - Java class mapped to TLOG table.
 *
 */
public class EafTransaction extends PersistentEntity implements java.io.Serializable {

    /** tlogActionTaken. */
    private String  tlogActionTaken;
    /** tlogDadmsId. */
    private String  tlogDadmsId;
    /** tlogDadmsUpdated. */
    private int     tlogDadmsUpdated;

    /** tlogDatetime. */
    private Date    tlogDatetime;
    /**
     * @see EntityDiffMap.java
     */
    private byte[]  tlogDiff;

    /** tlogEntityNm. */
    private String  tlogEntityNm;
    /** tlogEntityId. */
    private String  tlogEntityId;
    /** tlogParentEntityNm. */
    private String  tlogParentEntityNm;
    /** tlogParentEntityId. */
    private Integer tlogParentEntityId = 0;
    /** tlogId. */
    private int     tlogId;
    /** tlogOwner. */
    private String  tlogOwner;

    /** Enumeration of EAF transaction types. */
    public static enum actions {
        VIEW, ADD, UPDATE, DELETE, ARCHIVE
    };

    /**
     * Default constructor.
     */
    public EafTransaction() {
        this.setDatetime(new Date());
        this.setOwner("ITAS");
        this.setTlogOwner("ITAS");
        this.setTlogDatetime(new Date());
        this.setTlogDadmsUpdated(0);
        this.setArchive(0);
        this.setOrigOwnSys(this.getApp().getSchema().toUpperCase());
    }

    /**
     * Constructor to load object data members after object construction.
     *
     * @param incTlogId
     *            - id for retrieving record
     */
    public EafTransaction(final int incTlogId) {
        load(incTlogId);
    }

    /**
     * Constructor to set transaction Owner at object construction.
     *
     * @param gvars
     *            global variables
     */
    public EafTransaction(final GlobalVars gvars) {
        this();
        this.setTlogOwner(gvars.getPoc().getPocUsersLoginId());
    }

    /**
     * Constructor to set transaction Owner at object construction.
     *
     * @param poc
     *            current POC
     */
    public EafTransaction(final Poc poc) {
        this();
        this.setTlogOwner(poc.getPocUsersLoginId());
    }

    /**
     * EafTransaction constructor.
     *
     * To differentiate transactions made from within the different EAF modules,
     * OrigOwnSys should be set to a string from the EAFCommon.APPS enumeration.
     *
     * @param incTlogId
     *            Id
     * @param incAction
     *            from actions enumeration
     * @param incTlogEntityNm
     *            Entity name
     * @param incTlogEntityId
     *            Entity id
     * @param incTlogOwner
     *            Tlog owner
     * @param incTlogDatetime
     *            Datetime as YYYY/MM/dd HH:mm:ss
     * @param incTlogDadmsUpdated
     *            DADMS updated
     * @param incOrigOwnSys
     *            Primary EAF module name
     * @param incDatetime
     *            Datetime as YYYY/MM/dd HH:mm:ss
     */
    public EafTransaction(final int incTlogId, final actions incAction, final String incTlogEntityNm,
            final String incTlogEntityId, final String incTlogOwner, final Date incTlogDatetime,
            final int incTlogDadmsUpdated, final String incOrigOwnSys, final Date incDatetime) {
        this.tlogId = incTlogId;
        this.tlogActionTaken = incAction.name();
        this.tlogEntityNm = incTlogEntityNm;
        this.tlogEntityId = incTlogEntityId;
        this.tlogOwner = incTlogOwner;
        this.tlogDatetime = incTlogDatetime;
        this.tlogDadmsUpdated = incTlogDadmsUpdated;
        this.setOrigOwnSys(incOrigOwnSys);
    }

    /**
     * EafTransaction constructor.
     *
     * To differentiate transactions made from within the different EAF modules,
     * OrigOwnSys should be set to a string from the EAFCommon.APPS enumeration.
     *
     * @param incTlogId
     *            Id
     * @param incAction
     *            from actions enumeration
     * @param incTlogEntityNm
     *            Entity name
     * @param incTlogEntityId
     *            Entity id
     * @param incTlogOwner
     *            Tlog owner
     * @param incTlogDatetime
     *            Datetime as YYYY/MM/dd HH:mm:ss
     * @param incTlogDadmsUpdated
     *            DADMS updated
     * @param incTlogDadmsId
     *            DADMS id
     * @param incOrigOwnSys
     *            Primary EAF module name
     * @param incDatetime
     *            Datetime as YYYY/MM/dd HH:mm:ss
     */

    public EafTransaction(final int incTlogId, final actions incAction, final String incTlogEntityNm,
            final String incTlogEntityId, final String incTlogOwner, final Date incTlogDatetime,
            final int incTlogDadmsUpdated, final String incOrigOwnSys, final Date incDatetime,
            final String incTlogDadmsId) {
        this.tlogId = incTlogId;
        this.tlogActionTaken = incAction.name();
        this.tlogEntityNm = incTlogEntityNm;
        this.tlogEntityId = incTlogEntityId;
        this.tlogOwner = incTlogOwner;
        this.tlogDatetime = incTlogDatetime;
        this.tlogDadmsUpdated = incTlogDadmsUpdated;
        this.tlogDadmsId = incTlogDadmsId;
        this.setOrigOwnSys(incOrigOwnSys);
    }

    /**
     * Empty method body since id is a sequence.
     *
     * @return id
     */
    @Override
    public Integer getId() {
        return getTlogId();
    }

    /**
     * getMaxid.
     *
     * @return Maximum Tlog id
     */
    public final int getMaxId() {

        /**
         * Hibernate session to insert transaction into database.
         */
        org.hibernate.Session session = null;
        /**
         * Hibernate transaction.
         */
        org.hibernate.Transaction tx = null;

        Object hObj = new Object();

        try {

            //
            // Query table for maximum(id)
            //
            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();

            hObj = session.createQuery("select max(s.tlogId) from EafTransaction s").list().get(0);

            tx.commit();

        } catch (final RuntimeException e) {
            if ((tx != null) && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (final HibernateException e1) {
                    EafCommon.info("Error rolling back transaction");
                }
                throw e;
            }

        } finally {
            session.close();
        }

        return ((Integer) hObj).intValue();
    }

    /**
     * To ensure that a instantiated data access object has an "id" field set to
     * a value.
     *
     * @return Boolean - Check that getId() does not return null.
     *
     */
    @Override
    public Boolean hasId() {
        Boolean stat = Boolean.FALSE;

        try {
            if (this.getId() != 0) {
                stat = Boolean.TRUE;
            }
        } catch (final Exception e) {
            new EafException("", e);
        }

        return stat;
    }

    /**
     * Hibernate logic to retrieve a Tlog record.
     *
     * @param incId
     *            id
     */
    public final void load(final int incId) {

        /**
         * Hibernate session to insert transaction into database.
         */
        org.hibernate.Session session = null;
        /**
         * Hibernate transaction.
         */
        org.hibernate.Transaction tx = null;

        try {

            session = EafCommon.getSessionFactory(EafCommon.APPS.PUBLIC.getId()).openSession();

            tx = session.beginTransaction();

            final Object hObj = session.get(this.getClass(), incId);

            tx.commit();

            for (java.lang.reflect.Field f : hObj.getClass().getDeclaredFields()) {
                if (f.get(hObj) != null) {
                    f.set(this, f.get(hObj));
                }
            }

        } catch (final RuntimeException e) {
            if ((tx != null) && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (final HibernateException e1) {
                    EafCommon.info("Error rolling back transaction");
                }
                throw e;
            }
        } catch (final IllegalAccessException e) {
            new EafException("", e);

        } finally {
            session.close();
        }

    }

    /**
     * Hibernate logic to retrieve the latest Tlog record for a particular
     * Entity type and Id.
     *
     * @param incId
     *            id
     * @param entName
     *            String
     */
    public final void loadLatest(String entName, final int incId) {

        /**
         * Hibernate session to insert transaction into database.
         */
        org.hibernate.Session session = null;
        /**
         * Hibernate transaction.
         */
        org.hibernate.Transaction tx = null;

        try {

            session = EafCommon.getSessionFactory(EafCommon.APPS.PUBLIC.getId()).openSession();

            tx = session.beginTransaction();

            entName = entName.replace("eaf.core.entities.", "");

            final java.util.List<Object> tObj = session.createQuery(
                    "select s from EafTransaction s where " + "s.tlogEntityNm ilike '%" + entName
                            + "' and s.tlogEntityId = '" + incId + "' order by datetime desc limit 1 ").list();

            tx.commit();

            for (java.lang.reflect.Field f : tObj.get(0).getClass().getDeclaredFields()) {
                if (f.get(tObj.get(0)) != null) {
                    f.set(this, f.get(tObj.get(0)));
                }
            }

            String[] eafFields = { "archive", "owner", "datetime", "orgiOwnSys", "owner" };

            for (java.lang.reflect.Field f : tObj.get(0).getClass().getSuperclass().getDeclaredFields()) {
                if (java.util.Arrays.asList(eafFields).contains(f.getName())) {

                    java.lang.reflect.Method getter = tObj.get(0).getClass()
                            .getMethod("get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1));
                    Object val = getter.invoke(tObj.get(0));

                    if (val != null) {
                        java.lang.reflect.Method setter = tObj
                                .get(0)
                                .getClass()
                                .getMethod(
                                        "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1),
                                        getter.getReturnType());

                        setter.invoke(this, val);
                    }
                }
            }

        } catch (final RuntimeException e) {
            if ((tx != null) && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (final HibernateException e1) {
                    EafCommon.info("Error rolling back transaction");
                }
                throw e;
            }
        } catch (final IllegalAccessException e) {
            new EafException("", e);
        } catch (final Exception e) {
            new EafException("", e);
        } finally {
            session.close();
        }

    }

    /**
     * Hibernate logic to retrieve all Tlog records for a particular Entity type
     * and Id.
     *
     * @param incId
     *            id
     * @param entName
     *            String
     *
     * @return EafTransaction array
     */
    public static final EafTransaction[] loadAllForEntity(final String entName, final int incId) {

        /**
         * Hibernate session to insert transaction into database.
         */
        org.hibernate.Session session = null;
        /**
         * Hibernate transaction.
         */
        org.hibernate.Transaction tx = null;

        /*
         * Array of transactions
         */
        EafTransaction[] tArray = null;

        try {

            session = EafCommon.getSessionFactory(EafCommon.APPS.PUBLIC.getId()).openSession();

            tx = session.beginTransaction();

            final java.util.List<EafTransaction> tObj = session.createQuery(
                    "select s from EafTransaction s where (" + "s.tlogEntityNm like '%" + entName
                            + "' and s.tlogEntityId ='" + incId + "') or ( s.tlogParentEntityNm like '%" + entName
                            + "' and s.tlogParentEntityId = " + incId + ") order by datetime desc").list();

            tx.commit();

            if (tObj.size() > 0) {
                tArray = new EafTransaction[tObj.size()];
                for (int i = 0; i < tObj.size(); i++) {
                    tArray[i] = tObj.get(i);
                }
            }

        } catch (final Exception e) {
            if ((tx != null) && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (final HibernateException e1) {
                    EafCommon.info("Error rolling back transaction");
                }

            }

            new EafException("", e);

        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        return tArray;
    }

    /**
     * Commit transaction to the Tlog table in the public schema.
     *
     * @return new transaction id
     */
    public final int save() {

        /**
         * Hibernate session to insert transaction into database.
         */
        org.hibernate.Session session = null;
        /**
         * Hibernate transaction.
         */
        org.hibernate.Transaction tx = null;

        Integer id = new Integer(0);

        try {

            session = EafCommon.getSessionFactory(EafCommon.APPS.PUBLIC.getId()).openSession();

            tx = session.beginTransaction();

            id = (Integer) session.save(this);

            EafCommon.debug("trans # " + id.toString());

            tx.commit();

        } catch (final org.hibernate.JDBCException e) {
            try {
                EafCommon.debug(e.getSQLException().getNextException().toString());

            } catch (final Exception e1) {
                EafCommon.debug(e.getSQLException().toString());
            }
            new EafException("", e);

        } catch (final RuntimeException e) {
            if ((tx != null) && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (final HibernateException e1) {
                    EafCommon.info("Error rolling back transaction");
                }
                new EafException("", e);
            }
        } catch (final Exception e) {
            new EafException("", e);

        } finally {
            session.close();
        }

        return id.intValue();

    }

    /**
     * Override the PersistentEntity add function so that the transacion doesn't record itself in Tlog
     */
    
    public final Object add() { return save(); }
    
    /**
     * BuildDiffArray(). Hide complexity of initiating an EntityDiffMap for
     * UPDATE transactions
     *
     */

    public void buildDiffArray(Object ent0, Object ent1) {

        final EntityDiffMap dmap = new EntityDiffMap(ent0, ent1);
        setTlogDiff(dmap.getByteArray());

        dmap.printDiff();

    }

    /**
     * Empty method body since id is a sequence.
     *
     * @param incId
     *            id
     */
    @Override
    public void setId(final Object incId) {
        setTlogId(((Integer) incId).intValue());
    }

    /**
     * SetTlogActionTaken.
     *
     * @param incAction
     *            string
     */
    public void setTlogActionTaken(final String incAction) {
        this.tlogActionTaken = incAction;
    }

    /**
     * SetTlogAction.
     *
     * @param incAction
     *            from actions enumeration
     */
    public void setTlogAction(final actions incAction) {
        this.tlogActionTaken = incAction.name();
    }

    /**
     * SetAction.
     *
     * @param incAction
     *            from actions enumeration
     */
    public void setAction(final actions incAction) {
        this.tlogActionTaken = incAction.name();
    }

    /**
     * setTlogDadmsId.
     *
     * @param incTlogDadmsId
     *            DADMS id
     */
    public void setTlogDadmsId(final String incTlogDadmsId) {
        this.tlogDadmsId = incTlogDadmsId;
    }

    /**
     * setTlogDadmsUpdated.
     *
     * @param incTlogDadmsUpdated
     *            DADMS updated
     */
    public void setTlogDadmsUpdated(final int incTlogDadmsUpdated) {
        this.tlogDadmsUpdated = incTlogDadmsUpdated;
    }

    /**
     * setTlogDatetime.
     *
     * @param incTlogDatetime
     *            Datetime as YYYY/MM/DD HH:mm:ss
     */
    public void setTlogDatetime(final Date incTlogDatetime) {
        this.tlogDatetime = incTlogDatetime;
    }

    /**
     * setTlogDiff.
     *
     * @param df
     *            byte array from EntityDiffMap
     */
    public void setTlogDiff(final byte[] df) {
        this.tlogDiff = df;
    }

    /**
     * setTlogEntityNm.
     *
     * @param incTlogEntityNm
     *            Entity name
     */
    public void setTlogEntityNm(final String incTlogEntityNm) {
        this.tlogEntityNm = incTlogEntityNm;
    }

    /**
     * setEntityNm.
     *
     * @param incTlogEntityNm
     *            Entity name
     */
    public void setEntityNm(final String incTlogEntityNm) {
        this.tlogEntityNm = incTlogEntityNm;
    }

    /**
     * setTlogEntityId.
     *
     * @param incTlogEntityId
     *            Entity id
     */
    public void setTlogEntityId(final String incTlogEntityId) {
        this.tlogEntityId = incTlogEntityId;
    }

    /**
     * setEntityId.
     *
     * @param incTlogEntityId
     *            Entity id
     */
    public void setEntityId(final String incTlogEntityId) {
        this.tlogEntityId = incTlogEntityId;
    }

    /**
     * setTlogId.
     *
     * @param incId
     *            Id
     * */
    public void setTlogId(final int incId) {
        this.tlogId = incId;
    }

    /**
     * setTlogOwner.
     *
     * @param incTlogOwner
     *            EAF POC full name
     */
    public void setTlogOwner(final String incTlogOwner) {
        this.tlogOwner = incTlogOwner;
    }

    /**
     * setTlogOwner.
     *
     * @param incPoc
     *            EAF POC object
     */
    public void setTlogOwner(final Poc incPoc) {
        this.tlogOwner = incPoc.getPocFullNm();
    }

    /**
     * String value from tlogAction enumeration.
     *
     * @return action taken.
     */

    public String getTlogActionTaken() {
        return this.tlogActionTaken;
    }

    /**
     * Entry from tlogAction enumeration.
     *
     * @return action taken.
     */

    public final actions getAction() {
        return actions.valueOf(actions.class, this.tlogActionTaken);
    }

    /**
     * Entry from tlogAction enumeration.
     *
     * @return action taken.
     */

    public final actions getTlogAction() {
        return actions.valueOf(actions.class, this.tlogActionTaken);
    }

    /**
     * getTLogDadmsId().
     *
     * @return id
     */
    public String getTlogDadmsId() {
        return this.tlogDadmsId;
    }

    /**
     * getTLogDadmsupdate().
     *
     * @return updated
     */
    public int getTlogDadmsUpdated() {
        return this.tlogDadmsUpdated;
    }

    /**
     * getTLogDatetime().
     *
     * @return datetime
     */
    public Date getTlogDatetime() {
        return this.tlogDatetime;
    }

    /**
     * getTLogDiff().
     *
     * @return diff byte array
     */
    public byte[] getTlogDiff() {
        return this.tlogDiff;
    }

    /**
     * getTLogEntityNm().
     *
     * @return entity name
     */
    public String getTlogEntityNm() {
        return this.tlogEntityNm;
    }

    /**
     * getEntityNm().
     *
     * @return entity name
     */
    public String getEntityNm() {
        return this.tlogEntityNm;
    }

    /**
     * getTLogEntityId().
     *
     * @return id
     */
    public String getTlogEntityId() {
        return this.tlogEntityId;
    }

    /**
     * getEntityId().
     *
     * @return id
     */
    public String getEntityId() {
        return this.tlogEntityId;
    }

    /**
     * getTLogParentEntityNm().
     *
     * @return parent entity name
     */
    public String getTlogParentEntityNm() {
        return this.tlogParentEntityNm;
    }

    /**
     * getParentEntityNm().
     *
     * @return parent entity name
     */
    public String getParentEntityNm() {
        return this.tlogParentEntityNm;
    }

    /**
     * setTLogParentEntityNm().
     *
     * @param incEntity
     */
    public void setTlogParentEntityNm(final String incParentEntNm) {
        this.tlogParentEntityNm = incParentEntNm;
    }

    /**
     * setTLogParentEntityNm().
     *
     * @param incEntity
     */
    public void setTlogParentEntityNm(final Class incClass) {
        this.tlogParentEntityNm = incClass.getName();
    }

    /**
     * setParentEntityNm().
     *
     * @param incEntity
     */
    public void setParentEntityNm(final String incParentEntNm) {
        this.tlogParentEntityNm = incParentEntNm;
    }

    /**
     * getTLogParentEntityId().
     *
     * @return parent entity id
     */
    public Integer getTlogParentEntityId() {
        return this.tlogParentEntityId;
    }

    /**
     * setTLogParentEntityId().
     *
     * @param incParentEntId
     */
    public void setTlogParentEntityId(final Integer incParentEntId) {
        this.tlogParentEntityId = incParentEntId;
    }

    /**
     * getParentEntityId().
     *
     * @return parent entity id
     */
    public Integer getParentEntityId() {
        return this.tlogParentEntityId;
    }

    /**
     * setParentEntityId().
     *
     * @param incParentEntId
     */
    public void setParentEntityId(final Integer incParentEntId) {
        this.tlogParentEntityId = incParentEntId;
    }

    /**
     * getTLogId().
     *
     * @return id
     */
    public int getTlogId() {
        return this.tlogId;
    }

    /**
     * getTLogOwner().
     *
     * @return EAF POC full name
     */
    public String getTlogOwner() {
        return this.tlogOwner;
    }

    @Override
    public String getName() {
        // n/a
        return "";
    }

    @Override
    public void setName(final String incName) {
        // n/a
    }

    /**
     * printDiffTable() - wrapper to display full object difference HTML table,
     *
     * Rebuild 2 objects from the transaction diff byte array.
     *
     * Call printDiffTable(object1, object2) to render get full object
     * information/changes.
     *
     * Return HTML table with previous/changed data values side-by-side to view
     * entity history. Data values are acquired from the TLOG difference byte
     * array to reconstruct 2 POJOs.
     *
     * @param gvars
     *            - globalVars
     * @param request
     *            - EafRequest
     * @param options
     *            - opts
     *
     * @return HTML string
     */
    public final String printDiffTable(final GlobalVars gvars, final EaRequest request, final OPTIONS... options) {

        //
        // Reconstruct EAF objects
        //

        String formName;

        if (tlogEntityNm.contains("main.java.mil.navy.cnmoc"))
            formName = tlogEntityNm.substring(tlogEntityNm.indexOf("entities.") + 9);
        else
            formName = tlogEntityNm;

        final String[] groups = { "all" };

        PersistentEntity prevObj = null;
        PersistentEntity chngObj = null;

        EntityDiffMap dmap = null;

        Class<?> paramType;

        try {
            prevObj = (PersistentEntity) Class.forName("eaf.core.entities." + formName)
                    .newInstance();
            chngObj = (PersistentEntity) Class.forName("eaf.core.entities." + formName)
                    .newInstance();

            prevObj.load(tlogEntityId);
            chngObj.load(tlogEntityId);

            dmap = new EntityDiffMap(new ByteArrayInputStream(tlogDiff));

        } catch (Exception e) {
            new EafException("", e);

        }

        for (int i = 0; i < dmap.getDataFields().size(); i++) {

            String fieldNm = (String) dmap.getDataFields().get(i);

            try {
                paramType = prevObj.getClass()
                        .getMethod("get" + fieldNm.substring(0, 1).toUpperCase() + fieldNm.substring(1))
                        .getReturnType();

                Method pSet = prevObj.getClass().getMethod(
                        "set" + fieldNm.substring(0, 1).toUpperCase() + fieldNm.substring(1), paramType);
                Method cSet = chngObj.getClass().getMethod(
                        "set" + fieldNm.substring(0, 1).toUpperCase() + fieldNm.substring(1), paramType);

                Object[] args = new Object[1];

                //
                // Invoke setter method to populate temporary objects
                //

                if (!EafEntity.isEafDatatype(paramType.getName())) {
                    args[0] = dmap.getPrevValues().get(i);
                    pSet.invoke(prevObj, args);

                    args[0] = dmap.getChngValues().get(i);
                    cSet.invoke(chngObj, args);

                } else {
                    //
                    // An associated object (many-to-one) was changed.
                    // Instantiate the associated object using the key
                    // stored in the differences byte array.
                    //

                    try {
                        PersistentEntity pObj = (PersistentEntity) Class.forName(paramType.getName()).newInstance();

                        pObj.load(dmap.getPrevValues().get(i));

                        args[0] = pObj;
                        pSet.invoke(prevObj, args);

                        PersistentEntity cObj = (PersistentEntity) Class.forName(paramType.getName()).newInstance();

                        cObj.load(dmap.getChngValues().get(i));

                        args[0] = cObj;
                        pSet.invoke(chngObj, args);

                    } catch (Exception e) {
                        EafCommon.debug("Could not instantiate " + paramType.getName() + " for diff table");
                    }
                }
            } catch (Exception e) {
                new EafException("", e);
            }
        }

        if (prevObj != null && chngObj != null) {

            return EntityDiffMap.printDiffTable(prevObj, chngObj, formName, gvars, request, options);

        } else {

            return "";

        }

    }

}
