package org.jboss.pnc.jenkinsbuilddriver;

import java.io.IOException;
import java.util.logging.Logger;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-29.
 */
class BuildJob {
    private JenkinsServer jenkinsServer;
    private BuildConfiguration buildConfiguration;
    private BuildJobConfig buildJobConfig;
    private JobWithDetails job;
    private int buildNumber;
    
    private static final Logger log = Logger.getLogger(BuildJob.class.getName());

    public BuildJob(JenkinsServer jenkinsServer, BuildConfiguration buildConfiguration) {
        this.jenkinsServer = jenkinsServer;
        this.buildConfiguration = buildConfiguration;
    }

    public boolean configure(RunningEnvironment runningEnvironment, boolean override) throws BuildDriverException {
        String jobName = getJobName();

        this.buildJobConfig = new BuildJobConfig(
                jobName,
                buildConfiguration.getScmRepoURL(),
                buildConfiguration.getScmRevision(),
                buildConfiguration.getBuildScript());

        try {
            job = jenkinsServer.getJob(jobName);
        } catch (IOException e) {
            // mnovotny: no job exist so throwing exception is effectively stopping configuration 
            //throw new BuildDriverException("Cannot check for existing job.", e);           
        }

        try {
            if (job != null) {
                if (override) {
                    jenkinsServer.updateJob(jobName, buildJobConfig.getXml(), false);
                } else {
                    throw new BuildDriverException("Cannot update existing job without 'override=true'."); 
                }
            } else {
                log.info("job config:\n" + buildJobConfig.getXml());
                jenkinsServer.createJob(jobName, buildJobConfig.getXml(), false);
            }
        } catch (IOException e) {
            throw new BuildDriverException("Cannot create/update job.", e);
        }
        try {
            job = jenkinsServer.getJob(jobName);
        } catch (IOException e) {
            throw new BuildDriverException("Cannot retrieve just created job.", e);
        }
        return job != null;
    }

    public String getJobName() {
        return buildConfiguration.getName();
    }

    public int start() throws BuildDriverException {
        //TODO check if configured
        buildNumber = -1;
        try {
            buildNumber = job.getNextBuildNumber();
            job.build(false);
        } catch (IOException e) {
            throw new BuildDriverException("Cannot start project build.", e);
        }

        return buildNumber;
    }

    public int getBuildNumber() {
        return buildNumber;
    }
}
