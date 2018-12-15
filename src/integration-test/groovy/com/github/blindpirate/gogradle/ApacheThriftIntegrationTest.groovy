package com.github.blindpirate.gogradle

import com.github.blindpirate.gogradle.support.AccessWeb
import com.github.blindpirate.gogradle.support.IntegrationTestSupport
import com.github.blindpirate.gogradle.support.WithMockGo
import com.github.blindpirate.gogradle.support.WithResource
import com.github.blindpirate.gogradle.task.GolangTaskContainer
import com.github.blindpirate.gogradle.util.IOUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@WithResource
@RunWith(GogradleRunner)
@WithMockGo
@AccessWeb
class ApacheThriftIntegrationTest extends IntegrationTestSupport {
    @Before
    void setUp() {
        writeBuildAndSettingsDotGradle(buildDotGradleBase)
        IOUtils.write(resource, 'a.go', '''
package main 
import "git.apache.org/thrift.git/lib/go/thrift"
func Say(s string){
    fmt.Println(s)
}
''')
    }

    @Test
    void 'depending on thrift should succeed'() {
        newBuild(GolangTaskContainer.DEPENDENCIES_TASK_NAME)
        assert stdout.toString().contains('''\
github.com/my/project
\\-- git.apache.org/thrift.git''')
    }

    @Override
    File getProjectRoot() {
        return resource
    }
}
