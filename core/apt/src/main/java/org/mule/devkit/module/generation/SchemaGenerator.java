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

import org.mule.devkit.annotations.Configurable;
import org.mule.devkit.annotations.Module;
import org.mule.devkit.annotations.Parameter;
import org.mule.devkit.annotations.Processor;
import org.mule.devkit.annotations.Source;
import org.mule.devkit.annotations.SourceCallback;
import org.mule.devkit.annotations.Transformer;
import org.mule.devkit.generation.GenerationException;
import org.mule.devkit.model.schema.All;
import org.mule.devkit.model.schema.Annotation;
import org.mule.devkit.model.schema.Any;
import org.mule.devkit.model.schema.Attribute;
import org.mule.devkit.model.schema.ComplexContent;
import org.mule.devkit.model.schema.ComplexType;
import org.mule.devkit.model.schema.Documentation;
import org.mule.devkit.model.schema.Element;
import org.mule.devkit.model.schema.ExplicitGroup;
import org.mule.devkit.model.schema.ExtensionType;
import org.mule.devkit.model.schema.FormChoice;
import org.mule.devkit.model.schema.Import;
import org.mule.devkit.model.schema.LocalComplexType;
import org.mule.devkit.model.schema.LocalSimpleType;
import org.mule.devkit.model.schema.NumFacet;
import org.mule.devkit.model.schema.ObjectFactory;
import org.mule.devkit.model.schema.Pattern;
import org.mule.devkit.model.schema.Restriction;
import org.mule.devkit.model.schema.Schema;
import org.mule.devkit.model.schema.SchemaLocation;
import org.mule.devkit.model.schema.SimpleType;
import org.mule.devkit.model.schema.TopLevelComplexType;
import org.mule.devkit.model.schema.TopLevelElement;
import org.mule.devkit.model.schema.TopLevelSimpleType;
import org.mule.devkit.model.schema.Union;
import org.mule.util.StringUtils;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.math.BigInteger;

public class SchemaGenerator extends AbstractModuleGenerator {
    private Schema schema;
    private ObjectFactory objectFactory;

    public SchemaGenerator() {
        schema = new Schema();
        objectFactory = new ObjectFactory();
    }

    public void generate(javax.lang.model.element.Element element) throws GenerationException {
        Module module = element.getAnnotation(Module.class);
        String targetNamespace = module.namespace();
        if (targetNamespace == null || targetNamespace.length() == 0) {
            targetNamespace = SchemaConstants.BASE_NAMESPACE + module.name();
        }

        schema.setTargetNamespace(targetNamespace);
        schema.setElementFormDefault(FormChoice.QUALIFIED);
        schema.setAttributeFormDefault(FormChoice.UNQUALIFIED);

        importXmlNamespace();
        importSpringFrameworkNamespace();
        importMuleNamespace();
        importMuleSchemaDocNamespace();

        registerTypes();
        registerConfigElement(element);
        registerProcessors(targetNamespace, element);
        registerTransformers(element);

        String fileName = "META-INF/mule-" + module.name() + ".xsd";

        String location = module.schemaLocation();
        if (location == null || location.length() == 0) {
            location = schema.getTargetNamespace() + "/" + module.version() + "/mule-" + module.name() + ".xsd";
        }

        String namespaceHandlerName = context.getNameUtils().generateClassName((TypeElement) element, "NamespaceHandler");

        SchemaLocation schemaLocation = new SchemaLocation(schema, fileName, location, namespaceHandlerName);

        context.getSchemaModel().addSchemaLocation(schemaLocation);
    }

    private void registerTransformers(javax.lang.model.element.Element type) {
        java.util.List<ExecutableElement> methods = ElementFilter.methodsIn(type.getEnclosedElements());
        for (ExecutableElement method : methods) {
            Transformer transformer = method.getAnnotation(Transformer.class);
            if (transformer == null)
                continue;

            Element transformerElement = registerTransformer(context.getNameUtils().uncamel(method.getSimpleName().toString()));
            schema.getSimpleTypeOrComplexTypeOrGroup().add(transformerElement);
        }
    }

    private void registerProcessors(String targetNamespace, javax.lang.model.element.Element type) {
        java.util.List<ExecutableElement> methods = ElementFilter.methodsIn(type.getEnclosedElements());
        for (ExecutableElement method : methods) {
            Processor processor = method.getAnnotation(Processor.class);
            if (processor == null)
                continue;

            String name = method.getSimpleName().toString();
            if (processor.name().length() > 0)
                name = processor.name();
            String typeName = StringUtils.capitalize(name) + "Type";

            registerProcessorElement(targetNamespace, name, typeName);

            registerProcessorType(targetNamespace, typeName, method);
        }

        for (ExecutableElement method : methods) {
            Source source = method.getAnnotation(Source.class);
            if (source == null)
                continue;

            String name = method.getSimpleName().toString();
            if (source.name().length() > 0)
                name = source.name();
            String typeName = StringUtils.capitalize(name) + "Type";

            registerSourceElement(targetNamespace, name, typeName);

            registerSourceType(targetNamespace, typeName, method);
        }

    }

    private void registerProcessorElement(String targetNamespace, String name, String typeName) {
        Element element = new TopLevelElement();
        element.setName(context.getNameUtils().uncamel(name));
        element.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_MESSAGE_PROCESSOR);
        element.setType(new QName(targetNamespace, typeName));

        schema.getSimpleTypeOrComplexTypeOrGroup().add(element);
    }

    private void registerSourceElement(String targetNamespace, String name, String typeName) {
        Element element = new TopLevelElement();
        element.setName(context.getNameUtils().uncamel(name));
        element.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_INBOUND_ENDPOINT);
        element.setType(new QName(targetNamespace, typeName));

        schema.getSimpleTypeOrComplexTypeOrGroup().add(element);
    }

    private void registerProcessorType(String targetNamespace, String name, ExecutableElement element) {
        registerExtendedType(SchemaConstants.MULE_ABSTRACT_MESSAGE_PROCESSOR_TYPE, targetNamespace, name, element);
    }

    private void registerSourceType(String targetNamespace, String name, ExecutableElement element) {
        registerExtendedType(SchemaConstants.MULE_ABSTRACT_INBOUND_ENDPOINT_TYPE, targetNamespace, name, element);
    }

    private void registerExtendedType(QName base, String targetNamespace, String name, ExecutableElement element) {
        TopLevelComplexType complexType = new TopLevelComplexType();
        complexType.setName(name);

        ComplexContent complexContent = new ComplexContent();
        complexType.setComplexContent(complexContent);
        ExtensionType complexContentExtension = new ExtensionType();
        complexContentExtension.setBase(base);
        complexContent.setExtension(complexContentExtension);

        Attribute configRefAttr = createAttribute("config-ref", true, SchemaConstants.STRING, "Specify which configuration to use for this invocation.");
        complexContentExtension.getAttributeOrAttributeGroup().add(configRefAttr);

        All all = new All();
        complexContentExtension.setAll(all);

        if (element.getKind() == ElementKind.METHOD) {
            for (VariableElement variable : ((ExecutableElement) element).getParameters()) {
                if (variable.asType().toString().contains(SourceCallback.class.getName()))
                    continue;

                if (context.getTypeMirrorUtils().isXmlType(variable.asType())) {
                    all.getParticle().add(objectFactory.createElement(generateXmlElement(variable.getSimpleName().toString(), targetNamespace)));
                } else {
                    if (context.getTypeMirrorUtils().isArrayOrList(context.getTypeUtils(), variable.asType())) {
                        generateParameterCollectionElement(all, variable);
                    } else {
                        complexContentExtension.getAttributeOrAttributeGroup().add(createParameterAttribute(variable));
                    }
                }
            }
        }

        schema.getSimpleTypeOrComplexTypeOrGroup().add(complexType);
    }

    private void generateCollectionElement(All all, VariableElement variable, boolean optional) {
        TopLevelElement collectionElement = new TopLevelElement();
        all.getParticle().add(objectFactory.createElement(collectionElement));
        collectionElement.setName(variable.getSimpleName().toString());

        if (optional) {
            collectionElement.setMinOccurs(BigInteger.valueOf(0L));
        } else {
            collectionElement.setMinOccurs(BigInteger.valueOf(1L));
        }
        collectionElement.setMaxOccurs("1");

        LocalComplexType collectionComplexType = new LocalComplexType();
        collectionElement.setComplexType(collectionComplexType);
        ExplicitGroup sequence = new ExplicitGroup();
        collectionComplexType.setSequence(sequence);

        TopLevelElement collectionItemElement = new TopLevelElement();
        sequence.getParticle().add(objectFactory.createElement(collectionItemElement));

        collectionItemElement.setName(context.getNameUtils().singularize(collectionElement.getName()));
        collectionItemElement.setMinOccurs(BigInteger.valueOf(0L));
        collectionItemElement.setMaxOccurs("unbounded");

        DeclaredType variableType = (DeclaredType) variable.asType();
        java.util.List<? extends TypeMirror> variableTypeParameters = variableType.getTypeArguments();
        if (variableTypeParameters.size() != 0) {
            TypeMirror genericType = variableTypeParameters.get(0);

            if (isTypeSupported(genericType)) {
                collectionItemElement.setType(SchemaTypeConversion.convertType(genericType.toString(), schema.getTargetNamespace()));
            } else {
                collectionItemElement.setComplexType(generateRefComplexType());
            }
        } else {
            collectionItemElement.setComplexType(generateRefComplexType());
        }
    }

    private void generateParameterCollectionElement(All all, VariableElement variable) {
        Parameter parameter = variable.getAnnotation(Parameter.class);
        boolean optional = true;

        if (parameter != null)
            optional = parameter.optional();

        generateCollectionElement(all, variable, optional);
    }

    private void generateConfigurableCollectionElement(All all, VariableElement variable) {
        Configurable configurable = variable.getAnnotation(Configurable.class);

        generateCollectionElement(all, variable, configurable.optional());
    }

    private LocalComplexType generateRefComplexType() {
        LocalComplexType itemComplexType = new LocalComplexType();

        Attribute refAttribute = new Attribute();
        refAttribute.setUse(SchemaConstants.USE_REQUIRED);
        refAttribute.setName("ref");
        refAttribute.setType(SchemaConstants.STRING);

        itemComplexType.getAttributeOrAttributeGroup().add(refAttribute);
        return itemComplexType;
    }

    private TopLevelElement generateXmlElement(String elementName, String targetNamespace) {
        TopLevelElement xmlElement = new TopLevelElement();
        xmlElement.setName(elementName);
        xmlElement.setType(new QName(targetNamespace, "XmlType"));
        return xmlElement;
    }

    private ComplexType createAnyXmlType() {
        ComplexType xmlComplexType = new TopLevelComplexType();
        xmlComplexType.setName("XmlType");
        Any any = new Any();
        any.setProcessContents("lax");
        any.setMinOccurs(new BigInteger("0"));
        any.setMaxOccurs("unbounded");
        ExplicitGroup all = new ExplicitGroup();
        all.getParticle().add(any);
        xmlComplexType.setSequence(all);

        Attribute ref = createAttribute("ref", true, SchemaConstants.STRING, "The reference object for this parameter");
        xmlComplexType.getAttributeOrAttributeGroup().add(ref);

        return xmlComplexType;
    }

    private void registerConfigElement(javax.lang.model.element.Element element) {
        ExtensionType config = registerExtension("config");
        Attribute nameAttribute = createAttribute("name", true, SchemaConstants.STRING, "Give a name to this configuration so it can be later referenced by config-ref.");
        config.getAttributeOrAttributeGroup().add(nameAttribute);

        All all = new All();
        config.setAll(all);

        java.util.List<VariableElement> variables = ElementFilter.fieldsIn(element.getEnclosedElements());
        for (VariableElement variable : variables) {
            if (context.getTypeMirrorUtils().isArrayOrList(context.getTypeUtils(), variable.asType())) {
                generateConfigurableCollectionElement(all, variable);
            } else {
                config.getAttributeOrAttributeGroup().add(createConfigurableAttribute(variable));
            }
        }
    }

    private Attribute createConfigurableAttribute(VariableElement variable) {
        Configurable configurable = variable.getAnnotation(Configurable.class);
        if (configurable == null)
            return null;

        String name = variable.getSimpleName().toString();
        if (configurable.name().length() > 0)
            name = configurable.name();

        Attribute attribute = new Attribute();

        // set whenever or not is optional
        attribute.setUse(configurable.optional() ? SchemaConstants.USE_OPTIONAL : SchemaConstants.USE_REQUIRED);

        if (isTypeSupported(variable.asType())) {
            attribute.setName(name);
            attribute.setType(SchemaTypeConversion.convertType(variable.asType().toString(), schema.getTargetNamespace()));
        } else {
            // non-supported types will get "-ref" so beans can be injected
            attribute.setName(name + "-ref");
            attribute.setType(SchemaConstants.STRING);
        }

        // add default value
        if (configurable.defaultValue().length() > 0) {
            attribute.setDefault(configurable.defaultValue());
        }
        return attribute;
    }

    private Attribute createParameterAttribute(VariableElement variable) {
        Parameter parameter = variable.getAnnotation(Parameter.class);

        String name = variable.getSimpleName().toString();
        if (parameter != null && parameter.name().length() > 0)
            name = parameter.name();

        Attribute attribute = new Attribute();

        // set whenever or not is optional
        if (parameter != null) {
            attribute.setUse(parameter.optional() ? SchemaConstants.USE_OPTIONAL : SchemaConstants.USE_REQUIRED);
        } else {
            attribute.setUse(SchemaConstants.USE_REQUIRED);
        }


        if (isTypeSupported(variable.asType())) {
            attribute.setName(name);
            attribute.setType(SchemaTypeConversion.convertType(variable.asType().toString(), schema.getTargetNamespace()));
        } else {
            // non-supported types will get "-ref" so beans can be injected
            attribute.setName(name + "-ref");
            attribute.setType(SchemaConstants.STRING);
        }

        // add default value
        if (parameter != null && parameter.defaultValue().length() > 0) {
            attribute.setDefault(parameter.defaultValue());
        }
        return attribute;
    }

    private boolean isTypeSupported(TypeMirror typeMirror) {
        return SchemaTypeConversion.isSupported(typeMirror.toString());
    }

    private void importMuleSchemaDocNamespace() {
        Import muleSchemaDocImport = new Import();
        muleSchemaDocImport.setNamespace(SchemaConstants.MULE_SCHEMADOC_NAMESPACE);
        muleSchemaDocImport.setSchemaLocation(SchemaConstants.MULE_SCHEMADOC_SCHEMA_LOCATION);
        schema.getIncludeOrImportOrRedefine().add(muleSchemaDocImport);
    }

    private void importMuleNamespace() {
        Import muleSchemaImport = new Import();
        muleSchemaImport.setNamespace(SchemaConstants.MULE_NAMESPACE);
        muleSchemaImport.setSchemaLocation(SchemaConstants.MULE_SCHEMA_LOCATION);
        schema.getIncludeOrImportOrRedefine().add(muleSchemaImport);
    }

    private void importSpringFrameworkNamespace() {
        Import springFrameworkImport = new Import();
        springFrameworkImport.setNamespace(SchemaConstants.SPRING_FRAMEWORK_NAMESPACE);
        springFrameworkImport.setSchemaLocation(SchemaConstants.SPRING_FRAMEWORK_SCHEMA_LOCATION);
        schema.getIncludeOrImportOrRedefine().add(springFrameworkImport);
    }

    private void importXmlNamespace() {
        Import xmlImport = new Import();
        xmlImport.setNamespace(SchemaConstants.XML_NAMESPACE);
        schema.getIncludeOrImportOrRedefine().add(xmlImport);
    }

    private Attribute createAttribute(String name, boolean optional, QName type, String description) {
        Attribute attr = new Attribute();
        attr.setName(name);
        attr.setUse(optional ? SchemaConstants.USE_OPTIONAL : SchemaConstants.USE_REQUIRED);
        attr.setType(type);
        Annotation nameAnnotation = new Annotation();
        attr.setAnnotation(nameAnnotation);
        Documentation nameDocumentation = new Documentation();
        nameDocumentation.getContent().add(description);
        nameAnnotation.getAppinfoOrDocumentation().add(nameDocumentation);

        return attr;
    }

    private Element registerTransformer(String name) {
        Element transformer = new TopLevelElement();
        transformer.setName(name);
        transformer.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_TRANSFORMER);
        transformer.setType(SchemaConstants.MULE_ABSTRACT_TRANSFORMER_TYPE);

        return transformer;
    }

    private ExtensionType registerExtension(String name) {
        LocalComplexType complexType = new LocalComplexType();

        Element extension = new TopLevelElement();
        extension.setName(name);
        extension.setSubstitutionGroup(SchemaConstants.MULE_ABSTRACT_EXTENSION);
        extension.setComplexType(complexType);

        ComplexContent complexContent = new ComplexContent();
        complexType.setComplexContent(complexContent);
        ExtensionType complexContentExtension = new ExtensionType();
        complexContentExtension.setBase(SchemaConstants.MULE_ABSTRACT_EXTENSION_TYPE);
        complexContent.setExtension(complexContentExtension);

        schema.getSimpleTypeOrComplexTypeOrGroup().add(extension);

        return complexContentExtension;
    }

    private void registerTypes() {
        registerType("integerType", SchemaConstants.INTEGER);
        registerType("decimalType", SchemaConstants.DECIMAL);
        registerType("floatType", SchemaConstants.FLOAT);
        registerType("doubleType", SchemaConstants.DOUBLE);
        registerType("dateTimeType", SchemaConstants.DATETIME);
        registerType("longType", SchemaConstants.LONG);
        registerType("byteType", SchemaConstants.BYTE);
        registerType("booleanType", SchemaConstants.BOOLEAN);
        registerType("anyUriType", SchemaConstants.ANYURI);
        registerType("charType", SchemaConstants.STRING, 1, 1);

        registerAnyXmlType();
    }

    private void registerAnyXmlType() {
        ComplexType xmlComplexType = createAnyXmlType();
        schema.getSimpleTypeOrComplexTypeOrGroup().add(xmlComplexType);
    }

    private void registerType(String name, QName base) {
        registerType(name, base, -1, -1);
    }

    private void registerType(String name, QName base, int minlen, int maxlen) {
        SimpleType simpleType = new TopLevelSimpleType();
        simpleType.setName(name);
        Union union = new Union();
        simpleType.setUnion(union);

        union.getSimpleType().add(createSimpleType(base, minlen, maxlen));
        union.getSimpleType().add(createExpressionSimpleType());

        schema.getSimpleTypeOrComplexTypeOrGroup().add(simpleType);
    }

    private LocalSimpleType createSimpleType(QName base, int minlen, int maxlen) {
        LocalSimpleType simpleType = new LocalSimpleType();
        Restriction restriction = new Restriction();
        restriction.setBase(base);

        if (minlen != -1) {
            NumFacet minLenFacet = new NumFacet();
            minLenFacet.setValue(Integer.toString(minlen));
            JAXBElement<NumFacet> element = objectFactory.createMinLength(minLenFacet);
            restriction.getFacets().add(element);
        }

        if (maxlen != -1) {
            NumFacet maxLenFacet = new NumFacet();
            maxLenFacet.setValue(Integer.toString(maxlen));
            JAXBElement<NumFacet> element = objectFactory.createMaxLength(maxLenFacet);
            restriction.getFacets().add(element);
        }

        simpleType.setRestriction(restriction);

        return simpleType;
    }

    private LocalSimpleType createExpressionSimpleType() {
        LocalSimpleType expression = new LocalSimpleType();
        Restriction restriction = new Restriction();
        expression.setRestriction(restriction);
        restriction.setBase(SchemaConstants.STRING);
        Pattern pattern = new Pattern();
        pattern.setValue("\\#\\[[^\\]]+\\]");
        restriction.getFacets().add(pattern);

        return expression;
    }
}