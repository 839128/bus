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
package org.miaixz.bus.gitlab.services;

import java.io.Serial;
import java.util.Map;

import org.miaixz.bus.gitlab.models.GitLabForm;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JiraService extends NotificationService {

    @Serial
    private static final long serialVersionUID = 2852285168011L;

    public static final String URL_PROP = "url";
    public static final String API_URL_PROP = "api_url";
    public static final String PROJECT_KEY_PROP = "project_key";
    public static final String USERNAME_PROP = "username";
    public static final String JIRA_ISSUE_TRANSITION_ID_PROP = "jira_issue_transition_id";
    public static final String COMMIT_EVENTS_PROP = "commit_events";

    private CharSequence password;

    /**
     * Get the form data for this service based on it's properties.
     *
     * @return the form data for this service based on it's properties
     */
    @Override
    public GitLabForm servicePropertiesForm() {
        GitLabForm formData = new GitLabForm().withParam("merge_requests_events", getMergeRequestsEvents())
                .withParam(COMMIT_EVENTS_PROP, getCommitEvents()).withParam(URL_PROP, getUrl(), true)
                .withParam(API_URL_PROP, getApiUrl()).withParam(PROJECT_KEY_PROP, getProjectKey())
                .withParam(USERNAME_PROP, getUsername(), true).withParam("password", getPassword(), true)
                .withParam(JIRA_ISSUE_TRANSITION_ID_PROP, getJiraIssueTransitionId());
        return formData;
    }

    @JsonIgnore
    public Boolean getCommitEvents() {
        return (getProperty(COMMIT_EVENTS_PROP, null));
    }

    public void setCommitEvents(Boolean commitEvents) {
        setProperty(COMMIT_EVENTS_PROP, commitEvents);
    }

    public JiraService withCommitEvents(Boolean commitEvents) {
        setCommitEvents(commitEvents);
        return (this);
    }

    public JiraService withMergeRequestsEvents(Boolean mergeRequestsEvents) {
        return withMergeRequestsEvents(mergeRequestsEvents, this);
    }

    @JsonIgnore
    public CharSequence getPassword() {
        return password;
    }

    public void setPassword(CharSequence password) {
        this.password = password;
    }

    public JiraService withPassword(CharSequence password) {
        setPassword(password);
        return (this);
    }

    @JsonIgnore
    public String getUrl() {
        return (getProperty(URL_PROP));
    }

    public void setUrl(String url) {
        setProperty(URL_PROP, url);
    }

    public JiraService withUrl(String url) {
        setUrl(url);
        return (this);
    }

    @JsonIgnore
    public String getApiUrl() {
        return (getProperty(API_URL_PROP));
    }

    public void setApiUrl(String apiUrl) {
        setProperty(API_URL_PROP, apiUrl);
    }

    public JiraService withApiUrl(String apiUrl) {
        setApiUrl(apiUrl);
        return (this);
    }

    @JsonIgnore
    public String getProjectKey() {
        return (getProperty(PROJECT_KEY_PROP));
    }

    public void setProjectKey(String projectKey) {
        setProperty(PROJECT_KEY_PROP, projectKey);
    }

    public JiraService withProjectKey(String projectKey) {
        setProjectKey(projectKey);
        return (this);
    }

    @JsonIgnore
    public String getUsername() {
        return (getProperty(USERNAME_PROP));
    }

    public void setUsername(String username) {
        setProperty(USERNAME_PROP, username);
    }

    public JiraService withUsername(String username) {
        setUsername(username);
        return (this);
    }

    @JsonIgnore
    public Integer getJiraIssueTransitionId() {
        return (getProperty(JIRA_ISSUE_TRANSITION_ID_PROP, null));
    }

    public void setJiraIssueTransitionId(Integer jiraIssueTransitionId) {
        setProperty(JIRA_ISSUE_TRANSITION_ID_PROP, jiraIssueTransitionId);
    }

    public JiraService withJiraIssueTransitionId(Integer jiraIssueTransitionId) {
        setJiraIssueTransitionId(jiraIssueTransitionId);
        return (this);
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        fixJiraIssueTransitionId(properties);
        super.setProperties(properties);
    }

    /**
     * Make sure jiraIssueTransitionId is an integer and not an empty string.
     * 
     * @param properties the Map holding the properties
     */
    private void fixJiraIssueTransitionId(Map<String, Object> properties) {

        if (properties != null) {
            Object jiraIssueTransitionId = properties.get(JIRA_ISSUE_TRANSITION_ID_PROP);
            if (jiraIssueTransitionId instanceof String) {
                if (((String) jiraIssueTransitionId).trim().isEmpty()) {
                    properties.put(JIRA_ISSUE_TRANSITION_ID_PROP, null);
                } else {
                    properties.put(JIRA_ISSUE_TRANSITION_ID_PROP, Integer.valueOf((String) jiraIssueTransitionId));
                }
            }
        }
    }

}
