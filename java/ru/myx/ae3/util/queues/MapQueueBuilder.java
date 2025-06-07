package ru.myx.ae3.util.queues;

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
import ru.myx.ae3.util.settings.SupplierVfsFolderSettingsCached;
import ru.myx.ae3.vfs.Entry;
import ru.myx.ae3.vfs.Storage;

/** @author myx */
public class MapQueueBuilder implements BaseSupplier<BaseObject> {
	
	/** @return */
	public static final MapQueueBuilder builderSimple() {
		
		return new MapQueueBuilder();
	}

	Entry inputFile = null;
	
	Entry inputFolder = null;
	
	boolean wrapCached = false;
	BaseObject defaults = null;
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
		if (getter instanceof final BaseFunction baseFunction) {
			return baseFunction;
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
						.setDescriptorReducer(this.functionDescriptorReducer)//
						.setResultMapper(this.functionResultMapper)//
				;
			}
		}
		{
			throw new IllegalArgumentException("Unknown or unsupported input type");
		}
	}
	
	/** @param mapper
	 * @return */
	public MapQueueBuilder setActiveTaskChecker(final BaseFunction mapper) {
		
		this.functionTaskChecker = mapper;
		return this;
	}

	/** @param seconds
	 * @return */
	public MapQueueBuilder setActiveTaskCheckerIntervalSeconds(final int seconds) {
		
		this.functionTaskChecker = mapper;
		return this;
	}
	
	/** @return */
	public MapQueueBuilder setCachedLazy() {
		
		this.wrapCached = true;
		return this;
	}
	
	/** @param inputDefaults
	 * @return */
	public MapQueueBuilder setDefaults(final BaseObject inputDefaults) {
		
		this.defaults = inputDefaults == null || inputDefaults == BaseObject.UNDEFINED || inputDefaults == BaseObject.NULL
			? null
			: inputDefaults;
		return this;
	}
	
	/** @param reducer
	 * @return */
	public MapQueueBuilder setDescriptorReducer(final BaseFunction reducer) {
		
		this.functionDescriptorReducer = reducer;
		return this;
	}
	
	public MapQueueBuilder setFinishedPurgeDelayDays(final int count) {
		
		//
	}
	
	/** @param inputFile
	 * @return */
	public MapQueueBuilder setInputFile(final Entry inputFile) {
		
		if (this.inputFolder != null) {
			throw new IllegalStateException("InputFolder is already set!");
		}
		this.inputFile = inputFile;
		return this;
	}
	
	/** @param inputFolder
	 * @return */
	public MapQueueBuilder setInputFolder(final Entry inputFolder) {
		
		if (this.inputFile != null) {
			throw new IllegalStateException("InputFile is already set!");
		}
		this.inputFolder = inputFolder;
		return this;
	}
	
	/** @param path
	 * @return */
	public MapQueueBuilder setInputFolderPath(final String path) {
		
		if (this.inputFile != null) {
			throw new IllegalStateException("InputFile is already set!");
		}
		this.inputFolder = Storage.getCreateRelativeTreeFolder(//
				Storage.getRoot(Exec.currentProcess()), //
				Storage.UNION, //
				path//
		);
		return this;
	}
	
	/** @param mapper
	 * @return */
	public MapQueueBuilder setMapper(final BaseFunction mapper) {
		
		this.functionResultMapper = mapper;
		return this;
	}

	public MapQueueBuilder setMaxFinished(final int count) {

		//
	}
	
	public MapQueueBuilder setMaxPending(final int count) {

		//
	}
	
	public MapQueueBuilder setMaxWorking(final int count) {

		//
	}
	
	public MapQueueBuilder setParallelChecking(final boolean v) {

		//
	}
	
	/** @param mapper
	 * @return */
	public MapQueueBuilder setResultMapper(final BaseFunction mapper) {
		
		this.functionResultMapper = mapper;
		return this;
	}
	
}
