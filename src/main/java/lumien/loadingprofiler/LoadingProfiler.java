package lumien.loadingprofiler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;

@Mod(name = "LoadingProfiler", version = "1.0", modid = "LoadingProfiler")
public class LoadingProfiler
{
	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		Logger logger = LogManager.getLogger("LoadingProfiler");

		logger.log(Level.INFO, "Profile Results:");

		Collections.sort(Profiler.getProfiler().constructing);
		Collections.sort(Profiler.getProfiler().preInit);
		Collections.sort(Profiler.getProfiler().init);
		Collections.sort(Profiler.getProfiler().postInit);

		File logFile = new File("loading-log.log");
		if (logFile.exists())
		{
			logFile.delete();
			try
			{
				logFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		PrintWriter printWriter = null;
		try
		{
			printWriter = new PrintWriter(logFile);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		if (printWriter != null)
		{
			printWriter.write("Profiling Results\n");
			
			printWriter.write(" Constructing\n");
			for (ProfileResult result : Profiler.getProfiler().constructing)
			{
				printWriter.write("   " + result.modContainer.getModId() + ": " + result.time + "ms\n");
			}

			printWriter.write(" PreInitialization\n");
			for (ProfileResult result : Profiler.getProfiler().preInit)
			{
				printWriter.write("   " + result.modContainer.getModId() + ": " + result.time + "ms\n");
			}

			printWriter.write(" Initialization\n");
			for (ProfileResult result : Profiler.getProfiler().init)
			{
				printWriter.write("   " + result.modContainer.getModId() + ": " + result.time + "ms\n");
			}

			printWriter.write(" PostInitialization\n");
			for (ProfileResult result : Profiler.getProfiler().postInit)
			{
				printWriter.write("   " + result.modContainer.getModId() + ": " + result.time + "ms\n");
			}
		}

		logger.log(Level.DEBUG, " Constructing");
		for (ProfileResult result : Profiler.getProfiler().constructing)
		{
			logger.log(Level.DEBUG, "   " + result.modContainer.getModId() + ": " + result.time + "ms");
		}

		logger.log(Level.DEBUG, " PreInitialization");
		for (ProfileResult result : Profiler.getProfiler().preInit)
		{
			logger.log(Level.DEBUG, "   " + result.modContainer.getModId() + ": " + result.time + "ms");
		}

		logger.log(Level.DEBUG, " Initialization");
		for (ProfileResult result : Profiler.getProfiler().init)
		{
			logger.log(Level.DEBUG, "   " + result.modContainer.getModId() + ": " + result.time + "ms");
		}

		logger.log(Level.DEBUG, " PostInitialization");
		for (ProfileResult result : Profiler.getProfiler().postInit)
		{
			logger.log(Level.DEBUG, "   " + result.modContainer.getModId() + ": " + result.time + "ms");
		}

		printWriter.close();
	}
}
