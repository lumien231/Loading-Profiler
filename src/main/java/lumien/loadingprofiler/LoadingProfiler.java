package lumien.loadingprofiler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lumien.loadingprofiler.profiler.ModProfileResult;
import lumien.loadingprofiler.profiler.ModProfiler;
import lumien.loadingprofiler.profiler.TransformerProfileResult;
import lumien.loadingprofiler.profiler.TransformerProfiler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.asm.ASMTransformerWrapper.TransformerWrapper;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(name = "LoadingProfiler", version = "1.1", modid = "LoadingProfiler")
public class LoadingProfiler
{
	Logger logger;
	PrintWriter printWriter;

	long modThreshold;
	long transformerThreshold;
	long classThreshold;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		modThreshold = config.get("Settings", "Mod-Threshold", 0, "Any numbers higher than this will appear in the mod profiling results").getInt();
		transformerThreshold = config.get("Settings", "Transformer-Treshold", 20, "Any numbers higher than this will appear in the transformer specific profiling results").getInt();
		classThreshold = config.get("Settings", "Class-Treshold", 20, "Any numbers higher than this will appear in the class specific profiling results").getInt();

		if (config.hasChanged())
		{
			config.save();
		}
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event)
	{
		TransformerProfiler.enabled = false;
		logger = LogManager.getLogger("LoadingProfiler");

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
			write("Mod Loading Profiling Results:");
			logModResults(printWriter);

			write("Class Transformer Profiling Results:");
			logTransformerResults(printWriter);

			printWriter.close();
		}
	}

	private void write(String text)
	{
		printWriter.write(text + "\n");
		logger.log(Level.DEBUG, text);
	}

	private void logTransformerResults(PrintWriter printWriter)
	{
		HashMap<TransformerWrapper, Long> times = TransformerProfiler.getProfiler().getTimes();

		ArrayList<TransformerProfileResult> results = new ArrayList<TransformerProfileResult>();

		for (Entry<TransformerWrapper, Long> entry : times.entrySet())
		{
			results.add(new TransformerProfileResult(entry.getKey(), entry.getValue()));
		}

		Collections.sort(results);
		Map<String, Long> classResults = sortByValue(TransformerProfiler.getProfiler().getClassTimes());

		write("   Transformer Specific");
		long total = 0;
		for (TransformerProfileResult result : results)
		{
			String className = result.transformer.toString();
			long time = result.time / 1000000;
			if (time > transformerThreshold)
			{
				String transformerName = className.substring(className.indexOf("(") + 1, className.indexOf(","));
				write("      " + transformerName + ": " + time + "ms");
			}
			total += result.time;
		}

		long total2 = 0;
		write("   Class Specific");
		for (Entry<String, Long> entry : classResults.entrySet())
		{
			long time = entry.getValue() / 1000000;
			if (time > classThreshold)
			{
				write("      " + entry.getKey() + ": " + time + "ms");
			}
		}

		write("   Total: " + total / 1000000 + "ms");
	}

	private void logModResults(PrintWriter printWriter)
	{
		Collections.sort(ModProfiler.getProfiler().constructing);
		Collections.sort(ModProfiler.getProfiler().preInit);
		Collections.sort(ModProfiler.getProfiler().init);
		Collections.sort(ModProfiler.getProfiler().postInit);

		ArrayList<ModProfileResult> totalTimes = calculateTotals(ModProfiler.getProfiler().constructing, ModProfiler.getProfiler().preInit, ModProfiler.getProfiler().init, ModProfiler.getProfiler().postInit);
		Collections.sort(totalTimes);

		write("   Constructing");
		for (ModProfileResult result : ModProfiler.getProfiler().constructing)
		{
			long time = result.time / 1000000;
			if (time > modThreshold)
			{
				write("      " + result.modContainer.getModId() + ": " + time + "ms");
			}
		}

		write("   PreInitialization");
		for (ModProfileResult result : ModProfiler.getProfiler().preInit)
		{
			long time = result.time / 1000000;
			if (time > modThreshold)
			{
				write("      " + result.modContainer.getModId() + ": " + time + "ms");
			}
		}

		write("   Initialization");
		for (ModProfileResult result : ModProfiler.getProfiler().init)
		{
			long time = result.time / 1000000;
			if (time > modThreshold)
			{
				write("      " + result.modContainer.getModId() + ": " + time + "ms");
			}
		}

		write("   PostInitialization");
		for (ModProfileResult result : ModProfiler.getProfiler().postInit)
		{
			long time = result.time / 1000000;
			if (time > modThreshold)
			{
				write("      " + result.modContainer.getModId() + ": " + time + "ms");
			}
		}

		write("   Total Time");
		for (ModProfileResult result : totalTimes)
		{
			long time = result.time / 1000000;
			write("      " + result.modContainer.getModId() + ": " + time + "ms");
		}
	}

	private ArrayList<ModProfileResult> calculateTotals(ArrayList<ModProfileResult> constructing, ArrayList<ModProfileResult> preInit, ArrayList<ModProfileResult> init, ArrayList<ModProfileResult> postInit)
	{
		HashMap<ModContainer, Long> timeMap = new HashMap<ModContainer, Long>();

		for (ModProfileResult fr : constructing)
		{
			if (timeMap.containsKey(fr.modContainer))
			{
				timeMap.put(fr.modContainer, timeMap.get(fr.modContainer) + fr.time);
			}
			else
			{
				timeMap.put(fr.modContainer, fr.time);
			}
		}

		for (ModProfileResult fr : preInit)
		{
			if (timeMap.containsKey(fr.modContainer))
			{
				timeMap.put(fr.modContainer, timeMap.get(fr.modContainer) + fr.time);
			}
			else
			{
				timeMap.put(fr.modContainer, fr.time);
			}
		}

		for (ModProfileResult fr : init)
		{
			if (timeMap.containsKey(fr.modContainer))
			{
				timeMap.put(fr.modContainer, timeMap.get(fr.modContainer) + fr.time);
			}
			else
			{
				timeMap.put(fr.modContainer, fr.time);
			}
		}

		for (ModProfileResult fr : postInit)
		{
			if (timeMap.containsKey(fr.modContainer))
			{
				timeMap.put(fr.modContainer, timeMap.get(fr.modContainer) + fr.time);
			}
			else
			{
				timeMap.put(fr.modContainer, fr.time);
			}
		}

		ArrayList<ModProfileResult> results = new ArrayList<ModProfileResult>();

		for (Entry<ModContainer, Long> entry : timeMap.entrySet())
		{
			results.add(new ModProfileResult(entry.getKey(), entry.getValue()));
		}

		return results;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
	{
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>()
		{
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
			{
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
