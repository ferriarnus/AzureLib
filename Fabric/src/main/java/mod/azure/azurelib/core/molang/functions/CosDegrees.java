package mod.azure.azurelib.core.molang.functions;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

/**
 * Replacement function for {@link com.eliotlash.mclib.math.functions.classic.Cos}, operating in degrees rather than radians.
 */
public class CosDegrees extends Function {
	public CosDegrees(IValue[] values, String name) throws Exception {
		super(values, name);
	}

	@Override
	public int getRequiredArguments() {
		return 1;
	}

	@Override
	public double get() {
		return Math.cos(this.getArg(0) / 180 * Math.PI);
	}
}
