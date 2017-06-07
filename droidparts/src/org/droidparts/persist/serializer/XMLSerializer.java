/**
 * Copyright 2017 Alex Yanchenko
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
package org.droidparts.persist.serializer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import android.content.Context;
import android.util.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.droidparts.inner.ClassSpecRegistry;
import org.droidparts.inner.ConverterRegistry;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.serialize.XMLAnn;
import org.droidparts.inner.converter.Converter;
import org.droidparts.model.Model;

import static org.droidparts.inner.ReflectionUtils.newInstance;
import static org.droidparts.inner.ReflectionUtils.setFieldVal;
import static org.droidparts.util.Strings.isNotEmpty;

public class XMLSerializer<ModelType extends Model> extends AbstractSerializer<ModelType, Node, NodeList> {

	public static Document parseDocument(String xml) throws IOException, ParserConfigurationException, SAXException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
	}

	public XMLSerializer(Class<ModelType> cls, Context ctx) {
		super(cls, ctx);
	}

	@Override
	public ModelType deserialize(Node node) throws Exception {
		ModelType model = newInstance(cls);
		FieldSpec<XMLAnn>[] xmlSpecs = ClassSpecRegistry.getXMLSpecs(cls);
		for (FieldSpec<XMLAnn> spec : xmlSpecs) {
			readFromXMLAndSetFieldVal(model, spec, node, spec.ann.tag, spec.ann.attribute);
		}
		return model;
	}

	@Override
	public ArrayList<ModelType> deserializeAll(NodeList nodeList) throws Exception {
		ArrayList<ModelType> list = new ArrayList<ModelType>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			list.add(deserialize(nodeList.item(i)));
		}
		return list;
	}

	private void readFromXMLAndSetFieldVal(Object obj, FieldSpec<XMLAnn> spec, Node node, String tag, String attribute)
			throws Exception {
		Pair<String, String> keyParts = getNestedKeyParts(tag);
		if (keyParts != null) {
			String subKey = keyParts.first;
			try {
				Node childTag = getChildNode(node, subKey);
				readFromXMLAndSetFieldVal(obj, spec, childTag, keyParts.second, attribute);
			} catch (Exception e) {
				handleParseException(node, spec.ann.optional, subKey, attribute, e);
			}
		} else {
			boolean defaultOrSameTag = tag.equals(spec.field.getName()) || tag.equals(node.getNodeName());
			if (spec.genericArg1 == null && isNotEmpty(attribute)) {
				if (!tag.equals(node.getNodeName())) {
					Node child = getChildNode(node, tag);
					if (child != null) {
						node = child;
					} else if (!defaultOrSameTag) {
						handleParseException(node, spec.ann.optional, tag, attribute,
								new IllegalArgumentException("No node."));
					}
				}
			}
			Node attrNode = gotAttributeNode(node, isNotEmpty(attribute) ? attribute : tag);
			Node tagNode = getChildNode(node, tag);
			if (tagNode == null && defaultOrSameTag) {
				tagNode = node;
			}
			try {
				if (attrNode != null) {
					Object attrVal = getNodeVal(spec.field.getType(), spec.genericArg1, spec.genericArg2, attrNode,
							attribute);
					setFieldVal(obj, spec.field, attrVal);
				} else if (tagNode != null) {
					Object tagVal = getNodeVal(spec.field.getType(), spec.genericArg1, spec.genericArg2, tagNode,
							attribute);
					setFieldVal(obj, spec.field, tagVal);
				} else {
					throw new IllegalArgumentException("Tag or attribute not found.");
				}
			} catch (Exception e) {
				handleParseException(node, spec.ann.optional, tag, attribute, e);
			}
		}

	}

	protected <T, G1, G2> Object getNodeVal(Class<T> valType, Class<G1> genericArg1, Class<G2> genericArg2, Node node,
	                                        String attribute) throws Exception {
		Converter<T> converter = ConverterRegistry.getConverter(valType);
		return converter.readFromXML(valType, genericArg1, genericArg2, node, attribute);
	}

	private static Node gotAttributeNode(Node tagNode, String name) {
		NamedNodeMap attrs = tagNode.getAttributes();
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				if (name.equals(attr.getNodeName())) {
					return attr;
				}
			}
		}
		return null;
	}

	private static Node getChildNode(Node tagNode, String name) {
		NodeList childTags = tagNode.getChildNodes();
		for (int i = 0; i < childTags.getLength(); i++) {
			Node childTag = childTags.item(i);
			if (name.equals(childTag.getNodeName())) {
				return childTag;
			}
		}
		return null;
	}

	private static void handleParseException(Node node, boolean optional, String tag, String attribute, Exception e)
			throws SerializerException {
		StringBuilder sb = new StringBuilder();
		if (isNotEmpty(tag)) {
			sb.append(String.format("tag '%s'", tag));
		}
		if (isNotEmpty(attribute)) {
			sb.append(String.format(" attribute '%s'", attribute));
		}
		logOrThrow(node, optional, sb.toString(), e);
	}
}
