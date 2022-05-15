package ru.myx.ae3.util.settings;

import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BaseProperty;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.reflect.ReflectionHidden;
import ru.myx.ae3.util.base.BaseFunctionSupplierGetter;
import ru.myx.ae3.util.base.BaseSupplier;
import ru.myx.ae3.util.concurrent.BaseSupplierCachedLazy;
import ru.myx.ae3.util.fn.SupplierAbstractFunctionInterface;
import ru.myx.ae3.util.fn.SupplierVfsFileJsonToMapCached;
import ru.myx.ae3.util.fn.SupplierVfsFileXmlToMapCached;
import ru.myx.ae3.vfs.Entry;
import ru.myx.ae3.vfs.EntryContainer;
import ru.myx.ae3.vfs.EntryVfsRoot;
import ru.myx.ae3.vfs.Storage;
import ru.myx.ae3.vfs.union.StorageImplUnion;

/** @author myx */
public class SettingsBuilder implements BaseSupplier<BaseObject> {
	
	/** @return */
	public static final SettingsBuilder builderCachedLazy() {
		
		return new SettingsBuilder().setCachedLazy();
	}
	/** @return */
	public static final SettingsBuilder builderSimple() {
		
		return new SettingsBuilder();
	}
	Entry inputFile = null;
	
	Entry inputFolder = null;
	
	boolean wrapCached = false;
	BaseObject defaults = null;
	
	BaseFunction functionDescriptorFilter = null;
	
	BaseFunction functionDescriptorMapper = null;
	
	BaseFunction functionDescriptorReducer = null;
	
	BaseFunction functionResultMapper = null;
	
	BaseFunction functionChangeCallback = null;
	
	/** @return */
	public BaseSupplier<?> build() {
		
		if (this.wrapCached) {
			return new BaseSupplierCachedLazy<>()//
					.setValueSource(this.parserForInput())//
					.setChangeCallback(this.functionChangeCallback)//
			;
		}
		
		{
			if (this.functionChangeCallback != null) {
				throw new IllegalStateException("Change callback is allowed for cached responses (yet?)");
			}
			return this.parserForInput();
		}
		
	}
	
	@Override
	public BaseFunction buildGetter() {
		
		final BaseSupplier<?> getter = this.build();
		if (getter instanceof BaseFunction) {
			return (BaseFunction) getter;
		}
		return new BaseFunctionSupplierGetter(getter);
	}
	
	@Override
	@ReflectionHidden
	public BaseProperty buildProperty(final short propertyAttributes) {
		
		return BaseSupplier.super.buildProperty(propertyAttributes);
	}
	
	@Override
	public BaseObject get() {
		
		return this.build().get();
	}
	
	private final SupplierAbstractFunctionInterface parserForInput() {
		
		{
			final Entry file = this.inputFile;
			if (file != null) {
				if (this.functionDescriptorFilter != null) {
					throw new UnsupportedOperationException("DescriptionFilter is not supported for single-file settings.");
				}
				if (this.functionDescriptorMapper != null) {
					throw new UnsupportedOperationException("DescriptionMapper is not supported for single-file settings.");
				}
				if (this.functionDescriptorReducer != null) {
					throw new UnsupportedOperationException("DescriptionReducer is not supported for single-file settings.");
				}
				final String key = file.getKey().toLowerCase();
				if (key.endsWith(".json")) {
					return new SupplierVfsFileJsonToMapCached(file)//
							.setDefaults(this.defaults)//
							.setMapper(this.functionResultMapper);
				}
				if (key.endsWith(".xml")) {
					return new SupplierVfsFileXmlToMapCached(file)//
							.setDefaults(this.defaults)//
							.setMapper(this.functionResultMapper);
				}
				throw new IllegalArgumentException("Unknown or unsupported file type, name: " + file.getKey());
			}
		}
		{
			final Entry folder = this.inputFolder;
			if (folder != null) {
				return new SupplierVfsFolderSettingsCached(folder)//
						.setDefaults(this.defaults)//
						.setDescriptorFilter(this.functionDescriptorFilter)//
						.setDescriptorMapper(this.functionDescriptorMapper)//
						.setDescriptorReducer(this.functionDescriptorReducer)//
						.setResultMapper(this.functionResultMapper)//
				;
			}
		}
		{
			throw new IllegalArgumentException("Unknown or unsupported input type");
		}
	}
	
	/** @return */
	public SettingsBuilder setCachedLazy() {
		
		this.wrapCached = true;
		return this;
	}
	
	/** @param defaults
	 *            - default prototype object to derive settings from
	 * @return */
	public SettingsBuilder setDefaults(final BaseObject defaults) {
		
		this.defaults = defaults == null || defaults == BaseObject.UNDEFINED || defaults == BaseObject.NULL
			? null
			: defaults;
		return this;
	}
	
	/** @param filter
	 *            - function(name), return true or non-default record name
	 * @return */
	public SettingsBuilder setDescriptorFilter(final BaseFunction filter) {
		
		this.functionDescriptorFilter = filter;
		return this;
	}
	
	/** @param glob
	 *            - string (without 'glob:' prefix), like: "*.{vendor,customer}.{json,xml}"
	 * @return */
	public SettingsBuilder setDescriptorFilterGlob(final String glob) {
		
		this.functionDescriptorFilter = new FunctionDescriptorFilterGlob(glob);
		return this;
	}
	
	/** @param mapper
	 *            - function(entry, name)
	 * @return */
	public SettingsBuilder setDescriptorMapper(final BaseFunction mapper) {
		
		this.functionDescriptorMapper = mapper;
		return this;
	}
	
	/** @param reducer
	 *            - function(settings, description)
	 * @return */
	public SettingsBuilder setDescriptorReducer(final BaseFunction reducer) {
		
		this.functionDescriptorReducer = reducer;
		return this;
	}
	
	/** Sets to read settings from file.
	 *
	 * Settings are either single-file or folder: setInputFileXXX and setInputFolderXXX are mutualy
	 * exclusive.
	 *
	 * @param inputFile
	 *            - settings file
	 * @return */
	public SettingsBuilder setInputFile(final Entry inputFile) {
		
		if (this.inputFolder != null) {
			throw new IllegalStateException("InputFolder is already set!");
		}
		this.inputFile = inputFile;
		return this;
	}
	
	/** Sets to read settings from file (by file path).
	 *
	 * Settings are either single-file or folder: setInputFileXXX and setInputFolderXXX are mutualy
	 * exclusive.
	 *
	 * @param path
	 *            relative paths root in /union
	 * @return */
	public SettingsBuilder setInputFilePath(final String path) {
		
		if (this.inputFolder != null) {
			throw new IllegalStateException("InputFolder is already set!");
		}
		this.inputFile = Storage.getRelativeTreeFile(//
				Storage.getRoot(Exec.currentProcess()), //
				Storage.UNION, //
				path//
		);
		return this;
	}
	
	/** Sets to read settings descriptors from folder.
	 *
	 * Settings are either single-file or folder: setInputFileXXX and setInputFolderXXX are mutualy
	 * exclusive.
	 *
	 * @param inputFolder
	 * @return */
	public SettingsBuilder setInputFolder(final Entry inputFolder) {
		
		if (this.inputFile != null) {
			throw new IllegalStateException("InputFile is already set!");
		}
		this.inputFolder = inputFolder;
		return this;
	}
	
	/** Sets to read settings descriptors from folder (by folder path).
	 *
	 * Settings are either single-file or folder: setInputFileXXX and setInputFolderXXX are mutualy
	 * exclusive.
	 *
	 * @param path
	 *            relative paths root in /union
	 * @return */
	public SettingsBuilder setInputFolderPath(final String path) {
		
		if (this.inputFile != null) {
			throw new IllegalStateException("InputFile is already set!");
		}
		if (null == path) {
			throw new NullPointerException("argument is null!");
		}
		this.inputFolder = Storage.getRelativeTreeFolder(//
				Storage.getRoot(Exec.currentProcess()), //
				Storage.UNION, //
				path//
		);
		return this;
	}
	
	/** Sets to read settings descriptors from folder (by folder path).
	 *
	 * Settings are either single-file or folder: setInputFileXXX and setInputFolderXXX are mutualy
	 * exclusive.
	 *
	 * @param path
	 *            multiple paths relative paths root in /union
	 * @return */
	public SettingsBuilder setInputFolderPaths(final String[] path) {
		
		if (null == path) {
			throw new NullPointerException("argument is null!");
		}
		if (path.length == 1) {
			return this.setInputFolderPath(path[0]);
		}
		if (path.length == 0) {
			throw new IllegalArgumentException("Path array is empty!");
		}
		final EntryVfsRoot root = Storage.getRoot(Exec.currentProcess());
		final EntryContainer[] stack = new EntryContainer[path.length];
		for (int i = path.length - 1; i >= 0; --i) {
			stack[i] = Storage.getRelativeTreeFolder(//
					root, //
					Storage.UNION, //
					path[i]//
			);
		}
		
		this.inputFolder = Storage.createRoot(new StorageImplUnion(stack));
		return this;
	}
	
	/** Sets the optional mapper function to apply to final settings loaded.
	 *
	 * @param mapper
	 *            - function(current, previous)
	 * @return */
	public SettingsBuilder setResultMapper(final BaseFunction mapper) {
		
		this.functionResultMapper = mapper;
		return this;
	}
	
}
