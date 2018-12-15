package com.github.blindpirate.gogradle.dependencyresolution

import com.github.blindpirate.gogradle.GogradleRunner
import com.github.blindpirate.gogradle.support.*
import com.github.blindpirate.gogradle.task.GolangTaskContainer
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(GogradleRunner)
@WithResource
@WithMockGo
@WithIsolatedUserhome
class VcsNotationResolutionIntegrationTest extends IntegrationTestSupport {
    GitServer gitServer = GitServer.newServer()

    File projectRoot
    File repoRoot

    @Before
    void setup() {
        repoRoot = IOUtils.mkdir(resource, 'repo')
        projectRoot = IOUtils.mkdir(resource, 'project')

        writeBuildAndSettingsDotGradle("""
${buildDotGradleBase}
dependencies {
    golang {
        build name: 'a', branch: 'feature', url: 'http://localhost:${GitServer.DEFAULT_PORT}/a'
    }
}

golang {
    packagePath = 'myPackage'
    globalCacheFor 0, SECONDS
}
""")
    }

    @After
    void cleanup() {
        gitServer.stop()
    }

    @Test
    void 'repository should be pull each time when branch specified in dependency'() {
        // given
        GitServer.createRepository(repoRoot, '1.go')
        gitServer.addRepo('a', repoRoot)
        gitServer.newBranch(repoRoot, 'feature')
        gitServer.start(GitServer.DEFAULT_PORT)

        // when
        newBuild(GolangTaskContainer.VENDOR_TASK_NAME)

        // then
        assert new File(projectRoot, 'vendor/a/1.go').exists()

        // when
        gitServer.addFileToRepository(repoRoot, '2.go')
        newBuild(GolangTaskContainer.VENDOR_TASK_NAME, '--rerun-tasks')

        // then
        assert new File(projectRoot, 'vendor/a/2.go').exists()

        // when
        // https://github.com/gogradle/gogradle/issues/184#issuecomment-355498314
        IOUtils.clearDirectory(new File(userhome, 'go/repo'))
        GitServer.git('checkout master', repoRoot)

        // then
        newBuild(GolangTaskContainer.CLEAN_TASK_NAME, GolangTaskContainer.VENDOR_TASK_NAME)
    }

    @Override
    File getProjectRoot() {
        return projectRoot
    }
}
