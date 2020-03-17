def UPSTREAM_PROJECTS_LIST = [ "Mule-runtime/grizzly-ahc/1_14-mule" ]

Map pipelineParams = [ "upstreamProjects" : UPSTREAM_PROJECTS_LIST.join(','),
                       "devBranchesRegex" : "2.3.x-mule",
                       "projectType" : "Runtime" ]

runtimeBuild(pipelineParams)
