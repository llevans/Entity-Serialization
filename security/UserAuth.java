package eaf.core.entities.security;

import eaf.core.entities.PersistentEntity;
import eaf.core.security.EafAccess;
import eaf.core.security.EafRoles;

public class UserAuth extends PersistentEntity implements java.io.Serializable {

    /** id. */
    private int     userAuthId;
    /** Poc Id. */
    private int     pocId;
    /** Access Id. */
    private Integer accessId;
    /** Role Id. */
    private int     roleId;
    /** Access name. */
    private String  accessNm;
    /** Role name. */
    private String  roleNm;

    /** Public no-args constructor. */
    public UserAuth() {

    }

    /**
     * Public constructor.
     *
     * @param incPocId
     *            - id
     * @param incAccessId
     *            - id
     * @param incRoleId
     *            - id
     */
    public UserAuth(final int incPocId, final Integer incAccessId, final int incRoleId) {
        this.pocId = incPocId;
        this.accessId = incAccessId;
        this.roleId = incRoleId;
        this.accessNm = EafAccess.accessDomain[incAccessId];
        this.roleNm = EafRoles.roleDomain[incRoleId];
    }

    @Override
    public void setId(final Object incUserAuthId) {
        this.setUserAuthId(((Integer) incUserAuthId).intValue());
    }

    @Override
    public Integer getId() {
        return userAuthId;
    }

    /**
     * Id.
     *
     * @return id
     */
    public int getUserAuthId() {
        return userAuthId;
    }

    /**
     * Id.
     *
     * @param incUserAuthId
     *            - id
     */
    public void setUserAuthId(final int incUserAuthId) {
        this.userAuthId = incUserAuthId;
    }

    /**
     * Poc Id.
     *
     * @return id\
     */
    public int getPocId() {
        return pocId;
    }

    /**
     * Set Poc id.
     *
     * @param incPocId
     *            - id
     */
    public void setPocId(final int incPocId) {
        this.pocId = incPocId;
    }

    /**
     * Access id.
     *
     * @return id
     */
    public Integer getAccessId() {
        return accessId;
    }

    /**
     * Set access id.
     *
     * @param incAccessId
     *            - id
     */
    public void setAccessId(final Integer incAccessId) {
        this.accessId = incAccessId;
    }

    /**
     * Set role id.
     *
     * @param incRoleId
     *            - id
     */
    public void setRoleId(final int incRoleId) {
        this.roleId = incRoleId;
    }

    /**
     * Role id.
     *
     * @return id
     */
    public int getRoleId() {
        return this.roleId;
    }

    /**
     * Access name.
     *
     * @param incAccessNm
     *            - name
     */
    public void setAccessNm(final String incAccessNm) {
        this.accessNm = incAccessNm;
    }

    /**
     * Access name.
     *
     * @return name
     */
    public String getAccessNm() {
        return this.accessNm;
    }

    /**
     * Role name.
     *
     * @param incRoleNm
     *            - name
     */
    public void setRoleNm(final String incRoleNm) {
        this.roleNm = incRoleNm;
    }

    /**
     * Role name.
     *
     * @return name
     */
    public String getRoleNm() {
        return this.roleNm;
    }

    @Override
    public String getName() {
        return accessNm + "_" + roleNm;
    }

    @Override
    public void setName(final String incName) {
        // doit
    }

}
