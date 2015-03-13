package lumien.loadingprofiler.profiler;

import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;

public class TransformerProfileResult implements Comparable<TransformerProfileResult>
{
	public long time;
	public TransformerWrapper transformer;
	
	public TransformerProfileResult(TransformerWrapper transformer,long time)
	{
		this.transformer = transformer;
		this.time = time;
	}

	@Override
	public int compareTo(TransformerProfileResult o)
	{
		if (o.time == this.time)
		{
			return 0;
		}
		else if (o.time > this.time)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
}
