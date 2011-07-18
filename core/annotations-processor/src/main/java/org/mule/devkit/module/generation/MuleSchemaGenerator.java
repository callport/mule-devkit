/**
 * Mule Development Kit
 * Copyright 2010-2011 (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mule.devkit.module.generation;

import org.mule.devkit.generation.GenerationException;
import org.mule.devkit.model.schema.SchemaLocation;

import javax.lang.model.element.Element;

public class MuleSchemaGenerator extends AbstractModuleGenerator {
    public void generate(Element element) throws GenerationException {
        //SchemaLocation schemaLocation = new SchemaLocation(null, "META-INF/mule.xsd", "http://www.mulesoft.org/schema/mule/core/3.1/mule.xsd", null);

        //context.getSchemaModel().addSchemaLocation(schemaLocation);

    }
}
