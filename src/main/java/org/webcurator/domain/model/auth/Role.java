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

import java.io.Serializable;
import java.util.Set;

import org.hibernate.annotations.GenericGenerator;
import org.webcurator.domain.AgencyOwnable;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

/**
 * The Role class defines the relationship between users and privilege
 * within the WCT system. Users are assigned to roles, that in turn
 * have privileges. A user may belong to more than one role.
 * @author bprice
 */
// lazy="false"
@Entity
@Table(name = "WCTROLE")
@NamedQueries({
        @NamedQuery(name = "org.webcurator.domain.model.auth.Role.getRoles",
                query = "FROM Role rol order by rol.agency.name, rol.name"),
        @NamedQuery(name = "org.webcurator.domain.model.auth.Role.getAssociatedRolesByUser",
                query = "SELECT rol FROM Role rol, User usr JOIN usr.roles usrRoles WHERE usrRoles.oid = rol.oid AND usr.oid=?1 order by rol.name"),
        @NamedQuery(name = "org.webcurator.domain.model.auth.Role.getRolesByAgency",
                query = "SELECT rol FROM Role rol WHERE rol.agency.oid = ?1")
})
public class Role implements AgencyOwnable, Serializable{

   /** The query constant for retrieving an ordered list of roles */
   public final static String QRY_GET_ROLES = "org.webcurator.domain.model.auth.Role.getRoles";
   /** The query constant for retrieving an ordered lists of roles for a given user */
   public final static String QRY_GET_ASSOCIATED_ROLES_BY_USER = "org.webcurator.domain.model.auth.Role.getAssociatedRolesByUser";
   /** The query constant for retrieving an ordered list of roles for a given agency */
   public final static String QRY_GET_ROLES_BY_AGENCY ="org.webcurator.domain.model.auth.Role.getRolesByAgency";	
	
   /** The version ID for serialization */
   private static final long serialVersionUID = 3846098707837858936L;
   
   /** The database OID of the Role */
   @Id
   @NotNull
   @Column(name="ROL_OID")
   // Note: From the Hibernate 4.2 documentation:
   // The Hibernate team has always felt such a construct as fundamentally wrong.
   // Try hard to fix your data model before using this feature.
   @TableGenerator(name = "SharedTableIdGenerator",
           table = "ID_GENERATOR",
           pkColumnName = "IG_TYPE",
           valueColumnName = "IG_VALUE",
           pkColumnValue = "Role",
           allocationSize = 1) // 50 is the default
   @GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
   private Long oid;
   /** The name of the role */
   @Size(max=80)
   @NotNull
   @Column(name = "ROL_NAME")
   private String name;
   /** A descrption for the role */
   @Size(max=255)
   @Column(name = "ROL_DESCRIPTION")
   private String description;
   /** The set of Users that hold this role */
   @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
   @JoinTable(name = "USER_ROLE",
           joinColumns = { @JoinColumn(name = "URO_ROL_OID") },
           inverseJoinColumns = { @JoinColumn(name = "URO_USR_OID") })
   private Set<User> users;
   /** The set of privileges that this role is made up from. */
   @OneToMany(orphanRemoval = true, cascade = {CascadeType.ALL}, fetch=FetchType.LAZY)
   @JoinColumn(name = "PRV_ROLE_OID")
   private Set<RolePrivilege> rolePrivileges;
   /** The agency that this role belongs to */
   @ManyToOne
   @NotNull
   @JoinColumn(name = "ROL_AGENCY_OID")
   private Agency agency;


   /**
    * gets the display description of the role
    * @return the role description
    */
   public String getDescription() {
	   return description;
   }
   
   /**
    * Sets the description of the role.
    * @param description The description of the role.
    */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * gets the name of the Role
     * @return the Role name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the name of the role.
     * @param name The name of the role.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
    /**
     * gets the Set of Users that have this role
     * @return a set of Users
     */
    public Set<User> getUsers() {
        return users;
    }
    
    /**
     * Set the set of Users that have this role.
     * @param users The set of Users that have this role.
     */
    public void setUsers(Set<User> users) {
        this.users = users;
    }
    
    /**
     * get the primary key for the role
     * @return the role primary key
     */
    public Long getOid() {
        return oid;
    }
    
    /**
     * Hibernate method to set the OID of this object.
     * @param oid The new OID of the object.
     */
    public void setOid(Long oid) {
        this.oid = oid;
    }
    
    /**
     * Get the set of privileges that this role has access to.
     * @return gets the set of privileges for this role
     */
    public Set<RolePrivilege> getRolePrivileges() {
        return rolePrivileges;
    }
    
    /**
     * Set the set of privileges that this role has access to.
     * @param rolePrivileges The set of privileges that this role has access to.
     */
    public void setRolePrivileges(Set<RolePrivilege> rolePrivileges) {
    	//Hibernate uses it's own collections, so this should NEVER be used in objects that
    	//you expect to be managed by Hibernate.  Use clear/addRolePrivileges instead.
    	this.rolePrivileges = rolePrivileges;
    }

	public void clearRolePrivileges() {
		if(this.rolePrivileges!=null) {
			this.rolePrivileges.clear();
		}
	}

	public void addRolePrivileges(Set<RolePrivilege> selectedPrivScopes) {
		if(this.rolePrivileges==null) {
			this.rolePrivileges = selectedPrivScopes;
		} else {
	    	for(Object p:selectedPrivScopes.toArray()) {
	    		RolePrivilege privilege = (RolePrivilege)p;
	    		privilege.setRole(this);
	    		this.rolePrivileges.add(privilege);
	    	}
		}
	}
    
    /**
     * gets the Agency associated with this role
     * @return the Agency
     */
    public Agency getAgency() {
        return agency;
    }
    
    /**
     * Set the agency associated with this role.
     * @param agency The agency associated with this role.
     */
    public void setAgency(Agency agency) {
        this.agency = agency;
    }
       
    /**
     * Remove a User from this role.
     * @param user The user to remove.
     */
    public void removeUser(User user) {
        getUsers().remove(user);
    }
    
    /**
     * Get the agency that owns this role.
     * @return The agency that owns this role.
     */
    public Agency getOwningAgency() {
        return getAgency();
    }


}
