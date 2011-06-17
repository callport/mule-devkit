package org.mule.devkit.module.generation;

import javax.xml.namespace.QName;

public final class SchemaConstants {
    public static final String BASE_NAMESPACE = "http://www.mulesoft.org/schema/mule/";
    public static final String DEVKIT_NAMESPACE = "http://www.mulesoft.org/schema/mule/devkit";
    public static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    public static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    public static final String SPRING_FRAMEWORK_NAMESPACE = "http://www.springframework.org/schema/beans";
    public static final String SPRING_FRAMEWORK_SCHEMA_LOCATION = "http://www.springframework.org/schema/beans/spring-beans-3.0.xsd";
    public static final String MULE_NAMESPACE = "http://www.mulesoft.org/schema/mule/core";
    public static final String MULE_SCHEMA_LOCATION = "http://www.mulesoft.org/schema/mule/core/3.1/mule.xsd";
    public static final String MULE_SCHEMADOC_NAMESPACE = "http://www.mulesoft.org/schema/mule/schemadoc";
    public static final String MULE_SCHEMADOC_SCHEMA_LOCATION = "http://www.mulesoft.org/schema/mule/schemadoc/3.1/mule-schemadoc.xsd";
    public static final QName MULE_ABSTRACT_EXTENSION = new QName(MULE_NAMESPACE, "abstract-extension", "mule");
    public static final QName MULE_ABSTRACT_EXTENSION_TYPE = new QName(MULE_NAMESPACE, "abstractExtensionType", "mule");
    public static final QName MULE_ABSTRACT_MESSAGE_PROCESSOR = new QName(MULE_NAMESPACE, "abstract-message-processor", "mule");
    public static final QName MULE_ABSTRACT_MESSAGE_PROCESSOR_TYPE = new QName(MULE_NAMESPACE, "abstractInterceptingMessageProcessorType", "mule");
    public static final QName MULE_ABSTRACT_TRANSFORMER = new QName(MULE_NAMESPACE, "abstract-transformer", "mule");
    public static final QName MULE_ABSTRACT_TRANSFORMER_TYPE = new QName(MULE_NAMESPACE, "abstractTransformerType", "mule");
    public static final QName MULE_ABSTRACT_INBOUND_ENDPOINT = new QName(MULE_NAMESPACE, "abstract-inbound-endpoint", "mule");
    public static final QName MULE_ABSTRACT_INBOUND_ENDPOINT_TYPE = new QName(MULE_NAMESPACE, "abstractInboundEndpointType", "mule");
    public static final QName STRING = new QName(XSD_NAMESPACE, "string", "xs");
    public static final QName DECIMAL = new QName(XSD_NAMESPACE, "decimal", "xs");
    public static final QName FLOAT = new QName(XSD_NAMESPACE, "float", "xs");
    public static final QName INTEGER = new QName(XSD_NAMESPACE, "integer", "xs");
    public static final QName DOUBLE = new QName(XSD_NAMESPACE, "double", "xs");
    public static final QName DATETIME = new QName(XSD_NAMESPACE, "dateTime", "xs");
    public static final QName LONG = new QName(XSD_NAMESPACE, "long", "xs");
    public static final QName BYTE = new QName(XSD_NAMESPACE, "byte", "xs");
    public static final QName BOOLEAN = new QName(XSD_NAMESPACE, "boolean", "xs");
    public static final QName ANYURI = new QName(XSD_NAMESPACE, "anyURI", "xs");
    public static final String USE_REQUIRED = "required";
    public static final String USE_OPTIONAL = "optional";
}
