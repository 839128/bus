/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.hooks.web;

import java.io.Serial;
import java.util.Date;

import org.miaixz.bus.gitlab.support.JacksonJson;

public class JobEvent extends AbstractEvent {

    @Serial
    private static final long serialVersionUID = 2852232651592L;

    public static final String JOB_HOOK_X_GITLAB_EVENT = "Job Hook";
    public static final String OBJECT_KIND = "job";

    private String ref;
    private Boolean tag;
    private String beforeSha;
    private String sha;
    private Long jobId;
    private String jobName;
    private String jobStage;
    private String jobStatus;
    private Date jobStartedAt;
    private Date jobFinishedAt;
    private Integer jobDuration;
    private Boolean jobAllowFailure;
    private String jobFailureReason;
    private Long projectId;
    private String projectName;
    private EventUser user;
    private BuildCommit commit;
    private EventRepository repository;

    @Override
    public String getObjectKind() {
        return (OBJECT_KIND);
    }

    public void setObjectKind(String objectKind) {
        if (!OBJECT_KIND.equals(objectKind))
            throw new RuntimeException("Invalid object_kind (" + objectKind + "), must be '" + OBJECT_KIND + "'");
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Boolean getTag() {
        return tag;
    }

    public void setTag(Boolean tag) {
        this.tag = tag;
    }

    public String getBeforeSha() {
        return beforeSha;
    }

    public void setBeforeSha(String beforeSha) {
        this.beforeSha = beforeSha;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobStage() {
        return jobStage;
    }

    public void setJobStage(String jobStage) {
        this.jobStage = jobStage;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public Date getJobStartedAt() {
        return jobStartedAt;
    }

    public void setJobStartedAt(Date jobStartedAt) {
        this.jobStartedAt = jobStartedAt;
    }

    public Date getJobFinishedAt() {
        return jobFinishedAt;
    }

    public void setJobFinishedAt(Date jobFinishedAt) {
        this.jobFinishedAt = jobFinishedAt;
    }

    public Integer getJobDuration() {
        return jobDuration;
    }

    public void setJobDuration(Integer jobDuration) {
        this.jobDuration = jobDuration;
    }

    public Boolean getJobAllowFailure() {
        return jobAllowFailure;
    }

    public void setJobAllowFailure(Boolean jobAllowFailure) {
        this.jobAllowFailure = jobAllowFailure;
    }

    public String getJobFailureReason() {
        return jobFailureReason;
    }

    public void setJobFailureReason(String jobFailureReason) {
        this.jobFailureReason = jobFailureReason;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public EventUser getUser() {
        return user;
    }

    public void setUser(EventUser user) {
        this.user = user;
    }

    public BuildCommit getCommit() {
        return commit;
    }

    public void setCommit(BuildCommit commit) {
        this.commit = commit;
    }

    public EventRepository getRepository() {
        return repository;
    }

    public void setRepository(EventRepository repository) {
        this.repository = repository;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
