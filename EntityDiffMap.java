package eaf.core.entities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import eaf.core.common.EafCommon;
import eaf.core.common.EafException;
import eaf.core.common.FormDef;
import eaf.core.common.Transformer;
import eaf.core.common.GlobalVars;
import eaf.core.common.Transformer.OPTIONS;
import eaf.core.common.utilityClasses.EaRequest;
import eaf.core.web.pageMarkup;

import org.hibernate.mapping.Property;

/**
 * Class to provide EAF entity comparison functionality.
 *
 * Methods are used to display changes to an object prior to database commit,
 * and the methods can be applied to compare previous versions or view an
 * object's history.
 *
 * This class is built independently of the EafTransaction class, and its
 * methods are able to operate on the the byte array data member stored for a
 * transaction.
 *
 * with storing changes into a byte array into the Tlog table is only one aspect
 * of the methods written.
 *
 */
public class EntityDiffMap {

    /**
     * Array of changed Entity member values.
     */
    private final ArrayList<Object>     changed    = new ArrayList<Object>();

    /**
     * Array of Entity member names.
     */
    private final ArrayList<Object>     dataFields = new ArrayList<Object>();

    /**
     * Array of Entity member data types.
     */
    private final ArrayList<Object>     dataTypes  = new ArrayList<Object>();

    /**
     * Byte array output stream - for insertion into Postgres byte array column.
     * Call toByteArray() for insert into Postgres database.
     */
    private final ByteArrayOutputStream ba         = new ByteArrayOutputStream();

    /**
     * Input byte array stream - received from database on query select
     * tlog_diff from tlog.
     */
    private ObjectInputStream           in         = null;

    /**
     * Array of original Entity member values.
     */
    private final ArrayList<Object>     previous   = new ArrayList<Object>();

    /**
     * Constructor.
     */
    public EntityDiffMap() {

        /*
         * dataFields.add(new String("id")); dataTypes.add(new
         * String("integer")); previous.add(new Integer(100)); changed.add(new
         * Integer(40)); dataFields.add(new String("credit")); dataTypes.add(new
         * String("float")); previous.add(new Float(200.22)); changed.add(new
         * Float(750.55));
         */
    }

    /**
     * EntityDiffMap constructor.
     *
     * For retrieval of diff byte array from the database.
     *
     * Populate the ObjectInputStream "in" from the tlog_diff column from a Tlog
     * record.
     *
     * "Unpack" the ObjectInputStream into the dataFields, dataTypes, previous
     * and changed array lists.
     *
     * Prepare the EntityDiffMap object so that a "difference" HTML table can be
     * constructed.
     *
     * @param incStream
     *            - ByteArrayInputStream received from PG DB. <br>
     *
     * <br> {@code Sample code: } <br> {@code entityDiffMap    e2       = null;} <br>
     *            {@code eafTransaction   xaction  = new eafTransaction(); }<br>
     *            {@code xaction.load(342);        //get transaction w/ id #342 }
     * <br>
     *
     *            {@code e2 = new entityDiffMap(new ByteArrayInputStream
     * (xaction.getTlogDiff().getBytes())); }<br> {@code e2.printDiff(); } <br>
     *
     */

    public EntityDiffMap(final ByteArrayInputStream incStream) {

        try {
            this.in = new ObjectInputStream(incStream);
            this.unpack();
        } catch (final Exception e) {
            new EafException("", e);
        }
    }

    /**
     * EntityDiffMap constructor.
     *
     * For preparing diff byte array to push to the database.
     *
     * Compare the data members of 2 objects and populate the array lists -
     * dataFields, dataTypes, previous, changed.
     *
     * Then call "pack()" to push objects from the ArrayLists into the
     * ByteArrayOutputStream ba.
     *
     * @param ent0
     *            Original data object
     * @param ent1
     *            Changed data object <br>
     * <br> {@code Sample code:} <br> {@code entityDiffMap dmap =
     *                new entityDiffMap(poc, new_poc); } <br>
     *            {@code xaction.setTlogDiff(dmap.packit().toByteArray()); }
     */
    public EntityDiffMap(final Object ent0, final Object ent1) {

        compare(ent0, ent1);
        pack();
    }

    /**
     *
     * Return the byte array from the ByteArrayOutputStream.
     *
     * For push of byte array to the database, or assignment to tlogDiff.
     *
     * @return byte[] call toByteArray() from the ba output stream.
     *
     */

    public final byte[] getByteArray() {

        return ba.toByteArray();
    }

    /**
     *
     * Execute all class "getters" to compare data values between two objects.
     * If values are not equal, populate previous/changed Object array lists to
     * record differences.
     *
     * @param ent0
     *            Original data object
     * @param ent1
     *            Changed data object
     *
     */

    public final void compare(final Object ent0, final Object ent1) {

        Object val0 = null;
        Object val1 = null;

        final String moduleNm = ent0.getClass().getName().split("\\.")[PersistentEntity.CLASS_OFFSET];

        final org.hibernate.mapping.PersistentClass map = EafCommon.getConfiguration(
                EafCommon.APPS.getApp(moduleNm).getId()).getClassMapping(ent0.getClass().getName());

        final java.lang.reflect.Method[] methods = ent0.getClass().getDeclaredMethods();

        if (ent0.getClass().getName().equals(ent1.getClass().getName())) {

            for (Iterator p = map.getPropertyIterator(); p.hasNext();) {

                final Property prop = (Property) p.next();

                String eafDatatype = null;

                EafCommon.info("++compare " + prop.getName());

                // Excluding Sets or Collections
                // representing EAF Associations
                // Check persistent mapping *hbm.xml to verify data
                // member is not a collection
                if (prop.getType().isCollectionType()) {
                    continue;
                }

                val0 = null;
                val1 = null;

                final int getOffset = 3;

                try {
                    val0 = ent0
                            .getClass()
                            .getDeclaredMethod(
                                    "get" + prop.getName().substring(0, 1).toUpperCase() + prop.getName().substring(1))
                            .invoke(ent0);

                } catch (final Exception e) {
                    try {
                        val0 = ent0
                                .getClass()
                                .getSuperclass()
                                .getSuperclass()
                                .getDeclaredMethod(
                                        "get" + prop.getName().substring(0, 1).toUpperCase()
                                                + prop.getName().substring(1)).invoke(ent0);

                    } catch (final Exception e1) {
                        EafCommon.debug("err invoking get" + prop.getName());
                        e1.printStackTrace();
                    }
                }

                try {
                    val1 = ent0
                            .getClass()
                            .getDeclaredMethod(
                                    "get" + prop.getName().substring(0, 1).toUpperCase() + prop.getName().substring(1))
                            .invoke(ent1);

                } catch (final Exception e) {

                    try {
                        val1 = ent0
                                .getClass()
                                .getSuperclass()
                                .getSuperclass()
                                .getDeclaredMethod(
                                        "get" + prop.getName().substring(0, 1).toUpperCase()
                                                + prop.getName().substring(1)).invoke(ent1);
                    } catch (final Exception e1) {

                        EafCommon.debug("err invoking get" + prop.getName());
                        e1.printStackTrace();
                    }
                }

                if ((val0 == null) && (val1 == null)) {
                    continue;
                }

                // Currently, "diff" is only executed for
                // primitive/basic data types
                // This code does not compare EAF POJOs (yet)
                try {
                    if ((val0 != null && EafEntity.isEafDatatype(val0.getClass().getName()))
                            || (val1 != null && EafEntity.isEafDatatype(val1.getClass().getName()))) {

                        if (val0 != null) {
                            eafDatatype = val0.getClass().getName();
                            val0 = val0.getClass().getMethod("getId").invoke(val0);
                        }

                        if (val1 != null) {
                            eafDatatype = val1.getClass().getName();
                            val1 = val1.getClass().getMethod("getId").invoke(val1);
                        }

                    }

                } catch (java.lang.reflect.InvocationTargetException oe) {

                    EafCommon.info("LazyInit for " + prop.getName() + " ... skipping compare for TLog DiffMap");
                    EafCommon.info(oe.getCause().getMessage());
                    continue;

                } catch (Exception e) {
                    new EafException("", e);

                    try {
                        if (EafEntity.isEafDatatype(ent0
                                .getClass()
                                .getSuperclass()
                                .getSuperclass()
                                .getDeclaredMethod(
                                        "get" + prop.getName().substring(0, 1).toUpperCase()
                                                + prop.getName().substring(1)).getReturnType().getName())) {

                            eafDatatype = ent0
                                    .getClass()
                                    .getSuperclass()
                                    .getSuperclass()
                                    .getDeclaredMethod(
                                            "get" + prop.getName().substring(0, 1).toUpperCase()
                                                    + prop.getName().substring(1)).getReturnType().getName();

                            val0 = val0.getClass().getMethod("getId").invoke(val0);

                            val1 = val1.getClass().getMethod("getId").invoke(val1);

                        }

                    } catch (final Exception e1) {
                        new EafException("", e1);
                    }

                }

                if ((val0 != null && !val0.equals(val1)) || (val1 != null && !val1.equals(val0))) {

                    dataFields.add(prop.getName());

                    if (eafDatatype == null) {

                        try {
                            dataTypes.add(val0.getClass().getName());
                        } catch (Exception e) {
                            dataTypes.add(val1.getClass().getName());
                        }

                    } else {

                        dataTypes.add(eafDatatype);

                    }
                    previous.add(val0);
                    changed.add(val1);
                }
            }
        }
    }

    /**
     * Pack objects from data field names/data types/previous/changed array
     * lists into an ObjectStream to be converted to a byte array for database
     * insert.
     *
     * @return ByteArrayOutputStream
     */

    public final ByteArrayOutputStream pack() {

        try {
            final ObjectOutputStream oo = new ObjectOutputStream(ba);

            for (int i = 0; i < dataFields.size(); i++) {
                oo.writeObject(dataFields.get(i));
                oo.writeObject(dataTypes.get(i));
                oo.writeObject(previous.get(i));
                oo.writeObject(changed.get(i));
            }
        } catch (final Exception e) {
            new EafException("", e);
        }

        return ba;
    }

    /**
     *
     * Unpack objects from incoming ObjectStream (byte array from database) into
     * data field names/data types/previous/changed Object array lists.
     *
     */

    public final void unpack() {

        Object obj = null;

        try {
            while ((obj = in.readObject()) != null) {

                dataFields.add(obj);

                dataTypes.add(in.readObject());

                previous.add(in.readObject());

                changed.add(in.readObject());

                EafCommon.info("unpack tlog_diff: " + obj + ", " + dataTypes.get(dataTypes.size() - 1) + ", "
                        + previous.get(dataTypes.size() - 1) + ", " + changed.get(dataTypes.size() - 1));

            }
        } catch (final java.io.EOFException e) {
            // Avoid stack trace dump just because
            // end of byte array encountered.
            String err = "EOF of byte array encountered";
        } catch (final Exception e) {
            new EafException("", e);
        }

    }

    /**
     *
     * For debug/troubleshooting - print object differences array to the console
     * prior to a change is committed to the database.
     */

    public final void printDiff() {

        final int bufLength = 30;

        String t = new String();
        String s = new String();
        final char[] cc = new char[bufLength];

        if (dataFields.size() > 0) {

            for (int i = 0; i < bufLength; i++) {
                cc[i] = ' ';
            }
            s = "Fieldname";
            for (int i = 0; i < s.length(); i++) {
                cc[i] = s.charAt(i);
            }
            t = String.valueOf(cc);
            for (int i = 0; i < bufLength; i++) {
                cc[i] = ' ';
            }
            s = "Type";
            for (int i = 0; i < s.length(); i++) {
                cc[i] = s.charAt(i);
            }
            t += String.valueOf(cc);
            for (int i = 0; i < bufLength; i++) {
                cc[i] = ' ';
            }
            s = "Previous";
            for (int i = 0; i < s.length(); i++) {
                cc[i] = s.charAt(i);
            }
            t += String.valueOf(cc);
            for (int i = 0; i < bufLength; i++) {
                cc[i] = ' ';
            }
            s = "ChangeTo";
            for (int i = 0; i < s.length(); i++) {
                cc[i] = s.charAt(i);
            }
            t += String.valueOf(cc);

            EafCommon.debug(t);
            for (int i = 0; i < bufLength; i++) {
                cc[i] = '_';
            }
            EafCommon.debug(String.valueOf(cc) + String.valueOf(cc) + String.valueOf(cc) + String.valueOf(cc));

        }

        for (int i = 0; i < this.dataFields.size(); i++) {
            t = "";
            for (int j = 0; j < bufLength; j++) {
                cc[j] = ' ';
            }
            s = dataFields.get(i).toString();
            if (s.length() > bufLength) {
                s = s.substring(0, bufLength - 1);
            }
            for (int j = 0; j < s.length(); j++) {
                cc[j] = s.charAt(j);
            }
            t = String.valueOf(cc);
            for (int j = 0; j < bufLength; j++) {
                cc[j] = ' ';
            }
            s = dataTypes.get(i).toString();
            if (s.length() > bufLength) {
                s = s.substring(0, bufLength - 1);
            }
            for (int j = 0; j < s.length(); j++) {
                cc[j] = s.charAt(j);
            }
            t += String.valueOf(cc);
            for (int j = 0; j < bufLength; j++) {
                cc[j] = ' ';
            }
            if (previous.get(i) != null) {
                s = previous.get(i).toString();
            } else {
                s = "None";
            }
            if (s.length() > bufLength) {
                s = s.substring(0, bufLength - 1);
            }
            for (int j = 0; j < s.length(); j++) {
                cc[j] = s.charAt(j);
            }
            t += String.valueOf(cc);
            for (int j = 0; j < bufLength; j++) {
                cc[j] = ' ';
            }
            if (changed.get(i) != null) {
                s = changed.get(i).toString();
            } else {
                s = "None";
            }
            if (s.length() > bufLength) {
                s = s.substring(0, bufLength - 1);
            }
            for (int j = 0; j < s.length(); j++) {
                cc[j] = s.charAt(j);
            }
            t += String.valueOf(cc);
            EafCommon.debug(t);
        }
    }

    /**
     * Show changed data values ONLY from a transaction's diff byte array
     * (HTML).
     *
     * Return HTML table with previous/changed data values side-by-side to view
     * entity history. Data values are acquired from the TLOG difference byte
     * array and stored in a EntityDiffMap instance's dataFields, dataType,
     * previous, changed string arrays.
     *
     * @code EafTransaction x = new EafTransaction(960); EntityDiffMap diff =
     *       new EntityDiffMap( new ByteArrayInputStream(x.getTlogDiff()));
     *
     * @return HTML string
     */
    public final String printDelta(final FormDef... entForm) {

        String html = "";

        html += pageMarkup.tableBegin("", "", "", "", "", "");

        for (int i = 0; i < this.dataFields.size(); i++) {

            // if (entForm.length > 0 &&
            // entForm[0].getElement(dataFields.get(i).toString()) == null)
            // continue;

            html += pageMarkup.trBegin("", "", "", "", "", "");

            html += pageMarkup.tdBegin("", "", "", "", "", "");

            if (entForm.length == 0
                    || (entForm.length > 0 && entForm[0].getElement(dataFields.get(i).toString()) == null)) {
                html += EafCommon.capitalize(dataFields.get(i).toString(), true) + ": ";

            } else {
                html += entForm[0].getElement(dataFields.get(i).toString()).getTitle() + ": ";
            }

            html += pageMarkup.tdEnd();

            // spacer
            html += pageMarkup.tdBegin("", "", "", "", "", "width=20px");
            html += pageMarkup.tdEnd();

            String datatype = (String) dataTypes.get(i);

            html += pageMarkup.tdBegin("", "", "", "", "", "");
            if (i < previous.size() && previous.get(i) != null) {

                // An associated object (many-to-one) was changed.
                // Use getName() to render the data value.
                // Instantiate the associated object using the key
                // stored in the differences byte array.
                //
                if (EafEntity.isEafDatatype(datatype)) {
                    try {
                        PersistentEntity pObj = (PersistentEntity) Class.forName(datatype).newInstance();

                        pObj.xactionsOff();
                        pObj.load(previous.get(i));

                        html += pObj.getName();

                    } catch (Exception e) {
                        EafCommon.debug("Could not instantiate " + datatype + " for diff table");
                    }
                } else {
                    html += previous.get(i).toString();
                }
            }
            html += pageMarkup.tdEnd();

            html += pageMarkup.tdBegin("", "", "", "", "", "");
            html += pageMarkup.tdEnd();

            // spacer
            html += pageMarkup.tdBegin("", "", "", "", "", "width=20px");
            html += pageMarkup.tdEnd();

            html += pageMarkup.tdBegin("", "", "", "", "", "");
            html += "<font color=#CC3366>";

            if (i < changed.size() && changed.get(i) != null) {

                // An associated object (many-to-one) was changed.
                // Use getName() to render the data value.
                // Instantiate the associated object using the key
                // stored in the differences byte array.
                //
                if (EafEntity.isEafDatatype(datatype)) {
                    try {
                        PersistentEntity cObj = (PersistentEntity) Class.forName(datatype).newInstance();

                        cObj.xactionsOff();
                        cObj.load(changed.get(i));

                        html += cObj.getName();

                    } catch (Exception e) {
                        EafCommon.debug("Could not instantiate " + datatype + " for diff table");
                    }
                } else {
                    html += changed.get(i).toString();
                }

            }
            html += "</font>";
            html += pageMarkup.tdEnd();

            html += pageMarkup.trEnd();
        }

        html += pageMarkup.tableEnd();

        return html;
    }

    /**
     * Wrapper for printDelta() - show changed data values ONLY from a
     * transaction diff byte array (HTML).
     *
     * Return HTML table with previous/changed data values side-by-side to view
     * entity history. Data values are acquired from the TLOG difference byte
     * array and stored in a EntityDiffMap instance's dataFields, dataType,
     * previous, changed string arrays.
     *
     * Read the form xml designated by formName to get HTML element titles for
     * changed data fields.
     *
     * @return HTML string
     */
    public final String printDelta(final String formName, final GlobalVars gvars, final EaRequest request) {

        String shrtName = formName.replace("eaf.core.entities.", "");

        String xmlName = Transformer.formLookup(shrtName.replace(".", "/") + ".form.xml", request.getSession()
                .getServletContext());

        if (xmlName == null || xmlName.split("/")[xmlName.split("/").length - 1].equals("null")) {
            String html = printDelta();

            return html;
        } else {

            FormDef entForm = Transformer.readFormByXML(xmlName, gvars, request.getSession().getServletContext());

            entForm = Transformer.getValidationCriteria(entForm, xmlName);

            String html = printDelta(entForm);

            return html;
        }
    }

    /**
     * Options for rendering comparison difference table.
     */

    public static enum OPTIONS {
        /**
         * Don't open and close HTML table, programmer will do this.
         */
        NO_TABLE_TAGS
    };

    /**
     * Compare 2 objects.
     *
     * Wrapper for printDiffTable where groups defaults to {"all"}. Return HTML
     * table with current/proposed changed data values side-by-side to compare
     * entities prior the a database commit. This method doesn't access the
     * differences stored in a TLOG byte array, instead, 2 POJOS' content is
     * compared. Form XML is accessed to get widgets titles.
     *
     * This method does not read or coupled with the tlog_diff difference byte
     * array from Tlog.
     *
     *
     * @param o1
     *            Object
     * @param o2
     *            Object
     * @param formName
     *            "pub.Org"
     * @param gvars
     *            GlobalVars
     * @param request
     *            EaRequest
     * @param options
     *            Options for rendering
     * @return HTML string
     */
    public static final String printDiffTable(final Object o1, final Object o2, final String formName,
            final GlobalVars gvars, final EaRequest request, final OPTIONS... options) {

        String[] groups = { "all" };

        return printDiffTable(o1, o2, formName, gvars, request, groups, options);
    }

    /**
     * Compare 2 objects.
     *
     * Return HTML table with current/proposed changed data values side-by-side
     * to compare entities prior a database commit. This method doesn't access
     * the differences stored in a TLOG byte array, instead, 2 POJOS' content is
     * compared. Form XML is accessed to get widgets titles.
     *
     * This method does not read or coupled with the tlog_diff difference byte
     * array from Tlog.
     *
     * @param o1
     *            Object
     * @param o2
     *            Object
     * @param formName
     *            "pub.Org"
     * @param gvars
     *            GlobalVars
     * @param request
     *            EaRequest
     * @param groups
     *            sub-set of elements from form to render
     * @param options
     *            Entity comparison "diff" table options
     *
     * @return HTML string
     */
    public static final String printDiffTable(final Object o1, final Object o2, final String formName,
            final GlobalVars gvars, final EaRequest request, final String[] groups, final OPTIONS... options) {

        EafCommon.debug("Start diff table for " + o1 + " and " + o2 + " for " + formName);

        String html = "";

        if (!Arrays.asList(options).contains(OPTIONS.NO_TABLE_TAGS)) {

            html += pageMarkup.tableBegin("", "", "", "", "", "");
        }

        String xmlName = Transformer.formLookup(formName.replace(".", "/") + ".form.xml", request.getSession()
                .getServletContext());

        if (xmlName == null || xmlName.split("/")[xmlName.split("/").length - 1].equals("null")) {
            return "ERROR: Form could not be found.";
        }

        FormDef entForm = Transformer.readFormByXML(xmlName, gvars, request.getSession().getServletContext());

        for (int i = 0; i < entForm.getElementList().size(); i++) {

            if (!Arrays.asList(groups).contains("all")
                    && !Arrays.asList(groups).contains(entForm.getElementList().get(i).getGroup())) {
                continue;
            }

            Object retrievedValue1, retrievedValue2;

            String getterName = "";

            try {

                EafCommon.info("getter: " + "get"
                        + entForm.getElementList().get(i).getId().substring(0, 1).toUpperCase()
                        + entForm.getElementList().get(i).getId().substring(1));

                retrievedValue1 = o1
                        .getClass()
                        .getMethod(
                                "get" + entForm.getElementList().get(i).getId().substring(0, 1).toUpperCase()
                                        + entForm.getElementList().get(i).getId().substring(1)).invoke(o1);

                retrievedValue2 = o1
                        .getClass()
                        .getMethod(
                                "get" + entForm.getElementList().get(i).getId().substring(0, 1).toUpperCase()
                                        + entForm.getElementList().get(i).getId().substring(1)).invoke(o2);

            } catch (java.lang.NoSuchMethodException e) {
                try {
                    getterName = entForm.getElementList().get(i).getId();

                    getterName = "get" + getterName.substring(0, 1).toUpperCase()
                            + getterName.substring(1).replaceFirst("[A-Z][a-z0-9]*$", "");

                    EafCommon.info("getter: " + getterName);

                    retrievedValue1 = o1.getClass().getMethod(getterName).invoke(o1);

                    retrievedValue2 = o2.getClass().getMethod(getterName).invoke(o2);

                    EafCommon.info("got: " + retrievedValue1);
                    EafCommon.info("got: " + retrievedValue2);

                } catch (NoSuchMethodException e1) {
                    EafCommon.debug("In print diff - no method " + getterName);
                    continue;

                } catch (Exception e1) {
                    new EafException("", e1);
                    continue;
                }

            } catch (Exception e) {
                new EafException("", e);
                continue;
            }

            //
            // For values returned that are EAF POJOs,
            // use getName() to get value to display.
            //

            try {
                String retrievedObjectType = null;

                if (retrievedValue1 != null) {
                    EafCommon.info("retrievedValue1 is not null: " + retrievedValue1.toString());
                    retrievedObjectType = retrievedValue1.getClass().getName();
                    EafCommon.info("retrievedObjectType is now: " + retrievedValue1.getClass().getName());
                } else {
                    EafCommon.info("retrievedValue1 is null");
                }

                if (retrievedObjectType == null && retrievedValue2 != null) {
                    EafCommon.info("retrievedValue1 is null" + " but retrievedValue2 is not null: "
                            + retrievedValue2.toString());
                    retrievedObjectType = retrievedValue2.getClass().getName();
                    EafCommon.info("retrievedObjectType is now: " + retrievedValue2.getClass().getName());
                }

                if (EafEntity.isEafDatatype(retrievedObjectType)) {
                    if (retrievedValue1 != null) {
                        retrievedValue1 = retrievedValue1.getClass().getMethod("getName").invoke(retrievedValue1);
                    } else {
                        retrievedValue1 = "None";
                    }
                    if (retrievedValue2 != null) {
                        retrievedValue2 = retrievedValue2.getClass().getMethod("getName").invoke(retrievedValue2);
                    } else {
                        retrievedValue2 = "None";
                    }
                }

                if (retrievedValue1 == null || retrievedValue1.equals("")) {
                    retrievedValue1 = "None";
                }

                if (retrievedValue2 == null || retrievedValue2.equals("")) {
                    retrievedValue2 = "None";
                }

                //
                // Check if object value is actually a domain code
                // and perform a lookup
                //
                if (entForm.getElementList().get(i).getDomain() != null) {
                    retrievedValue1 = entForm.getElementList().get(i).getDomain()
                            .getValue(retrievedValue1.toString().trim());

                    retrievedValue2 = entForm.getElementList().get(i).getDomain()
                            .getValue(retrievedValue2.toString().trim());

                }

            } catch (Exception e) {
                new EafException("", e);
                continue;
            }

            //
            // Render HTML table row and cells
            //
            html += pageMarkup.trBegin("", "", "", "", "", "");

            html += pageMarkup.tdBegin("", "", "", "", "", "");
            html += pageMarkup.bBegin("", "", "", "", "");
            html += pageMarkup.hrefBegin("", "", "", "", "", "", entForm.getElementList().get(i).getDescr(), false,
                    false, false, "", false);
            html += entForm.getElementList().get(i).getTitle() + ": ";
            html += pageMarkup.hrefEnd();
            html += pageMarkup.bEnd();
            html += pageMarkup.tdEnd();

            html += pageMarkup.tdBegin("", "", "", "", "", "");

            if (!retrievedValue1.equals("None")) {
                html += retrievedValue1.toString();
            } else {
                html += "<b>None</b>";
            }

            html += pageMarkup.tdEnd();
            html += pageMarkup.tdBegin("", "", "", "", "", "");
            html += "&nbsp;&nbsp;&nbsp;";
            html += pageMarkup.tdEnd();
            html += pageMarkup.tdBegin("", "", "", "", "", "");

            if (!retrievedValue1.toString().equals(retrievedValue2.toString())) {
                html += "<font color=#CC3366>";
            }

            if (!retrievedValue2.equals("None")) {
                html += retrievedValue2.toString();
            } else {
                html += "<b>None</b>";
            }

            if (!retrievedValue1.toString().equals(retrievedValue2.toString())) {
                html += "</font>";
            }

            html += pageMarkup.tdEnd();

            html += pageMarkup.trEnd();

        }

        if (!Arrays.asList(options).contains(OPTIONS.NO_TABLE_TAGS)) {
            html += pageMarkup.tableEnd();
        }

        return html;
    }

    /**
     * Return array list of field names for data members changed.
     *
     * @return ArrayList<Object> list
     */

    public final ArrayList<Object> getDataFields() {
        return dataFields;
    }

    /**
     * Return array list of field types for data members changed.
     *
     * @return ArrayList<Object> list
     */

    public final ArrayList<Object> getDataTypes() {
        return dataTypes;
    }

    /**
     *
     * Return array list of previous values for data members changed.
     *
     * @return ArrayList<Object> list
     */

    public final ArrayList<Object> getPrevValues() {
        return previous;
    }

    /**
     *
     * Return array list of changed values for data members changed.
     *
     * @return ArrayList<Object> list
     */

    public final ArrayList<Object> getChngValues() {
        return changed;
    }

    /**
     *
     * Push values into private ArrayLists.
     *
     * @param String
     *            listtype
     *
     * @parm Object value
     *
     */

    public void put(final String listtype, final Object value) {

        if (listtype.toLowerCase().equals("datafields"))
            dataFields.add(value);
        else if (listtype.toLowerCase().equals("datatypes"))
            dataTypes.add(value);
        else if (listtype.toLowerCase().equals("changed"))
            changed.add(value);
        else if (listtype.toLowerCase().equals("previous"))
            previous.add(value);
    }
}
