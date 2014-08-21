/**
 * Copyright 2014 Alex Yanchenko
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

import static org.droidparts.inner.ReflectionUtils.newInstance;
import static org.droidparts.inner.ReflectionUtils.setFieldVal;
import static org.droidparts.util.Strings.isNotEmpty;

import java.util.ArrayList;
import java.util.Collection;

import org.droidparts.inner.ClassSpecRegistry;
import org.droidparts.inner.ConverterRegistry;
import org.droidparts.inner.ann.FieldSpec;
import org.droidparts.inner.ann.serialize.XMLAnn;
import org.droidparts.inner.converter.Converter;
import org.droidparts.model.Model;
import org.droidparts.util.L;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Pair;

public class XMLSerializer<ModelType extends Model> extends
		AbstractSerializer<ModelType, Node, NodeList> {

	public XMLSerializer(Class<ModelType> cls, Context ctx) {
		super(cls, ctx);
	}

	@Override
	public ModelType deserialize(Node node) throws Exception {
		ModelType model = newInstance(cls);
		FieldSpec<XMLAnn>[] xmlSpecs = ClassSpecRegistry.getXMLSpecs(cls);
		for (FieldSpec<XMLAnn> spec : xmlSpecs) {
			readFromXMLAndSetFieldVal(model, spec, node, spec.ann.tag,
					spec.ann.attribute);
		}
		return model;
	}

	@Override
	public ArrayList<ModelType> deserializeAll(NodeList nodeList)
			throws Exception {
		ArrayList<ModelType> list = new ArrayList<ModelType>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			list.add(deserialize(nodeList.item(i)));
		}
		return list;
	}

	public void serialize(Node parent, ModelType item) throws Exception {
		throw new UnsupportedOperationException();
	}

	public void serializeList(Node parent, Collection<ModelType> items)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	private void readFromXMLAndSetFieldVal(Object obj, FieldSpec<XMLAnn> spec,
			Node node, String tag, String attribute) throws Exception {
		Pair<String, String> keyParts = getNestedKeyParts(tag);
		if (keyParts != null) {
			String subKey = keyParts.first;
			Node childTag = getChildTag(node, subKey);
			if (childTag != null) {
				readFromXMLAndSetFieldVal(obj, spec, childTag, keyParts.second,
						attribute);
			} else {
				throwIfNotOptional(spec);
			}
		} else {
			boolean defaultOrSameTag = tag.equals(spec.field.getName())
					|| tag.equals(node.getNodeName());
			if (spec.componentType == null && isNotEmpty(attribute)) {
				if (!tag.equals(node.getNodeName())) {
					Node child = getChildTag(node, tag);
					if (child != null) {
						node = child;
					} else if (!defaultOrSameTag) {
						throwIfNotOptional(spec);
					}
				}
			}
			Node attrNode = gotAttributeNode(node,
					isNotEmpty(attribute) ? attribute : tag);
			Node tagNode = getTagNode(node.getChildNodes(), tag);
			if (tagNode == null && defaultOrSameTag) {
				tagNode = node;
			}
			if (attrNode != null || tagNode != null) {
				try {
					if (attrNode != null) {
						Object attrVal = getNodeVal(spec.field.getType(),
								spec.componentType, attrNode, attribute);
						setFieldVal(obj, spec.field, attrVal);
					} else if (tagNode != null) {
						Object tagVal = getNodeVal(spec.field.getType(),
								spec.componentType, tagNode, attribute);
						setFieldVal(obj, spec.field, tagVal);
					}
				} catch (Exception e) {
					// XXX tag or attribute
					L.w("Failed to deserialize '%s': %s.", spec.ann.tag,
							e.getMessage());
					throwIfNotOptional(spec);
				}
			} else {
				throwIfNotOptional(spec);
			}
		}

	}

	protected <T, V> Object getNodeVal(Class<T> valType,
			Class<V> componentType, Node node, String attribute)
			throws Exception {
		Converter<T> converter = ConverterRegistry.getConverter(valType);
		return converter.readFromXML(valType, componentType, node, attribute);
	}

	private static void throwIfNotOptional(FieldSpec<XMLAnn> spec)
			throws IllegalArgumentException {
		if (!spec.ann.optional) {
			throw new IllegalArgumentException(String.format(
					"Required tag '%s' or attribute '%s' not present.",
					spec.ann.tag, spec.ann.attribute));
		}
	}

	private static Node gotAttributeNode(Node node, String name) {
		NamedNodeMap attrs = node.getAttributes();
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

	private static Node getTagNode(NodeList tags, String name) {
		for (int i = 0; i < tags.getLength(); i++) {
			Node tag = tags.item(i);
			if (name.equals(tag.getNodeName())) {
				return tag;
			}
		}
		return null;
	}

	private static Node getChildTag(Node tag, String childName) {
		NodeList childTags = tag.getChildNodes();
		for (int i = 0; i < childTags.getLength(); i++) {
			Node childTag = childTags.item(i);
			if (childName.equals(childTag.getNodeName())) {
				return childTag;
			}
		}
		return null;
	}
}
