// TODO LLE add global var that can be changed per user (acl_users?) to allow
// for ORG & Directorate slicing
package eaf.core.entities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import eaf.core.common.EafCommon;
import eaf.core.common.EafException;
import eaf.core.common.GlobalVars;
import eaf.core.entities.pub.EafTransaction;
import eaf.core.entities.pub.Poc;
import eaf.core.entities.pub.ViewTransaction;

import org.hibernate.HibernateException;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;

import eaf.core.EafTest;

/**
 * Parent class providing ORM/Hibernate functionality.
 * 
 * 
 */
public abstract class PersistentEntity extends EafEntity implements Comparable {

    public static final int CLASS_OFFSET = 8;

    /**
     * 
     * Return the max value of the integer primary key of the database table
     * mapped to the incoming POJO class name.
     * 
     * <br> {@code Example usage: } <br> {@code int maxId = poc
     *  .getMaxId("eaf.core.entities.pub.Poc"); }
     * 
     * @param className
     *            - EAF class
     * @return integer - max id
     */

    public static final int getMaxId(final String className) {

        org.hibernate.Transaction tx = null;
        org.hibernate.Session session = null;

        int maxId = 0;

        String schemaNm, primaryKey, entityNm;

        try {

            if (className.split("\\.").length > PersistentEntity.CLASS_OFFSET) {
                schemaNm = className.split("\\.")[PersistentEntity.CLASS_OFFSET];
            } else {
                schemaNm = "pub";
            }

            final int schemaId = EafCommon.APPS.getApp(schemaNm).getId();

            //
            // Get meta-data for SQL select string
            //
            primaryKey = EafCommon.getSessionFactory(schemaId).getClassMetadata(className).getIdentifierPropertyName();

            entityNm = className.split("\\.")[className.split("\\.").length - 1];

            //
            // Query table for maximum(id)
            //
            session = EafCommon.getSessionFactory(schemaId).openSession();

            tx = session.beginTransaction();

            final java.util.List<Object> oList = session.createQuery(
                    "select max(s." + primaryKey + ") from " + entityNm + " s").list();

            tx.commit();

            if (oList.get(0) == null) {
                maxId = 0;
            } else {
                maxId = ((Integer) oList.get(0)).intValue();
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

        return maxId;
    }

    /**
     * Entity name.
     */
    private String            entityNm   = null;

    /**
     * Hibernate object returned from HQL query.
     */
    private Object            hObj;
    /**
     * isValid.
     */
    private Boolean           isValid    = true;

    /**
     * Primary key column name of mapped database table.
     */
    private String            primaryKey = null;                    ;

    /**
     * Validation messages.
     */
    private ArrayList<String> validMsgs  = new ArrayList<String>();

    /**
     * Hibernate database session.
     */
    // private org.hibernate.Session session = null;

    /**
     * Hibernate database transaction.
     */
    // private org.hibernate.Transaction tx = null;

    /**
     * default constructor.
     */
    public PersistentEntity() {
        super();
    }

    /**
     * Constructor w/ APP ID.
     * 
     * @param app
     *            - APPS enumerated object
     */
    public PersistentEntity(final EafCommon.APPS app) {
        this.setApp(app);
    }

    /**
     * Entity constructor w/ access to global variables
     */
    public PersistentEntity(final GlobalVars gVars) {
        super(gVars);
    }

    /**
     * Entity constructor w/ access to currently logged in POC
     */
    public PersistentEntity(final Poc currPoc) {
        super(currPoc);
    }

    /**
     * 
     * Perform a database insert. If saveTransactions flag is true, TLOG is
     * updated. <br> {@code Example usage: } <br> {@code Poc poc = new Poc(); } <br>
     * {@code poc.setPocFnm("John"); } <br> {@code poc.setPocLnm("Doe"); } <br>
     * {@code int poc_id = poc.add(); } <br>
     * 
     * @return - int - primary key of inserted database record
     * 
     */

    public Object add() {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        Object id = new Integer(-1);
        final Object[] args = new Object[1];

        // Hibernate Identifier generation types:
        //
        // [uuid] -> [class org.hibernate.id.UUIDHexGenerator]
        // [hilo] -> [class org.hibernate.id.TableHiLoGenerator]
        // [assigned] -> [class org.hibernate.id.Assigned]
        // [identity] -> [class org.hibernate.id.IdentityGenerator]
        // [select] -> [class org.hibernate.id.SelectGenerator]
        // [sequence] -> [class org.hibernate.id.SequenceGenerator]
        // [seqhilo] -> [class org.hibernate.id.SequenceHiLoGenerator]
        // [increment] -> [class org.hibernate.id.IncrementGenerator]
        // [foreign] -> [class org.hibernate.id.ForeignGenerator]
        // [guid] -> [class org.hibernate.id.GUIDGenerator]
        // [uuid.hex] -> [class org.hibernate.id.UUIDHexGenerator]
        // [sequence-identity] ->
        // [class org.hibernate.id.SequenceIdentityGenerator]
        // [enhanced-sequence] ->
        // [class org.hibernate.id.enhanced.SequenceStyleGenerator]
        // [enhanced-table] ->
        // [class org.hibernate.id.enhanced.TableGenerator]

        final SimpleValue identifier = (SimpleValue) EafCommon.getConfiguration(getAppID())
                .getClassMapping(this.getClass().getName()).getIdentifierProperty().getValue();

        try {

            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();

            // When the identifier is "assigned", this
            // method will assign the primary id value with
            // calculate max(id) + 1
            if (identifier.getIdentifierGeneratorStrategy().equals("assigned")) {

                if (!this.hasId()) {

                    //
                    // Get meta-data for SQL select string
                    //
                    primaryKey = EafCommon.getSessionFactory(getAppID()).getClassMetadata(this.getClass().getName())
                            .getIdentifierPropertyName();

                    entityNm = this.getClass().getName().split("\\.")[this.getClass().getName().split("\\.").length - 1];

                    //
                    // Query table for maximum(id)
                    //

                    final List oList = session.createQuery("select max(s." + primaryKey + ") from " + entityNm + " s")
                            .list();

                    if (oList.get(0) != null) {
                        args[0] = ((Integer) oList.get(0)) + 1;
                    } else {
                        args[0] = 1;
                    }

                    this.getClass().getMethod("setId", Object.class).invoke(this, args);

                    EafTest.printObject(this);
                }
            }

            id = session.save(this);

            tx.commit();

        } catch (final org.hibernate.JDBCException e) {

            new EafException("", e);

        } catch (final RuntimeException e) {
            if ((tx != null) && tx.isActive()) {
                try {
                    tx.rollback();
                    EafCommon.info("roll back");
                } catch (final HibernateException e1) {
                    EafCommon.info("Error rolling back transaction");
                }

            }
            new EafException("", e);
        } catch (final Exception e) {
            new EafException("", e);

        } finally {
            if (session.isOpen()) {
                session.close();
            }

        }

        if (tx.wasCommitted() && this.getSaveTransactions() && identifier.getType().getName().equals("integer")) {

            // final EafTransaction xaction = new EafTransaction();
            if (xaction == null) {
                xaction = new EafTransaction();
            }

            xaction.setTlogOwner(this.getOwner());

            if (xaction.getTlogActionTaken() == null) {
                xaction.setTlogAction(EafTransaction.actions.ADD);
            }
            xaction.setTlogEntityNm(this.getClass().getName()
                    .replace("eaf.core.entities.", ""));
            xaction.setOrigOwnSys(this.getApp().getSchema().toUpperCase());

            try {
                xaction.setTlogEntityId(id.toString());

            } catch (final Exception e) {
                new EafException("", e);
            }

            if (isAssociationTable()) {

                final org.hibernate.mapping.PersistentClass persistMap = EafCommon.getConfiguration(getAppID())
                        .getClassMapping(this.getClass().getName());

                for (final java.util.Iterator i = persistMap.getPropertyIterator(); i.hasNext();) {
                    final Property prop = (Property) i.next();
                    try {

                        if ((prop.getType().getClass().equals(org.hibernate.type.ManyToOneType.class)
                                && (xaction.getParentEntityNm() != null) && !prop.getType().toString()
                                .contains(xaction.getParentEntityNm()))
                                || (prop.getType().getClass().equals(org.hibernate.type.ManyToOneType.class)
                                        && (xaction.getParentEntityNm() != null)
                                        && prop.getType().toString().contains(xaction.getParentEntityNm()) && !prop
                                        .getName().toLowerCase().contains("ord"))) {

                            final EntityDiffMap dmap = new EntityDiffMap();

                            dmap.put("dataFields", prop.getName());
                            dmap.put("dataTypes", prop.getType().getReturnedClass().toString().split(" ")[1]);
                            dmap.put("previous", 0);
                            dmap.put(
                                    "changed",
                                    this.getClass()
                                            .getMethod(
                                                    "get" + prop.getName().substring(0, 1).toUpperCase()
                                                            + prop.getName().substring(1))
                                            .invoke(this, new Object[] {}));

                            xaction.setTlogDiff(dmap.pack().toByteArray());
                        }
                    } catch (final Exception e) {
                        new EafException("", e);
                    }

                }
            }

            xaction.save();

            xaction = null;

        }

        final Object new_id = id;

        // TODO Uncomment clear() for staging release.
        // clear();

        return new_id;

    }

    /**
     * 
     * Archive a single database record. If saveTransactions flag is true, TLOG
     * is updated. <br> {@code Example usage: } <br> {@code Poc poc = new Poc(); } <br>
     * {@code poc.load(2912); } <br> {@code poc.archive(); }
     * 
     */

    public final void archive() {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        try {

            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();

            this.setArchive(1);

            session.update(this);

            tx.commit();

        } catch (final org.hibernate.JDBCException e) {

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
            if (session.isOpen()) {
                session.close();
            }
        }

        if (tx.wasCommitted() && this.getSaveTransactions()) { // delete object

            // final EafTransaction xaction = new EafTransaction();
            if (xaction == null) {
                xaction = new EafTransaction();
            }

            xaction.setTlogOwner(this.getOwner());

            if (xaction.getTlogActionTaken() == null) {
                xaction.setTlogAction(EafTransaction.actions.UPDATE);
            }
            xaction.setTlogEntityNm(this.getClass().getName()
                    .replace("eaf.core.entities.", ""));
            xaction.setOrigOwnSys(this.getApp().getSchema().toUpperCase());

            xaction.buildDiffArray(hObj, this);

            try {
                xaction.setTlogEntityId(this.getClass().getMethod("getId").invoke(this).toString());
            } catch (final Exception e) {
                new EafException("", e);
            }
            xaction.save();

            xaction = null;

        }

        // TODO Uncomment clear() for staging release.
        // clear();

    }

    /**
     * This method clears a POJO to prevent the potential risk of database
     * corruption after the intended work has been completed.
     * 
     * To secure the application, persistent objects will be made unusable once
     * the task in which the object was required is finished.
     * 
     */

    public void clear() {

        //
        // Clear out the POJO Id.
        //

        setId(0);

        final org.hibernate.mapping.PersistentClass persistMap = EafCommon.getConfiguration(getAppID())
                .getClassMapping(this.getClass().getName());

        java.lang.reflect.Method setter;

        final Object[] args = new Object[1];

        for (final java.lang.reflect.Field f : this.getClass().getDeclaredFields()) {

            try {

                // While looping through ALL declared data
                // members, should only affect fields obtainable from
                // the database
                // Check persistent mapping *hbm.xml to verify data
                // member is a database field
                if (persistMap.getProperty(f.getName()) == null) {
                    continue;
                }

                setter = this.getClass().getMethod(
                        "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), f.getType());

                args[0] = null;

                setter.invoke(this, args);

            } catch (final org.hibernate.MappingException e) {
                EafCommon.debug("no load " + f.getName() + " not in db map.");

            } catch (final Exception e) {
                new EafException("", e);
            }
        }

        //
        // Clear default data members - archive, origOwningSys, datetime
        //
        for (final java.lang.reflect.Field f : this.getClass().getSuperclass().getSuperclass().getDeclaredFields()) {
            if (!f.getType().getName().startsWith("main.java.mil.navy.cnmoc") && !Modifier.isFinal(f.getModifiers())) {

                try {

                    if (f.getName().equalsIgnoreCase("archive")) {
                        setter = this.getClass()
                                .getMethod(
                                        "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1),
                                        int.class);

                    } else {
                        setter = this.getClass().getMethod(
                                "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1),
                                f.getType());

                    }

                    args[0] = null;

                    setter.invoke(this, args);

                } catch (final Exception e) {
                    // new EafException("", e);
                }
            }
        }

        //
        // Clear out the internal h_obj object and the POJO Id.
        //
        hObj = null;
    }

    @Override
    public int compareTo(Object anotherEntity) throws ClassCastException {
        if (!(anotherEntity instanceof PersistentEntity)) {
            throw new ClassCastException("A Persisten Entity object expected.");
        }
        final Object otherId = ((PersistentEntity) anotherEntity).getId();
        return (Integer) this.getId() - (Integer) otherId;
    }

    /**
     * 
     * Delete a single database record. If saveTransactions flag is true, TLOG
     * is updated. <br> {@code Example usage: } <br> {@code Poc poc = new Poc(); } <br>
     * {@code poc.load(2912); } <br> {@code poc.delete(); }
     * 
     */

    public final void delete() {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        try {

            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();

            session.delete(this);

            tx.commit();

        } catch (final org.hibernate.JDBCException e) {
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
            if (session.isOpen()) {
                session.close();
            }
        }

        if (tx.wasCommitted() && this.getSaveTransactions()) { // delete object

            // final EafTransaction xaction = new EafTransaction();
            if (xaction == null) {
                xaction = new EafTransaction();
            }

            xaction.setTlogOwner(this.getOwner());

            if (xaction.getTlogActionTaken() == null) {
                xaction.setTlogAction(EafTransaction.actions.DELETE);
            }
            xaction.setTlogEntityNm(this.getClass().getName()
                    .replace("eaf.core.entities.", ""));
            xaction.setOrigOwnSys(this.getApp().getSchema().toUpperCase());

            try {
                xaction.setTlogEntityId(this.getClass().getMethod("getId").invoke(this).toString());
            } catch (final Exception e) {
                new EafException("", e);
            }

            if (isAssociationTable()) {
                final org.hibernate.mapping.PersistentClass persistMap = EafCommon.getConfiguration(getAppID())
                        .getClassMapping(this.getClass().getName());

                for (final java.util.Iterator i = persistMap.getPropertyIterator(); i.hasNext();) {
                    final Property prop = (Property) i.next();

                    try {

                        if ((prop.getType().getClass().equals(org.hibernate.type.ManyToOneType.class) && !prop
                                .getType().toString().contains(xaction.getParentEntityNm()))
                                || (prop.getType().getClass().equals(org.hibernate.type.ManyToOneType.class)
                                        && prop.getType().toString().contains(xaction.getParentEntityNm()) && !prop
                                        .getName().toLowerCase().contains("ord"))) {

                            final EntityDiffMap dmap = new EntityDiffMap();

                            dmap.put("dataFields", prop.getName());
                            dmap.put("dataTypes", prop.getType().getReturnedClass().toString().split(" ")[1]);
                            dmap.put("changed", 0);
                            dmap.put(
                                    "previous",
                                    this.getClass()
                                            .getMethod(
                                                    "get" + prop.getName().substring(0, 1).toUpperCase()
                                                            + prop.getName().substring(1))
                                            .invoke(this, new Object[] {}));

                            xaction.setTlogDiff(dmap.pack().toByteArray());
                        }
                    } catch (final Exception e) {
                        new EafException("", e);
                    }

                }
            }

            xaction.save();

            xaction = null;

        }
        // TODO Uncomment clear() for staging release.
        // clear();

    }

    /**
     * Get last transaction for this entity.
     * 
     * @return EafTransaction
     */
    public final EafTransaction getLastTransaction() {

        EafTransaction result = null;

        if (hasId()) {
            final EafTransaction latest = new EafTransaction();
            latest.loadLatest(this.getClass().getName(), ((Integer) getId()).intValue());

            if (latest.hasId()) {
                result = latest;
            }
        }

        return result;
    }

    /**
     * 
     * Return max value of a the mapped database table's integer primary key.
     * 
     * <br> {@code Example usage: } <br> {@code Poc poc = new Poc(); }<br>
     * {@code int maxId = poc.getMaxId(); }
     * 
     * @return int - max id
     */

    public int getMaxId() {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        java.util.List<Object> oList = null;

        try {

            //
            // Get meta-data for SQL select string
            //
            primaryKey = EafCommon.getSessionFactory(getAppID()).getClassMetadata(this.getClass())
                    .getIdentifierPropertyName();

            entityNm = this.getClass().getName().split("\\.")[this.getClass().getName().split("\\.").length - 1];

            //
            // Query table for maximum(id)
            //
            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();

            oList = session.createQuery("select max(s." + primaryKey + ") from " + entityNm + " s").list();

            tx.commit();

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

        if (oList.get(0) == null) {
            return 0;
        } else {
            return ((Integer) oList.get(0)).intValue();
        }
    }

    /**
     * Get transactions for this entity.
     * 
     * @return EafTransaction[] array
     */
    public final EafTransaction[] getTransactions() {

        if (hasId()) {
            final EafTransaction[] latest = EafTransaction.loadAllForEntity(this.getClass().getName(),
                    ((Integer) this.getId()).intValue());
            return latest;

        } else {
            return null;
        }
    }

    /**
     * Validation messages array.
     * 
     * @return - string array
     */
    public final ArrayList<String> getValidMsgs() {
        return validMsgs;
    }

    /**
     * This method tests to see if the abstract method "getId()" has a concrete
     * definition and that the object's identity data member is not null.
     * 
     * {@code Example usage: } <br> {@code    POC poc = new POC(); } <br>
     * {@code    poc.setId(2912); } <br> {@code    Boolean b = poc.hasId(); } <br>
     * {@code   //b set to True } <br>
     * 
     * @return boolean
     * 
     */

    @Override
    public Boolean hasId() {

        Boolean flag = Boolean.FALSE;

        try {
            if (EafCommon.getSessionFactory(getAppID()).getClassMetadata(this.getClass()).getIdentifierType().getName()
                    .equals("integer")) {
                if ((this.getClass().getMethod("getId").invoke(this) != null)
                        && (((Integer) (this.getClass().getMethod("getId").invoke(this))).intValue() > 0)) {
                    flag = Boolean.TRUE;
                }

            } else if (EafCommon.getSessionFactory(getAppID()).getClassMetadata(this.getClass()).getIdentifierType()
                    .getName().equals("string")) {

                if ((this.getClass().getMethod("getId").invoke(this) != null)
                        && !((String) (this.getClass().getMethod("getId").invoke(this))).equals("")) {
                    flag = Boolean.TRUE;
                }

            } else if (this.getClass().getMethod("getId").invoke(this) != null) {
                flag = Boolean.TRUE;
            }

        } catch (final Exception e) {
            EafCommon.debug("Generic \"getId()\" method can not be invoked");
            EafCommon.debug("ensure method is defined.");
        }

        return flag;
    }

    /**
     * Validation status.
     * 
     * @return - Boolean
     */

    public final Boolean isValid() {
        return isValid;
    }

    /**
     * Load a POJO from a database table, requires that the data object's id has
     * been set. <br>
     * 
     * {@code Example usage: } <br> {@code   POC poc = new POC(); } <br>
     * {@code    poc.setId(2912); } <br> {@code    poc.load(); } <br>
     */

    public final void load() {

        try {
            if (this.hasId()) {
                load(this.getClass().getMethod("getId").invoke(this));
            }

        } catch (final Exception e) {
            new EafException("", e);
        }
    }

    /**
     * This method loads a POJO from a mapped database table where the primary
     * key equals the incoming id. <br>
     * If saveTransactions flag is true, TLOG in updated with a "VIEW" entry.
     * 
     * @param incId
     *            Object - {@code String | Integer } - load POJO from database
     *            table where primary key of data record is the incoming id <br>
     * 
     *            {@code Example usage: } <br> {@code    POC poc = new POC(); } <br>
     *            {@code    poc.load(2912); } <br>
     * 
     *            {@code    Usstate state = new USstate(); } <br>
     *            {@code    state.load("MS"); } <br>
     */

    public void load(final Object incId) {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        Object id = incId;

        try {

            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();
            if (EafCommon.getSessionFactory(getAppID()).getClassMetadata(this.getClass()).getIdentifierType().getName()
                    .equals("integer")) {
                if (id.getClass().getName().equals("java.lang.String")) {
                    id = Integer.parseInt(id.toString());
                }
                hObj = session.get(this.getClass(), (Integer) id);

            } else if (EafCommon.getSessionFactory(getAppID()).getClassMetadata(this.getClass()).getIdentifierType()
                    .getName().equals("string")) {
                hObj = session.get(this.getClass(), id.toString());

            } else {

                final java.io.Serializable cid = (java.io.Serializable) Class.forName(incId.getClass().getName()).cast(
                        incId);

                hObj = session.get(this.getClass().getName(), cid);

            }

            tx.commit();

            /*
             * for (java.lang.reflect.Field f :
             * hObj.getClass().getDeclaredFields()) if (f.get(hObj) != null)
             * f.set(this, f.get(hObj));
             */

            final org.hibernate.mapping.PersistentClass persistMap = EafCommon.getConfiguration(getAppID())
                    .getClassMapping(this.getClass().getName());

            java.lang.reflect.Method getter;
            java.lang.reflect.Method setter;

            final Object[] args = new Object[1];

            for (final java.lang.reflect.Field f : hObj.getClass().getDeclaredFields()) {

                try {

                    // While looping through ALL declared data
                    // members, should only load fields obtainable from
                    // the database -- excluding Sets or Collections
                    // representing EAF Associations
                    // Check persistent mapping *hbm.xml to verify data
                    // member is a database field and not a collection
                    if (persistMap.getProperty(f.getName()) == null) {
                        continue;
                    } else if (persistMap.getProperty(f.getName()).getType().isCollectionType()) {
                        continue;
                    }

                    getter = this.getClass().getMethod(
                            "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1));

                    setter = this.getClass().getMethod(
                            "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), f.getType());

                    args[0] = getter.invoke(hObj);

                    setter.invoke(this, args);

                } catch (final org.hibernate.MappingException e) {
                    EafCommon.debug("no load " + f.getName() + " not in db map.");

                } catch (final Exception e) {
                    new EafException("", e);
                }
            }

            // Load default data members - archive, origOwningSys, datetime
            // Owner is set according to the current logged in POC retrieved
            // from GlobalVars
            //
            for (final java.lang.reflect.Field f : hObj.getClass().getSuperclass().getSuperclass().getDeclaredFields()) {
                if (!f.getType().getName().startsWith("main.java.mil.navy.cnmoc")
                        && !Modifier.isFinal(f.getModifiers())
                        && (f.getName().equals("owner") && getOwner().equals("ITAS"))
                        && !f.getName().equals("saveTransactions")) {

                    try {

                        getter = this.getClass().getMethod(
                                "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1));

                        if (f.getName().equalsIgnoreCase("archive")) {
                            setter = this.getClass().getMethod(
                                    "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1),
                                    int.class);

                        } else {
                            setter = this.getClass().getMethod(
                                    "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1),
                                    f.getType());

                        }

                        args[0] = getter.invoke(hObj);

                        setter.invoke(this, args);

                    } catch (final Exception e) {
                        new EafException("", e);
                    }
                }
            }

        } catch (final NullPointerException e) {
            EafCommon.info("Load error " + this.getClass() + " from schema " + getAppID());

        } catch (final RuntimeException e) {

            if ((tx != null) && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (final HibernateException e1) {
                    EafCommon.info("Error rolling back transaction");
                }
                throw e;
            }

        } catch (final Exception e) {
            new EafException("", e);

        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }

        if (tx.wasCommitted() && this.getSaveTransactions()) { // load object

            ViewTransaction view_xaction = new ViewTransaction();

            view_xaction.setTlogOwner(this.getOwner());

            view_xaction.setTlogEntityNm(this.getClass().getName()
                    .replace("eaf.core.entities.", ""));
            view_xaction.setOrigOwnSys(this.getApp().getSchema().toUpperCase());

            try {

                if (this.getClass().getMethod("getId").invoke(this) != null) {

                    view_xaction.setTlogEntityId(this.getClass().getMethod("getId").invoke(this).toString());

                    view_xaction.save();

                    view_xaction = null;
                }

            } catch (final java.lang.ClassCastException e) {

                EafCommon.info(this.getClass(), "Error at assigning a TLOG id to a java.lang.String for load entity.");
            } catch (final Exception e) {
                new EafException("", e);
            }

        }

    }

    /**
     * This method loads a POJO from a mapped database table where the named
     * property equals the incoming value. <br>
     * If saveTransactions flag is true, TLOG is updated with a "VIEW" entry.
     * 
     * @param incProp
     *            String - Load object from database table where the property is
     *            of type incProp
     * @param incValue
     *            Object - will be cast to String or Integer - value property is
     *            queried on in DB to find requested row <br>
     * 
     *            {@code Example usage: } <br> {@code Poc poc = new Poc(); } <br>
     *            {@code poc.load("poc_users_login_id", "DOE.JOHN.C.9987322"); } <br>
     * 
     */

    public final void load(final String incProp, final Object incValue) {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        List hlist = null;

        try {

            final String property = EafCommon.camelCase(incProp);

            entityNm = this.getClass().getName().split("\\.")[this.getClass().getName().split("\\.").length - 1];

            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();

            final org.hibernate.mapping.Property prop = EafCommon.getConfiguration(getAppID())
                    .getClassMapping(this.getClass().getName()).getProperty(property);

            if ((prop != null) & prop.getType().getName().equals("integer")) {
                hlist = session.createQuery("select s from " + entityNm + " s where s." + property + " = " + incValue)
                        .list();

                if (hlist.size() > 0) {
                    hObj = hlist.get(0);
                } else {
                    return;
                }

            } else if ((prop != null) && prop.getType().getName().equals("string")) {
                hlist = session.createQuery(
                        "select s from " + entityNm + " s where s." + property + " = \'" + incValue + "\'").list();

                if (hlist.size() > 0) {
                    hObj = hlist.get(0);
                } else {
                    return;
                }

            }

            tx.commit();

            final org.hibernate.mapping.PersistentClass persistMap = EafCommon.getConfiguration(getAppID())
                    .getClassMapping(this.getClass().getName());

            java.lang.reflect.Method getter;
            java.lang.reflect.Method setter;

            final Object[] args = new Object[1];

            for (final java.lang.reflect.Field f : hObj.getClass().getDeclaredFields()) {

                try {
                    // Even though looping through ALL declared data
                    // members, should only load fields obtained
                    // from the database
                    // Check persistent mapping *hbm.xml to verify data
                    // member is a database field

                    if (persistMap.getProperty(f.getName()) == null) {
                        continue;
                    }

                    getter = this.getClass().getMethod(
                            "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1));

                    setter = this.getClass().getMethod(
                            "set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), f.getType());

                    args[0] = getter.invoke(hObj);

                    setter.invoke(this, args);

                } catch (final org.hibernate.MappingException e) {
                    EafCommon.debug("no load " + f.getName() + " not in db map.");

                } catch (final Exception e) {
                    new EafException("", e);
                }

                // Load default data members - archive, origOwningSys, datetime
                // Owner is set according to the current logged in POC retrieved
                // from GlobalVars
                //
                for (final java.lang.reflect.Field f1 : hObj.getClass().getSuperclass().getSuperclass()
                        .getDeclaredFields()) {

                    if (!f1.getType().getName().startsWith("main.java.mil.navy.cnmoc")
                            && !Modifier.isFinal(f.getModifiers())
                            && (f.getName().equals("owner") && getOwner().equals("ITAS"))
                            && !f.getName().equals("saveTransactions")) {

                        try {

                            getter = this.getClass().getMethod(
                                    "get" + f1.getName().substring(0, 1).toUpperCase() + f1.getName().substring(1));

                            if (f1.getName().equalsIgnoreCase("archive")) {
                                setter = this.getClass().getMethod(
                                        "set" + f1.getName().substring(0, 1).toUpperCase() + f1.getName().substring(1),
                                        int.class);

                            } else {
                                setter = this.getClass().getMethod(
                                        "set" + f1.getName().substring(0, 1).toUpperCase() + f1.getName().substring(1),
                                        f1.getType());

                            }

                            args[0] = getter.invoke(hObj);

                            setter.invoke(this, args);

                        } catch (final Exception e) {
                            new EafException("", e);
                        }
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

        } catch (final Exception e) {
            new EafException("", e);

        } finally {
            if (session.isOpen()) {
                session.close();
            }

        }

        if (tx.wasCommitted() && this.getSaveTransactions()) { // load object

            ViewTransaction view_xaction = new ViewTransaction();

            view_xaction.setTlogOwner(this.getOwner());
            // view_xaction.setTlogAction(EafTransaction.actions.VIEW);
            view_xaction.setTlogEntityNm(this.getClass().getName()
                    .replace("eaf.core.entities.", ""));
            view_xaction.setOrigOwnSys(this.getApp().getSchema().toUpperCase());

            try {
                view_xaction.setTlogEntityId(this.getClass().getMethod("getId").invoke(this).toString());

            } catch (final Exception e) {
                new EafException("", e);
            }

            view_xaction.save();

            view_xaction = null;
        }

    }

    /**
     * 
     * Load set of associated objects mapped to database. <br>
     * With default value for "lazy" parameter set to TRUE, One-To-Many
     * associated objects are not loaded with initial load() request.
     * 
     * {@code Example usage: } <br> {@code     Poc poc = new Poc(); } <br>
     * {@code     poc.load(2912); } <br> {@code     poc.setPocDispId(3); } <br>
     * {@code     poc.update(); } <br>
     * 
     * @param incSetName
     *            - association object name
     */

    public final void loadSet(final String incSetName) {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        session = EafCommon.getSessionFactory(getAppID()).openSession();

        tx = session.beginTransaction();
        primaryKey = EafCommon.getSessionFactory(getAppID()).getClassMetadata(this.getClass())
                .getIdentifierPropertyName();
        final String[] className = this.getClass().getName().split("\\.");

        final String hql = "select s from " + className[className.length - 1] + " s left join fetch s." + incSetName
                + " where s." + primaryKey + " = " + this.getId();

        final List olist = session.createQuery(hql).list();
        tx.commit();
        if (session.isOpen()) {
            session.close();
        }

        java.lang.reflect.Method getter = null;
        java.lang.reflect.Method setter = null;
        try {
            getter = this.getClass().getMethod(
                    "get" + incSetName.substring(0, 1).toUpperCase() + incSetName.substring(1));
            setter = this.getClass().getMethod(
                    "set" + incSetName.substring(0, 1).toUpperCase() + incSetName.substring(1), getter.getReturnType());
        } catch (final SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            if (olist.size() > 0) {
                final Object parentObj = olist.get(0);
                getter.invoke(parentObj);
                setter.invoke(this, getter.invoke(parentObj));
            }
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            new EafException("", e);
        } catch (final IllegalAccessException e) {
            // TODO Auto-generated catch block
            new EafException("", e);
        } catch (final InvocationTargetException e) {
            // TODO Auto-generated catch block
            new EafException("", e);
        }

    }

    /**
     * Validation status.
     * 
     * @param incVal
     *            - boolean
     */
    public final void setValid(final Boolean incVal) {
        isValid = incVal;
    }

    /**
     * Validation messages array.
     * 
     * @param incMsgs
     *            - string array
     */
    public final void setValidMsgs(final ArrayList<String> incMsgs) {
        validMsgs = incMsgs;
    }

    /**
     * 
     * Unarchive a single database record. If saveTransactions flag is true,
     * TLOG is updated. <br> {@code Example usage: } <br> {@code Poc poc = new Poc(); } <br>
     * {@code poc.load(2912); } <br> {@code poc.archive(); }
     * 
     */

    public final void unarchive() {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        try {

            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();

            this.setArchive(0);

            session.update(this);

            tx.commit();

        } catch (final org.hibernate.JDBCException e) {

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
            if (session.isOpen()) {
                session.close();
            }
        }

        if (tx.wasCommitted() && this.getSaveTransactions()) { // delete object

            // final EafTransaction xaction = new EafTransaction();
            if (xaction == null) {
                xaction = new EafTransaction();
            }

            xaction.setTlogOwner(this.getOwner());

            if (xaction.getTlogActionTaken() == null) {
                xaction.setTlogAction(EafTransaction.actions.UPDATE);
            }
            xaction.setTlogEntityNm(this.getClass().getName()
                    .replace("eaf.core.entities.", ""));
            xaction.setOrigOwnSys(this.getApp().getSchema().toUpperCase());

            xaction.buildDiffArray(hObj, this);

            try {
                xaction.setTlogEntityId(this.getClass().getMethod("getId").invoke(this).toString());
            } catch (final Exception e) {
                new EafException("", e);
            }
            xaction.save();

            xaction = null;

        }
        // TODO Uncomment clear() for staging release.
        // clear();

    }

    /**
     * 
     * Perform database update on a single record. <br>
     * If saveTransactions flag is true, TLOG is updated.
     * 
     * {@code Example usage: } <br> {@code     Poc poc = new Poc(); } <br>
     * {@code     poc.load(2912); } <br> {@code     poc.setPocDispId(3); } <br>
     * {@code     poc.update(); } <br>
     * 
     */
    public final Object update() {

        Object db_stat = -1;

        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;

        try {

            session = EafCommon.getSessionFactory(getAppID()).openSession();

            tx = session.beginTransaction();

            session.update(this);

            tx.commit();

            db_stat = this.getClass().getMethod("getId").invoke(this);

        } catch (final org.hibernate.JDBCException e) {
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
            if (session.isOpen()) {
                session.close();
            }
        }

        if (tx.wasCommitted() && this.getSaveTransactions()) { // update object

            // final EafTransaction xaction = new EafTransaction();
            if (xaction == null) {
                xaction = new EafTransaction();
            }

            xaction.setTlogOwner(this.getOwner());

            if (xaction.getTlogActionTaken() == null) {
                xaction.setTlogAction(EafTransaction.actions.UPDATE);
            }
            if (xaction.getTlogEntityNm() == null) {
                xaction.setTlogEntityNm(this.getClass().getName()
                        .replace("eaf.core.entities.", ""));
            }
            xaction.setOrigOwnSys(this.getApp().getSchema().toUpperCase());

            // Older logic, the buildDiffArray() method was introduced
            // in EafTransaction to wrap the creation of the EntityDiffMap
            // and "pack" the changed data members into an
            // "Object data stream" or byte array
            //
            // final EntityDiffMap dmap = new EntityDiffMap(hObj, this);
            // xaction.setTlogDiff(dmap.ba.toByteArray());

            xaction.buildDiffArray(hObj, this);

            try {
                if (xaction.getTlogEntityId() == null) {
                    xaction.setTlogEntityId(this.getClass().getMethod("getId").invoke(this).toString());
                }

            } catch (final Exception e) {
                new EafException("", e);
            }

            if (isAssociationTable()) {
                final org.hibernate.mapping.PersistentClass persistMap = EafCommon.getConfiguration(getAppID())
                        .getClassMapping(this.getClass().getName());

                for (final java.util.Iterator<?> i = persistMap.getPropertyIterator(); i.hasNext();) {
                    final Property prop = (Property) i.next();

                    try {
                        if ((prop.getType().getClass().equals(org.hibernate.type.ManyToOneType.class) && !prop
                                .getType().toString().contains(xaction.getParentEntityNm()))
                                || (prop.getType().getClass().equals(org.hibernate.type.ManyToOneType.class)
                                        && prop.getType().toString().contains(xaction.getParentEntityNm()) && !prop
                                        .getName().toLowerCase().contains("ord"))) {

                            final EntityDiffMap dmap = new EntityDiffMap();

                            dmap.put("dataFields", prop.getName());
                            dmap.put("dataTypes", prop.getType().getReturnedClass().toString().split(" ")[1]);
                            dmap.put(
                                    "previous",
                                    hObj.getClass()
                                            .getMethod(
                                                    "get" + prop.getName().substring(0, 1).toUpperCase()
                                                            + prop.getName().substring(1))
                                            .invoke(this, new Object[] {}));
                            dmap.put(
                                    "changed",
                                    this.getClass()
                                            .getMethod(
                                                    "get" + prop.getName().substring(0, 1).toUpperCase()
                                                            + prop.getName().substring(1))
                                            .invoke(this, new Object[] {}));

                            xaction.setTlogDiff(dmap.pack().toByteArray());
                        }
                    } catch (final Exception e) {
                        new EafException("", e);
                    }

                }
            }

            xaction.save();

            xaction = null;

        }

        // TODO Uncomment clear() for staging release.
        // clear();

        return db_stat;

    }

}
