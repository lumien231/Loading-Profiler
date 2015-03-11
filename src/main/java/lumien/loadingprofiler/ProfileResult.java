package lumien.loadingprofiler;

import net.minecraftforge.fml.common.ModContainer;

public class ProfileResult implements Comparable<ProfileResult>
{
	public long time;
	public ModContainer modContainer;
	
	public ProfileResult(ModContainer modContainer,long time)
	{
		this.modContainer = modContainer;
		this.time = time;
	}

	@Override
	public int compareTo(ProfileResult o)
	{
		return (int) (o.time - this.time);
	}
}
