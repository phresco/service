/**
 * Phresco Service
 *
 * Copyright (C) 1999-2013 Photon Infotech Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.photon.phresco.service.api;

import java.io.File;

import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.exception.PhrescoException;

public interface DependencyProcessor {

	/**
	 * Uses the {@link ProjectInfo} object to identify the dependent modules, libraries, technology, etc and handle
	 * them appropriately in the specified Project path
	 * @param info {@link ProjectInfo} object
	 * @param projectPath Path of project to be created.
	 * @throws PhrescoException
	 */
	void process(ApplicationInfo applicationInfo, File projectPath) throws PhrescoException;

}
