package org.webcurator.domain.model.core;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.GenericGenerator;
import org.webcurator.domain.model.auth.Agency;

import javax.validation.constraints.NotNull;
import javax.persistence.*;

/**
 * An <code>IndicatorCriteria</code> defines the tolerance and limit values
 * of an <code>IndicatorCriteria</code> and is not associated with any specific <code>TargetInstance</code>.<p>
 * It is used to generate an <code>IndicatorCriteria</code> after a harvest has run for a specfic <code>TargetInstance</code>.
 * 
 * @author twoods
 *
 */
// lazy="true"
@Entity
@Table(name = "INDICATOR_CRITERIA")
@NamedQueries({
        @NamedQuery(name = "org.webcurator.domain.model.core.IndicatorCriteria.getIndicatorCriterias",
                query = "SELECT ic FROM IndicatorCriteria ic ORDER BY ic_agc_oid, ic.name"),
        @NamedQuery(name = "org.webcurator.domain.model.core.IndicatorCriteria.getIndicatorCriteriasByAgency",
                query = "SELECT ic FROM IndicatorCriteria ic WHERE ic.agency.oid=?1 ORDER BY ic.name"),
        @NamedQuery(name = "org.webcurator.domain.model.core.IndicatorCriteria.getIndicatorCriteriaByOid",
                query = "SELECT ic FROM IndicatorCriteria ic WHERE ic_oid=?1")
})
public class IndicatorCriteria {
	
	/** Query key for retrieving all reason objects */
    public static final String QRY_GET_INDICATOR_CRITERIAS = "org.webcurator.domain.model.core.IndicatorCriteria.getIndicatorCriterias";
	/** Query key for retrieving reason objects by agency OID */
    public static final String QRY_GET_INDICATOR_CRITERIAS_BY_AGENCY = "org.webcurator.domain.model.core.IndicatorCriteria.getIndicatorCriteriasByAgency";
	/** Query key for retrieving a reason objects by oid*/
    public static final String QRY_GET_INDICATOR_CRITERIA_BY_OID = "org.webcurator.domain.model.core.IndicatorCriteria.getIndicatorCriteriaByOid";
    
	/** unique identifier **/
    @Id
    @NotNull
    @Column(name="IC_OID")
    // Note: From the Hibernate 4.2 documentation:
    // The Hibernate team has always felt such a construct as fundamentally wrong.
    // Try hard to fix your data model before using this feature.
    @TableGenerator(name = "SharedTableIdGenerator",
            table = "ID_GENERATOR",
            pkColumnName = "IG_TYPE",
            valueColumnName = "IG_VALUE",
            pkColumnValue = "General",
            allocationSize = 1) // 50 is the default
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SharedTableIdGenerator")
	private Long oid;
	
	/** The name of the <code>IndicatorCriteria</code> that will be displayed **/
	@NotNull
	@Column(name = "IC_NAME")
	private String name;
	
	/** An explanation of what the <code>IndicatorCriteria</code> represents **/
	@Column(name = "IC_DESCRIPTION")
	String description;
	
	/** The upper limit set for this <code>IndicatorCriteria</code> as a percentage (eg: +10%) **/
	@Column(name = "IC_UPPER_LIMIT_PERCENTAGE")
	private Float upperLimitPercentage;

	/** The lower limit set for this <code>IndicatorCriteria</code> as a percentage (eg: -10%) **/
	@Column(name = "IC_LOWER_LIMIT_PERCENTAGE")
	private Float lowerLimitPercentage;
	
	/** The upper limit set for this <code>IndicatorCriteria</code> as a floating point number (some <code>Indicators</code> do not have associated percentage limits) **/
	@Column(name = "IC_UPPER_LIMIT")
	private Float upperLimit;

	/** The lower limit set for this <code>IndicatorCriteria</code> as a floating point number (some <code>Indicators</code> do not have associated percentage limits) **/
	@Column(name = "IC_LOWER_LIMIT")
	private Float lowerLimit;
	
    /** The agency the <code>IndicatorCriteria</code> belongs to */
    @ManyToOne
    @NotNull
    @JoinColumn(name = "IC_AGC_OID")
    private Agency agency;
    
	/**
	 * The unit of measurement used for the <code>IndicatorCriteria</code>.
	 */
	@NotNull
	@Column(name = "IC_UNIT")
	private String unit = null;
	
	/**
	 * Display the delta between the reference crawl and the <code>TargetInstance</code> in the UI
	 */
	@NotNull
	@Column(name = "IC_SHOW_DELTA")
	private Boolean showDelta = false;
	
	/**
	 * Display the indicator report if generated by the rules in the UI
	 */
	@NotNull
	@Column(name = "IC_ENABLE_REPORT")
	private Boolean enableReport = false;
	
	/**
	 * <code>Set</code> of measurement units defined for an <code>IndicatorCriteria</code>
	 */
	public static transient Set<String> UNITS;
	
	public IndicatorCriteria(){
		// add the measurement units
		UNITS = new HashSet<String>();
		UNITS.add("byte");
		UNITS.add("integer");
		UNITS.add("millisecond");
	}
	
	/**
	 * Get the database OID of the <code>IndicatorCriteria</code>.
	 * @return the primary key
	 */
	public Long getOid() {
		return oid;
	}
	
	/**
	 * Set the database oid of the <code>IndicatorCriteria</code>.
	 * @param oid The new database oid.
	 */
	public void setOid(Long oid) {
		this.oid = oid;
	}
	
    /**
     * Gets the name of the <code>IndicatorCriteria</code>.
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the <code>IndicatorCriteria</code>.
     * @param name The new name for the <code>IndicatorCriteria</code>.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the description of the <code>IndicatorCriteria</code>.
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the <code>IndicatorCriteria</code>.
     * @param name The new description for the <code>IndicatorCriteria</code>.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the upper limit percentage value of the <code>IndicatorCriteria</code>.
     * @return Returns the upper limit percentage value.
     */
    public Float getUpperLimitPercentage() {
        return upperLimitPercentage;
    }

    /**
     * Sets the upper limit percentage value of the <code>IndicatorCriteria</code>.
     * @param name The new upper limit percentage value for the <code>IndicatorCriteria</code>.
     */
    public void setUpperLimitPercentage(Float floatValue) {
        this.upperLimitPercentage = floatValue;
    }
    
    /**
     * Gets the lower limit percentage value of the <code>IndicatorCriteria</code>.
     * @return Returns the lower limit percentage value.
     */
    public Float getLowerLimitPercentage() {
        return lowerLimitPercentage;
    }

    /**
     * Sets the lower limit percentage value of the <code>IndicatorCriteria</code>.
     * @param name The new lower limit percentage value for the <code>IndicatorCriteria</code>.
     */
    public void setLowerLimitPercentage(Float floatValue) {
        this.lowerLimitPercentage = floatValue;
    }
    
    /**
     * Gets the upper limit value of the <code>IndicatorCriteria</code>.
     * @return Returns the upper limit value.
     */
    public Float getUpperLimit() {
        return upperLimit;
    }
    
    /**
     * Sets the upper limit value of the <code>IndicatorCriteria</code>.
     * @param name The new upper limit value for the <code>IndicatorCriteria</code>.
     */
    public void setUpperLimit(Float floatValue) {
        this.upperLimit = floatValue;
    }

    /**
     * Gets the lower limit value of the <code>IndicatorCriteria</code>.
     * @return Returns the lower limit value.
     */
    public Float getLowerLimit() {
        return lowerLimit;
    }

    /**
     * Sets the lower limit value of the <code>IndicatorCriteria</code>.
     * @param name The new lower limit value for the <code>IndicatorCriteria</code>.
     */
    public void setLowerLimit(Float floatValue) {
        this.lowerLimit = floatValue;
    }
    
    /**
     * gets the Agency to which this <code>IndicatorCriteria</code> belongs. 
     * @return the Agency object
     */
    public Agency getAgency() {
        return agency;
    }

    /**
     * Set the agency which can use this <code>IndicatorCriteria</code>.
     * @param agency The agency that can use this <code>IndicatorCriteria</code>.
     */
    public void setAgency(Agency agency) {
        this.agency = agency;
    }
    
    /**
     * fetches the <code>IndicatorCriteria</code>s unit of measurement
     * @return the unit of measurement (eg: integer, millisecond, byte)
     */
    public String getUnit() {
    	return unit;
    }
    
    /**
     * sets the unit of measurement for this <code>IndicatorCriteria</code> (eg: integer, millisecond, byte)
     */
    public void setUnit(String unit) {
    	this.unit = unit;
    }
    
    /**
     * fetches the <code>IndicatorCriteria</code>s delta visibility
     * @return true if the delta for the <code>IndicatorCriteria</code> should be displayed in the UI, false otherwise
     */
    public Boolean getShowDelta() {
    	return showDelta;
    }
    
    /**
     * sets the delta display visibility for this <code>IndicatorCriteria</code> 
     */
    public void setShowDelta(Boolean showDelta) {
    	this.showDelta = showDelta;
    }
    
    /**
     * fetches the visibility of any indicator report associated with this indicator (report is generated in the rules)
     * @return true if the indicator report should be displayed in the UI, false otherwise
     */
    public Boolean getEnableReport() {
    	return enableReport;
    }
    
    /**
     * sets the indicator report display visibility for this <code>IndicatorCriteria</code> 
     */
    public void setEnableReport(Boolean enableReport) {
    	this.enableReport = enableReport;
    }
    
    /**
     * return the string value for this indicator criteria
     */
    public final String toString() {
    	StringBuilder stringValue = new StringBuilder("oid=");
    	stringValue.append(getOid());
    	stringValue.append(":name="); 
    	stringValue.append(getName());
    	stringValue.append(":agency=");
    	stringValue.append(getAgency());
    	stringValue.append(":lowerLimit="); 
    	stringValue.append(getLowerLimit());
    	stringValue.append(":upperLimit=");
    	stringValue.append(getUpperLimit());
    	stringValue.append(":lowerLimitPercentage="); 
    	stringValue.append(getLowerLimitPercentage());
    	stringValue.append(":upperLimitPercentage="); 
    	stringValue.append(getUpperLimitPercentage());
    	stringValue.append(":unit=");
    	stringValue.append(getUnit());
    	
    	return stringValue.toString();
    }
}
