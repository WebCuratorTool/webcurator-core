package org.webcurator.core.harvester.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import java.util.List;

public class MockHarvestAgent implements HarvestAgent {

	private static Log log = LogFactory.getLog(MockHarvestAgent.class);
	private boolean memoryWarning = false;
	private String _profile = "";
	
	public void abort(String job) 
	{
		log.debug("abort - "+job);
	}

	public int completeHarvest(String job, int failureStep) {
		log.debug("complete harvest - "+job);
		return 0;
	}

	public String getName() {
		return "Test Agent";
	}

	public HarvestAgentStatusDTO getStatus() {
		HarvestAgentStatusDTO sdto = new HarvestAgentStatusDTO();
		sdto.setMemoryWarning(memoryWarning);
		return sdto;
	}

	public void initiateHarvest(String job, String profile, String seeds) 
	{
		_profile = profile;
		log.debug("initiate harvest - "+job);
	}

	@Override
	public void recoverHarvests(List<String> activeJobs){}

	public void loadSettings(String job)
	{
		log.debug("load settings - "+job);
	}

	public void pause(String job) 
	{
		log.debug("pause - "+job);
	}

	public void pauseAll() {
		log.debug("pause all");
	}

	public void purgeAbortedTargetInstances(List<String> targetInstanceNames) {
	    targetInstanceNames.forEach(name -> log.debug("purge aborted ti - " + name));
	}

	public void restrictBandwidth(String job, int bandwidthLimit) 
	{
		log.debug("restrict bandwidth of "+job+" to "+bandwidthLimit);
	}

	public void resume(String job) {
		log.debug("resume - "+job);
	}

	public void resumeAll() {
		log.debug("resume all");
	}

	public void stop(String job) {
		log.debug("stop - "+job);
	}

	public void updateProfileOverrides(String job, String profile) {
		log.debug("update profile overrides - "+job);
	}

	public boolean getMemoryWarning() {
		return memoryWarning;
	}

	public void setMemoryWarning(boolean memoryWarning) {
		this.memoryWarning = memoryWarning;
	}
	
	public String getProfileString()
	{
		return _profile;
	}

	public boolean isValidProfile(String profile) {
		return false;
	}

	/**
	 * Execute the shell script in the Heritrix3 server for the job.
	 * @param jobName the job
	 * @param engine the script engine: beanshell, groovy, or nashorn (ECMAScript)
	 * @param shellScript the script to execute
	 * @return the script result
	 */
	public HarvestAgentScriptResult executeShellScript(String jobName, String engine, String shellScript) {
		return null;
	}
}
