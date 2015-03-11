package lumien.loadingprofiler;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Profiler
{
	Logger logger = LogManager.getLogger("LoadingProfiler");

	static Profiler INSTANCE;

	// Times
	public ArrayList<ProfileResult> constructing;
	public ArrayList<ProfileResult> preInit;
	public ArrayList<ProfileResult> init;
	public ArrayList<ProfileResult> postInit;

	// State
	ModContainer currentContainer;
	long savedTime;

	private Profiler()
	{
		constructing = new ArrayList<ProfileResult>();
		preInit = new ArrayList<ProfileResult>();
		init = new ArrayList<ProfileResult>();
		postInit = new ArrayList<ProfileResult>();
	}

	private void logPre(FMLEvent event, ModContainer modContainer)
	{
		currentContainer = modContainer;
		savedTime = System.currentTimeMillis();
	}

	private void logPost(FMLEvent event, ModContainer modContainer)
	{
		if (currentContainer != modContainer)
		{
			logger.log(Level.INFO, "Changed Mod Container WOT");
		}
		else
		{
			if (event instanceof FMLConstructionEvent)
			{
				// Constructing
				constructing.add(new ProfileResult(modContainer, System.currentTimeMillis() - savedTime));
			}
			else if (event instanceof FMLPreInitializationEvent)
			{
				// PreInit
				preInit.add(new ProfileResult(modContainer, System.currentTimeMillis() - savedTime));
			}
			else if (event instanceof FMLInitializationEvent)
			{
				// Init
				init.add(new ProfileResult(modContainer, System.currentTimeMillis() - savedTime));
			}
			else if (event instanceof FMLPostInitializationEvent)
			{
				// Post Init
				postInit.add(new ProfileResult(modContainer, System.currentTimeMillis() - savedTime));
			}
		}
	}

	public static void pre(FMLEvent event, ModContainer modContainer)
	{
		getProfiler().logPre(event, modContainer);
	}

	public static void post(FMLEvent event, ModContainer modContainer)
	{
		getProfiler().logPost(event, modContainer);
	}

	public static Profiler getProfiler()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new Profiler();
		}

		return INSTANCE;
	}
}
