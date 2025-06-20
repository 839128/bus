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

import org.miaixz.bus.gitlab.models.GitLabForm;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serial;

public class HipChatService extends NotificationService {

    @Serial
    private static final long serialVersionUID = 2852285062156L;

    public static final String TOKEN_PROP = "token";
    public static final String COLOR_PROP = "color";
    public static final String NOTIFY_PROP = "notify";
    public static final String ROOM_PROP = "room";
    public static final String API_VERSION_PROP = "api_version";
    public static final String SERVER_PROP = "server";
    public static final String NOTIFY_ONLY_BROKEN_PIPELINES_PROP = "notify_only_broken_pipelines";

    /**
     * Get the form data for this service based on it's properties.
     *
     * @return the form data for this service based on it's properties
     */
    @Override
    public GitLabForm servicePropertiesForm() {
        GitLabForm formData = new GitLabForm().withParam("push_events", getPushEvents())
                .withParam("issues_events", getIssuesEvents())
                .withParam("confidential_issues_events", getConfidentialIssuesEvents())
                .withParam("merge_requests_events", getMergeRequestsEvents())
                .withParam("tag_push_events", getTagPushEvents()).withParam("note_events", getNoteEvents())
                .withParam("confidential_note_events", getConfidentialNoteEvents())
                .withParam("pipeline_events", getPipelineEvents()).withParam("token", getToken(), true)
                .withParam("color", getColor()).withParam("notify", getNotify()).withParam("room", getRoom())
                .withParam("api_version", getApiVersion()).withParam("server", getServer())
                .withParam("notify_only_broken_pipelines", getNotifyOnlyBrokenPipelines());
        return formData;
    }

    public HipChatService withPushEvents(Boolean pushEvents) {
        return withPushEvents(pushEvents, this);
    }

    public HipChatService withIssuesEvents(Boolean issuesEvents) {
        return withIssuesEvents(issuesEvents, this);
    }

    public HipChatService withConfidentialIssuesEvents(Boolean confidentialIssuesEvents) {
        return withConfidentialIssuesEvents(confidentialIssuesEvents, this);
    }

    public HipChatService withMergeRequestsEvents(Boolean mergeRequestsEvents) {
        return withMergeRequestsEvents(mergeRequestsEvents, this);
    }

    public HipChatService withTagPushEvents(Boolean tagPushEvents) {
        return withTagPushEvents(tagPushEvents, this);
    }

    public HipChatService withNoteEvents(Boolean noteEvents) {
        return withNoteEvents(noteEvents, this);
    }

    public HipChatService withConfidentialNoteEvents(Boolean confidentialNoteEvents) {
        return withConfidentialNoteEvents(confidentialNoteEvents, this);
    }

    public HipChatService withPipelineEvents(Boolean pipelineEvents) {
        return withPipelineEvents(pipelineEvents, this);
    }

    public HipChatService withWikiPageEvents(Boolean wikiPageEvents) {
        return withWikiPageEvents(wikiPageEvents, this);
    }

    public HipChatService withJobEvents(Boolean jobEvents) {
        return withPipelineEvents(jobEvents, this);
    }

    public String getToken() {
        return ((String) getProperty(TOKEN_PROP));
    }

    public void setToken(String token) {
        setProperty(TOKEN_PROP, token);
    }

    public HipChatService withToken(String token) {
        setToken(token);
        return (this);
    }

    public String getColor() {
        return getProperty(COLOR_PROP);
    }

    public void setColor(String color) {
        setProperty(COLOR_PROP, color);
    }

    public HipChatService withColor(String color) {
        setColor(color);
        return (this);
    }

    public Boolean getNotify() {
        return (getProperty(NOTIFY_PROP, null));
    }

    public void setNotify(Boolean notify) {
        setProperty(NOTIFY_PROP, notify);
    }

    public HipChatService withNotify(Boolean notify) {
        setNotify(notify);
        return (this);
    }

    public String getRoom() {
        return getProperty(ROOM_PROP);
    }

    public void setRoom(String room) {
        setProperty(ROOM_PROP, room);
    }

    public HipChatService withRoom(String room) {
        setRoom(room);
        return (this);
    }

    public String getApiVersion() {
        return getProperty(API_VERSION_PROP);
    }

    public void setApiVersion(String apiVersion) {
        setProperty(API_VERSION_PROP, apiVersion);
    }

    public HipChatService withApiVersion(String apiVersion) {
        setApiVersion(apiVersion);
        return (this);
    }

    public String getServer() {
        return getProperty(SERVER_PROP);
    }

    public void setServer(String server) {
        setProperty(SERVER_PROP, server);
    }

    public HipChatService withServer(String server) {
        setServer(server);
        return (this);
    }

    @JsonIgnore
    public Boolean getNotifyOnlyBrokenPipelines() {
        return ((Boolean) getProperty(NOTIFY_ONLY_BROKEN_PIPELINES_PROP, Boolean.FALSE));
    }

    public void setNotifyOnlyBrokenPipelines(Boolean notifyOnlyBrokenPipelines) {
        setProperty(NOTIFY_ONLY_BROKEN_PIPELINES_PROP, notifyOnlyBrokenPipelines);
    }

    public HipChatService withNotifyOnlyBrokenPipelines(Boolean notifyOnlyBrokenPipelines) {
        setNotifyOnlyBrokenPipelines(notifyOnlyBrokenPipelines);
        return (this);
    }

}
