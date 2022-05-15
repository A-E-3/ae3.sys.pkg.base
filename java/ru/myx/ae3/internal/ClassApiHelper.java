package ru.myx.ae3.internal;

import java.util.Iterator;

import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseMap;
import ru.myx.ae3.base.BaseNative;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitive;
import ru.myx.ae3.base.BasePrimitiveString;
import ru.myx.ae3.base.BaseProperty;
import ru.myx.ae3.base.BaseString;
import ru.myx.ae3.concurrent.FunctionWrapOnceToThisProperty;
import ru.myx.ae3.concurrent.FunctionWrapSyncThis;
import ru.myx.ae3.reflect.ReflectionExplicit;
import ru.myx.ae3.reflect.ReflectionManual;

/** @author myx */
@ReflectionManual
public final class ClassApiHelper {
	
	/** @param name
	 * @param inherit
	 * @param constructor
	 * @param properties
	 * @param statics
	 * @return
	 * @throws Exception */
	@ReflectionExplicit
	public static BaseObject createClass(final CharSequence name, final BaseObject inherit, final BaseFunction constructor, final BaseObject properties, final BaseObject statics)
			throws Exception {
		
		final BaseObject prototype;
		{
			if (inherit == BaseObject.UNDEFINED || inherit == null) {
				prototype = BaseObject.createObject();
			} else {
				final BaseObject inheritPrototype = inherit.baseGet("prototype", BaseObject.UNDEFINED);
				prototype = BaseObject.createObject(
						inheritPrototype == BaseObject.UNDEFINED || inheritPrototype.baseIsPrimitive()
							? inherit
							: inheritPrototype);
			}
		}
		constructor.baseDefine("prototype", prototype);
		if (properties instanceof BaseMap) {
			for (final Iterator<? extends BasePrimitive<?>> i = properties.baseKeysOwnPrimitive(); i.hasNext();) {
				final BasePrimitiveString k = i.next().baseToString();
				BaseObject d = properties.baseGet(k, BaseObject.UNDEFINED);
				
				if (d == BaseObject.UNDEFINED) {
					// console.warn("createClass: " + name + ", propertyKey: " + key + " has invalid
					// descriptor: " + Format.jsDescribe(d));
					continue;
				}
				
				final String execute = d.baseGet("execute", BaseString.EMPTY).baseToJavaString();
				if ("once".equals(execute)) {
					final BaseFunction getter = d.baseGet("get", BaseObject.UNDEFINED).baseCall();
					if (getter != null) {
						d = BaseObject.createObject(d);
						d.baseDefine( //
								"get",
								new FunctionWrapSyncThis(//
										new FunctionWrapOnceToThisProperty(k, getter, null, prototype, BaseProperty.propertyAttributesFromDescriptor(d))//
								)//
						);
					}
				}
				BaseNative.defineProperty(prototype, k, d);
			}
			// BaseNative.defineProperties(p, properties);
		}
		if (name != null && !(properties instanceof BaseMap && properties.baseGetOwnProperty(name) != null)) {
			prototype.baseDefine(
					name, //
					constructor, //
					BaseProperty.ATTRS_MASK_NNN);
		}
		if (statics instanceof BaseMap) {
			for (final Iterator<? extends BasePrimitive<?>> i = statics.baseKeysOwnPrimitive(); i.hasNext();) {
				final BasePrimitiveString k = i.next().baseToString();
				BaseObject d = statics.baseGet(k, BaseObject.UNDEFINED);
				
				if (d == BaseObject.UNDEFINED) {
					// console.warn("createClass: " + name + ", propertyKey: " + key + " has invalid
					// descriptor: " + Format.jsDescribe(d));
					continue;
				}
				
				final String execute = d.baseGet("execute", BaseString.EMPTY).baseToJavaString();
				if ("once".equals(execute)) {
					final BaseFunction getter = d.baseGet("get", BaseObject.UNDEFINED).baseCall();
					if (getter != null) {
						d = BaseObject.createObject(d);
						if (!d.baseGet("configurable", BaseObject.UNDEFINED).baseToJavaBoolean()) {
							d.baseDefine("configurable", true);
						}
						d.baseDefine( //
								"get",
								new FunctionWrapSyncThis(//
										new FunctionWrapOnceToThisProperty(k, getter, constructor, null, BaseProperty.propertyAttributesFromDescriptor(d))//
								)//
						);
					}
				}
				BaseNative.defineProperty(constructor, k, d);
			}
			// BaseNative.defineProperties(constructor, statics);
		}
		if (name != null && !(statics instanceof BaseMap && statics.baseGetOwnProperty("toString") != null)) {
			constructor.baseDefine(
					"toString", //
					Base.createFunction("return \"[class " + name + "]\";"), //
					BaseProperty.ATTRS_MASK_NNN);
		}
		return constructor;
	}
	
}
