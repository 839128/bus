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

import org.miaixz.bus.gitlab.support.JacksonJson;
import org.miaixz.bus.gitlab.support.JacksonJsonEnumHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serial;

public class Note implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852265960807L;

    /** Enum to use for ordering the results. */
    public static enum OrderBy {
        CREATED_AT, UPDATED_AT;

        private static JacksonJsonEnumHelper<OrderBy> enumHelper = new JacksonJsonEnumHelper<>(OrderBy.class);

        @JsonCreator
        public static OrderBy forValue(String value) {
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

    // This is not used because the GitLab example JSON is using a funny string for the MERGE_REQUEST notable_type
    // ("Merge request").
    // Once they fix the bug, the notableType field can be changed from String to NotableType.
    public static enum NoteableType {
        COMMIT, EPIC, ISSUE, MERGE_REQUEST, SNIPPET;

        private static JacksonJsonEnumHelper<NoteableType> enumHelper = new JacksonJsonEnumHelper<>(NoteableType.class,
                true, true);

        @JsonCreator
        public static NoteableType forValue(String value) {
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

    public static enum Type {
        DISCUSSION_NOTE, DIFF_NOTE;

        private static JacksonJsonEnumHelper<Type> enumHelper = new JacksonJsonEnumHelper<>(Type.class, true, true);

        @JsonCreator
        public static Type forValue(String value) {
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

    private String attachment;
    private Author author;
    private String body;
    private Date createdAt;
    private Boolean downvote;
    private Date expiresAt;
    private String fileName;
    private Long id;
    private Long noteableId;

    // Use String for noteableType until the constant is fixed in the GitLab API
    private String noteableType;

    private Long noteableIid;
    private Boolean system;
    private String title;
    private Date updatedAt;
    private Boolean upvote;
    private Boolean resolved;
    private Boolean resolvable;
    private Participant resolvedBy;
    private Date resolvedAt;
    private Boolean internal;
    private Type type;

    private Position position;

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getDownvote() {
        return downvote;
    }

    public void setDownvote(Boolean downvote) {
        this.downvote = downvote;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNoteableId() {
        return noteableId;
    }

    public void setNoteableId(Long noteableId) {
        this.noteableId = noteableId;
    }

    public String getNoteableType() {
        return noteableType;
    }

    public void setNoteableType(String noteableType) {
        this.noteableType = noteableType;
    }

    public Long getNoteableIid() {
        return noteableIid;
    }

    public void setNoteableIid(Long noteableIid) {
        this.noteableIid = noteableIid;
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        this.system = system;
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

    public Boolean getUpvote() {
        return upvote;
    }

    public void setUpvote(Boolean upvote) {
        this.upvote = upvote;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public Boolean getResolvable() {
        return resolvable;
    }

    public void setResolvable(Boolean resolvable) {
        this.resolvable = resolvable;
    }

    public Participant getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Participant resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

}
