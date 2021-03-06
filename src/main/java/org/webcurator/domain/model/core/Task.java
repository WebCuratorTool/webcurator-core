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

import org.hibernate.annotations.GenericGenerator;
import org.webcurator.domain.model.auth.Agency;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
import javax.persistence.*;

/**
 * A Task represents an action that must be taken by a user within the WCT system.
 * A Task can be assigned directly to a User of the system, or viewable by all Users with
 * a specified privilege.
 * @author bprice
 */
// lazy="false"
@Entity
@Table(name = "TASK")
public class Task {
    /** The database OID of the task */
    @Id
    @NotNull
    @Column(name="TSK_OID")
    // Note: From the Hibernate 4.2 documentation:
    // The Hibernate team has always felt such a construct as fundamentally wrong.
    // Try hard to fix your data model before using this feature.
    @TableGenerator(name = "SharedTableIdGenerator",
            table = "ID_GENERATOR",
            pkColumnName = "IG_TYPE",
            valueColumnName = "IG_VALUE",
            pkColumnValue = "Task",
            allocationSize = 1) // 50 is the default
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
    private Long oid;
    /** The sender of the task */
    @Size(max=80)
    @NotNull
    @Column(name = "TSK_SENDER")
    private String sender;
    /** The OID of the user the task is assigned to */
    @Column(name = "TSK_USR_OID")
    private Long assigneeOid;
    /** The date the task was sent */
    @NotNull
    @Column(name = "TSK_SENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentDate;
    /** The subject of the task */
    @Size(max=255)
    @NotNull
    @Column(name = "TSK_SUBJECT")
    private String subject;
    /** The message of the task */
    @Size(max=2000)
    @Column(name = "TSK_MESSAGE")
    private String message;
    /** The privilege required to complete the task */
    @Size(max=40)
    @Column(name = "TSK_PRIVILEGE")
    private String privilege;
    /** The agency the task should be visible to */
    @ManyToOne
    @NotNull
    @JoinColumn(name = "TSK_AGC_OID")
    private Agency agency;
    /** The database OID of the resource to which the task is related */
    @NotNull
    @Column(name = "TSK_RESOURCE_OID")
    private Long resourceOid;
    /** The classname of the resource to which the task is related */
    @Size(max=80)
    @NotNull
    @Column(name = "TSK_RESOURCE_TYPE")
    private String resourceType;
    /** The type of message */
    @Size(max=40)
    @NotNull
    @Column(name = "TSK_MSG_TYPE")
    private String messageType;
    
    /** The name of the owner of the task */
    private transient String owner;
    
    /**
     * No-arg constructor.
     */
    public Task() {
    }
    
    /**
     * gets the Assignee Oid for this task
     * @return the Asignee Oid of the Task
     */
    public Long getAssigneeOid() {
        return assigneeOid;
    }
    
    /** 
     * Sets the assignee of the task.
     * @param assigneeOid The OID of the User that has claimed the tsak.
     */
    public void setAssigneeOid(Long assigneeOid) {
        this.assigneeOid = assigneeOid;
    }
    
    /**
     * gets the Message to display
     * @return the Message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the message body.
     * @param message The message body.
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Gets the database OID of the task.
     * @return Returns the oid.
     */
    public Long getOid() {
        return oid;
    }
    
    /**
     * Sets the OID of the task.
     * @param oid The new database OID.
     */
    public void setOid(Long oid) {
        this.oid = oid;
    }
    
    /**
     * gets the Sender of the Task, this can be a User or a System component
     * @return the Sender name as a String
     */
    public String getSender() {
        return sender;
    }
    
    /**
     * Sets the send of the task.
     * @param sender The name of the send as a string.
     */
    public void setSender(String sender) {
        this.sender = sender;
    }
    
    /**
     * gets the Date and Time this Task was sent
     * @return the Date/Time of the Task
     */
    public Date getSentDate() {
        return sentDate;
    }
    
    /**
     * Sets the date the task was sent.
     * @param sentDate The date the task was sent.
     */
    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }
    
    /**
     * gets the Subject line of the Task, this is a summary of the
     * Task. For full details refer to the getMessage() method.
     * @return the Task subject
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Set the subject line of the task. This is a summary of the task.
     * @param subject The subject line.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * gets the Privilege for which this Task is appropriate
     * @return the Privilege code
     */
    public String getPrivilege() {
        return privilege;
    }

    /**
     * Set the privilege required to complete this task. This is used to 
     * determine which users should see which tasks.
     * @param privilege The privilege required to complete the task.
     */
    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    /**
     * gets the Agency for which this Task is appropriate. Usually used
     * in combination with the Privilege attribute to determine who can
     * see the Task within the Agency.
     * @return the Agency object
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Set the agency that should be able to see this task.
     * @param agency The agency that should be able to see this task.
     */
    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    /**
     * Get the owner of this task.
     * @return The name of the owner of the task.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Set the owner of this task.
     * @param owner The name of the owner of this task.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

	/**
	 * Gets the message type for this message.
	 * @see org.webcurator.core.notification.MessageType
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * Set the message type for this message.
	 * @see org.webcurator.core.notification.MessageType
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * Get the OID of the resource associated with this task.
	 * @return the resourceOid
	 */
	public Long getResourceOid() {
		return resourceOid;
	}

	/**
	 * Set the OID of the resource associated with this task.
	 * @param resourceOid the resourceOid to set
	 */
	public void setResourceOid(Long resourceOid) {
		this.resourceOid = resourceOid;
	}

	/**
	 * Get the classname of the resource related to this task.
	 * @return the resourceType
	 */
	public String getResourceType() {
		return resourceType;
	}

	/**
	 * Set the classname of the resource associated with this task.
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
}
