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
package org.miaixz.bus.gitlab;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.StatusType;
import java.io.Serial;

/**
 * This is the exception that will be thrown if any exception occurs while communicating with a GitLab API endpoint.
 */
public class GitLabApiException extends Exception {

    @Serial
    private static final long serialVersionUID = 2852289982699L;

    private StatusType statusInfo;
    private int httpStatus;
    private String message;
    private Map<String, List<String>> validationErrors;
    private MultivaluedMap<String, String> headers;

    /**
     * Create a GitLabApiException instance with the specified message.
     *
     * @param message the message for the exception
     */
    public GitLabApiException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * Create a GitLabApiException instance with the specified message and HTTP status code.
     *
     * @param message    the message for the exception
     * @param httpStatus the HTTP status code for the exception
     */
    public GitLabApiException(String message, int httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    /**
     * Create a GitLabApiException instance based on the ClientResponse.
     *
     * @param response the JAX-RS response that caused the exception
     */
    public GitLabApiException(Response response) {

        super();
        statusInfo = response.getStatusInfo();
        httpStatus = response.getStatus();
        headers = response.getStringHeaders();

        if (response.hasEntity()) {

            try {

                String message = response.readEntity(String.class);
                this.message = message;

                // Determine what is in the content of the response and process it accordingly
                MediaType mediaType = response.getMediaType();
                if (mediaType != null && "json".equals(mediaType.getSubtype())) {

                    JsonNode json = JacksonJson.toJsonNode(message);

                    // First see if it is a "message", if so it is either a simple message,
                    // or a Map<String, List<String>> of validation errors
                    JsonNode jsonMessage = json.get("message");
                    if (jsonMessage != null) {

                        // If the node is an object, then it is validation errors
                        if (jsonMessage.isObject()) {

                            validationErrors = new LinkedHashMap<>();
                            Iterator<Entry<String, JsonNode>> fields = jsonMessage.fields();
                            while (fields.hasNext()) {

                                Entry<String, JsonNode> field = fields.next();
                                String fieldName = field.getKey();
                                List<String> values = new ArrayList<>();
                                validationErrors.put(fieldName, values);
                                for (JsonNode value : field.getValue()) {
                                    values.add(value.asText());
                                }
                            }

                            if (!validationErrors.isEmpty()) {
                                this.message = "The following fields have validation errors: "
                                        + String.join(", ", validationErrors.keySet()) + "\n"
                                        + validationErrors.entrySet().stream().map(e -> {
                                            return "* " + e.getKey() + e.getValue().stream()
                                                    .collect(Collectors.joining("\n     - ", "\n     - ", ""));
                                        }).collect(Collectors.joining("\n"));
                            }

                        } else if (jsonMessage.isArray()) {

                            List<String> values = new ArrayList<>();
                            for (JsonNode value : jsonMessage) {
                                values.add(value.asText());
                            }

                            if (values.size() > 0) {
                                this.message = String.join("\n", values);
                            }

                        } else if (jsonMessage.isTextual()) {
                            this.message = jsonMessage.asText();
                        } else {
                            this.message = jsonMessage.toString();
                        }

                    } else {

                        JsonNode jsonError = json.get("error");
                        if (jsonError != null) {
                            this.message = jsonError.asText();
                        }
                    }
                }

            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Create a GitLabApiException instance based on the exception.
     *
     * @param e the Exception to wrap
     */
    public GitLabApiException(Exception e) {
        super(e);
        message = e.getMessage();
    }

    /**
     * Get the message associated with the exception.
     *
     * @return the message associated with the exception
     */
    @Override
    public final String getMessage() {
        return (message != null ? message : getReason());
    }

    /**
     * Returns the HTTP status reason message, returns null if the causing error was not an HTTP related exception.
     *
     * @return the HTTP status reason message
     */
    public final String getReason() {
        return (statusInfo != null ? statusInfo.getReasonPhrase() : null);
    }

    /**
     * Returns the HTTP status code that was the cause of the exception. returns 0 if the causing error was not an HTTP
     * related exception
     *
     * @return the HTTP status code, returns 0 if the causing error was not an HTTP related exception
     */
    public final int getHttpStatus() {
        return (httpStatus);
    }

    /**
     * Returns true if this GitLabApiException was caused by validation errors on the GitLab server, otherwise returns
     * false.
     *
     * @return true if this GitLabApiException was caused by validation errors on the GitLab server, otherwise returns
     *         false
     */
    public boolean hasValidationErrors() {
        return (validationErrors != null);
    }

    /**
     * Returns a Map&lt;String, List&lt;String&gt;&gt; instance containing validation errors if this GitLabApiException
     * was caused by validation errors on the GitLab server, otherwise returns null.
     *
     * @return a Map&lt;String, List&lt;String&gt;&gt; instance containing validation errors if this GitLabApiException
     *         was caused by validation errors on the GitLab server, otherwise returns null
     */
    public Map<String, List<String>> getValidationErrors() {
        return (validationErrors);
    }

    /**
     * Returns the response headers. Returns null if the causing error was not a response related exception.
     *
     * @return the response headers or null.
     */
    public final MultivaluedMap<String, String> getHeaders() {
        return (headers);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + httpStatus;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((statusInfo == null) ? 0 : statusInfo.hashCode());
        result = prime * result + ((validationErrors == null) ? 0 : validationErrors.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        GitLabApiException other = (GitLabApiException) obj;
        if (httpStatus != other.httpStatus) {
            return false;
        }

        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message)) {
            return false;
        }

        if (statusInfo == null) {
            if (other.statusInfo != null)
                return false;
        } else if (!statusInfo.equals(other.statusInfo)) {
            return false;
        }

        if (validationErrors == null) {
            if (other.validationErrors != null)
                return false;
        } else if (!validationErrors.equals(other.validationErrors)) {
            return false;
        }

        if (!Objects.equals(this.headers, other.headers)) {
            return false;
        }

        return true;
    }

}
