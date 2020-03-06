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
package org.webcurator.core.permissionmapping;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.query.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.webcurator.domain.model.core.Permission;
import org.webcurator.domain.model.core.Site;
import org.webcurator.domain.model.permissionmapping.Mapping;
import org.webcurator.domain.model.permissionmapping.MappingView;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * Implementation of the Hierarchical Permission Mapping DAO.
 *
 * @author bbeaumont
 * @see org.webcurator.core.permissionmapping.HierPermMappingDAO
 */
@SuppressWarnings({"rawtypes","unchecked"})
@Transactional
public class HierPermMappingDAOImpl extends HibernateDaoSupport implements HierPermMappingDAO {
    /**
     * The logger for this class
     */
    private final static Log log = LogFactory.getLog(HierPermMappingDAOImpl.class);

    /**
     * The transaction template to use.
     */
    private TransactionTemplate txTemplate = null;

    /* (non-Javadoc)
     * @see org.webcurator.core.permissionmapping.HierPermMappingDAO#saveOrUpdate(org.webcurator.domain.model.permissionmapping.Mapping)
     */
    public void saveOrUpdate(final Mapping aMapping) {
        // A Mapping can only be saved if it references real objects.
        if (aMapping.getOid() == null ||
                aMapping.getOid() == null) {
            return;
        }

        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {
                            //log.debug("Before Saving of Target");
                            currentSession().saveOrUpdate(aMapping);
                            //log.debug("After Saving Target");
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );

    }

    /**
     * Atomic delete according to permission and urlPattern
     * @param permissionId
     * @param urlPatternId
     */
    private void delete(final Long permissionId, final Long urlPatternId) {
        // A Mapping can only be deleted if it references real objects.
        if (permissionId == null || urlPatternId == null) {
            return;
        }

        // Run the deletion.
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {

                            Query q = currentSession().getNamedQuery(Mapping.DELETE);
                            q.setParameter("urlPatternId", urlPatternId);
                            q.setParameter("permissionId", permissionId);

                            //log.debug("Before Deleting Mappings");

                            int rowsAffected = q.executeUpdate();

                            log.debug("After Deleting Mappings: " + rowsAffected);
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.permissionmapping.HierPermMappingDAO#delete(org.webcurator.domain.model.permissionmapping.Mapping)
     */
    public void delete(final Mapping mapping) {
        this.delete(mapping.getPermissionId(), mapping.getUrlPatternId());
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.permissionmapping.HierPermMappingDAO#deleteMappings(org.webcurator.domain.model.core.Site)
     */
    public void deleteMappings(final Site site) {
        Query query=currentSession().createNamedQuery(Permission.QUERY_BY_SITE_ID,Permission.class);
        query.setParameter("siteId",site.getOid());
        List<Permission> permissions=query.getResultList();
        permissions.forEach(permission->{
            deleteMappings(permission);
        });
    }

    public void deleteMappings(final Permission permission){
        if(permission==null||permission.getOid()==null){
            log.warn("Permission is not existing");
            return;
        }

        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {

                            Query q = currentSession().getNamedQuery(Mapping.DELETE_BY_PERMISSION);
                            q.setParameter("permissionId", permission.getOid());

                            //log.debug("Before Deleting Mappings");

                            int rowsAffected = q.executeUpdate();

                            log.debug("After Deleting Mappings: " + rowsAffected);
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.permissionmapping.HierPermMappingDAO#getMapping(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    public List<Mapping> getMapping(Long mappingOid) {
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(Mapping.QUERY_BY_OID)
                        .setParameter(1, mappingOid)
                        .list());
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.permissionmapping.HierPermMappingDAO#getMappings(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<Mapping> getMappings(String domain) {
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(Mapping.QUERY_BY_DOMAIN)
                        .setParameter(1, domain)
                        .list());
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.permissionmapping.HierPermMappingDAO#getMappingsView(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<MappingView> getMappingsView(String domain) {
        return getHibernateTemplate().execute(session ->
                session.getNamedQuery(MappingView.QUERY_BY_DOMAIN)
                        .setParameter(1, domain)
                        .list());
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.permissionmapping.HierPermMappingDAO#updateMappings(org.webcurator.domain.model.core.Site, java.util.Set)
     */
    public void updateMappings(final Site aSite, final Set<Mapping> newMappings) {
        log.debug("Into updateMappings method");
        // Run the deletion.
        txTemplate.execute(
                new TransactionCallback() {
                    @SuppressWarnings("unchecked")
                    public Object doInTransaction(TransactionStatus ts) {
                        try {

                            CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
                            Criteria query = (Criteria) criteriaBuilder.createQuery(Mapping.class);
                            query.createCriteria("permission")
                                    .createCriteria("site")
                                    .add(Restrictions.eq("oid", aSite.getOid()));

                            List<Mapping> mappings = query.list();

                            for (Mapping m : mappings) {
                                if (!newMappings.contains(m)) {
                                    log.debug("Deleting: " + m.getOid());
                                    currentSession().delete(m);
                                } else {
                                    log.debug("Keeping: " + m.getOid());
                                    newMappings.remove(m);
                                }
                            }

                            for (Mapping m : newMappings) {
                                currentSession().save(m);
                            }
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );
    }


    /**
     * Save all of the mappings.
     *
     * @param mappings The mappings to save.
     */
    public void saveMappings(final List<Mapping> mappings) {
        txTemplate.execute(
                new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus ts) {
                        try {

                            for (Mapping m : mappings) {
                                currentSession().saveOrUpdate(m);
                            }
                        } catch (Exception ex) {
                            log.debug("Setting Rollback Only", ex);
                            ts.setRollbackOnly();
                        }
                        return null;
                    }
                }
        );

        // Clear the session to evict anything
        // that we don't want hanging around.
//        currentSession().clear();
    }


    /**
     * Set the template.
     *
     * @param txTemplate The txTemplate to set.
     */
    public void setTxTemplate(TransactionTemplate txTemplate) {
        this.txTemplate = txTemplate;
    }

}
