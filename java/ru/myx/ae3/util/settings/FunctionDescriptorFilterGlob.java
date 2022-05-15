package ru.myx.ae3.util.settings;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import ru.myx.ae3.base.BaseFunctionAbstract;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.ExecCallableBoth;

class FunctionDescriptorFilterGlob extends BaseFunctionAbstract implements ExecCallableBoth.NativeJ1 {
	
	private final PathMatcher matcher;

	FunctionDescriptorFilterGlob(final String glob) {
		
		this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
	}
	
	@Override
	public BaseObject callNJ1(final BaseObject instance, final BaseObject argument) {

		if (argument instanceof CharSequence) {
			if (this.matcher.matches(Paths.get(argument.baseToJavaString()))) {
				return argument;
			}
		}
		return BaseObject.NULL;
	}
}
