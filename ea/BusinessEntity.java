package eaf.core.entities.ea;

// Generated Jun 23, 2011 4:29:16 PM by Hibernate Tools 3.4.0.Beta1

import eaf.core.common.GlobalVars;
import eaf.core.common.dao.EaDAO;

import eaf.cm.BusinessEntity.BeUtilities;
import eaf.core.entities.ea.BeOrgAssoc;
import eaf.core.entities.pub.Org;
import eaf.core.entities.pub.Poc;

import eaf.core.entities.PersistentEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * EAF Business Entity
 **/
public class BusinessEntity extends PersistentEntity implements java.io.Serializable {

    private Integer                   beId         = 0;
    private String                    beNm;
    private String                    beAcronym;
    private String                    beDesc;
    private Integer                   bePocId;

    private Set<BusinessEntity>       parent       = null;   // loaded by Hibernate
    private Set<BusinessEntity>       child        = null;   // loaded by Hibernate

    private Set<MsnArea>              directorates = null;
    private Set<Workstation>          workstations = null;
    private Set<Doc>                  docs         = null;

    private ArrayList<BusinessEntity> allparents   = null;   // manually populated
    private Set<BusinessEntity>       allchildren  = null;   // manually populated

    protected int                     beType;

    protected Integer                 parentOrgId;

    protected Integer                 parentBeId   = 0;

    protected BusinessEntity          parentInfoSys;

    public static String[]            TYPES        = { "InfoSys", "OpNode", "System", "System" };

    public static String[]            NAMES        = { "Information Systems", "Operational Nodes", "Systems",
            "Sub-Systems"                         };

    private BeOrgAssoc                beOrgAssoc;

    private EaDAO eaDao;
    
    /**
     * Default constructor.
     */
    public BusinessEntity() {

    }

    /**
     * Constructor w/ globalVars.
     *
     * @param gVars
     */
    public BusinessEntity(final GlobalVars gVars) {
        super(gVars);

    }

    public BusinessEntity(final Poc currPoc) {
        super(currPoc);

    }
    /**
     * Method to acquire the hierarchical list of business entity parents.
     *
     * As an entity is stored into the allparents set, it's parent is determined
     * and stored into the set.
     *
     *
     */
    public void loadParents() {
        BusinessEntity tmp = this;
        tmp.loadSet("parent");

        while (tmp.getParent().size() > 0) {
            allparents.add((BusinessEntity) tmp.getParent().toArray()[0]);
            tmp = (BusinessEntity) tmp.getParent().toArray()[0];
            tmp.loadSet("parent");
        }

        parentOrgId = BeUtilities.findParentOrg(beId);

        if (!isInfoSys() && allparents.size() > 0) {
            parentBeId = allparents.get(0).getBeId();
        }
    }

    /**
     * Load method that compares the organizational access of the POC attempting
     * to load the BE to the target business entity's parent Organization.
     *
     * @param incId
     *
     **/

    public void load(final Object incId) {

        String be_id;

        try {
            be_id = (String) incId;
        } catch (Exception e) {
            be_id = incId.toString();
        }
        Integer parentOrg = 0;

        parentOrg = BeUtilities.findParentOrg(Integer.parseInt(be_id));

        if (parentOrg == null || parentOrg == 0 || currentPOCAccessibleOrgs.contains(parentOrg)) {
            if (incId instanceof String)
                super.load(Integer.parseInt((String) incId));
            else
                super.load(incId);
            parentOrgId = BeUtilities.findParentOrg(beId);
        }
        
        

    }

    /**
     * Method to return business entity integer id.
     *
     * @return be_id
     */
    public Integer getBeId() {
        return this.beId;
    }

    /**
     * Method to set the business entity integer id.
     *
     * @param incBeId
     */
    public void setBeId(final Integer incBeId) {
        beId = incBeId;
    }

    /**
     * Method to get the business entity name.
     *
     * @return be_nm
     *
     */
    public String getBeNm() {
        return this.beNm;
    }

    /**
     * Method to set the business entity name.
     *
     * @param incBeNm
     */
    public void setBeNm(final String incBeNm) {
        this.beNm = incBeNm;
    }

    /**
     * Method to get the business entity acronym.
     *
     * @return string acronym
     *
     */
    public String getBeAcronym() {
        return this.beAcronym;
    }

    /**
     * Method to set business entity acronym.
     *
     * @param incBeAcronym
     *
     */
    public void setBeAcronym(final String incBeAcronym) {
        this.beAcronym = incBeAcronym;
    }

    /**
     * Method to get business entity description.
     *
     * @return string description
     *
     */
    public String getBeDesc() {
        return this.beDesc;
    }

    /**
     * Method to set business entity description.
     *
     * @param incBeDesc
     */
    public void setBeDesc(final String incBeDesc) {
        this.beDesc = incBeDesc;
    }

    /**
     * Method to get the associated POC id.
     *
     * @return integer POC id
     *
     */
    public Integer getBePocId() {
        return this.bePocId;
    }

    /**
     * Method to set the associated POC id.
     *
     * @param incBePocId
     */
    public void setBePocId(final Integer incBePocId) {
        this.bePocId = incBePocId;
    }

    /**
     * Method to set the business entity organization association object.
     *
     * @param incAssoc
     */
    public void setBeOrgAssoc(final BeOrgAssoc incAssoc) {
        this.beOrgAssoc = incAssoc;
    }

    /**
     * Method to get the business entity organization association object.
     *
     * @return BeOrgAssoc object
     */
    public BeOrgAssoc getBeOrgAssoc() {
        return this.beOrgAssoc;
    }

    /**
     * Method to get the hierarchical list of business entity parents.
     *
     * @return parent business entity array
     *
     */
    public ArrayList<BusinessEntity> getParents() {

        if (allparents == null) {
            allparents = new ArrayList<BusinessEntity>();
            loadParents();
        }
        return this.allparents;
    }

    /**
     * Method to set the hierarchical list of business entity parents.
     *
     * @param incParents
     */
    public void setParents(final ArrayList<BusinessEntity> incParents) {
        this.allparents = incParents;
    }

    /**
     * Method to get the business entity parent.
     *
     * @return BusinessEntity parent
     */
    public Set<BusinessEntity> getParent() {

        if (parent == null) {
            parent = new HashSet<BusinessEntity>();
            loadSet("parent");
        }
        return this.parent;
    }

    /**
     * Method to set the business entity parent.
     *
     * @param incParent
     */
    public void setParent(final Set<BusinessEntity> incParent) {
        this.parent = incParent;
    }

    /**
     * Method to get the directorates associated with the business entity
     * (systems only).
     *
     * @return Directorate set
     */
    public Set<MsnArea> getDirectorates() {

        if (directorates == null) {
            directorates = new HashSet<MsnArea>();
            loadSet("directorates");
        }
        return this.directorates;
    }

    /**
     * Method to set the directorates associated with the business entity
     * (systems only).
     *
     * @param incDirectorates
     */
    public void setDirectorates(final Set<MsnArea> incDirectorates) {
        this.directorates = incDirectorates;
    }

    /**
     * Method to get the workstations associated with a business entity (systems
     * only).
     *
     * @return Workstations set
     */
    public Set<Workstation> getWorkstations() {

        if (workstations == null) {
            workstations = new HashSet<Workstation>();
               loadSet("workstations");
        }
        return this.workstations;
    }

    /**
     * Method to set the workstations associated to a business entity (systems
     * only).
     *
     * @param incWorkstations
     *
     */
    public void setWorkstations(final Set<Workstation> incWorkstations) {
        this.workstations = incWorkstations;
    }

    /**
     * Method to get the documents associated with a busineses entity.
     *
     * @return Documents set
     */
    public Set<Doc> getDocs() {

        if (docs == null) {
            docs = new HashSet<Doc>();
            loadSet("docs");
        }
        return this.docs;
    }

    /**
     * Method to set the documnets associated to a business entity.
     *
     * @param incDocs
     */
    public void setDocs(final Set<Doc> incDocs) {
        this.docs = incDocs;
    }

    /**
     * Method to get the children of a business entity - this set is built
     * manually after the POJO load to avoid an exception of operating on a
     * property that has a lazy initialization.
     *
     * @return BusinessEntity set
     */

    public Set<BusinessEntity> getChildren() {

        if (allchildren == null) {
            allchildren = new HashSet<BusinessEntity>();

            loadSet("child");

            allchildren = child;
        }
        return this.allchildren;
    }

    /**
     * Method to set the children of a business entity - this set is built
     * manually after the POJO load to avoid an exception of operating on a
     * property that has a lazy initialization..
     *
     * @param incChildren
     */
    public void setChildren(final Set<BusinessEntity> incChildren) {
        this.allchildren = incChildren;
    }

    /**
     * Method to set the children of a business entity - this set is mapped
     * directly in hibernate mapping and can be loaded using the "loadset"
     * method.
     *
     * @param incChildren
     */
    public void setChild(final Set<BusinessEntity> incChildren) {
        this.child = incChildren;
    }

    /**
     * Method to get the children of a business entity - this set is mapped
     * directly in the hibernate mapping and can be loaded using the "loadset"
     * method.
     *
     * @return BusinessEntity set
     */
    public Set<BusinessEntity> getChild() {
        return this.child;
    }

    /**
     * Method to get the business entity id.
     *
     * @return integer id
     */
    @Override
    public final Integer getId() {
        return beId;
    }

    /**
     * Method to set the business entity id.
     *
     * @param integer
     *            id
     */
    @Override
    public final void setId(final Object incId) {
        setBeId(((Integer) incId).intValue());
    }

    /**
     * Method to get the business entity name.
     *
     * return string name
     */
    @Override
    public final String getName() {
        return beNm;
    }

    /**
     * Method to set the business entity name.
     *
     * @param string
     *            name
     */
    @Override
    public final void setName(final String incName) {
        beNm = incName;
    }

    /**
     * Method to determine if the type of business entity is an Information
     * System.
     *
     * @return boolean
     */
    public Boolean isInfoSys() {

        if (!isSystem() && getParents().size() == 0 && beOrgAssoc != null)
            return true;
        else
            return false;
    }

    /**
     * Method to determine if the type of business entity is an Operation Node.
     *
     *
     * @return boolean
     */
    public Boolean isOpNode() {

        if (getParents().size() > 0 && !isSystem())
            return true;
        else
            return false;
    }

    /**
     * Method to determine if the type of business entity is a System.
     *
     *
     * @return boolean
     */
    public Boolean isSystem() {

    	loadSet("workstations");
        if (getWorkstations().size() > 0)
            return true;
        else
            return false;
    }

    /**
     * Method to return the business entity type.
     *
     * @return integer
     */
    public int getBeType() {

        if (isSystem()) {
            return 2;
        } else if (isOpNode()) {
            return 1;
        } else if (isInfoSys()) {
            return 0;
        } else
            return -1;
    }

    /**
     * Method to return the business entity type of the parent.
     *
     * @return String
     */
    public String getParentType() {

        if (getParents().size() == 1) {
            return "0";
        } else {
            return "1";
        }
    }

    /**
     * Method to return the id of the organization this business entity belongs
     * to.
     *
     * @return integer id
     */
    public Integer getParentOrgId() {

        return parentOrgId;
    }

    /**
     * Method to return the parent Organization object.
     *
     * @return Org
     */
    public Org getParentOrg() {

        Org parentOrg = new Org();

        parentOrg.load(parentOrgId);

        return parentOrg;

    }

    /**
     * Method to return the id of the parent business entity.
     *
     * @return integer id
     */
    public Integer getParentBeId() {

        return parentBeId;
    }

    /**
     * Method to return the parent Information System business entity object.
     *
     * @return BusinessEntity infosys
     */
    public BusinessEntity getParentInfoSys() {

        if (parentInfoSys == null) {
            getParents();
            parentInfoSys = allparents.get(allparents.size() - 1);
        }

        return parentInfoSys;

    }

    /**
     * Method to return the name of the POC associated with this Business
     * Entity.
     *
     * @return string name
     */
    public String getBePocNm() {

        String pocNm = "";

        if (bePocId != null) {
            Poc p = new Poc();

            p.load(getBePocId());

            pocNm = p.getPocFullNm();
        }

        return pocNm;

    }


    public String getViewUrl(){
        return "EAJAVA/cm/BusinessEntity/view?currentid=" + beId;
    }

}
