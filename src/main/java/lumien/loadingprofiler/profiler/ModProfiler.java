package lumien.loadingprofiler.profiler;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ModProfiler
{
	Logger logger = LogManager.getLogger("LoadingProfiler");

	static ModProfiler INSTANCE;

	// Times
	public ArrayList<ModProfileResult> constructing;
	public ArrayList<ModProfileResult> preInit;
	public ArrayList<ModProfileResult> init;
	public ArrayList<ModProfileResult> postInit;

	// State
	ModContainer currentContainer;
	long savedTime;

	private ModProfiler()
	{
		constructing = new ArrayList<ModProfileResult>();
		preInit = new ArrayList<ModProfileResult>();
		init = new ArrayList<ModProfileResult>();
		postInit = new ArrayList<ModProfileResult>();
	}

	private void logPre(FMLEvent event, ModContainer modContainer)
	{
		currentContainer = modContainer;
		savedTime = System.nanoTime();
	}

	private void logPost(FMLEvent event, ModContainer modContainer)
	{
		TransformerProfiler transformerProfiler = TransformerProfiler.getProfiler();
		if (transformerProfiler.transformerRan)
		{
			transformerProfiler.transformerRan = false;
			savedTime+=transformerProfiler.nestedTime;
		}
		if (currentContainer != modContainer)
		{
			logger.log(Level.INFO, "Changed Mod Container WOT");
		}
		else
		{
			if (event instanceof FMLConstructionEvent)
			{
				// Constructing
				constructing.add(new ModProfileResult(modContainer, System.nanoTime() - savedTime));
			}
			else if (event instanceof FMLPreInitializationEvent)
			{
				// PreInit
				preInit.add(new ModProfileResult(modContainer, System.nanoTime() - savedTime));
			}
			else if (event instanceof FMLInitializationEvent)
			{
				// Init
				init.add(new ModProfileResult(modContainer, System.nanoTime() - savedTime));
			}
			else if (event instanceof FMLPostInitializationEvent)
			{
				// Post Init
				postInit.add(new ModProfileResult(modContainer, System.nanoTime() - savedTime));
			}
		}
	}

	public static void preEvent(FMLEvent event, ModContainer modContainer)
	{
		getProfiler().logPre(event, modContainer);
	}

	public static void postEvent(FMLEvent event, ModContainer modContainer)
	{
		getProfiler().logPost(event, modContainer);
	}

	public static ModProfiler getProfiler()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new ModProfiler();
		}

		return INSTANCE;
	}
}
