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
package org.webcurator.domain.model.core;

import java.util.Date;
import java.lang.Comparable;

import org.hibernate.annotations.GenericGenerator;
import org.webcurator.domain.model.auth.User;

import javax.persistence.*;

/**
 * An audited note attached to an object.  
 * @author nwaight 
 */
// lazy="false"
@Entity
@Table(name = "ANNOTATIONS")
@NamedQuery(name = "org.webcurator.domain.model.core.Annotation.getNotesForObject",
		query="from org.webcurator.domain.model.core.Annotation an where an.objectType = :type and an.objectOid = :oid")
public class Annotation implements Comparable {
	/** The name of the get annotations for object query. */
	public static final String QRY_GET_NOTES = "org.webcurator.domain.model.core.Annotation.getNotesForObject";
	/** name of the object type query parameter. */
	public static final String PARAM_TYPE = "type";
	/** name of the object oid query parameter. */
	public static final String PARAM_OID = "oid";
	
	/** the primary key for the annotation. */
	@Id
	@Column(name="AN_OID", nullable =  false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MultipleHiLoPerTableGenerator")
	@GenericGenerator(name = "MultipleHiLoPerTableGenerator",
			strategy = "org.hibernate.id.MultipleHiLoPerTableGenerator",
			parameters = {
					@Parameter(name = "table", value = "ID_GENERATOR"),
					@Parameter(name = "primary_key_column", value = "IG_TYPE"),
					@Parameter(name = "value_column", value = "IG_VALUE"),
					@Parameter(name = "primary_key_value", value = "Annotation")
			})
	private Long oid;
	/** The date for the annotation was created. */
	@Column(name = "AN_DATE", columnDefinition = "TIMESTAMP(9)", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date date;
	/** the annotation text. */
	@Column(name = "AN_NOTE", length = 1000, nullable = false)
	private String note;
	/** the user that added the annotation. */
	@ManyToOne
	@JoinColumn(name = "AN_USER_OID", foreignKey = @ForeignKey(name = "FK_NOTE_USER_OID"), nullable = false)
	private User user;
	/** the type of the annotations parent object. */
	@Column(name = "AN_OBJ_TYPE", length = 500, nullable = false)
	private String objectType;
	/** the oid of the annotations parent object. */
	@Column(name = "AN_OBJ_OID", nullable = false)
	private Long objectOid;
	/** Is this annotation alertable */
	@Column(name = "AN_ALERTABLE")
	private boolean alertable;
	
	/**
	 * No-arg constructor.
	 */
	public Annotation() { 
	}
	
	/**
	 * Create a new annotation.
	 * @param date The date/time of the annotation.
	 * @param note The message string.
	 */
	public Annotation(Date date, String note) { 
		this.date = date;
		this.note = note;
	}
	
	/**
	 * Create a new annotation.
	 * @param aDate The date/time of the annotaiton.
	 * @param aNote The message string.
	 * @param aUser The user that created the annotation.
	 * @param aObjectOid The OID of the associated object.
	 * @param alertable
	 */
	public Annotation(Date aDate, String aNote, User aUser, Long aObjectOid, String aObjectType, boolean alertable) { 
		this.date = aDate;
		this.note = aNote;
		this.user = aUser;
		this.objectOid = aObjectOid;
		this.objectType = aObjectType;
		this.alertable = alertable;
	}
	
	/** 
	 * @return the date the annotation was created.
	 */
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * 
	 */
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the objectOid
	 */
	public Long getObjectOid() {
		return objectOid;
	}

	/**
	 * @param objectOid the objectOid to set
	 */
	public void setObjectOid(Long objectOid) {
		this.objectOid = objectOid;
	}

	/**
	 * @return the objectType
	 */
	public String getObjectType() {
		return objectType;
	}

	/**
	 * Set the object type of the annotation. This should be set to the 
	 * classname returned by <code>ClassName.class.getName()</code>.
	 * @param objectType the objectType to set
	 */
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	/**
	 * Returns true if this annotation is alertable.
	 * @return true if alertable; otherwise false.
	 */
	public boolean isAlertable() {
		return alertable;
	}

	/**
	 * Sets whether this is annotation is alertable.
	 * @param alertable true to make this the annotation alertable; otherwise false.
	 */
	public void setAlertable(boolean alertable) {
		this.alertable = alertable;
	}
	
	/**
	 * Retrieve the OID of the Annotation.
	 * @return the oid
	 */
	public Long getOid() {
		return oid;
	}

	/**
	 * Set the oid of the annotations.
	 * @param oid the oid to set
	 */
	public void setOid(Long oid) {
		this.oid = oid;
	}
	
	public int compareTo(Object other)
	{
		if(other != null)
		{
			if(other instanceof Annotation)
			{
				//sort newest first
				return ((Annotation)other).date.compareTo(this.date);
			}
			else			{
				throw new java.lang.IllegalArgumentException("Invalid comparison with type: "+other.getClass().getName());
			}
		}
		else
		{
			throw new java.lang.NullPointerException();
		}
		
	}
}
