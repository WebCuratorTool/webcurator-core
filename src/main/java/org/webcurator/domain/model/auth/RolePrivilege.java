/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.domain.model.auth;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The RolePrivilege class holds all the system privileges that allow
 * access to WCT functions. Privileges are assigned to users via the
 * users role.
 * 
 * A RolePrivilege is a mapping from Role to Privilege with an attached
 * scope that defines how widely the privilege can be used. 
 * 
 * All privilege strings are defined in the Privilege class.
 * 
 * @see org.webcurator.domain.model.auth.Privilege
 * 
 * @author bprice
 */
// lazy="true"
@Entity
@Table(name = "ROLE_PRIVILEGE")
@NamedQuery(name = "org.webcurator.domain.model.auth.RolePrivilege.getUserPrivileges",
        query = "SELECT distinct rolpriv FROM RolePrivilege rolpriv JOIN rolpriv.role AS rolePrivRole JOIN rolePrivRole.users AS rolePrivRoleUsers WHERE rolePrivRoleUsers.username=?")
public class RolePrivilege implements Serializable {

	/** Version ID for serialization */
    private static final long serialVersionUID = 8223039237243696910L;

    /** Query to get the privileges for a particular user */
    public static final String QRY_GET_USER_PRIVILEGES = "org.webcurator.domain.model.auth.RolePrivilege.getUserPrivileges";

    /** The database OID for the Role Privilege. */
    @Id
    @Column(name="PRV_OID", nullable =  false)
    // Note: From the Hibernate 4.2 documentation:
    // The Hibernate team has always felt such a construct as fundamentally wrong.
    // Try hard to fix your data model before using this feature.
    @TableGenerator(name = "SharedTableIdGenerator",
            table = "ID_GENERATOR",
            pkColumnName = "IG_TYPE",
            valueColumnName = "IG_VALUE",
            pkColumnValue = "RolePriv",
            allocationSize = 1) // 50 is the default
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
    private Long oid;
    /** The identifier of the privilege */
    @Column(name = "PRV_CODE", length = 40, nullable = false)
    private String privilege;
    /** The scope of the privilege - i.e. how widely the privilege applies */
    @Column(name = "PRV_SCOPE", nullable = false)
    private int privilegeScope;
    /** The role that this privilege belongs to. */
    @ManyToOne
    @JoinColumn(name = "PRV_ROLE_OID", foreignKey = @ForeignKey(name = "FK_PRIV_ROLE_OID"))
    private Role role;
    
    /**
     * get the privilege name
     * @return the name of the privilege
     */
    public String getPrivilege() {
        return privilege;
    }
    
    /**
     * Set the name of the privilege.
     * @param privilege The name of the privilege.
     */
    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }
    
    /**
     * gets the Role that this privilege belongs to
     * @return the Role object
     */
    public Role getRole() {
        return role;
    }
    
    /**
     * Set the role the privilege belongs to.
     * @param role The role the privilege belongs to.
     */
    public void setRole(Role role) {
        this.role = role;
    }
    
    /**
     * gets the Scope of this privilege
     * @return the scope
     */
    public int getPrivilegeScope() {
        return privilegeScope;
    }
    
    /**
     * Set the scope of this privilege.
     * @param privilegeScope The scope.
     */
    public void setPrivilegeScope(int privilegeScope) {
        this.privilegeScope = privilegeScope;
    }
    
    /**
     * gets the Primary key for the Privilege
     * @return the Oid
     */
    public Long getOid() {
        return oid;
    }

    /**
     * Hibernate method to set the OID of the RolePrivilege.
     * @param oid The new database OID.
     */
    public void setOid(Long oid) {
        this.oid = oid;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        RolePrivilege rolePriv = (RolePrivilege) obj;
        return this.toString().equals(rolePriv.toString());
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getPrivilege()+":"+this.getPrivilegeScope();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override 
    public int hashCode() {
    	//WARNING this doesn't consider the role property - likely a bug, but unsure how it's used so cannot
    	//change this without possibly breaking other things
        return toString().hashCode();
    }
}
