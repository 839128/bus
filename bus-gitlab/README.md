#### 项目说明

GitLab API (bus-gitlab) provides a full featured and easy to consume Java library for working with GitLab repositories
via the GitLab REST API. Additionally, full support for working with GitLab webhooks and system hooks is also provided.

---

## Table of Contents

* [GitLab Server Version Support](#gitLab-server-version-support)<br/>
* [Using GitLab-API](#using-gitlab4j-api)<br/>
* [Java 8 Requirement](#java-8-requirement)<br/>
* [Javadocs](#javadocs)<br/>
* [Project Set Up](#project-set-up)<br/>
* [Usage Examples](#usage-examples)<br/>
* [Setting Request Timeouts](#setting-request-timeouts)<br/>
* [Connecting Through a Proxy Server](#connecting-through-a-proxy-server)<br/>
* [GitLab API V3 and V4 Support](#gitLab-api-v3-and-v4-support)<br/>
* [Logging of API Requests and Responses](#logging-of-api-requests-and-responses)<br/>
* [Results Paging](#results-paging)<br/>
* [Java 8 Stream Support](#java-8-stream-support)<br/>
* [Eager evaluation example usage](#eager-evaluation-example-usage)<br/>
* [Lazy evaluation example usage](#lazy%20evaluation-example-usage)<br/>
* [Java 8 Optional&lt;T&gt; Support](#java-8-optional-support)<br/>
* [Issue Time Estimates](#issue-time-estimates)<br/>
* [Making API Calls](#making-api-calls)<br/>
* [Available Sub APIs](#available-sub-apis)

---

## GitLab Server Version Support

GitLab-API supports version 11.0+ of GitLab Community Edition [(gitlab-ce)](https://gitlab.com/gitlab-org/gitlab-ce/)
and GitLab Enterprise Edition [(gitlab-ee)](https://gitlab.com/gitlab-org/gitlab-ee/).

GitLab released GitLab Version 11.0 in June of 2018 which included many major changes to GitLab. If you are using GitLab
server earlier than version 11.0, it is highly recommended that you either update your GitLab install or use a version
of this library that was released around the same time as the version of GitLab you are using.

**NOTICE**:
As of GitLab 11.0 support for the GitLab API v3 has been removed from the GitLab server (
see https://about.gitlab.com/2018/06/01/api-v3-removal-impending/). Support for GitLab API v3 will be removed from this
library sometime in 2019. If you are utilizing the v3 support, please update your code to use GitLab API v4.

---

## Using GitLab-API

### **Javadocs**

Javadocs are available
here: [![javadoc.io](https://javadoc.io/badge2/org.gitlab4j/gitlab4j-api/javadoc.io.svg)](https://javadoc.io/doc/org.gitlab4j/gitlab4j-api)

### **Project Set Up**

To utilize GitLab&trade; API in your Java project, simply add the following dependency to your project's build
file:<br />
**Gradle: build.gradle**

```
dependencies {
    ...
    compile group: 'org.miaixz', name: 'bus-gitlab', version: 'x.x.x'
}
```

**Maven: pom.xml**

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-gitlab</artifactId>
    <version>x.x.x</version>
</dependency>
```

### **Usage Examples**

GitLab-API is quite simple to use, all you need is the URL to your GitLab server and the Personal Access Token from your
GitLab Account Settings page. Once you have that info it is as simple as:

```
// Create a GitLabApi instance to communicate with your GitLab server
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.server.com", "YOUR_PERSONAL_ACCESS_TOKEN");

// Get the list of projects your account has access to
List<Project> projects = gitLabApi.getProjectApi().getProjects();
```

You can also login to your GitLab server with username, and password:

```
// Log in to the GitLab server using a username and password
GitLabApi gitLabApi = GitLabApi.oauth2Login("http://your.gitlab.server.com", "username", "password");
```

As of GitLab-API 4.6.6, all API requests support performing the API call as if you were another user, provided you are
authenticated as an administrator:

```
// Create a GitLabApi instance to communicate with your GitLab server (must be an administrator)
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.server.com", "YOUR_PERSONAL_ACCESS_TOKEN");

// sudo as as a different user, in this case the user named "johndoe", all future calls will be done as "johndoe"
gitLabApi.sudo("johndoe")

// To turn off sudo mode
gitLabApi.unsudo();
```

---

### **Setting Request Timeouts**

As of GitLab-API 4.14.21 support has been added for setting the conect and read timeouts for the API client:

```
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.com", "YOUR_PERSONAL_ACCESS_TOKEN", proxyConfig);

// Set the connect timeout to 1 second and the read timeout to 5 seconds
gitLabApi.setRequestTimeout(1000, 5000);
```

---

### **Connecting Through a Proxy Server**

As of GitLab-API 4.8.2 support has been added for connecting to the GitLab server using an HTTP proxy server:

```
// Log in to the GitLab server using a proxy server (with basic auth on proxy)
Map<String, Object> proxyConfig = ProxyClientConfig.createProxyClientConfig(
        "http://your-proxy-server", "proxy-username", "proxy-password");
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.com", "YOUR_PERSONAL_ACCESS_TOKEN", null, proxyConfig);

// Log in to the GitLab server using a proxy server (no auth on proxy)
Map<String, Object> proxyConfig = ProxyClientConfig.createProxyClientConfig("http://your-proxy-server");
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.com", "YOUR_PERSONAL_ACCESS_TOKEN", null, proxyConfig);

// Log in to the GitLab server using an NTLM (Windows DC) proxy
Map<String, Object> ntlmProxyConfig = ProxyClientConfig.createNtlmProxyClientConfig(
        "http://your-proxy-server", "windows-username", "windows-password", "windows-workstation", "windows-domain");
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.com", "YOUR_PERSONAL_ACCESS_TOKEN", null, ntlmProxyConfig);
```

See the Javadoc on the GitLabApi class for a complete list of methods accepting the proxy configuration (
clientConfiguration parameter)

---

### **GitLab API V3 and V4 Support**

As of GitLab-API 4.2.0 support has been added for GitLab API V4. If your application requires GitLab API V3, you can
still use GitLab-API by creating your GitLabApi instance as follows:

```
// Create a GitLabApi instance to communicate with your GitLab server using GitLab API V3
GitLabApi gitLabApi = new GitLabApi(ApiVersion.V3, "http://your.gitlab.server.com", "YOUR_PRIVATE_TOKEN");
```

**NOTICE**:
As of GitLab 11.0 support for the GitLab API v3 has been removed from the GitLab server (
see https://about.gitlab.com/2018/06/01/api-v3-removal-impending/). Support for GitLab API v3 will be removed from this
library sometime in 2019. If you are utilizing the v3 support, please update your code to use GitLab API v4.

---

### **Logging of API Requests and Responses**

As of GitLab-API 4.8.39 support has been added to log the requests to and the responses from the GitLab API. Enable
logging using one of the following methods on the GitLabApi instance:

```
GitLabApi gitLabApi = new GitLabApi("http://your.gitlab.server.com", "YOUR_PERSONAL_ACCESS_TOKEN");

// Log using the shared logger and default level of FINE
gitLabApi.enableRequestResponseLogging();

// Log using the shared logger and the INFO level
gitLabApi.enableRequestResponseLogging(java.util.logging.Level.INFO);

// Log using the specified logger and the INFO level
gitLabApi.enableRequestResponseLogging(yourLoggerInstance, java.util.logging.Level.INFO);

// Log using the shared logger, at the INFO level, and include up to 1024 bytes of entity logging
gitLabApi.enableRequestResponseLogging(java.util.logging.Level.INFO, 1024);

// Log using the specified logger, at the INFO level, and up to 1024 bytes of entity logging
gitLabApi.enableRequestResponseLogging(yourLoggerInstance, java.util.logging.Level.INFO, 1024);
```

---

### **Results Paging**

GitLab-API provides an easy to use paging mechanism to page through lists of results from the GitLab API. Here are a
couple of examples on how to use the Pager:

```
// Get a Pager instance that will page through the projects with 10 projects per page
Pager<Project> projectPager = gitlabApi.getProjectsApi().getProjects(10);

// Iterate through the pages and print out the name and description
while (projectsPager.hasNext())) {
    for (Project project : projectPager.next()) {
        System.out.println(project.getName() + " -: " + project.getDescription());
    }
}
```

As of GitLab-API 4.9.2, you can also fetch all the items as a single list using a Pager instance:

```
// Get a Pager instance so we can load all the projects into a single list, 10 items at a time:
Pager<Project> projectPager = gitlabApi.getProjectsApi().getProjects(10);
List<Project> allProjects = projectPager.all();
```

---

### **Java 8 Stream Support**

As of GitLab-API 4.9.2, all GitLabJ-API methods that return a List result have a similarlly named method that returns a
Java 8 Stream. The Stream returning methods use the following naming convention:```getXxxxxStream()```.

**IMPORTANT**
The built-in methods that return a Stream do so using ___eager evaluation___, meaning all items are pre-fetched from the
GitLab server and a Stream is returned which will stream those items.  **Eager evaluation does NOT support parallel
reading of data from ther server, it does however allow for parallel processing of the Stream post data fetch.**

To stream using ___lazy evaluation___, use the GitLab-API methods that return a```Pager``` instance, and then call
the```lazyStream()``` method on the```Pager``` instance to create a lazy evaluation Stream. The Stream utilizes
the```Pager``` instance to page through the available items. **A lazy Stream does NOT support parallel operations or
skipping.**

#### **Eager evaluation condition usage:**

```
// Stream the visible projects printing out the project name.
Stream<Project> projectStream = gitlabApi.getProjectApi().getProjectsStream();
projectStream.map(Project::getName).forEach(name -> System.out.println(name));

// Operate on the stream in parallel, this condition sorts User instances by username
// NOTE: Fetching of the users is not done in parallel,
// only the sorting of the users is a parallel operation.
Stream<User> stream = gitlabApi.getUserApi().getUsersStream();
List<User> users = stream.parallel().sorted(comparing(User::getUsername)).collect(toList());
```

#### **Lazy evaluation condition usage:**

```
// Get a Pager instance to that will be used to lazily stream Project instances.
// In this condition, 10 Projects per page will be pre-fetched.
Pager<Project> projectPager = gitlabApi.getProjectApi().getProjects(10);

// Lazily stream the Projects, printing out each project name, limit the output to 5 project names
projectPager.lazyStream().limit(5).map(Project::getName).forEach(name -> System.out.println(name));
```

---

### **Java 8 Optional Support**

GitLab-API supports Java 8 Optional&lt;T&gt; for API calls that result in the return of a single item. Here is an
condition on how to use the Java 8 Optional&lt;T&gt; API calls:

```
Optional<Group> optionalGroup =  gitlabApi.getGroupApi().getOptionalGroup("my-group-path");
if (optionalGroup.isPresent())
    return optionalGroup.get();

return gitlabApi.getGroupApi().addGroup("my-group-name", "my-group-path");
```

---

### **Issue Time Estimates**

GitLab issues allow for time tracking. The following time units are currently available:

* months (mo)
* weeks (w)
* days (d)
* hours (h)
* minutes (m)

Conversion rates are 1mo = 4w, 1w = 5d and 1d = 8h.

---

## Making API Calls

The API has been broken up into sub API classes to make it easier to consume and to separate concerns. The GitLab sub
API classes typically have a one-to-one relationship with the API documentation
at [GitLab API](https://docs.gitlab.com/ce/api/). Following is a sample of the GitLab sub API class mapping to the
GitLab API documentation:

```GroupApi``` -> https://docs.gitlab.com/ce/api/groups.html<br/>
```MergeRequestApi``` -> https://docs.gitlab.com/ce/api/merge_requests.html<br/>
```ProjectApi``` -> https://docs.gitlab.com/ce/api/projects.html<br/>
```UserApi``` -> https://docs.gitlab.com/ce/api/users.html<br/>

### **Available Sub APIs**

The following is a list of the available sub APIs along with a sample use of each API. See
the <a href="https://javadoc.io/doc/org.gitlab4j/gitlab4j-api" target="_top">Javadocs</a> for a complete list of
available methods for each sub API.

---
&nbsp;&nbsp;[ApplicationsApi](#applicationsapi)<br/>
&nbsp;&nbsp;[ApplicationSettingsApi](#applicationsettingsapi)<br/>
&nbsp;&nbsp;[AwardEmojiApi](#awardemojiapi)<br/>
&nbsp;&nbsp;[BoardsApi](#boardsapi)<br/>
&nbsp;&nbsp;[CommitsApi](#commitsapi)<br/>
&nbsp;&nbsp;[ContainerRegistryApi](#containerregistryapi)<br/>
&nbsp;&nbsp;[DeployKeysApi](#deploykeysapi)<br/>
&nbsp;&nbsp;[DiscussionsApi](#discussionsapi)<br/>
&nbsp;&nbsp;[EnvironmentsApi](#environmentsapi)<br/>
&nbsp;&nbsp;[EpicsApi](#epicsapi)<br/>
&nbsp;&nbsp;[EventsApi](#eventsapi)<br/>
&nbsp;&nbsp;[GroupApi](#groupapi)<br/>
&nbsp;&nbsp;[HealthCheckApi](#healthcheckapi)<br/>
&nbsp;&nbsp;[ImportExportApi](#importexportapi)<br/>
&nbsp;&nbsp;[IssuesApi](#issuesapi)<br/>
&nbsp;&nbsp;[JobApi](#jobapi)<br/>
&nbsp;&nbsp;[LabelsApi](#labelsapi)<br/>
&nbsp;&nbsp;[LicenseApi](#licenseapi)<br/>
&nbsp;&nbsp;[LicenseTemplatesApi](#licensetemplatesapi)<br/>
&nbsp;&nbsp;[LabelsApi](#labelsapi)<br/>
&nbsp;&nbsp;[MergeRequestApi](#mergerequestapi)<br/>
&nbsp;&nbsp;[MilestonesApi](#milestonesapi)<br/>
&nbsp;&nbsp;[NamespaceApi](#namespaceapi)<br/>
&nbsp;&nbsp;[NotesApi](#notesapi)<br/>
&nbsp;&nbsp;[NotificationSettingsApi](#notificationsettingsapi)<br/>
&nbsp;&nbsp;[PackagesApi](#packagesapi)<br/>
&nbsp;&nbsp;[PipelineApi](#pipelineapi)<br/>
&nbsp;&nbsp;[ProjectApi](#projectapi)<br/>
&nbsp;&nbsp;[ProtectedBranchesApi](#protectedbranchesapi)<br/>
&nbsp;&nbsp;[ReleasesApi](#releasesapi)<br/>
&nbsp;&nbsp;[RepositoryApi](#repositoryapi)<br/>
&nbsp;&nbsp;[RepositoryFileApi](#repositoryfileapi)<br/>
&nbsp;&nbsp;[ReourceLabelEventsApi](#resourcelabeleventsapi)<br/>
&nbsp;&nbsp;[RunnersApi](#runnersapi)<br/>
&nbsp;&nbsp;[SearchApi](#searchapi)<br/>
&nbsp;&nbsp;[ServicesApi](#servicesapi)<br/>
&nbsp;&nbsp;[SessionApi](#sessionapi)<br/>
&nbsp;&nbsp;[SnippetsApi](#snippetsapi)<br/>
&nbsp;&nbsp;[SystemHooksApi](#systemhooksapi)<br/>
&nbsp;&nbsp;[TagsApi](#tagsapi)<br/>
&nbsp;&nbsp;[TodosApi](#todosapi)<br/>
&nbsp;&nbsp;[UserApi](#userapi)<br/>
&nbsp;&nbsp;[WikisApi](#wikisapi)


### Sub API Examples
----------------

#### ApplicationsApi

```
// Add an OAUTH Application to GitLab
ApplicationScope[] scopes = {ApplicationScope.SUDO, ApplicationScope.PROFILE};
gitLabApi.getApplicationsApi().createApplication("My OAUTH Application", "https//condition.com/myapp/callback", scopes);
```

#### ApplicationSettingsApi

```
// Get the current GitLab server application settings
ApplicationSettings appSettings = gitLabApi.getApplicationSettingsApi().getAppliationSettings();
```

#### AwardEmojiApi

```
// Get a list of AwardEmoji belonging to the specified issue (group ID = 1, issues IID = 1)
List<AwardEmoji> awardEmojis = gitLabApi.getAwardEmojiApi().getIssuAwardEmojis(1, 1);
```

#### BoardsApi

```
// Get a list of the Issue Boards belonging to the specified project
List<Board> boards = gitLabApi.getBoardsApi().getBoards(projectId);
```

#### CommitsApi

```
// Get a list of commits associated with the specified branch that fall within the specified time window
// This uses the ISO8601 date utilities the in ISO8601 class
Date since = ISO8601.toDate("2017-01-01T00:00:00Z");
Date until = new Date(); // now
List<Commit> commits = gitLabApi.getCommitsApi().getCommits(1234, "new-feature", since, until);
```

#### ContainerRegistryApi

```
// Get a list of the registry repositories belonging to the specified project
List<RegistryRepository> registryRepos = gitLabApi.ContainerRegistryApi().getRepositories(projectId);
```

#### DeployKeysApi

```
// Get a list of DeployKeys for the authenticated user
List<DeployKey> deployKeys = gitLabApi.getDeployKeysApi().getDeployKeys();
```

#### DiscussionsApi

```
// Get a list of Discussions for the specified merge request
List<Discussion> discussions = gitLabApi.getDiscussionsApi().getMergeRequestDiscussions(projectId, mergeRequestIid);
```

#### EnvironmentsApi

```
// Get a list of Environments for the specified project
List<Environment> environments = gitLabApi.getEnvironmentsApi().getEnvironments(projectId);
```

#### EpicsApi

```
// Get a list epics of the requested group and its subgroups.
List<Epic> epics = gitLabApi.getEpicsApi().getEpics(1);
```

#### EventsApi

```
// Get a list of Events for the authenticated user
Date after = new Date(0); // After Epoch
Date before = new Date(); // Before now
List<Event> events = gitLabApi.getEventsApi().getAuthenticatedUserEvents(null, null, before, after, DESC);
```

#### GroupApi

```
// Get a list of groups that you have access to
List<Group> groups = gitLabApi.getGroupApi().getGroups();
```

#### HealthCheckApi

```
// Get the liveness endpoint health check results. Assumes ip_whitelisted per:
// https://docs.gitlab.com/ee/administration/monitoring/ip_whitelist.html
HealthCheckInfo healthCheck = gitLabApi.getHealthCheckApi().getLiveness();
```

#### ImportExportApi

```
// Schedule a project export for the specified project ID
gitLabApi.getImportExportApi().scheduleExport(projectId);

// Get the project export status for the specified project ID
ExportStatus exportStatus = gitLabApi.getImportExportApi().getExportStatus(projectId);
```

#### IssuesApi

```
// Get a list of issues for the specified project ID
List<Issue> issues = gitLabApi.getIssuesApi().getIssues(1234);
```

#### JobApi

```
// Get a list of jobs for the specified project ID
List<Job> jobs = gitLabApi.getJobApi().getJobs(1234);
```

#### LabelsApi

```
// Get a list of labels for the specified project ID
List<Label> labels = gitLabApi.getLabelsApi().getLabels(1234);
```

#### LicenseApi

```
// Retrieve information about the current license
License license = gitLabApi.getLicenseApi().getLicense();
```

#### LicenseTemplatesApi

```
// Get a list of open sourcse license templates
List<LicenseTemplate> licenses = gitLabApi.getLicenseTemplatesApi().getLicenseTemplates();
```

#### MergeRequestApi

```
// Get a list of the merge requests for the specified project
List<MergeRequest> mergeRequests = gitLabApi.getMergeRequestApi().getMergeRequests(1234);
```

#### MilestonesApi

```
// Get a list of the milestones for the specified project
List<Milestone> milestones = gitLabApi.getMilestonesApi().getMilestones(1234);
```

#### NamespaceApi

```
// Get all namespaces that match "foobar" in their name or path
List<Namespace> namespaces = gitLabApi.getNamespaceApi().findNamespaces("foobar");
```

#### NotesApi

```
// Get a list of the issues's notes for project ID 1234, issue IID 1
List<Note> notes = gitLabApi.getNotesApi().getNotes(1234, 1);
```

#### NotificationSettingsApi

```
// Get the current global notification settings
NotificationSettings settings = gitLabApi.getNotificationSettingsApi().getGlobalNotificationSettings();
```

#### PackagesApi

```
// Get all packages for the specified project ID
List<Packages> packages = gitLabApi.getPackagesApi().getPackages(1234);
```

#### PipelineApi

```
// Get all pipelines for the specified project ID
List<Pipeline> pipelines = gitLabApi.getPipelineApi().getPipelines(1234);
```

#### ProjectApi

```
// Get a list of accessible projects
public List<Project> projects = gitLabApi.getProjectApi().getProjects();
```

```
// Create a new project
Project projectSpec = new Project()
    .withName("my-project")
    .withDescription("My project for demonstration.")
    .withIssuesEnabled(true)
    .withMergeRequestsEnabled(true)
    .withWikiEnabled(true)
    .withSnippetsEnabled(true)
    .withPublic(true);

Project newProject = gitLabApi.getProjectApi().createProject(projectSpec);
```

#### ProtectedBranchesApi

```
List<ProtectedBranch> branches = gitLabApi.getProtectedBranchesApi().getProtectedBranches(project.getId());
```

#### ReleasesApi

```
// Get a list of releases for the specified project
List<Release> releases = gitLabApi.getReleasesApi().getReleases(projectId);
```

#### RepositoryApi

```
// Get a list of repository branches from a project, sorted by name alphabetically
List<Branch> branches = gitLabApi.getRepositoryApi().getBranches(projectId);
```

```
// Search repository branches from a project, by name
List<Branch> branches = gitLabApi.getRepositoryApi().getBranches(projectId, searchTerm);
```

#### RepositoryFileApi

```
// Get info (name, size, ...) and the content from a file in repository
RepositoryFile file = gitLabApi.getRepositoryFileApi().getFile("file-path", 1234, "ref");
```

#### ResourceLabelEventsApi

```
// Get the label events for the specified merge request
List<LabelEvent> labelEvents = gitLabApi.getResourceLabelEventsApi()
        .getMergeRequestLabelEvents(projectId, mergeRequestIid);
```

#### RunnersApi

```
// Get All Runners.
List<Runner> runners = gitLabApi.getRunnersApi().getAllRunners();
```

#### SearchApi

```
// Do a global search for Projects
List<?> projects = gitLabApi.getSearchApi().globalSearch(SearchScope.PROJECTS, "text-to-search-for");
```

#### ServicesApi

```
// Activate/Update the Slack Notifications service
SlackService slackService =  new SlackService()
        .withMergeRequestsEvents(true)
        .withWebhook("https://hooks.slack.com/services/ABCDEFGHI/KJLMNOPQR/wetrewq7897HKLH8998wfjjj")
        .withUsername("GitLab");
gitLabApi.getServicesApi().updateSlackService("project-path", slackService);
```

#### SessionApi

```
// Log in to the GitLab server and get the session info
gitLabApi.getSessionApi().login("your-username", "your-email", "your-password");
```

#### SnippetsApi

```
// Get a list of the authenticated user's snippets
List<Snippet> snippets = gitLabApi.getSnippetsApi().getSnippets();
```

#### SystemHooksApi

```
// Get a list of installed system hooks
List<SystemHook> hooks = gitLabApi.getSystemHooksApi().getSystemHooks();
```

#### TagsApi

```
// Get a list of tags for the specified project ID
List<Tag> tags = gitLabApi.getTagsApi().getTags(projectId);
```

#### TodosApi

```
// Get a list of all pending todos for the current user
List<Todo> todos = gitLabApi.getTodosApi().gePendingTodos();
```

#### UserApi

```
// Get the User info for user_id 1
User user = gitLabApi.getUserApi().getUser(1);

// Create a new user with no password who will recieve a reset password email
User userConfig = new User()
    .withEmail("jdoe@condition.com")
    .withName("Jane Doe")
    .withUsername("jdoe");
String password = null;
boolean sendResetPasswordEmail = true;
gitLabApi.getUserApi().createUser(userConfig, password, sendResetPasswordEmail);
```

#### WikisApi

```
// Get a list of pages in project wiki
List<WikiPage> wikiPages = gitLabApi.getWikisApi().getPages();
```
