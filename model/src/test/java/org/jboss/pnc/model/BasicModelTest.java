package org.jboss.pnc.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class BasicModelTest {

    private static EntityManagerFactory emFactory;

    @BeforeClass
    public static void initEntityManagerFactory() {
        emFactory = Persistence.createEntityManagerFactory("newcastle-test");
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        emFactory.close();
    }

    /**
     * Clean up all the tables after each test run
     */
    @After
    public void cleanupDatabaseTables() {

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery("delete from ProductRelease").executeUpdate();
            em.createNativeQuery("delete from ProductMilestone").executeUpdate();
            em.createNativeQuery("delete from ProductVersion").executeUpdate();
            em.createNativeQuery("delete from Product").executeUpdate();
            em.createNativeQuery("delete from BuildRecordSet").executeUpdate();
            em.createNativeQuery("delete from BuildConfiguration_aud").executeUpdate();
            em.createNativeQuery("delete from BuildConfiguration").executeUpdate();
            em.createNativeQuery("delete from Project").executeUpdate();
            em.createNativeQuery("delete from Environment").executeUpdate();
            em.createNativeQuery("delete from License").executeUpdate();
            tx.commit();
        
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test
    public void testInsertProduct() throws Exception {
        
        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(ModelTestDataFactory.getInstance().getProduct1());
            tx.commit();

            Query rowCountQuery = em.createQuery("select count(*) from Product product");
            Long count = (Long) rowCountQuery.getSingleResult();
            Assert.assertEquals(1, count.longValue());
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }

    }

    @Test
    public void testInsertProductVersions() throws Exception {

        Product product1 = ModelTestDataFactory.getInstance().getProduct1();
        ProductVersion productVersion1 = ModelTestDataFactory.getInstance().getProductVersion1();
        productVersion1.setProduct(product1);
        BuildRecordSet buildRecordSet1 = ModelTestDataFactory.getInstance().getBuildRecordSet();
        ProductMilestone productMilestone1 = ModelTestDataFactory.getInstance().getProductMilestone1version1();
        productMilestone1.setProductVersion(productVersion1);
        productMilestone1.setPerformedBuildRecordSet(buildRecordSet1);
        BuildRecordSet buildRecordSet2 = ModelTestDataFactory.getInstance().getBuildRecordSet();
        ProductRelease productRelease1 = ModelTestDataFactory.getInstance().getProductRelease1();
        productRelease1.setProductVersion(productVersion1);
        productRelease1.setProductMilestone(productMilestone1);

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(product1);
            em.persist(productVersion1);
            em.persist(buildRecordSet1);
            em.persist(productMilestone1);
            em.persist(buildRecordSet2);
            em.persist(productRelease1);
            tx.commit();

            Query rowCountQuery = em.createQuery("select count(*) from ProductRelease product_release");
            Long count = (Long) rowCountQuery.getSingleResult();
            Assert.assertEquals(1, count.longValue());
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test
    public void testInsertProjects() throws Exception {

        License licenseApache20 = ModelTestDataFactory.getInstance().getLicenseApache20();
        License licenseGPLv3 = ModelTestDataFactory.getInstance().getLicenseGPLv3();
        Project project1 = ModelTestDataFactory.getInstance().getProject1();
        project1.setLicense(licenseApache20);
        Project project2 = ModelTestDataFactory.getInstance().getProject2();
        project2.setLicense(licenseGPLv3);

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(licenseApache20);
            em.persist(project1);
            em.persist(licenseGPLv3);
            em.persist(project2);
            tx.commit();

            Query rowCountQuery = em.createQuery("select count(*) from Project project");
            Long count = (Long) rowCountQuery.getSingleResult();
            Assert.assertEquals(2, count.longValue());
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test
    public void testInsertBuildConfigurationAudited() throws Exception {

        License licenseApache20 = ModelTestDataFactory.getInstance().getLicenseApache20();
        Project project1 = ModelTestDataFactory.getInstance().getProject1();
        project1.setLicense(licenseApache20);
        Environment environmentDefault = ModelTestDataFactory.getInstance().getEnvironmentDefault();
        BuildConfiguration buildConfiguration1 = ModelTestDataFactory.getInstance().getBuildConfiguration1();
        buildConfiguration1.setProject(project1);
        buildConfiguration1.setEnvironment(environmentDefault);

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx1 = em.getTransaction();
        EntityTransaction tx2 = em.getTransaction();

        try {
            tx1.begin();
            em.persist(licenseApache20);
            em.persist(environmentDefault);
            em.persist(project1);
            em.persist(buildConfiguration1);
            tx1.commit();

            tx2.begin();
            buildConfiguration1 = em.find(BuildConfiguration.class, buildConfiguration1.getId());
            buildConfiguration1.setDescription("Updated build config description");
            em.merge(buildConfiguration1);;
            tx2.commit();

            Query rowCountQuery = em.createQuery("select count(*) from BuildConfigurationAudited bca where id=" + buildConfiguration1.getId());
            Long count = (Long) rowCountQuery.getSingleResult();
            // Should have 2 audit records, 1 for insert, and 1 for update
            Assert.assertEquals(2, count.longValue());
            
        } catch (RuntimeException e) {
            if (tx1 != null && tx1.isActive()) {
                tx1.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test(expected=RollbackException.class)
    public void testProjectInsertConstraintFailure() throws Exception {
        
        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            // Expect this to fail because of missing license foreign key
            em.persist(ModelTestDataFactory.getInstance().getProject1());
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }

    }


}
