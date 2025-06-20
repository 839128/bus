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

import org.miaixz.bus.gitlab.models.Constants.PackageOrderBy;
import org.miaixz.bus.gitlab.models.Constants.PackageStatus;
import org.miaixz.bus.gitlab.models.Constants.SortOrder;
import java.io.Serial;

/**
 * This class is used to filter Projects when getting lists of projects for a specified group.
 */
public class PackageFilter implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852268635108L;

    private Boolean excludeSubgroups;
    private PackageOrderBy orderBy;
    private SortOrder sort;
    private PackageType packageType;
    private String packageName;
    private Boolean includeVersionless;
    private PackageStatus status;
    private String packageVersion;

    /**
     * Exclude Subgroups.
     *
     * @param excludeSubgroups if true, packages from projects from subgroups are not listed.
     * @return the reference to this ProjectFilter instance
     */
    public PackageFilter withExcludeSubgroups(Boolean excludeSubgroups) {
        this.excludeSubgroups = excludeSubgroups;
        return (this);
    }

    /**
     * Return projects ordered by created_at, name, version, type, or project_path
     *
     * @param orderBy specifies what field to order by
     * @return the reference to this ProjectFilter instance
     */
    public PackageFilter withOrderBy(PackageOrderBy orderBy) {
        this.orderBy = orderBy;
        return (this);
    }

    /**
     * Return projects sorted in asc or desc order. Default is desc.
     *
     * @param sort sort direction, ASC or DESC
     * @return the reference to this ProjectFilter instance
     */
    public PackageFilter withSortOder(SortOrder sort) {
        this.sort = sort;
        return (this);
    }

    /**
     * Filter the returned packages by type.
     *
     * @param packageType One of conan, maven, npm, pypi, composer, nuget, helm, generic or golang
     * @return the reference to this ProjectFilter instance
     */
    public PackageFilter withPackageType(PackageType packageType) {
        this.packageType = packageType;
        return (this);
    }

    /**
     * Filter the project packages with a fuzzy search by name
     *
     * @param packageName
     * @return the reference to this ProjectFilter instance
     */
    public PackageFilter withPackageName(String packageName) {
        this.packageName = packageName;
        return (this);
    }

    /**
     * @param includeVersionless if true, versionless packages are included in the response
     * @return the reference to this ProjectFilter instance
     */
    public PackageFilter withIncludeVersionless(Boolean includeVersionless) {
        this.includeVersionless = includeVersionless;
        return (this);
    }

    /**
     * Filter the returned packages by status.
     *
     * @param status One of default (default), hidden, or processing
     * @return the reference to this ProjectFilter instance
     */
    public PackageFilter withStatus(PackageStatus status) {
        this.status = status;
        return (this);
    }

    /**
     * Filter the returned packages by version.
     * 
     * @param packageVersion package packageVersion
     * @return the reference to this ProjectFilter instance
     */
    public PackageFilter withPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
        return (this);
    }

    /**
     * Get the query params specified by this filter.
     *
     * @return a GitLabApiForm instance holding the query parameters for this ProjectFilter instance
     */
    public GitLabForm getQueryParams() {
        return (new GitLabForm().withParam("order_by", orderBy).withParam("sort", sort)
                .withParam("exclude_subgroups", excludeSubgroups).withParam("package_type", packageType)
                .withParam("package_name", packageName).withParam("include_versionless", includeVersionless)
                .withParam("status", status).withParam("package_version", packageVersion));
    }

}
