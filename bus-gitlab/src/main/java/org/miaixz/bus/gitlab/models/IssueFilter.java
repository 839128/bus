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

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.gitlab.models.Constants.IssueOrderBy;
import org.miaixz.bus.gitlab.models.Constants.IssueScope;
import org.miaixz.bus.gitlab.models.Constants.IssueState;
import org.miaixz.bus.gitlab.models.Constants.SortOrder;
import org.miaixz.bus.gitlab.support.ISO8601;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serial;

/**
 * This class is used to filter issues when getting lists of them.
 */
public class IssueFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852257636989L;

    /**
     * Return only the issues having the given iid.
     */
    private List<Long> iids;

    /**
     * {@link Constants.IssueState} Return all issues or just those that are opened or closed.
     */
    private IssueState state;

    /**
     * Modify the scope of the search attribute. title, description, or a string joining them with comma. Default is
     * title,description
     */
    private List<String> in;

    /**
     * Comma-separated list of label names, issues must have all labels to be returned. No+Label lists all issues with
     * no labels.
     */
    private List<String> labels;

    /**
     * The milestone title. No+Milestone lists all issues with no milestone.
     */
    private String milestone;

    /**
     * {@link Constants.IssueScope} Return issues for the given scope: created_by_me, assigned_to_me or all. For
     * versions before 11.0, use the now deprecated created-by-me or assigned-to-me scopes instead.
     */
    private IssueScope scope;

    /**
     * Return issues created by the given user id.
     */
    private Long authorId;

    /**
     * Return issues assigned to the given user id.
     */
    private Long assigneeId;

    /**
     * Return issues reacted by the authenticated user by the given emoji.
     */
    private String myReactionEmoji;

    /**
     * {@link Constants.IssueOrderBy} Return issues ordered by created_at or updated_at fields. Default is created_at.
     */
    private IssueOrderBy orderBy;

    /**
     * {@link Constants.SortOrder} Return issues sorted in asc or desc order. Default is desc.
     */
    private SortOrder sort;

    /**
     * Search project issues against their title and description.
     */
    private String search;

    /**
     * Return issues created on or after the given time.
     */
    private Date createdAfter;

    /**
     * Return issues created on or before the given time.
     */
    private Date createdBefore;

    /**
     * Return issues updated on or after the given time.
     */
    private Date updatedAfter;

    /**
     * Return issues updated on or before the given time.
     */
    private Date updatedBefore;

    /**
     * Return issues in current iteration.
     */
    private String iterationTitle;

    /*
     * Return issues without these parameters
     */
    private Map<IssueField, Object> not;

    /*- properties -*/
    public List<Long> getIids() {
        return iids;
    }

    public void setIids(List<Long> iids) {
        this.iids = iids;
    }

    public List<String> getIn() {
        return in;
    }

    public IssueState getState() {
        return state;
    }

    public void setState(IssueState state) {
        this.state = state;
    }

    public void setIn(List<String> in) {
        this.in = in;
    }

    /*- builder -*/
    public IssueFilter withIids(List<Long> iids) {
        this.iids = iids;
        return (this);
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public IssueScope getScope() {
        return scope;
    }

    public void setScope(IssueScope scope) {
        this.scope = scope;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getMyReactionEmoji() {
        return myReactionEmoji;
    }

    public void setMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
    }

    public IssueOrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(IssueOrderBy orderBy) {
        this.orderBy = orderBy;
    }

    public SortOrder getSort() {
        return sort;
    }

    public void setSort(SortOrder sort) {
        this.sort = sort;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Date getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
    }

    public Date getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
    }

    public Date getUpdatedAfter() {
        return updatedAfter;
    }

    public void setUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
    }

    public Date getUpdatedBefore() {
        return updatedBefore;
    }

    public void setUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
    }

    public String getIterationTitle() {
        return iterationTitle;
    }

    public void setIterationTitle(String iterationTitle) {
        this.iterationTitle = iterationTitle;
    }

    public Map<IssueField, Object> getNot() {
        return not;
    }

    public void setNot(Map<IssueField, Object> not) {
        this.not = not;
    }

    /*- params generator -*/
    @JsonIgnore
    public GitLabForm getQueryParams(int page, int perPage) {
        return (getQueryParams().withParam(Constants.PAGE_PARAM, page).withParam(Constants.PER_PAGE_PARAM, perPage));
    }

    public IssueFilter withState(IssueState state) {
        this.state = state;
        return (this);
    }

    public IssueFilter withLabels(List<String> labels) {
        this.labels = labels;
        return (this);
    }

    public IssueFilter withMilestone(String milestone) {
        this.milestone = milestone;
        return (this);
    }

    public IssueFilter withScope(IssueScope scope) {
        this.scope = scope;
        return (this);
    }

    public IssueFilter withAuthorId(Long authorId) {
        this.authorId = authorId;
        return (this);
    }

    public IssueFilter withAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
        return (this);
    }

    public IssueFilter withMyReactionEmoji(String myReactionEmoji) {
        this.myReactionEmoji = myReactionEmoji;
        return (this);
    }

    public IssueFilter withOrderBy(IssueOrderBy orderBy) {
        this.orderBy = orderBy;
        return (this);
    }

    public IssueFilter withSort(SortOrder sort) {
        this.sort = sort;
        return (this);
    }

    public IssueFilter withSearch(String search) {
        this.search = search;
        return (this);
    }

    public IssueFilter withCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
        return (this);
    }

    public IssueFilter withCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
        return (this);
    }

    public IssueFilter withUpdatedAfter(Date updatedAfter) {
        this.updatedAfter = updatedAfter;
        return (this);
    }

    public IssueFilter withUpdatedBefore(Date updatedBefore) {
        this.updatedBefore = updatedBefore;
        return (this);
    }

    public IssueFilter withIterationTitle(String iterationTitle) {
        this.iterationTitle = iterationTitle;
        return (this);
    }

    /**
     * Add 'not' filter.
     *
     * @param not the 'not' filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withNot(Map<IssueField, Object> not) {
        this.not = not;
        return (this);
    }

    /**
     * Add 'not' filter entry.
     *
     * @param field the field to be added to the 'not' value
     * @param value the value for the entry
     * @return the reference to this IssueField instance
     */
    public IssueFilter withNot(IssueField field, Object value) {
        if (not == null) {
            not = new LinkedHashMap<>();
        }
        not.put(field, value);
        return (this);
    }

    /**
     * Add labels to the 'not' filter entry.
     *
     * @param labels the labels to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutLabels(String... labels) {
        return withNot(IssueField.LABELS, String.join(",", labels));
    }

    /*
     * Add iids to the 'not' filter entry.
     *
     * @param iids the iids to add to the filter
     * 
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutIids(String... iids) {
        return withNot(IssueField.IIDS, String.join(",", iids));
    }

    /**
     * Add author_id to the 'not' filter entry.
     *
     * @param authorId the id of the author to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutAuthorId(Long authorId) {
        return withNot(IssueField.AUTHOR_ID, authorId);
    }

    /**
     * Add author_username to the 'not' filter entry.
     *
     * @param authorUsername the username of the author to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutAuthorUsername(String authorUsername) {
        return withNot(IssueField.AUTHOR_USERNAME, authorUsername);
    }

    /**
     * Add assignee_id to the 'not' filter entry.
     *
     * @param assigneeId the id of the assignee to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutAssigneeId(Long assigneeId) {
        return withNot(IssueField.ASSIGNEE_ID, assigneeId);
    }

    /**
     * Add assignee_username to the 'not' filter entry.
     *
     * @param assigneeUsername the username of the assignee to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutAssigneeUsername(String assigneeUsername) {
        return withNot(IssueField.ASSIGNEE_USERNAME, assigneeUsername);
    }

    /**
     * Add iteration_id to the 'not' filter entry.
     *
     * @param iterationId the id of the iteration to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutIterationId(Long iterationId) {
        return withNot(IssueField.ITERATION_ID, iterationId);
    }

    /**
     * Add iteration_title to the 'not' filter entry.
     *
     * @param iterationTitle the title of the iteration to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutIterationTitle(String iterationTitle) {
        return withNot(IssueField.ITERATION_TITLE, iterationTitle);
    }

    /**
     * Add milestone_id to the 'not' filter entry.
     *
     * @param milestoneId the id of the milestone to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutMilestoneId(Long milestoneId) {
        return withNot(IssueField.MILESTONE_ID, milestoneId);
    }

    /**
     * Add milestone to the 'not' filter entry.
     *
     * @param milestone the title of the milestone to add to the filter
     * @return the reference to this IssueFilter instance
     */
    public IssueFilter withoutMilestone(String milestone) {
        return withNot(IssueField.MILESTONE, milestone);
    }

    @JsonIgnore
    public GitLabForm getQueryParams() {
        return (new GitLabForm().withParam("iids", iids).withParam("state", state)
                .withParam("labels", (labels != null ? String.join(",", labels) : null))
                .withParam("in", (in != null ? String.join(",", in) : null)).withParam("milestone", milestone)
                .withParam("scope", scope).withParam("author_id", authorId).withParam("assignee_id", assigneeId)
                .withParam("my_reaction_emoji", myReactionEmoji).withParam("order_by", orderBy).withParam("sort", sort)
                .withParam("search", search).withParam("created_after", ISO8601.toString(createdAfter, false))
                .withParam("created_before", ISO8601.toString(createdBefore, false))
                .withParam("updated_after", ISO8601.toString(updatedAfter, false))
                .withParam("updated_before", ISO8601.toString(updatedBefore, false)))
                        .withParam("iteration_title", iterationTitle).withParam("not", toStringMap(not), false);
    }

    public enum IssueField {
        ASSIGNEE_ID, ASSIGNEE_USERNAME, AUTHOR_ID, AUTHOR_USERNAME, IIDS, ITERATION_ID, ITERATION_TITLE, LABELS,
        MILESTONE, MILESTONE_ID;

        private static JacksonJsonEnumHelper<IssueField> enumHelper = new JacksonJsonEnumHelper<>(IssueField.class);

        @JsonCreator
        public static IssueField forValue(String value) {
            return enumHelper.forValue(value);
        }

        @JsonValue
        public String toValue() {
            return (enumHelper.toString(this));
        }

        @Override
        public String toString() {
            return (enumHelper.toString(this));
        }
    }

    private Map<String, Object> toStringMap(Map<IssueField, Object> map) {
        if (map == null) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<IssueField, Object> entry : map.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue());
        }
        return result;
    }

}
