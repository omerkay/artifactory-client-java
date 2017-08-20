package org.jfrog.artifactory.client

import org.hamcrest.CoreMatchers
import org.jfrog.artifactory.client.model.RepositoryType
import org.jfrog.artifactory.client.model.impl.LocalRepoChecksumPolicyTypeImpl
import org.jfrog.artifactory.client.model.impl.RemoteRepoChecksumPolicyTypeImpl
import org.jfrog.artifactory.client.model.impl.RepositoryTypeImpl
import org.jfrog.artifactory.client.model.impl.SnapshotVersionBehaviorImpl
import org.jfrog.artifactory.client.model.repository.PomCleanupPolicy
import org.jfrog.artifactory.client.model.repository.settings.RepositorySettings
import org.jfrog.artifactory.client.model.repository.settings.impl.P2RepositorySettingsImpl
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

/**
 * test that client correctly sends and receives repository configuration with `p2` package type
 *
 * @author Ivan Vasylivskyi (ivanvas@jfrog.com)
 */
public class P2PackageTypeRepositoryTests extends BaseRepositoryTests {

    @Override
    RepositorySettings getRepositorySettings(RepositoryType repositoryType) {
        if (repositoryType == RepositoryTypeImpl.LOCAL) {
            return null
        }

        def settings = new P2RepositorySettingsImpl()

        settings.with {
            // local
            checksumPolicyType = LocalRepoChecksumPolicyTypeImpl.values()[rnd.nextInt(LocalRepoChecksumPolicyTypeImpl.values().length)]
            handleReleases = rnd.nextBoolean()
            handleSnapshots = rnd.nextBoolean()
            maxUniqueSnapshots = rnd.nextInt()
            snapshotVersionBehavior = SnapshotVersionBehaviorImpl.values()[rnd.nextInt(SnapshotVersionBehaviorImpl.values().length)]
            suppressPomConsistencyChecks = rnd.nextBoolean()

            // remote
            fetchJarsEagerly = rnd.nextBoolean()
            fetchSourcesEagerly = rnd.nextBoolean()
            listRemoteFolderItems = rnd.nextBoolean()
            rejectInvalidJars = rnd.nextBoolean()
            remoteRepoChecksumPolicyType = RemoteRepoChecksumPolicyTypeImpl.values()[rnd.nextInt(RemoteRepoChecksumPolicyTypeImpl.values().length)]

            // virtual
            keyPair // no key pairs configured
            pomRepositoryReferencesCleanupPolicy = PomCleanupPolicy.values()[rnd.nextInt(PomCleanupPolicy.values().length)]
        }

        return settings
    }

    @BeforeMethod
    protected void setUp() {
        // only remote ant virtual repository is supported
        prepareLocalRepo = false

        super.setUp()
    }

    @Test(groups = "p2PackageTypeRepo")
    public void testP2RemoteRepo() {
        artifactory.repositories().create(0, remoteRepo)
        def expectedSettings = remoteRepo.repositorySettings

        def resp = artifactory.repository(remoteRepo.getKey()).get()
        resp.getRepositorySettings().with {
            assertThat(packageType, CoreMatchers.is(expectedSettings.getPackageType()))

            // local
            assertThat(checksumPolicyType, CoreMatchers.nullValue())
            assertThat(handleReleases, CoreMatchers.is(expectedSettings.getHandleReleases())) // always in resp payload
            assertThat(maxUniqueSnapshots, CoreMatchers.is(expectedSettings.getMaxUniqueSnapshots())) // always in resp payload
            assertThat(maxUniqueSnapshots, CoreMatchers.is(expectedSettings.getMaxUniqueSnapshots()))  // always in resp payload
            assertThat(snapshotVersionBehavior, CoreMatchers.nullValue())
            assertThat(suppressPomConsistencyChecks, CoreMatchers.is(expectedSettings.getSuppressPomConsistencyChecks()))
            // always sent by artifactory

            // remote
            assertThat(fetchJarsEagerly, CoreMatchers.is(expectedSettings.getFetchJarsEagerly()))
            assertThat(fetchSourcesEagerly, CoreMatchers.is(expectedSettings.getFetchSourcesEagerly()))
            assertThat(listRemoteFolderItems, CoreMatchers.is(expectedSettings.getListRemoteFolderItems()))
            assertThat(rejectInvalidJars, CoreMatchers.is(expectedSettings.getRejectInvalidJars()))
            // TODO: property is not returned by the artifactory, asserting on default value
            // assertThat(remoteRepoChecksumPolicyType, CoreMatchers.is(specRepo.getRemoteRepoChecksumPolicyType()))
            assertThat(remoteRepoChecksumPolicyType, CoreMatchers.is(RemoteRepoChecksumPolicyTypeImpl.generate_if_absent))

            // virtual
            assertThat(keyPair, CoreMatchers.nullValue())
            assertThat(pomRepositoryReferencesCleanupPolicy, CoreMatchers.nullValue())
        }
    }

    @Test(groups = "p2PackageTypeRepo")
    public void testP2VirtualRepo() {
        artifactory.repositories().create(0, virtualRepo)
        def expectedSettings = virtualRepo.repositorySettings

        def resp = artifactory.repository(virtualRepo.getKey()).get()
        resp.getRepositorySettings().with {
            assertThat(packageType, CoreMatchers.is(expectedSettings.getPackageType()))

            // local
            assertThat(checksumPolicyType, CoreMatchers.nullValue())
            assertThat(handleReleases, CoreMatchers.nullValue())
            assertThat(handleSnapshots, CoreMatchers.nullValue())
            assertThat(maxUniqueSnapshots, CoreMatchers.nullValue())
            assertThat(snapshotVersionBehavior, CoreMatchers.nullValue())
            assertThat(suppressPomConsistencyChecks, CoreMatchers.nullValue())

            // remote
            assertThat(fetchJarsEagerly, CoreMatchers.nullValue())
            assertThat(fetchSourcesEagerly, CoreMatchers.nullValue())
            assertThat(listRemoteFolderItems, CoreMatchers.nullValue())
            assertThat(rejectInvalidJars, CoreMatchers.nullValue())
            assertThat(remoteRepoChecksumPolicyType, CoreMatchers.nullValue())

            // virtual
            assertThat(keyPair, CoreMatchers.is('')) // empty = keyPair is not set
            assertThat(pomRepositoryReferencesCleanupPolicy, CoreMatchers.is(expectedSettings.getPomRepositoryReferencesCleanupPolicy()))
            // always sent by artifactory
        }
    }
}
