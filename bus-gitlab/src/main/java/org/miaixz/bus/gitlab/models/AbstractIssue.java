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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.miaixz.bus.gitlab.models.Constants.IssueState;
import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public abstract class AbstractIssue implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852235017309L;

    private References references;

    private Assignee assignee;
    private List<Assignee> assignees;
    private Author author;
    private Boolean confidential;
    private Date createdAt;
    private Date updatedAt;
    private Date closedAt;
    private User closedBy;
    private String description;
    private Date dueDate;

    @JsonProperty("id")
    private ValueNode actualId;

    @JsonIgnore
    private String externalId;

    @JsonIgnore
    private Long id;

    private Long iid;
    private List<String> labels;
    private Milestone milestone;
    private Long projectId;
    private IssueState state;
    private String title;
    private Integer userNotesCount;
    private String webUrl;
    private String severity;
    private Integer weight;
    private Boolean discussionLocked;
    private TimeStats timeStats;
    private String issueType;
    private IssueEpic epic;
    private Boolean imported;

    private Integer upvotes;
    private Integer downvotes;
    private Integer mergeRequestsCount;
    private Boolean hasTasks;
    private String taskStatus;
    private String importedFrom;
    private String healthStatus;
    private Iteration iteration;
    private TaskCompletionStatus taskCompletionStatus;

    public References getReferences() {
        return references;
    }

    public Assignee getAssignee() {
        return assignee;
    }

    public void setAssignee(Assignee assignee) {
        this.assignee = assignee;
    }

    public List<Assignee> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<Assignee> assignees) {
        this.assignees = assignees;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Boolean getConfidential() {
        return confidential;
    }

    public void setConfidential(Boolean confidential) {
        this.confidential = confidential;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public ValueNode getActualId() {
        return actualId;
    }

    public void setActualId(ValueNode id) {
        actualId = id;
        if (actualId instanceof TextNode) {
            externalId = actualId.asText();
        } else if (actualId instanceof IntNode || actualId instanceof LongNode) {
            this.id = actualId.asLong();
        }
    }

    public Long getId() {
        return (id);
    }

    public void setId(Long id) {
        this.id = id;
        if (id != null) {
            actualId = new LongNode(id);
            externalId = null;
        }
    }

    public String getExternalId() {
        return (externalId);
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
        if (externalId != null) {
            actualId = new TextNode(externalId);
            id = null;
        }
    }

    public Long getIid() {
        return iid;
    }

    public void setIid(Long iid) {
        this.iid = iid;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public IssueState getState() {
        return state;
    }

    public void setState(IssueState state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public User getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(User closedBy) {
        this.closedBy = closedBy;
    }

    public Integer getUserNotesCount() {
        return userNotesCount;
    }

    public void setUserNotesCount(Integer userNotesCount) {
        this.userNotesCount = userNotesCount;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public void setReferences(References references) {
        this.references = references;
    }

    public Boolean getImported() {
        return imported;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Boolean getDiscussionLocked() {
        return discussionLocked;
    }

    public void setDiscussionLocked(Boolean discussionLocked) {
        this.discussionLocked = discussionLocked;
    }

    public TimeStats getTimeStats() {
        return timeStats;
    }

    public void setTimeStats(TimeStats timeStats) {
        this.timeStats = timeStats;
    }

    public Integer getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    public Integer getMergeRequestsCount() {
        return mergeRequestsCount;
    }

    public void setMergeRequestsCount(Integer mergeRequestsCount) {
        this.mergeRequestsCount = mergeRequestsCount;
    }

    public Boolean getHasTasks() {
        return hasTasks;
    }

    public void setHasTasks(Boolean hasTasks) {
        this.hasTasks = hasTasks;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setImported(Boolean imported) {
        this.imported = imported;
    }

    public String getImportedFrom() {
        return importedFrom;
    }

    public void setImportedFrom(String importedFrom) {
        this.importedFrom = importedFrom;
    }

    public String getIssueType() {
        return issueType;
    }

    public Iteration getIteration() {
        return iteration;
    }

    public void setIteration(Iteration iteration) {
        this.iteration = iteration;
    }

    public TaskCompletionStatus getTaskCompletionStatus() {
        return taskCompletionStatus;
    }

    public void setTaskCompletionStatus(TaskCompletionStatus taskCompletionStatus) {
        this.taskCompletionStatus = taskCompletionStatus;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public IssueEpic getEpic() {
        return epic;
    }

    public void setEpic(IssueEpic epic) {
        this.epic = epic;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    public static class TaskCompletionStatus implements Serializable {

        @Serial
        private static final long serialVersionUID = 2852235115381L;

        private Integer count;
        private Integer completedCount;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Integer getCompletedCount() {
            return completedCount;
        }

        public void setCompletedCount(Integer completedCount) {
            this.completedCount = completedCount;
        }

        @Override
        public String toString() {
            return (JacksonJson.toJsonString(this));
        }
    }

}
