/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.shell.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.PagedResources;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.table.BeanListModel;
import org.springframework.shell.table.BorderFactory;
import org.springframework.shell.table.BorderSpecification;
import org.springframework.shell.table.CellMatchers;
import org.springframework.shell.table.KeyValueHorizontalAligner;
import org.springframework.shell.table.KeyValueSizeConstraints;
import org.springframework.shell.table.MapFormatter;
import org.springframework.shell.table.NoWrapSizeConstraints;
import org.springframework.shell.table.SimpleHorizontalAligner;
import org.springframework.shell.table.TableModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.xd.rest.client.RuntimeOperations;
import org.springframework.xd.rest.domain.DetailedContainerResource;
import org.springframework.xd.rest.domain.ModuleMetadataResource;
import org.springframework.xd.shell.XDShell;
import org.springframework.xd.shell.util.Table;
import org.springframework.xd.shell.util.TableHeader;
import org.springframework.xd.shell.util.TableRow;

/**
 * Commands to interact with cluster for containers/modules.
 * 
 * @author Ilayaperumal Gopinathan
 */
@Component
public class RuntimeCommands implements CommandMarker {

	private static final String LIST_CONTAINERS = "runtime containers";

	private static final String LIST_MODULES = "runtime modules";

	@Autowired
	private XDShell xdShell;

	@CliAvailabilityIndicator({ LIST_CONTAINERS, LIST_MODULES })
	public boolean available() {
		return xdShell.getSpringXDOperations() != null;
	}

	@CliCommand(value = LIST_CONTAINERS, help = "List runtime containers")
	public Table listContainers() {
		final PagedResources<DetailedContainerResource> containers = runtimeOperations().listContainers();
		final Table table = new Table();
		table.addHeader(1, new TableHeader("Container Id"))
				.addHeader(2, new TableHeader("Host"))
				.addHeader(3, new TableHeader("IP Address"))
				.addHeader(4, new TableHeader("PID"))
				.addHeader(5, new TableHeader("Groups"))
				.addHeader(6, new TableHeader("Custom Attributes"));
		for (DetailedContainerResource container : containers) {
			Map<String, String> copy = new HashMap<String, String>(container.getAttributes());
			final TableRow row = table.newRow();
			row.addValue(1, copy.remove("id"))
					.addValue(2, copy.remove("host"))
					.addValue(3, copy.remove("ip"))
					.addValue(4, copy.remove("pid"));
			String groups = copy.remove("groups");
			row.addValue(5, groups == null ? "" : groups);
			row.addValue(6, copy.isEmpty() ? "" : copy.toString());
		}
		return table;
	}

	@CliCommand(value = LIST_MODULES, help = "List runtime modules")
	public String listDeployedModules(@CliOption(key="newTable", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help = "use new table") boolean newTable,
			@CliOption(key="width", unspecifiedDefaultValue = "125", help = "term width") int width,
			@CliOption(mandatory = false, key = { "containerId" }, help = "to filter by container id") String containerId,
			@CliOption(mandatory = false, key = { "moduleId" }, help = "to filter by module id") String moduleId) {
		Iterable<ModuleMetadataResource> runtimeModules;
		if (StringUtils.hasText(containerId) && StringUtils.hasText(moduleId)) {
			runtimeModules = Collections.singletonList(runtimeOperations().listDeployedModule(containerId, moduleId));
		}
		else if (StringUtils.hasText(containerId)) {
			runtimeModules = runtimeOperations().listDeployedModulesByContainer(containerId);
		}
		else if (StringUtils.hasText(moduleId)) {
			runtimeModules = runtimeOperations().listDeployedModulesByModuleId(moduleId);
		}
		else {
			runtimeModules = runtimeOperations().listDeployedModules();
		}
		final Table table = new Table();
		table.addHeader(1, new TableHeader("Module Id")).addHeader(2,
				new TableHeader("Container Id")).addHeader(3, new TableHeader("Options")).addHeader(4,
				new TableHeader("Deployment Properties")).addHeader(5, new TableHeader("Unit status"));
		for (ModuleMetadataResource module : runtimeModules) {
			final TableRow row = table.newRow();
			String unitStatus = (module.getDeploymentStatus() != null) ? module.getDeploymentStatus().name() : "";
			row.addValue(1, String.format("%s.%s.%s", module.getUnitName(), module.getModuleType(), module.getName()))
					.addValue(2, module.getContainerId()).addValue(3, module.getModuleOptions().toString()).addValue(4,
							module.getDeploymentProperties().toString()).addValue(5, unitStatus);
		}


		LinkedHashMap<String, Object> header = new LinkedHashMap<>();
		header.put("moduleId", "Module Id");
		header.put("containerId", "Container Id");
		header.put("moduleOptions", "Options");
		header.put("deploymentProperties", "Deployment Properties");
		header.put("deploymentStatus", "Unit Status");

		TableModel tableModel = new BeanListModel<>(ModuleMetadataResource.class, runtimeModules, header);
		org.springframework.shell.table.Table table2 = new org.springframework.shell.table.Table(tableModel);

		table2.align(CellMatchers.row(0), new SimpleHorizontalAligner(SimpleHorizontalAligner.Align.center));

		table2.format(CellMatchers.ofType(Map.class), new MapFormatter(" = "));
		table2.align(CellMatchers.ofType(Map.class), new KeyValueHorizontalAligner(" = "));
		table2.size(CellMatchers.ofType(Map.class), new KeyValueSizeConstraints(" = "));

		table2.align(CellMatchers.column(4), new SimpleHorizontalAligner(SimpleHorizontalAligner.Align.center));
		BorderFactory.headerAndVerticals(table2);

		width = jline.TerminalFactory.get().getWidth();
		return newTable ? table2.render(width) : table.toString();




//		return table;
	}

	private RuntimeOperations runtimeOperations() {
		return xdShell.getSpringXDOperations().runtimeOperations();
	}

}
