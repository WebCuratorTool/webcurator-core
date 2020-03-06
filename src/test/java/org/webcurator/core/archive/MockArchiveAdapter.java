package org.webcurator.core.archive;

import java.util.Map;

import org.webcurator.domain.model.core.TargetInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MockArchiveAdapter implements ArchiveAdapter {

	protected static Log log = LogFactory.getLog(MockArchiveAdapter.class);

	@SuppressWarnings("rawtypes")
	public void submitToArchive(TargetInstance instance, String sipXML, Map customDepositFormElements,
			int harvestNumber) throws Exception {
		if(log.isInfoEnabled())
		{
			log.info("Submitting "+instance.getOid()+" to archive - Harvest Number: "+harvestNumber);
		}
		if(log.isDebugEnabled())
		{
			log.debug("SIP: "+sipXML);
		}
	}

}
