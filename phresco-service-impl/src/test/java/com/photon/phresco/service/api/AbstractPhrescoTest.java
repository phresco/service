/**
 * Phresco Service Implemenation
 *
 * Copyright (C) 1999-2014 Photon Infotech Inc.
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

import org.junit.After;
import org.junit.Before;

import com.photon.phresco.exception.PhrescoException;

/**
 * Abstract class for all server tests.
 *
 */
public class AbstractPhrescoTest {

    ArchetypeExecutor archetypeExecutor = null;

    @Before
    public void setUp() throws PhrescoException {
        //the setup method need not static here. ignore the compilation warning.
        PhrescoServerFactory.initialize();
        archetypeExecutor = PhrescoServerFactory.getArchetypeExecutor();

        //To Initialize the cache
//        PhrescoServerFactory.getRepositoryManager().getApplicationTypes();
    }

    @After
    public void tearDown() throws PhrescoException {
    }

}
