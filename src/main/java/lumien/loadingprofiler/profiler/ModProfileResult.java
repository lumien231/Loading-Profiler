package lumien.loadingprofiler.profiler;

import net.minecraftforge.fml.common.ModContainer;

public class ModProfileResult implements Comparable<ModProfileResult>
{
	public long time;
	public ModContainer modContainer;

	public ModProfileResult(ModContainer modContainer, long time)
	{
		this.modContainer = modContainer;
		this.time = time;
	}

	@Override
	public int compareTo(ModProfileResult o)
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
