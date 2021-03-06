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

package org.mule.devkit.generation;

import org.mule.devkit.model.code.CodeModel;
import org.mule.devkit.model.code.DefinedClass;
import org.mule.devkit.model.code.writer.FilerCodeWriter;
import org.mule.devkit.model.schema.SchemaModel;
import org.mule.devkit.utils.NameUtils;
import org.mule.devkit.utils.TypeMirrorUtils;

import javax.annotation.processing.Filer;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratorContext {
    private CodeModel codeModel;
    private SchemaModel schemaModel;
    private List<DefinedClass> registerAtBoot;
    private Map<String, DefinedClass> roles;
    private Types types;
    private Elements elements;
    private TypeMirrorUtils typeMirrorUtils;
    private NameUtils nameUtils;

    public GeneratorContext(Filer filer, Types types, Elements elements) {
        this.registerAtBoot = new ArrayList<DefinedClass>();
        this.codeModel = new CodeModel(new FilerCodeWriter(filer));
        this.schemaModel = new SchemaModel(new FilerCodeWriter(filer));
        this.roles = new HashMap<String, DefinedClass>();
        this.elements = elements;
        this.types = types;
        this.typeMirrorUtils = new TypeMirrorUtils(this.types);
        this.nameUtils = new NameUtils(this.elements);
    }

    public CodeModel getCodeModel() {
        return codeModel;
    }

    public List<DefinedClass> getRegisterAtBoot() {
        return registerAtBoot;
    }

    public void registerAtBoot(DefinedClass clazz) {
        this.registerAtBoot.add(clazz);
    }

    public SchemaModel getSchemaModel() {
        return schemaModel;
    }

    public Types getTypeUtils() {
        return this.types;
    }

    public Elements getElementsUtils() {
        return this.elements;
    }

    public void setClassRole(String role, DefinedClass clazz) {
        this.roles.put(role, clazz);
    }

    public DefinedClass getClassForRole(String role) {
        return this.roles.get(role);
    }

    public TypeMirrorUtils getTypeMirrorUtils() {
        return typeMirrorUtils;
    }

    public NameUtils getNameUtils() {
        return nameUtils;
    }
}
