/*******************************************************************************
 *
 * Copyright (c) 2011 Oracle Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * Paul Nyheim, Nikita Levyankov
 *
 *******************************************************************************/
package hudson.plugins.git.browser;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.browsers.QueryBuilder;
import hudson.util.FormValidation;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import hudson.plugins.git.GitChangeSet;
import hudson.plugins.git.GitChangeSet.Path;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class ViewGitWeb extends GitRepositoryBrowser {

    private static final long serialVersionUID = 1L;
    private final URL url;
    private final String projectName;

    @DataBoundConstructor
    public ViewGitWeb(String url, String projectName) throws MalformedURLException {
        this.url = normalizeToEndWithSlash(new URL(url));
        this.projectName = projectName;
    }

    @Override
    public URL getDiffLink(Path path) throws IOException {
        if (path.getEditType() == EditType.EDIT) {
            String spec = buildCommitDiffSpec(path);
            return new URL(url, url.getPath() + spec);
        }
        return null;
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
        if (path.getEditType() == EditType.DELETE) {
            String spec = buildCommitDiffSpec(path);
            return new URL(url, url.getPath() + spec);
        }
        String spec = param().add("p=" + projectName).add("a=viewblob").add("h=" + path.getDst()).add("f=" +  path.getPath()).toString();
        return new URL(url, url.getPath() + spec);
    }

    private String buildCommitDiffSpec(Path path)
        throws UnsupportedEncodingException {
        return param().add("p=" + projectName).add("a=commitdiff").add("h=" + path.getChangeSet().getId()).toString()
            + "#" + URLEncoder.encode(path.getPath(), "UTF-8").toString();
    }

    @Override
    public URL getChangeSetLink(GitChangeSet changeSet) throws IOException {
        return new URL(url,
            url.getPath() + param().add("p=" + projectName).add("a=commit").add("h=" + changeSet.getId()).toString());
    }

    private QueryBuilder param() {
        return new QueryBuilder(url.getQuery());
    }

    public URL getUrl() {
        return url;
    }

    public String getProjectName() {
        return projectName;
    }

    @Extension
    public static class ViewGitWebDescriptor extends Descriptor<RepositoryBrowser<?>> {
        public String getDisplayName() {
            return "viewgit";
        }

        @Override
        public ViewGitWeb newInstance(StaplerRequest req, JSONObject jsonObject) throws FormException {
            return req.bindParameters(ViewGitWeb.class, "viewgit.");
        }

        public FormValidation doCheckUrl(@QueryParameter(fixEmpty = true) final String url)
            throws IOException, ServletException {
            return new GitUrlChecker(url, "ViewGit").check();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ViewGitWeb that = (ViewGitWeb) o;

        return new EqualsBuilder()
            .append(url, that.url)
            .append(projectName, that.projectName)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
