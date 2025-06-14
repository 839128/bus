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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.miaixz.bus.gitlab.models.Namespace;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

/**
 * This class implements the client side API for the GitLab namespace calls.
 */
public class NamespaceApi extends AbstractApi {

    public NamespaceApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get a list of the namespaces of the authenticated user. If the user is an administrator, a list of all namespaces
     * in the GitLab instance is created.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces</code>
     * </pre>
     *
     * @return a List of Namespace instances
     * @throws GitLabApiException if any exception occurs
     */
    public List<Namespace> getNamespaces() throws GitLabApiException {
        return (getNamespaces(getDefaultPerPage()).all());
    }

    /**
     * Get a list of the namespaces of the authenticated user. If the user is an administrator, a list of all namespaces
     * in the GitLab instance is returned.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces</code>
     * </pre>
     *
     * @param page    the page to get
     * @param perPage the number of Namespace instances per page
     * @return a List of Namespace instances in the specified page range
     * @throws GitLabApiException if any exception occurs
     */
    public List<Namespace> getNamespaces(int page, int perPage) throws GitLabApiException {
        Response response = get(Response.Status.OK, getPageQueryParams(page, perPage), "namespaces");
        return (response.readEntity(new GenericType<List<Namespace>>() {
        }));
    }

    /**
     * Get a Pager of the namespaces of the authenticated user. If the user is an administrator, a Pager of all
     * namespaces in the GitLab instance is returned.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces</code>
     * </pre>
     *
     * @param itemsPerPage the number of Project instances that will be fetched per page
     * @return a Pager of Namespace instances
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Namespace> getNamespaces(int itemsPerPage) throws GitLabApiException {
        return (new Pager<Namespace>(this, Namespace.class, itemsPerPage, null, "namespaces"));
    }

    /**
     * Get a Stream of the namespaces of the authenticated user. If the user is an administrator, a Stream of all
     * namespaces in the GitLab instance is returned.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces</code>
     * </pre>
     *
     * @return a Stream of Namespace instances
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Namespace> getNamespacesStream() throws GitLabApiException {
        return (getNamespaces(getDefaultPerPage()).stream());
    }

    /**
     * Get all namespaces that match a string in their name or path.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces?search=:query</code>
     * </pre>
     *
     * @param query the search string
     * @return the Namespace List with the matching namespaces
     * @throws GitLabApiException if any exception occurs
     */
    public List<Namespace> findNamespaces(String query) throws GitLabApiException {
        return (findNamespaces(query, getDefaultPerPage()).all());
    }

    /**
     * Get all namespaces that match a string in their name or path in the specified page range.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces?search=:query</code>
     * </pre>
     *
     * @param query   the search string
     * @param page    the page to get
     * @param perPage the number of Namespace instances per page
     * @return the Namespace List with the matching namespaces
     * @throws GitLabApiException if any exception occurs
     */
    public List<Namespace> findNamespaces(String query, int page, int perPage) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("search", query, true).withParam(PAGE_PARAM, page)
                .withParam(PER_PAGE_PARAM, perPage);
        Response response = get(Response.Status.OK, formData.asMap(), "namespaces");
        return (response.readEntity(new GenericType<List<Namespace>>() {
        }));
    }

    /**
     * Get a Pager of all namespaces that match a string in their name or path.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces?search=:query</code>
     * </pre>
     *
     * @param query        the search string
     * @param itemsPerPage the number of Project instances that will be fetched per page
     * @return a Pager of Namespace instances with the matching namespaces
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Namespace> findNamespaces(String query, int itemsPerPage) throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("search", query, true);
        return (new Pager<Namespace>(this, Namespace.class, itemsPerPage, formData.asMap(), "namespaces"));
    }

    /**
     * Get all namespaces that match a string in their name or path as a Stream.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces?search=:query</code>
     * </pre>
     *
     * @param query the search string
     * @return a Stream with the matching namespaces
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Namespace> findNamespacesStream(String query) throws GitLabApiException {
        return (findNamespaces(query, getDefaultPerPage()).stream());
    }

    /**
     * Get all details of a namespace.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces/:id</code>
     * </pre>
     *
     * @param namespaceIdOrPath the namespace ID, path of the namespace, or a Namespace instance holding the namespace
     *                          ID or path
     * @return the Namespace instance for the specified path
     * @throws GitLabApiException if any exception occurs
     */
    public Namespace getNamespace(Object namespaceIdOrPath) throws GitLabApiException {
        Response response = get(Response.Status.OK, null, "namespaces", getNamespaceIdOrPath(namespaceIdOrPath));
        return (response.readEntity(Namespace.class));
    }

    /**
     * Get all details of a namespace as an Optional instance.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /namespaces/:id</code>
     * </pre>
     *
     * @param namespaceIdOrPath the namespace ID, path of the namespace, or a Namespace instance holding the namespace
     *                          ID or path
     * @return the Group for the specified group path as an Optional instance
     */
    public Optional<Namespace> getOptionalNamespace(Object namespaceIdOrPath) {
        try {
            return (Optional.ofNullable(getNamespace(namespaceIdOrPath)));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

}
