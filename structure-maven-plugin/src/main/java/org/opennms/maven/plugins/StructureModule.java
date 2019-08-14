/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.maven.plugins;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.project.MavenProject;

public class StructureModule extends MavenIdentifier {

    private final String pom;
    private final MavenIdentifier parent;
    private final List<MavenIdentifier> dependencies;

    public StructureModule(MavenProject project) {
        super(project);
        this.pom = project.getFile().getAbsolutePath();
        this.parent = project.getParent() != null ? new MavenIdentifier(project.getParent()) : null;

        dependencies = project.getDependencies().stream()
                .map(MavenIdentifier::new)
                .collect(Collectors.toList());
    }

    public String getPom() {
        return pom;
    }

    public MavenIdentifier getParent() {
        return parent;
    }

    public List<MavenIdentifier> getDependencies() {
        return dependencies;
    }
}
