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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.miaixz.bus.gitlab.models.Blame;
import org.miaixz.bus.gitlab.models.Constants;
import org.miaixz.bus.gitlab.models.RepositoryFile;
import org.miaixz.bus.gitlab.models.RepositoryFileResponse;

import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * This class provides an entry point to all the GitLab API repository files calls. See
 * <a href="https://docs.gitlab.com/ce/api/repository_files.html">Repository Files API at GitLab</a> for more
 * information.
 */
public class RepositoryFileApi extends AbstractApi {

    public RepositoryFileApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * Get an Optional instance with the value holding information on a file in the repository. Allows you to receive
     * information about file in repository like name, size. Only works with GitLab 11.1.0+, value will be an empty
     * object for earlier versions of GitLab.
     *
     * <pre>
     * <code>GitLab Endpoint: HEAD /projects/:id/repository/files</code>
     * </pre>
     *
     * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
     * @param filePath        (required) - Full path to the file. Ex. lib/class.rb
     * @param ref             (required) - The name of branch, tag or commit
     * @return an Optional instance with the specified RepositoryFile as a value
     * @since GitLab-11.1.0
     */
    public Optional<RepositoryFile> getOptionalFileInfo(Object projectIdOrPath, String filePath, String ref) {
        try {
            return (Optional.ofNullable(getFileInfo(projectIdOrPath, filePath, ref)));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

    /**
     * Get information on a file in the repository. Allows you to receive information about file in repository like
     * name, size. Only works with GitLab 11.1.0+, returns an empty object for earlier versions of GitLab.
     *
     * <pre>
     * <code>GitLab Endpoint: HEAD /projects/:id/repository/files</code>
     * </pre>
     *
     * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
     * @param filePath        (required) - Full path to the file. Ex. lib/class.rb
     * @param ref             (required) - The name of branch, tag or commit
     * @return a RepositoryFile instance with the file info
     * @throws GitLabApiException if any exception occurs
     * @since GitLab-11.1.0
     */
    public RepositoryFile getFileInfo(Object projectIdOrPath, String filePath, String ref) throws GitLabApiException {

        Form form = new Form();
        addFormParam(form, "ref", (ref != null ? urlEncode(ref) : null), true);
        Response response = head(Response.Status.OK, form.asMap(), "projects", getProjectIdOrPath(projectIdOrPath),
                "repository", "files", urlEncode(filePath));

        RepositoryFile file = new RepositoryFile();
        file.setBlobId(response.getHeaderString("X-Gitlab-Blob-Id"));
        file.setCommitId(response.getHeaderString("X-Gitlab-Commit-Id"));
        file.setContentSha256(response.getHeaderString("X-Gitlab-Content-Sha256"));

        String encoding = response.getHeaderString("X-Gitlab-Encoding");
        file.setEncoding(Constants.Encoding.forValue(encoding));

        file.setFileName(response.getHeaderString("X-Gitlab-File-Name"));
        file.setFilePath(response.getHeaderString("X-Gitlab-File-Path"));
        file.setLastCommitId(response.getHeaderString("X-Gitlab-Last-Commit-Id"));
        file.setRef(response.getHeaderString("X-Gitlab-Ref"));

        String sizeStr = response.getHeaderString("X-Gitlab-Size");
        file.setSize(sizeStr != null ? Integer.valueOf(sizeStr) : -1);

        return (file);
    }

    /**
     * Get an Optional instance with the value holding information and content for a file in the repository. Allows you
     * to receive information about file in repository like name, size, and content. Only works with GitLab 11.1.0+,
     * value will be an empty object for earlier versions of GitLab.
     *
     * <pre>
     * <code>GitLab Endpoint: HEAD /projects/:id/repository/files</code>
     * </pre>
     *
     * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
     * @param filePath        (required) - Full path to the file. Ex. lib/class.rb
     * @param ref             (required) - The name of branch, tag or commit
     * @return an Optional instance with the specified RepositoryFile as a value
     */
    public Optional<RepositoryFile> getOptionalFile(Object projectIdOrPath, String filePath, String ref) {
        try {
            return (Optional.ofNullable(getFile(projectIdOrPath, filePath, ref, true)));
        } catch (GitLabApiException glae) {
            return (GitLabApi.createOptionalFromException(glae));
        }
    }

    /**
     * Get file from repository. Allows you to receive information about file in repository like name, size, content.
     * Note that file content is Base64 encoded.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/files</code>
     * </pre>
     *
     * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
     * @param filePath        (required) - Full path to the file. Ex. lib/class.rb
     * @param ref             (required) - The name of branch, tag or commit
     * @return a RepositoryFile instance with the file info and file content
     * @throws GitLabApiException if any exception occurs
     */
    public RepositoryFile getFile(Object projectIdOrPath, String filePath, String ref) throws GitLabApiException {
        return (getFile(projectIdOrPath, filePath, ref, true));
    }

    /**
     * Get file from repository. Allows you to receive information about file in repository like name, size, and
     * optionally content. Note that file content is Base64 encoded.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/files</code>
     * </pre>
     *
     * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
     * @param filePath        (required) - Full path to the file. Ex. lib/class.rb
     * @param ref             (required) - The name of branch, tag or commit
     * @param includeContent  if true will also fetch file content
     * @return a RepositoryFile instance with the file info and optionally file content
     * @throws GitLabApiException if any exception occurs
     */
    public RepositoryFile getFile(Object projectIdOrPath, String filePath, String ref, boolean includeContent)
            throws GitLabApiException {

        if (!includeContent) {
            return (getFileInfo(projectIdOrPath, filePath, ref));
        }

        Form form = new Form();
        addFormParam(form, "ref", (ref != null ? urlEncode(ref) : null), true);
        Response response = get(Response.Status.OK, form.asMap(), "projects", getProjectIdOrPath(projectIdOrPath),
                "repository", "files", urlEncode(filePath));
        return (response.readEntity(RepositoryFile.class));
    }

    /**
     * Create new file in repository
     *
     * <pre>
     * <code>GitLab Endpoint: POST /projects/:id/repository/files</code>
     * </pre>
     *
     * file_path (required) - Full path to new file. Ex. lib/class.rb branch_name (required) - The name of branch
     * encoding (optional) - 'text' or 'base64'. Text is default. content (required) - File content commit_message
     * (required) - Commit message
     *
     * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
     * @param file            a ReposityoryFile instance with info for the file to create
     * @param branchName      the name of branch
     * @param commitMessage   the commit message
     * @return a RepositoryFile instance with the created file info
     * @throws GitLabApiException if any exception occurs
     */
    public RepositoryFileResponse createFile(Object projectIdOrPath, RepositoryFile file, String branchName,
            String commitMessage) throws GitLabApiException {

        Form formData = createForm(file, branchName, commitMessage);
        Response response = post(Response.Status.CREATED, formData, "projects", getProjectIdOrPath(projectIdOrPath),
                "repository", "files", urlEncode(file.getFilePath()));
        return (response.readEntity(RepositoryFileResponse.class));
    }

    /**
     * Update existing file in repository
     *
     * <pre>
     * <code>GitLab Endpoint: PUT /projects/:id/repository/files</code>
     * </pre>
     *
     * file_path (required) - Full path to new file. Ex. lib/class.rb branch_name (required) - The name of branch
     * encoding (optional) - 'text' or 'base64'. Text is default. content (required) - File content commit_message
     * (required) - Commit message
     *
     * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
     * @param file            a ReposityoryFile instance with info for the file to update
     * @param branchName      the name of branch
     * @param commitMessage   the commit message
     * @return a RepositoryFile instance with the updated file info
     * @throws GitLabApiException if any exception occurs
     */
    public RepositoryFileResponse updateFile(Object projectIdOrPath, RepositoryFile file, String branchName,
            String commitMessage) throws GitLabApiException {

        Form formData = createForm(file, branchName, commitMessage);
        Response response = put(Response.Status.OK, formData.asMap(), "projects", getProjectIdOrPath(projectIdOrPath),
                "repository", "files", urlEncode(file.getFilePath()));
        return (response.readEntity(RepositoryFileResponse.class));
    }

    /**
     * Delete existing file in repository
     *
     * <pre>
     * <code>GitLab Endpoint: DELETE /projects/:id/repository/files</code>
     * </pre>
     *
     * file_path (required) - Full path to file. Ex. lib/class.rb branch_name (required) - The name of branch
     * commit_message (required) - Commit message
     *
     * @param projectIdOrPath the id, path of the project, or a Project instance holding the project ID or path
     * @param filePath        full path to new file. Ex. lib/class.rb
     * @param branchName      the name of branch
     * @param commitMessage   the commit message
     * @throws GitLabApiException if any exception occurs
     */
    public void deleteFile(Object projectIdOrPath, String filePath, String branchName, String commitMessage)
            throws GitLabApiException {

        if (filePath == null) {
            throw new RuntimeException("filePath cannot be null");
        }

        Form form = new Form();
        addFormParam(form, "branch", (branchName != null ? urlEncode(branchName) : null), true);
        addFormParam(form, "commit_message", commitMessage, true);
        delete(Response.Status.NO_CONTENT, form.asMap(), "projects", getProjectIdOrPath(projectIdOrPath), "repository",
                "files", urlEncode(filePath));
    }

    /**
     * Get the raw file for the file by commit sha and path. Thye file will be saved to the specified directory. If the
     * file already exists in the directory it will be overwritten.
     *
     * V3:
     * 
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/blobs/:sha</code>
     * </pre>
     *
     * V4:
     * 
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/files/:filepath</code>
     * </pre>
     *
     * @param projectIdOrPath    the project in the form of an Long(ID), String(path), or Project instance
     * @param commitOrBranchName the commit or branch name to get the file for
     * @param filepath           the path of the file to get
     * @param directory          the File instance of the directory to save the file to, if null will use
     *                           "java.io.tmpdir"
     * @return a File instance pointing to the download of the specified file
     * @throws GitLabApiException if any exception occurs
     */
    public File getRawFile(Object projectIdOrPath, String commitOrBranchName, String filepath, File directory)
            throws GitLabApiException {

        InputStream in = getRawFile(projectIdOrPath, commitOrBranchName, filepath);

        try {

            if (directory == null) {
                directory = new File(System.getProperty("java.io.tmpdir"));
            }

            String filename = new File(filepath).getName();
            File file = new File(directory, filename);

            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return (file);

        } catch (IOException ioe) {
            throw new GitLabApiException(ioe);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Get the raw file contents for a file by commit sha and path.
     *
     * V3:
     * 
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/blobs/:sha</code>
     * </pre>
     *
     * V4:
     * 
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/files/:filepath</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param ref             the commit or branch name to get the file contents for
     * @param filepath        the path of the file to get
     * @return an InputStream to read the raw file from
     * @throws GitLabApiException if any exception occurs
     */
    public InputStream getRawFile(Object projectIdOrPath, String ref, String filepath) throws GitLabApiException {

        Form formData = new GitLabApiForm().withParam("ref", (ref != null ? ref : null), true);
        Response response = getWithAccepts(Response.Status.OK, formData.asMap(), MediaType.MEDIA_TYPE_WILDCARD,
                "projects", getProjectIdOrPath(projectIdOrPath), "repository", "files", urlEncode(filepath), "raw");
        return response.readEntity(InputStream.class);
    }

    /**
     * Gets the query params based on the API version.
     *
     * @param file          the RepositoryFile instance with the info for the query params
     * @param branchName    the branch name
     * @param commitMessage the commit message
     * @return a Form instance with the correct query params.
     */
    protected Form createForm(RepositoryFile file, String branchName, String commitMessage) {

        Form form = new Form();
        addFormParam(form, "branch", branchName, true);
        addFormParam(form, "encoding", file.getEncoding(), false);

        // Cannot use addFormParam() as it does not accept an empty or whitespace only string
        String content = file.getContent();
        if (content == null) {
            throw new IllegalArgumentException("content cannot be null");
        }
        form.param("content", content);

        addFormParam(form, "commit_message", commitMessage, true);
        return (form);
    }

    /**
     * Get a List of file blame from repository. Allows you to receive blame information. Each blame range contains
     * lines and corresponding commit information.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/files/:file_path/blame</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param filePath        the path of the file to get the blame for
     * @param ref             the name of branch, tag or commit
     * @return a List of Blame instances for the specified filePath and ref
     * @throws GitLabApiException if any exception occurs
     */
    public List<Blame> getBlame(Object projectIdOrPath, String filePath, String ref) throws GitLabApiException {
        return (getBlame(projectIdOrPath, filePath, ref, getDefaultPerPage()).all());
    }

    /**
     * Get a Pager of file blame from repository. Allows you to receive blame information. Each blame range contains
     * lines and corresponding commit information.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/files/:file_path/blame</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param filePath        the path of the file to get the blame for
     * @param ref             the name of branch, tag or commit
     * @param itemsPerPage    the number of Project instances that will be fetched per page
     * @return a Pager of Blame instances for the specified filePath and ref
     * @throws GitLabApiException if any exception occurs
     */
    public Pager<Blame> getBlame(Object projectIdOrPath, String filePath, String ref, int itemsPerPage)
            throws GitLabApiException {
        GitLabApiForm formData = new GitLabApiForm().withParam("ref", ref, true);
        return (new Pager<Blame>(this, Blame.class, itemsPerPage, formData.asMap(), "projects",
                getProjectIdOrPath(projectIdOrPath), "repository", "files", urlEncode(filePath), "blame"));
    }

    /**
     * Get a Stream of file blame from repository. Allows you to receive blame information. Each blame range contains
     * lines and corresponding commit information.
     *
     * <pre>
     * <code>GitLab Endpoint: GET /projects/:id/repository/files/:file_path/blame</code>
     * </pre>
     *
     * @param projectIdOrPath the project in the form of an Long(ID), String(path), or Project instance
     * @param filePath        the path of the file to get the blame for
     * @param ref             the name of branch, tag or commit
     * @return a Stream of Blame instances for the specified filePath and ref
     * @throws GitLabApiException if any exception occurs
     */
    public Stream<Blame> getBlameStream(Object projectIdOrPath, String filePath, String ref) throws GitLabApiException {
        return (getBlame(projectIdOrPath, filePath, ref, getDefaultPerPage()).stream());
    }

}
